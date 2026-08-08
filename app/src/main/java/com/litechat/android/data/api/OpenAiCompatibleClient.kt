package com.litechat.android.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
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
import java.io.BufferedReader
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class StreamEvent {
    data class Delta(val text: String) : StreamEvent()
    data class Error(val message: String) : StreamEvent()
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
     */
    fun streamChat(
        baseUrl: String,
        apiKey: String,
        model: String,
        messages: List<ChatMessageDto>,
        temperature: Float,
        allowNonStreamFallback: Boolean = true,
    ): Flow<StreamEvent> = callbackFlow {
        cancel()

        val url = completionsUrl(baseUrl)
        var gotDelta = false
        var streamError: String? = null
        var canceled = false

        try {
            val response = executeChat(url, apiKey, model, messages, temperature, stream = true)
            try {
                if (!response.isSuccessful) {
                    val err = response.body?.string()?.take(500) ?: response.message
                    streamError = "HTTP ${response.code}: $err"
                } else {
                    parseSse(response) { event ->
                        when (event) {
                            is StreamEvent.Delta -> {
                                if (event.text.isNotEmpty()) gotDelta = true
                                trySend(event).isSuccess
                            }
                            is StreamEvent.Error -> {
                                streamError = event.message
                                trySend(event).isSuccess
                            }
                            StreamEvent.Done -> true
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

        if (!canceled) {
            // Copy to local val: smart-cast impossible on a closure-mutated var.
            val streamErr = streamError
            val shouldFallback = allowNonStreamFallback &&
                !gotDelta &&
                streamErr != null &&
                isStreamClassFailure(streamErr)

            if (shouldFallback) {
                try {
                    val full = completeChat(baseUrl, apiKey, model, messages, temperature)
                    if (full.isNotEmpty()) {
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

    private fun parseSse(response: Response, emit: (StreamEvent) -> Boolean) {
        val reader: BufferedReader = response.body?.charStream()?.buffered() ?: return
        reader.use { br ->
            while (true) {
                val line = br.readLine() ?: break
                if (line.isEmpty()) continue
                if (!line.startsWith("data:")) continue
                val data = line.removePrefix("data:").trim()
                if (data == "[DONE]") break
                if (data.isEmpty()) continue
                val delta = extractDelta(data)
                if (delta != null) {
                    if (delta.isNotEmpty() && !emit(StreamEvent.Delta(delta))) break
                } else {
                    val err = extractError(data)
                    if (err != null && !emit(StreamEvent.Error(err))) break
                }
            }
        }
    }

    private fun extractDelta(data: String): String? {
        return try {
            val root = json.parseToJsonElement(data).jsonObject
            val choices = root["choices"]?.jsonArray ?: return null
            if (choices.isEmpty()) return ""
            val choice = choices[0].jsonObject
            val delta = choice["delta"]?.jsonObject
            val content = delta?.get("content")?.jsonPrimitive?.contentOrNull
            content
                ?: choice["text"]?.jsonPrimitive?.contentOrNull
                ?: ""
        } catch (_: Exception) {
            null
        }
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

    private fun extractError(data: String): String? {
        return try {
            val root = json.parseToJsonElement(data).jsonObject
            val err = root["error"]
            when (err) {
                is JsonObject -> err["message"]?.jsonPrimitive?.contentOrNull
                else -> err?.toString()
            }
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
                n.contains("json") ||
                n.contains("stream") ||
                n.contains("connection") ||
                n.contains("eof") ||
                n.contains("reset") ||
                n.contains("http 4") ||
                n.contains("http 5") ||
                n.contains("network")
        }

        fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS) // streaming
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
}
