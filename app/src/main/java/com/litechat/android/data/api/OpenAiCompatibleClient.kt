package com.litechat.android.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import android.util.Base64
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class StreamEvent {
    data class Delta(val text: String) : StreamEvent()
    data class Error(val message: String) : StreamEvent()
    /** Emitted when content was delivered via the non-stream path (fallback
     *  after stream failure, or preferNonStream skip). Consumers may use this
     *  to remember the baseUrl prefers non-stream (C-004). */
    data object FallbackUsed : StreamEvent()
    data object Done : StreamEvent()
}

data class ChatMessageDto(
    val role: String,
    val content: String,
)

/**
 * Minimal OpenAI-compatible streaming client.
 * Single shared OkHttpClient — low memory, connection reuse.
 *
 * Stream-fallback pattern adapted from numAi-plus (lost-repo dig):
 * when a provider botches SSE and no tokens arrived, retry once with stream=false.
 */
class OpenAiCompatibleClient(
    private val client: OkHttpClient = defaultClient(),
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private var activeCall: Call? = null

    fun cancel() {
        activeCall?.cancel()
        activeCall = null
    }

    /**
     * Stream tokens; on stream-class failure with zero deltas, one non-stream retry.
     * Mirrors numAi-plus AUTO streaming mode.
     *
     * @param preferNonStream when true (baseUrl flagged stream-broken, C-004),
     *   skip the SSE attempt entirely and deliver via [completeChat]. The flag
     *   is TTL-based (see SettingsRepository.markStreamBroken), so providers
     *   that fix streaming recover automatically after expiry.
     */
    fun streamChat(
        baseUrl: String,
        apiKey: String,
        model: String,
        messages: List<ChatMessageDto>,
        temperature: Float,
        allowNonStreamFallback: Boolean = true,
        preferNonStream: Boolean = false,
    ): Flow<StreamEvent> = callbackFlow {
        cancel()

        val url = completionsUrl(baseUrl)
        var gotDelta = false
        var streamError: String? = null
        var canceled = false

        if (!preferNonStream) {
            try {
                val response = executeChat(url, apiKey, model, messages, temperature, stream = true)
                try {
                    if (!response.isSuccessful) {
                        val err = response.body?.string()?.take(500) ?: response.message
                        streamError = "HTTP ${response.code}: $err"
                    } else {
                        val body = response.body
                        if (body != null) {
                            StreamParser.parseSSE(body.byteStream())
                                .buffer(
                                    capacity = 16,
                                    onBufferOverflow = BufferOverflow.DROP_OLDEST,
                                )
                                .mapNotNull { ChatSseParser.parseEvent(it) }
                                .collect { event ->
                                    when (event) {
                                        is StreamEvent.Delta -> {
                                            if (event.text.isNotEmpty()) gotDelta = true
                                            trySend(event).isSuccess
                                        }
                                        is StreamEvent.Error -> {
                                            streamError = event.message
                                            trySend(event).isSuccess
                                        }
                                        StreamEvent.FallbackUsed -> {} // not emitted by parser
                                        StreamEvent.Done -> {} // not emitted by parser
                                    }
                                }
                        }
                    }
                } finally {
                    response.close()
                }
            } catch (e: IOException) {
                if (activeCall?.isCanceled() == true) {
                    canceled = true
                } else {
                    streamError = e.message ?: "Network error"
                }
            } catch (e: Exception) {
                streamError = e.message ?: "Unknown error"
            }
        }

        if (!canceled) {
            // Copy to local val: smart-cast impossible on a closure-mutated var.
            val streamErr = streamError
            val shouldFallback = (allowNonStreamFallback || preferNonStream) &&
                !gotDelta &&
                (preferNonStream || (streamErr != null && isStreamClassFailure(streamErr)))

            if (shouldFallback) {
                try {
                    val full = completeChat(baseUrl, apiKey, model, messages, temperature)
                    if (full.isNotEmpty()) {
                        trySend(StreamEvent.FallbackUsed)
                        trySend(StreamEvent.Delta(full))
                    } else if (streamError != null) {
                        trySend(StreamEvent.Error(streamError!!))
                    }
                } catch (e: Exception) {
                    trySend(
                        StreamEvent.Error(
                            "Stream failed ($streamError); fallback: ${e.message}",
                        ),
                    )
                }
            } else if (streamError != null && !gotDelta) {
                trySend(StreamEvent.Error(streamError!!))
            }
            trySend(StreamEvent.Done)
        } else {
            trySend(StreamEvent.Done)
        }

        awaitClose { cancel() }
    }.flowOn(Dispatchers.IO)

    /** Non-streaming chat/completions (numAi-plus fallback path). */
    fun completeChat(
        baseUrl: String,
        apiKey: String,
        model: String,
        messages: List<ChatMessageDto>,
        temperature: Float,
    ): String {
        val url = completionsUrl(baseUrl)
        executeChat(url, apiKey, model, messages, temperature, stream = false).use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}: ${raw.take(400)}")
            }
            return extractCompleteContent(raw)
                ?: throw IOException("Empty completion body")
        }
    }

    /**
     * C-005: list model ids from `{base}/models` (OpenAI-compatible servers).
     * Lenient parse: returns ids found in `data[].id`; malformed responses
     * yield an empty list rather than a crash. Empty key + local Ollama still
     * works (no Authorization header when key is blank).
     */
    fun listModels(baseUrl: String, apiKey: String): List<String> {
        val root = baseUrl.trim().trimEnd('/')
        val url = if (root.endsWith("/models")) root else "$root/models"
        val builder = Request.Builder()
            .url(url)
            .get()
            .header("Accept", "application/json")
        if (apiKey.isNotBlank()) {
            builder.header("Authorization", "Bearer $apiKey")
        }
        val call = client.newCall(builder.build())
        activeCall = call
        call.execute().use { response ->
            if (!response.isSuccessful) return emptyList()
            val raw = response.body?.string().orEmpty()
            return try {
                val rootObj = json.parseToJsonElement(raw).jsonObject
                val data = rootObj["data"]?.jsonArray ?: return emptyList()
                data.mapNotNull { el ->
                    (el.jsonObject["id"] as? JsonPrimitive)?.contentOrNull
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    /**
     * C-011: generate image via OpenAI /v1/images/generations.
     * Uses the same API key + OkHttpClient as chat. Returns raw PNG/JPEG bytes.
     * Throws IOException on failure.
     */
    fun generateImage(
        baseUrl: String,
        apiKey: String,
        prompt: String,
        model: String = "gpt-image-2",
        size: String = "1024x1024",
    ): ByteArray {
        val root = baseUrl.trim().trimEnd('/')
        val url = if (root.contains("/v1/images")) root else "$root/v1/images/generations"
        val body = buildJsonObject {
            put("model", model)
            put("prompt", prompt)
            put("n", 1)
            put("size", size)
            put("response_format", "b64_json")
        }
        val builder = Request.Builder()
            .url(url)
            .post(body.toString().toRequestBody(JSON))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
        if (apiKey.isNotBlank()) {
            builder.header("Authorization", "Bearer $apiKey")
        }
        val call = client.newCall(builder.build())
        activeCall = call
        call.execute().use { response ->
            if (!response.isSuccessful) {
                val err = response.body?.string()?.take(400).orEmpty()
                throw IOException("HTTP ${response.code}: $err")
            }
            val raw = response.body?.string().orEmpty()
            val rootObj = json.parseToJsonElement(raw).jsonObject
            val data = rootObj["data"]?.jsonArray
                ?: throw IOException("No data in image response")
            if (data.isEmpty()) throw IOException("Empty image response")
            val b64 = data[0].jsonObject["b64_json"]?.jsonPrimitive?.contentOrNull
                ?: throw IOException("No b64_json in image response")
            return Base64.decode(b64, Base64.DEFAULT)
        }
    }

    private fun executeChat(
        url: String,
        apiKey: String,
        model: String,
        messages: List<ChatMessageDto>,
        temperature: Float,
        stream: Boolean,
    ): Response {
        val body = ChatCompletionRequest(
            model = model,
            messages = messages.map { Msg(it.role, it.content) },
            temperature = temperature,
            stream = stream,
        )
        val payload = json.encodeToString(ChatCompletionRequest.serializer(), body)
        val builder = Request.Builder()
            .url(url)
            .post(payload.toRequestBody(JSON))
            .header("Content-Type", "application/json")
            .header("Accept", if (stream) "text/event-stream" else "application/json")
        if (apiKey.isNotBlank()) {
            builder.header("Authorization", "Bearer $apiKey")
        }
        builder.header("HTTP-Referer", "https://litechat.local")
        builder.header("X-Title", "LiteChat")
        builder.header("User-Agent", "LiteChat/0.1 (Android; weak-RAM-first)")
        val call = client.newCall(builder.build())
        activeCall = call
        return call.execute()
    }

    private fun completionsUrl(baseUrl: String): String {
        val root = baseUrl.trim().trimEnd('/')
        return if (root.endsWith("/chat/completions")) root else "$root/chat/completions"
    }

    private fun extractCompleteContent(raw: String): String? {
        return try {
            val root = json.parseToJsonElement(raw).jsonObject
            val choices = root["choices"]?.jsonArray ?: return null
            if (choices.isEmpty()) return null
            val msg = choices[0].jsonObject["message"]?.jsonObject
            msg?.get("content")?.jsonPrimitive?.contentOrNull
                ?: choices[0].jsonObject["text"]?.jsonPrimitive?.contentOrNull
        } catch (_: Exception) {
            null
        }
    }

    @Serializable
    private data class ChatCompletionRequest(
        val model: String,
        val messages: List<Msg>,
        val temperature: Float = 0.7f,
        val stream: Boolean = true,
    )

    @Serializable
    private data class Msg(
        val role: String,
        val content: String,
    )

    companion object {
        private val JSON = "application/json; charset=utf-8".toMediaType()

        /**
         * Heuristic from numAi-plus MainActivity stream-failure detector.
         * Zero tokens + these symptoms → non-stream retry is worth it.
         */
        fun isStreamClassFailure(message: String): Boolean {
            val n = message.lowercase()
            if (n.isEmpty()) return false
            return n.contains("socket closed") ||
                n.contains("unexpected") ||
                n.contains("malformed") ||
                n.contains("timeout") ||
                n.contains("timed out") ||
                n.contains("json") ||
                n.contains("stream") ||
                n.contains("connection") ||
                n.contains("eof") ||
                n.contains("reset") ||
                n.contains("http 4") ||
                n.contains("http 5") ||
                n.contains("network")
        }

        fun defaultClient(vararg interceptors: okhttp3.Interceptor): OkHttpClient {
            val builder = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.SECONDS) // streaming
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false) // Imp#3: our RetryInterceptor handles this
            interceptors.forEach { builder.addInterceptor(it) }
            return builder.build()
        }
    }
}
