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
import kotlinx.serialization.json.add
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.io.IOException
import java.util.concurrent.TimeUnit
import com.litechat.android.data.prefs.ApiKeySanitizer
import com.litechat.android.data.prefs.ProviderCatalog

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

/** P-013: extra request knobs. Null / default means "do not send". */
data class ChatOptions(
    val topP: Float? = null,
    val presencePenalty: Float? = null,
    val frequencyPenalty: Float? = null,
    val maxTokens: Int? = null,
    val promptCache: Boolean = false,
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
    private var userCancelled = false

    fun cancel() {
        userCancelled = true
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
        options: ChatOptions = ChatOptions(),
    ): Flow<StreamEvent> = callbackFlow {
        // REVIEW: unqualified cancel() inside callbackFlow binds to
        // SendChannel.cancel() — it killed our own pipe instead of the
        // active HTTP call. Qualify so it cancels the call and marks
        // user-cancel for the next send.
        this@OpenAiCompatibleClient.cancel()
        userCancelled = false // fresh send — caller (send()) cleared stopRequested first

        val url = completionsUrl(baseUrl)
        var gotDelta = false
        var streamError: String? = null
        var canceled = false

        if (!preferNonStream) {
            try {
                val response = executeChat(url, apiKey, model, messages, temperature, stream = true, options = options)
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
            } catch (e: java.util.concurrent.CancellationException) {
                throw e // never swallow user Stop as a retryable error
            } catch (e: IOException) {
                if (userCancelled || activeCall?.isCanceled() == true) {
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
                    val full = completeChat(baseUrl, apiKey, model, messages, temperature, options)
                    if (full.isNotEmpty()) {
                        trySend(StreamEvent.FallbackUsed)
                        trySend(StreamEvent.Delta(full))
                    } else if (streamError != null) {
                        trySend(StreamEvent.Error(streamError!!))
                    }
                } catch (e: java.util.concurrent.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    if (userCancelled || activeCall?.isCanceled() == true) {
                        canceled = true
                    } else {
                        trySend(
                            StreamEvent.Error(
                                "Stream failed ($streamError); fallback: ${e.message}",
                            ),
                        )
                    }
                }
            } else if (streamError != null && !gotDelta) {
                trySend(StreamEvent.Error(streamError!!))
            }
            trySend(StreamEvent.Done)
        } else {
            trySend(StreamEvent.Done)
        }

        awaitClose { this@OpenAiCompatibleClient.cancel() }
    }.flowOn(Dispatchers.IO)

    /** Non-streaming chat/completions (numAi-plus fallback path). */
    fun completeChat(
        baseUrl: String,
        apiKey: String,
        model: String,
        messages: List<ChatMessageDto>,
        temperature: Float,
        options: ChatOptions = ChatOptions(),
    ): String {
        val url = completionsUrl(baseUrl)
        executeChat(url, apiKey, model, messages, temperature, stream = false, options = options).use { response ->
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
            builder.header("Authorization", "Bearer ${ApiKeySanitizer.headerSafe(apiKey)}")
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
        /**
     * C-013: fetch and extract text from a web page via Jsoup.
     * Returns plain text, truncated to ~8K chars (≈2K tokens).
     */
    fun fetchPage(url: String): String {
        val doc = org.jsoup.Jsoup.connect(BrowseUrl.normalize(url))
            .userAgent("LiteChat/0.1 (Android; BYOK)")
            .timeout(15_000)
            .get()
        return doc.body().text().take(8_192)
    }

    /**
     * P-005: DuckDuckGo HTML search. Returns a plain-text list of
     * title + URL + snippet (top hits). No second HTTP client.
     */
    fun fetchSearch(query: String): String {
        val q = URLEncoder.encode(query, "UTF-8")
        val page = "https://html.duckduckgo.com/html/?q=$q"
        val req = Request.Builder()
            .url(page)
            .header("User-Agent", "LiteChat/0.1 (Android; BYOK)")
            .build()
        val call = client.newCall(req)
        activeCall = call
        val html = call.execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Search HTTP ${response.code}")
            }
            response.body?.string().orEmpty()
        }
        val doc = org.jsoup.Jsoup.parse(html, page)
        val hits = doc.select("div.result, div.web-result")
        if (hits.isEmpty()) {
            val titles = doc.select("a.result__a")
            if (titles.isEmpty()) return "No search results."
            return titles.take(5).mapIndexed { i, a ->
                val href = a.absUrl("href").ifBlank { a.attr("href") }
                "${i + 1}. ${a.text()}\n$href"
            }.joinToString("\n\n")
        }
        return hits.take(5).mapIndexed { i, el ->
            val a = el.selectFirst("a.result__a") ?: el.selectFirst("a")
            val title = a?.text().orEmpty()
            val href = a?.absUrl("href")?.ifBlank { a.attr("href") }.orEmpty()
            val snip = el.selectFirst(".result__snippet, .result__body")?.text().orEmpty()
            "${i + 1}. $title\n$href\n$snip"
        }.joinToString("\n\n").ifBlank { "No search results." }
    }

    /**
     * P-011: edit an existing image. Honest error if the host has no edits API.
     */
    fun editImage(
        baseUrl: String,
        apiKey: String,
        imageFile: File,
        prompt: String,
    ): ByteArray {
        val editModel = ProviderCatalog.resolveEditModel(baseUrl)
            ?: throw IOException(ProviderCatalog.cannotEditPicturesLine(baseUrl))
        return if (ProviderCatalog.fromBaseUrl(baseUrl).id == "xai") {
            editXaiImage(baseUrl, apiKey, imageFile, prompt, editModel)
        } else {
            editOpenAiImage(baseUrl, apiKey, imageFile, prompt, editModel)
        }
    }

    /** R-020: xAI rejects multipart. JSON + data URI. */
    private fun editXaiImage(
        baseUrl: String,
        apiKey: String,
        imageFile: File,
        prompt: String,
        editModel: String,
    ): ByteArray {
        if (imageFile.length() > 4L * 1024 * 1024) {
            throw IOException("That picture is too big to edit.")
        }
        val mime = when {
            imageFile.name.endsWith(".jpg", ignoreCase = true) ||
                imageFile.name.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
            else -> "image/png"
        }
        val dataUri = "data:$mime;base64," +
            Base64.encodeToString(imageFile.readBytes(), Base64.NO_WRAP)
        val payload = xaiEditJson(editModel, prompt, dataUri)
        val url = imagesUrl(baseUrl, "edits")
        val builder = Request.Builder()
            .url(url)
            .post(payload.toRequestBody(JSON))
            .header("Content-Type", "application/json")
        if (apiKey.isNotBlank()) builder.header("Authorization", "Bearer ${ApiKeySanitizer.headerSafe(apiKey)}")
        val call = client.newCall(builder.build())
        activeCall = call
        call.execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                if (response.code == 404 || response.code == 405) {
                    throw IOException(ProviderCatalog.cannotEditPicturesLine(baseUrl))
                }
                throw mediaHttpError("pictures", response.code, raw)
            }
            return decodeEditedImage(raw, apiKey)
        }
    }

    private fun editOpenAiImage(
        baseUrl: String,
        apiKey: String,
        imageFile: File,
        prompt: String,
        editModel: String,
    ): ByteArray {
        val root = baseUrl.trim().trimEnd('/')
        val url = imagesUrl(root, "edits")
        val mime = when {
            imageFile.name.endsWith(".png", ignoreCase = true) -> "image/png"
            imageFile.name.endsWith(".jpg", ignoreCase = true) ||
                imageFile.name.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
            else -> "image/png"
        }
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart(
                "image",
                imageFile.name,
                imageFile.asRequestBody(mime.toMediaType()),
            )
            .addFormDataPart("prompt", prompt)
            .addFormDataPart("model", editModel)
            .addFormDataPart("n", "1")
            .addFormDataPart("response_format", "b64_json")
            .build()
        val builder = Request.Builder().url(url).post(body)
        if (apiKey.isNotBlank()) builder.header("Authorization", "Bearer ${ApiKeySanitizer.headerSafe(apiKey)}")
        val call = client.newCall(builder.build())
        activeCall = call
        call.execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                if (response.code == 404 || response.code == 405) {
                    throw IOException(ProviderCatalog.cannotEditPicturesLine(baseUrl))
                }
                throw IOException("HTTP ${response.code}: ${raw.take(400)}")
            }
            return decodeEditedImage(raw, apiKey)
        }
    }

    private fun decodeEditedImage(raw: String, apiKey: String): ByteArray {
        val rootObj = json.parseToJsonElement(raw).jsonObject
        val first = rootObj["data"]?.jsonArray?.firstOrNull()?.jsonObject
        val b64 = first?.get("b64_json")?.jsonPrimitive?.contentOrNull
            ?: rootObj["b64_json"]?.jsonPrimitive?.contentOrNull
        if (!b64.isNullOrBlank()) return Base64.decode(b64, Base64.DEFAULT)
        val urlHit = first?.get("url")?.jsonPrimitive?.contentOrNull
            ?: rootObj["url"]?.jsonPrimitive?.contentOrNull
        if (!urlHit.isNullOrBlank()) return downloadImageBytes(urlHit, apiKey)
        throw IOException("This provider cannot edit pictures.")
    }

    /** P-001: Whisper-compatible transcription. */
    fun transcribeAudio(baseUrl: String, apiKey: String, audioFile: File): String {
        val stt = ProviderCatalog.resolveSttModel(baseUrl)
            ?: throw IOException("${ProviderCatalog.fromBaseUrl(baseUrl).name} cannot turn speech into text.")
        val root = baseUrl.trim().trimEnd('/')
        val url = if (root.endsWith("/audio/transcriptions")) root
        else "$root/audio/transcriptions".replace("/v1/v1/", "/v1/")
        val finalUrl = if (url.contains("/audio/transcriptions")) url
        else "$root/v1/audio/transcriptions"
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                audioFile.name,
                audioFile.asRequestBody("audio/m4a".toMediaType()),
            )
            .addFormDataPart("model", stt)
            .build()
        val builder = Request.Builder().url(finalUrl).post(body)
        if (apiKey.isNotBlank()) builder.header("Authorization", "Bearer ${ApiKeySanitizer.headerSafe(apiKey)}")
        val call = client.newCall(builder.build())
        activeCall = call
        call.execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}: ${raw.take(300)}")
            }
            return json.parseToJsonElement(raw).jsonObject["text"]
                ?.jsonPrimitive?.contentOrNull
                ?: throw IOException("No text in transcription")
        }
    }

    /** P-001: TTS to a file (never a full ByteArray in heap). */
    fun speakToFile(
        baseUrl: String,
        apiKey: String,
        text: String,
        dest: File,
        voice: String = "alloy",
    ) {
        val tts = ProviderCatalog.resolveTtsModel(baseUrl)
            ?: throw IOException("${ProviderCatalog.fromBaseUrl(baseUrl).name} cannot speak out loud.")
        val root = baseUrl.trim().trimEnd('/')
        val url = if (root.endsWith("/audio/speech")) root else "$root/audio/speech".let {
            if (it.contains("/v1/audio/speech")) it else "$root/v1/audio/speech"
        }
        val payload = buildJsonObject {
            put("model", tts)
            put("input", text.take(4096))
            put("voice", voice)
        }.toString()
        val builder = Request.Builder()
            .url(url)
            .post(payload.toRequestBody(JSON))
            .header("Accept", "audio/mpeg")
        if (apiKey.isNotBlank()) builder.header("Authorization", "Bearer ${ApiKeySanitizer.headerSafe(apiKey)}")
        val call = client.newCall(builder.build())
        activeCall = call
        call.execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}: ${response.body?.string()?.take(200)}")
            }
            val body = response.body ?: throw IOException("Empty speech body")
            try {
                FileOutputStream(dest).use { out ->
                    body.byteStream().copyTo(out)
                }
            } catch (e: Exception) {
                dest.delete()
                throw e
            }
        }
    }

    /**
     * C-013: fetch and extract text from a web page via Jsoup.
     * Returns plain text, truncated to ~8K chars (≈2K tokens).
     */
    fun generateImage(
        baseUrl: String,
        apiKey: String,
        prompt: String,
        model: String? = null,
        size: String = "1024x1024",
    ): ByteArray {
        val chosen = model?.takeIf { it.isNotBlank() && it.contains("image", ignoreCase = true) }
            ?: ProviderCatalog.resolveImageModel(baseUrl)
            ?: throw IOException(ProviderCatalog.cannotMakePicturesLine(baseUrl))
        val tries = listOf(chosen) + ProviderCatalog.imageModelFallbacks(baseUrl)
        var last: IOException? = null
        val native = ProviderCatalog.imageUsesNativeGenerate(baseUrl)
        for (id in tries.distinct()) {
            try {
                return if (native) {
                    generateGeminiImage(baseUrl, apiKey, prompt, id)
                } else {
                    generateImageOnce(baseUrl, apiKey, prompt, id, size)
                }
            } catch (e: IOException) {
                last = e
                val msg = e.message.orEmpty()
                val notFound = msg.contains("404") || msg.contains("NOT_FOUND", ignoreCase = true) ||
                    msg.contains("not found", ignoreCase = true)
                if (!notFound) throw e
            }
        }
        throw last ?: IOException(ProviderCatalog.cannotMakePicturesLine(baseUrl))
    }

    private fun generateGeminiImage(
        baseUrl: String,
        apiKey: String,
        prompt: String,
        model: String,
    ): ByteArray {
        val url = geminiGenerateContentUrl(baseUrl, model)
        val body = buildJsonObject {
            put(
                "contents",
                kotlinx.serialization.json.buildJsonArray {
                    add(
                        buildJsonObject {
                            put(
                                "parts",
                                kotlinx.serialization.json.buildJsonArray {
                                    add(buildJsonObject { put("text", prompt) })
                                },
                            )
                        },
                    )
                },
            )
            put(
                "generationConfig",
                buildJsonObject {
                    put(
                        "responseModalities",
                        kotlinx.serialization.json.buildJsonArray {
                            add(kotlinx.serialization.json.JsonPrimitive("TEXT"))
                            add(kotlinx.serialization.json.JsonPrimitive("IMAGE"))
                        },
                    )
                },
            )
        }
        val builder = Request.Builder()
            .url(url)
            .post(body.toString().toRequestBody(JSON))
            .header("Content-Type", "application/json")
            .geminiKey(apiKey)
        val call = client.newCall(builder.build())
        activeCall = call
        call.execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw mediaHttpError("pictures", response.code, raw)
            val b64 = firstInlineImageB64(raw)
                ?: throw IOException("This provider cannot make pictures.")
            return Base64.decode(b64, Base64.DEFAULT)
        }
    }

    private fun generateImageOnce(
        baseUrl: String,
        apiKey: String,
        prompt: String,
        model: String,
        size: String,
    ): ByteArray {
        val root = baseUrl.trim().trimEnd('/')
        val url = if (ProviderCatalog.imageUsesOpenRouter(baseUrl)) {
            openrouterImagesUrl(root)
        } else {
            imagesUrl(root, "generations")
        }
        val body = buildJsonObject {
            put("model", model)
            put("prompt", prompt)
            put("n", 1)
            if (!ProviderCatalog.imageUsesOpenRouter(baseUrl)) {
                put("size", size)
                put("response_format", "b64_json")
            }
        }
        val builder = Request.Builder()
            .url(url)
            .post(body.toString().toRequestBody(JSON))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
        if (apiKey.isNotBlank()) {
            builder.header("Authorization", "Bearer ${ApiKeySanitizer.headerSafe(apiKey)}")
        }
        val call = client.newCall(builder.build())
        activeCall = call
        call.execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw mediaHttpError("pictures", response.code, raw)
            }
            val rootObj = json.parseToJsonElement(raw).jsonObject
            val data = rootObj["data"]?.jsonArray
                ?: throw IOException("This provider cannot make pictures.")
            if (data.isEmpty()) throw IOException("This provider cannot make pictures.")
            val first = data[0].jsonObject
            val b64 = first["b64_json"]?.jsonPrimitive?.contentOrNull
            if (!b64.isNullOrBlank()) {
                return Base64.decode(b64, Base64.DEFAULT)
            }
            val urlHit = first["url"]?.jsonPrimitive?.contentOrNull
            if (!urlHit.isNullOrBlank()) {
                return downloadImageBytes(urlHit, apiKey)
            }
            throw IOException("This provider cannot make pictures.")
        }
    }

    private fun downloadImageBytes(url: String, apiKey: String): ByteArray {
        val tmp = java.io.File.createTempFile("pic", ".bin")
        try {
            streamUrlToFile(url, apiKey, tmp, googleKey = false)
            if (tmp.length() > 8L * 1024 * 1024) {
                throw IOException("That picture is too big.")
            }
            return tmp.readBytes()
        } finally {
            tmp.delete()
        }
    }

    /**
     * C-027: Create a video generation job via Sora-compatible API.
     * Returns the job id for polling.
     */
    fun createVideo(
        baseUrl: String,
        apiKey: String,
        prompt: String,
        seconds: Int = 8,
        size: String = "1280x720",
    ): String {
        val model = ProviderCatalog.resolveVideoModel(baseUrl)
            ?: throw IOException(ProviderCatalog.cannotMakeVideosLine(baseUrl))
        return when {
            ProviderCatalog.videoUsesNativeVeo(baseUrl) ->
                createVeoVideo(baseUrl, apiKey, model, prompt)
            ProviderCatalog.videoUsesXaiImagine(baseUrl) ->
                createXaiVideo(baseUrl, apiKey, model, prompt, seconds)
            else -> createSoraVideo(baseUrl, apiKey, model, prompt, seconds, size)
        }
    }

    private fun createVeoVideo(
        baseUrl: String,
        apiKey: String,
        model: String,
        prompt: String,
    ): String {
        val url = veoStartUrl(baseUrl, model)
        val body = buildJsonObject {
            put(
                "instances",
                kotlinx.serialization.json.buildJsonArray {
                    add(buildJsonObject { put("prompt", prompt) })
                },
            )
        }
        val builder = Request.Builder()
            .url(url)
            .post(body.toString().toRequestBody(JSON))
            .header("Content-Type", "application/json")
            .geminiKey(apiKey)
        val call = client.newCall(builder.build())
        activeCall = call
        call.execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw mediaHttpError("videos", response.code, raw)
            val obj = json.parseToJsonElement(raw).jsonObject
            return obj["name"]?.jsonPrimitive?.content
                ?: throw IOException("This Gemini key cannot make videos.")
        }
    }

    private fun createXaiVideo(
        baseUrl: String,
        apiKey: String,
        model: String,
        prompt: String,
        seconds: Int,
    ): String {
        val url = xaiVideoStartUrl(baseUrl)
        val body = buildJsonObject {
            put("model", model)
            put("prompt", prompt)
            put("duration", seconds)
        }
        val builder = Request.Builder()
            .url(url)
            .post(body.toString().toRequestBody(JSON))
            .header("Content-Type", "application/json")
        if (apiKey.isNotBlank()) builder.header("Authorization", "Bearer ${ApiKeySanitizer.headerSafe(apiKey)}")
        val call = client.newCall(builder.build())
        activeCall = call
        call.execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw mediaHttpError("videos", response.code, raw)
            val obj = json.parseToJsonElement(raw).jsonObject
            return obj["request_id"]?.jsonPrimitive?.content
                ?: obj["id"]?.jsonPrimitive?.content
                ?: throw IOException("This Grok key cannot make videos.")
        }
    }

    private fun createSoraVideo(
        baseUrl: String,
        apiKey: String,
        model: String,
        prompt: String,
        seconds: Int,
        size: String,
    ): String {
        val url = openaiVideosUrl(baseUrl)
        val body = buildJsonObject {
            put("model", model)
            put("prompt", prompt)
            put("seconds", seconds)
            put("size", size)
        }
        val builder = Request.Builder()
            .url(url)
            .post(body.toString().toRequestBody(JSON))
            .header("Content-Type", "application/json")
        if (apiKey.isNotBlank()) builder.header("Authorization", "Bearer ${ApiKeySanitizer.headerSafe(apiKey)}")
        val call = client.newCall(builder.build())
        activeCall = call
        call.execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw mediaHttpError("videos", response.code, raw)
            val obj = json.parseToJsonElement(raw).jsonObject
            return obj["id"]?.jsonPrimitive?.content
                ?: throw IOException("This provider cannot make videos.")
        }
    }

    /**
     * C-027/C-028: Poll a video job, streaming the finished MP4 to [destFile]
     * instead of loading it into heap (C-028: the old path did
     * `body.bytes()` — a full 5-20MB ByteArray). Streams with Okio/byteStream
     * and returns the file. Throws if job failed, not found, or timed out.
     */
    fun pollVideo(
        baseUrl: String,
        apiKey: String,
        jobId: String,
        destFile: java.io.File,
        timeout: Long = 300_000,
    ): java.io.File {
        return when {
            ProviderCatalog.videoUsesNativeVeo(baseUrl) ->
                pollVeoVideo(baseUrl, apiKey, jobId, destFile, timeout)
            ProviderCatalog.videoUsesXaiImagine(baseUrl) ->
                pollXaiVideo(baseUrl, apiKey, jobId, destFile, timeout)
            else -> pollSoraVideo(baseUrl, apiKey, jobId, destFile, timeout)
        }
    }

    private fun pollVeoVideo(
        baseUrl: String,
        apiKey: String,
        jobId: String,
        destFile: java.io.File,
        timeout: Long,
    ): java.io.File {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeout) {
            val url = veoPollUrl(baseUrl, jobId)
            val builder = Request.Builder().url(url).get().geminiKey(apiKey)
            val call = client.newCall(builder.build())
            activeCall = call
            val raw = call.execute().use { it.body?.string().orEmpty() }
            val obj = json.parseToJsonElement(raw).jsonObject
            val done = obj["done"].toString() == "true"
            if (done) {
                val uri = obj["response"]
                    ?.jsonObject?.get("generateVideoResponse")
                    ?.jsonObject?.get("generatedSamples")
                    ?.jsonArray?.firstOrNull()
                    ?.jsonObject?.get("video")
                    ?.jsonObject?.get("uri")
                    ?.jsonPrimitive?.contentOrNull
                    ?: throw IOException("This Gemini key cannot make videos.")
                streamUrlToFile(uri, apiKey, destFile, googleKey = true)
                return destFile
            }
            Thread.sleep(10_000)
        }
        throw IOException("Video generation timed out")
    }

    private fun pollXaiVideo(
        baseUrl: String,
        apiKey: String,
        jobId: String,
        destFile: java.io.File,
        timeout: Long,
    ): java.io.File {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeout) {
            val url = xaiVideoPollUrl(baseUrl, jobId)
            val builder = Request.Builder().url(url).get()
            if (apiKey.isNotBlank()) builder.header("Authorization", "Bearer ${ApiKeySanitizer.headerSafe(apiKey)}")
            val call = client.newCall(builder.build())
            activeCall = call
            val raw = call.execute().use { it.body?.string().orEmpty() }
            val obj = json.parseToJsonElement(raw).jsonObject
            when (obj["status"]?.jsonPrimitive?.contentOrNull ?: "unknown") {
                "done" -> {
                    val dl = obj["video"]?.jsonObject?.get("url")?.jsonPrimitive?.contentOrNull
                        ?: throw IOException("This Grok key cannot make videos.")
                    streamUrlToFile(dl, apiKey, destFile, googleKey = false)
                    return destFile
                }
                "failed", "expired" -> throw IOException("This Grok key cannot make videos.")
                else -> Thread.sleep(5_000)
            }
        }
        throw IOException("Video generation timed out")
    }

    private fun pollSoraVideo(
        baseUrl: String,
        apiKey: String,
        jobId: String,
        destFile: java.io.File,
        timeout: Long,
    ): java.io.File {
        val root = openaiVideosUrl(baseUrl)
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeout) {
            val url = "$root/$jobId"
            val builder = Request.Builder().url(url).get()
            if (apiKey.isNotBlank()) builder.header("Authorization", "Bearer ${ApiKeySanitizer.headerSafe(apiKey)}")
            val call = client.newCall(builder.build())
            activeCall = call
            val raw = call.execute().use { it.body?.string().orEmpty() }
            val obj = json.parseToJsonElement(raw).jsonObject
            when (obj["status"]?.jsonPrimitive?.content ?: "unknown") {
                "completed" -> {
                    streamUrlToFile(
                        "$root/$jobId/content?variant=video",
                        apiKey,
                        destFile,
                        googleKey = false,
                    )
                    return destFile
                }
                "failed" -> throw IOException("This provider cannot make videos.")
                "queued", "in_progress" -> Thread.sleep(2_000)
                else -> throw IOException("This provider cannot make videos.")
            }
        }
        throw IOException("Video generation timed out")
    }

    private fun streamUrlToFile(
        url: String,
        apiKey: String,
        destFile: java.io.File,
        googleKey: Boolean,
    ) {
        val builder = Request.Builder().url(url).get()
        if (apiKey.isNotBlank()) {
            if (googleKey) builder.geminiKey(apiKey) else builder.header("Authorization", "Bearer ${ApiKeySanitizer.headerSafe(apiKey)}")
        }
        val call = client.newCall(builder.build())
        activeCall = call
        try {
            call.execute().use { r ->
                if (!r.isSuccessful) {
                    throw mediaHttpError("videos", r.code, r.body?.string().orEmpty())
                }
                val body = r.body ?: throw IOException("Empty video body")
                java.io.FileOutputStream(destFile).use { out ->
                    body.byteStream().use { input -> input.copyTo(out) }
                }
            }
        } catch (e: Exception) {
            destFile.delete()
            throw e
        }
    }

    private fun mediaHttpError(kind: String, code: Int, raw: String): IOException {
        return IOException(friendlyMediaError(kind, code, raw))
    }

    /** Gemini native doors reject Authorization: Bearer + an API key (401 OAuth). */
    private fun Request.Builder.geminiKey(apiKey: String): Request.Builder {
        val k = ApiKeySanitizer.headerSafe(apiKey)
        if (k.isNotBlank()) header("x-goog-api-key", k)
        return this
    }

    private fun executeChat(
        url: String,
        apiKey: String,
        model: String,
        messages: List<ChatMessageDto>,
        temperature: Float,
        stream: Boolean,
        options: ChatOptions = ChatOptions(),
    ): Response {
        val payload = buildJsonObject {
            put("model", ProviderCatalog.resolveModel(model))
            put("temperature", temperature)
            put("stream", stream)
            put(
                "messages",
                kotlinx.serialization.json.buildJsonArray {
                    messages.forEach { m ->
                        add(
                            buildJsonObject {
                                put("role", m.role)
                                put("content", m.content)
                            },
                        )
                    }
                },
            )
            val topP = options.topP
            if (topP != null && topP != 1f) put("top_p", topP)
            val pres = options.presencePenalty
            if (pres != null && pres != 0f) put("presence_penalty", pres)
            val freq = options.frequencyPenalty
            if (freq != null && freq != 0f) put("frequency_penalty", freq)
            val maxTok = options.maxTokens
            if (maxTok != null && maxTok > 0) put("max_tokens", maxTok)
            if (options.promptCache) put("prompt_cache_key", "byo-ai")
        }.toString()
        val builder = Request.Builder()
            .url(url)
            .post(payload.toRequestBody(JSON))
            .header("Content-Type", "application/json")
            .header("Accept", if (stream) "text/event-stream" else "application/json")
        if (apiKey.isNotBlank()) {
            builder.header("Authorization", "Bearer ${ApiKeySanitizer.headerSafe(apiKey)}")
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

        fun xaiEditJson(model: String, prompt: String, dataUri: String): String =
            buildJsonObject {
                put("model", model)
                put("prompt", prompt)
                put(
                    "image",
                    buildJsonObject {
                        put("url", dataUri)
                        put("type", "image_url")
                    },
                )
            }.toString()

        fun friendlyMediaError(kind: String, code: Int, raw: String): String {
            if (code == 404 || code == 405) return "This provider cannot make $kind."
            val blob = raw.replace('\n', ' ')
            if (blob.contains("API keys are not supported", ignoreCase = true) ||
                blob.contains("Expected OAuth2", ignoreCase = true)
            ) {
                return "Google will not take this key on the $kind door. Pick Gemini and paste an AI Studio key. Not Vertex or Cloud."
            }
            return "HTTP $code: ${blob.take(120)}"
        }

        /** Catalog baseUrl already ends in /v1. Never emit /v1/v1/…. */
        fun imagesUrl(baseUrl: String, kind: String): String {
            val root = baseUrl.trim().trimEnd('/')
            return when {
                root.endsWith("/images/$kind") -> root
                root.contains("/images/generations") && kind == "edits" ->
                    root.replace("/generations", "/edits")
                else -> "$root/images/$kind"
            }
        }

        /** OpenRouter pictures: POST /api/v1/images — not /images/generations. */
        fun openrouterImagesUrl(baseUrl: String): String {
            val root = baseUrl.trim().trimEnd('/')
            return if (root.endsWith("/images")) root else "$root/images"
        }

        fun geminiNativeRoot(baseUrl: String): String {
            var root = baseUrl.trim().trimEnd('/')
            if (root.endsWith("/openai")) root = root.removeSuffix("/openai")
            return root.trimEnd('/')
        }

        fun geminiGenerateContentUrl(baseUrl: String, model: String): String =
            "${geminiNativeRoot(baseUrl)}/models/$model:generateContent"

        fun firstInlineImageB64(raw: String): String? {
            val obj = Json { ignoreUnknownKeys = true }.parseToJsonElement(raw).jsonObject
            val parts = obj["candidates"]?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("content")
                ?.jsonObject?.get("parts")
                ?.jsonArray ?: return null
            for (el in parts) {
                val part = el.jsonObject
                val blob = part["inlineData"]?.jsonObject
                    ?: part["inline_data"]?.jsonObject
                val data = blob?.get("data")?.jsonPrimitive?.contentOrNull
                if (!data.isNullOrBlank()) return data
            }
            return null
        }

        fun veoStartUrl(baseUrl: String, model: String): String =
            "${geminiNativeRoot(baseUrl)}/models/$model:predictLongRunning"

        fun veoPollUrl(baseUrl: String, operationName: String): String {
            val name = operationName.trim().removePrefix("/")
            return "${geminiNativeRoot(baseUrl)}/$name"
        }

        fun openaiVideosUrl(baseUrl: String): String {
            val root = baseUrl.trim().trimEnd('/')
            return if (root.endsWith("/videos")) root else "$root/videos"
        }

        fun xaiVideoStartUrl(baseUrl: String): String =
            "${baseUrl.trim().trimEnd('/')}/videos/generations"

        fun xaiVideoPollUrl(baseUrl: String, jobId: String): String =
            "${baseUrl.trim().trimEnd('/')}/videos/$jobId"

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
