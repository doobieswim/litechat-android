package com.litechat.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.litechat.android.core.flags.FeatureFlags
import com.litechat.android.data.AppContainer
import com.litechat.android.data.api.ChatMessageDto
import com.litechat.android.data.api.RetryPolicy
import com.litechat.android.data.api.StreamEvent
import com.litechat.android.data.connectivity.ConnectivityObserver
import com.litechat.android.data.context.ContextTrimmer
import com.litechat.android.data.db.ConversationEntity
import com.litechat.android.data.db.MessageEntity
import com.litechat.android.data.prefs.AppSettings
import com.litechat.android.data.prefs.PromptTemplate
import com.litechat.android.data.prefs.SettingsRepository
import com.litechat.android.util.DeviceCompat
import com.litechat.android.util.ImageCacheConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

data class ChatUiState(
    val settings: AppSettings = AppSettings(),
    val conversations: List<ConversationEntity> = emptyList(),
    val activeConversationId: String? = null,
    val messages: List<MessageEntity> = emptyList(),
    val input: String = "",
    val isStreaming: Boolean = false,
    val streamingText: String = "",
    val error: String? = null,
    val apiKeyPresent: Boolean = false,
    /** Imp#2: true when connectivity is lost — UI shows "Waiting for connection…". */
    val waitingForConnection: Boolean = false,
    /** Imp#3: retry progress label, e.g. "Retry (2/3)". Null when not retrying. */
    val retryProgress: String? = null,
    /** C-011: true while image generation is in progress (~5-15s for DALL-E). */
    val isGeneratingImage: Boolean = false,
    /** C-012: prompt templates (reactive from settings). */
    val templates: List<PromptTemplate> = emptyList(),
    /** C-010: count of messages trimmed by context budget. */
    val truncatedCount: Int = 0,
    /** Estimated cost of last assistant response. */
    val lastCost: String? = null,
)

class ChatViewModel(
    private val container: AppContainer,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private var streamJob: Job? = null
    private var messagesCollectJob: Job? = null
    private var connectivityJob: Job? = null

    init {
        viewModelScope.launch {
            combine(
                container.settingsRepository.settings,
                container.chatRepository.observeConversations(),
                container.billingRepository.proOwned,
            ) { settings, convos, billingPro ->
                Triple(settings, convos, billingPro || settings.isPro)
            }.collect { (settings, convos, isPro) ->
                // Imp#5: single Pro gate through FeatureFlags
                FeatureFlags.setPro(isPro || settings.isPro)
                if (isPro && !settings.isPro) {
                    container.settingsRepository.update(isPro = true)
                }
                val merged = settings.copy(isPro = FeatureFlags.isPro)
                _state.update {
                    it.copy(
                        settings = merged,
                        conversations = convos,
                        apiKeyPresent = container.settingsRepository.getApiKey().isNotBlank(),
                        activeConversationId = it.activeConversationId?.takeIf { id ->
                            convos.any { c -> c.id == id }
                        },
                    )
                }
            }
        }

        // Imp#2: connectivity observer — pause when disconnected
        connectivityJob = viewModelScope.launch {
            container.connectivityObserver.state.observeForever { state ->
                val waiting = state == ConnectivityObserver.State.Disconnected
                _state.update { it.copy(waitingForConnection = waiting) }
            }
        }

        container.billingRepository.startConnection()

        // C-012: observe templates from settings
        viewModelScope.launch {
            container.settingsRepository.templates.collect { list ->
                val isPro = FeatureFlags.isPro ||
                    _state.value.settings.isPro
                val visible = if (isPro) list
                else list.take(SettingsRepository.FREE_TEMPLATE_LIMIT)
                _state.update { it.copy(templates = visible) }
            }
        }
    }

    fun setInput(value: String) {
        _state.update { it.copy(input = InputPolicy.cap(value)) }
    }

    companion object {
        const val MAX_INPUT_CHARS = InputPolicy.MAX_INPUT_CHARS
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun selectConversation(id: String) {
        messagesCollectJob?.cancel()
        // C-018: switch to the conversation's saved model if present.
        val convModel = _state.value.conversations.find { it.id == id }?.model
        _state.update {
            it.copy(
                activeConversationId = id,
                messages = emptyList(),
                streamingText = "",
                error = null,
                settings = if (!convModel.isNullOrBlank())
                    it.settings.copy(model = convModel) else it.settings,
            )
        }
        messagesCollectJob = viewModelScope.launch {
            container.chatRepository.observeMessages(id).collect { list ->
                _state.update { s -> s.copy(messages = list) }
            }
        }
    }

    fun newChat() {
        viewModelScope.launch {
            val c = container.chatRepository.createConversation(
                model = _state.value.settings.model
            )
            selectConversation(c.id)
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            container.chatRepository.deleteConversation(id)
            if (_state.value.activeConversationId == id) {
                messagesCollectJob?.cancel()
                _state.update {
                    it.copy(activeConversationId = null, messages = emptyList())
                }
            }
        }
    }

    fun stopStreaming() {
        streamJob?.cancel()
        container.openAiClient.cancel()
        _state.update { it.copy(isStreaming = false, retryProgress = null) }
    }

    /** C-012: insert a rendered template into the input field. */
    fun insertTemplate(template: PromptTemplate) {
        val rendered = template.render()
        setInput(rendered)
    }

    /** C-012: save a new or edited template. Pro-gated beyond free limit. */
    fun saveTemplate(template: PromptTemplate) {
        viewModelScope.launch {
            container.settingsRepository.saveTemplate(template)
        }
    }

    /** C-012: delete a template by id. */
    fun deleteTemplate(id: String) {
        viewModelScope.launch {
            container.settingsRepository.deleteTemplate(id)
        }
    }

    fun send() {
        val text = _state.value.input.trim()
        if (text.isEmpty() || _state.value.isStreaming) return

        // Imp#2: block send when disconnected
        if (!container.connectivityObserver.isConnected) {
            _state.update { it.copy(waitingForConnection = true, error = "Waiting for connection…") }
            return
        }

        val key = container.settingsRepository.getApiKey()
        val settings = _state.value.settings
        val localEndpoint = settings.baseUrl.contains("127.0.0.1") ||
            settings.baseUrl.contains("localhost")
        if (key.isBlank() && !localEndpoint) {
            _state.update { it.copy(error = "Add an API key in Settings") }
            return
        }

        // C-013: /browse command — fetch web page, inject into context.
        if (text.startsWith("/browse ")) {
            val url = text.removePrefix("/browse ").trim()
            if (url.isEmpty()) {
                _state.update { it.copy(error = "Usage: /browse <url>") }
                return
            }
            viewModelScope.launch {
                var convId = _state.value.activeConversationId
                if (convId == null) {
                    val c = container.chatRepository.createConversation(
                        title = "/browse ${url.take(40)}",
                        model = settings.model,
                    )
                    convId = c.id
                    selectConversation(convId)
                }
                _state.update { it.copy(input = "", error = null) }
                container.chatRepository.addMessage(convId, "user", "/browse $url")
                try {
                    val pageText = container.openAiClient.fetchPage(url)
                    val ctx = "

[Content from $url]
$pageText"
                    container.chatRepository.addMessage(convId, "assistant", ctx)
                } catch (e: Exception) {
                    _state.update { it.copy(error = "Browse failed: ${e.message?.take(100)}") }
                    container.chatRepository.addMessage(convId, "assistant",
                        "Failed to fetch $url: ${e.message?.take(200)}")
                }
            }
            return
        }

        // C-011: /imagine slash command — generate image via same BYOK key.
        if (text.startsWith("/imagine ")) {
            val prompt = text.removePrefix("/imagine ").trim()
            if (prompt.isEmpty()) {
                _state.update { it.copy(error = "Usage: /imagine <prompt>") }
                return
            }
            viewModelScope.launch {
                var convId = _state.value.activeConversationId
                if (convId == null) {
                    val c = container.chatRepository.createConversation(
                        title = "/imagine ${prompt.take(40)}",
                        model = settings.model,
                    )
                    convId = c.id
                    selectConversation(convId)
                }
                _state.update { it.copy(input = "", error = null, isGeneratingImage = true) }
                // Insert user's prompt as a regular message.
                container.chatRepository.addMessage(convId, "user", "/imagine $prompt")
                try {
                    val imageBytes = container.openAiClient.generateImage(
                        baseUrl = settings.baseUrl,
                        apiKey = key,
                        prompt = prompt,
                    )
                    // Save to cache dir, downscaled for weak devices.
                    val maxDim = ImageCacheConfig.maxSaveDimension(
                        DeviceCompat.snapshot(container.ctx).band
                    )
                    val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    val scaled = if (bitmap != null && (bitmap.width > maxDim || bitmap.height > maxDim)) {
                        val ratio = maxDim.toFloat() / maxOf(bitmap.width, bitmap.height)
                        android.graphics.Bitmap.createScaledBitmap(
                            bitmap,
                            (bitmap.width * ratio).toInt(),
                            (bitmap.height * ratio).toInt(),
                            true
                        )
                    } else bitmap
                    val outStream = java.io.ByteArrayOutputStream()
                    scaled?.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, outStream)
                    val file = java.io.File(
                        container.ctx.cacheDir,
                        "gen_${System.currentTimeMillis()}.jpg"
                    )
                    file.writeBytes(outStream.toByteArray())
                    scaled?.recycle()
                    bitmap?.recycle()
                    container.chatRepository.addMessage(
                        convId, "assistant",
                        "[IMAGE:${file.absolutePath}]"
                    )
                } catch (e: Exception) {
                    _state.update {
                        it.copy(
                            error = "Image generation failed: ${e.message?.take(120)}"
                        )
                    }
                    container.chatRepository.addMessage(
                        convId, "assistant",
                        "Image generation failed: ${e.message?.take(200) ?: "Unknown error"}"
                    )
                } finally {
                    _state.update { it.copy(isGeneratingImage = false) }
                }
            }
            return
        }

        viewModelScope.launch {
            var convId = _state.value.activeConversationId
            if (convId == null) {
                val c = container.chatRepository.createConversation(
                    title = text.take(48).ifBlank { "New chat" },
                    model = settings.model,
                )
                convId = c.id
                selectConversation(convId)
            } else if (_state.value.messages.isEmpty()) {
                container.chatRepository.renameConversation(convId, text.take(48))
            }

            _state.update { it.copy(input = "", error = null) }
            container.chatRepository.addMessage(convId, "user", text)

            val history = container.chatRepository.listMessages(convId)
            // Kai 9000 "honesty rule": measurably reduces model fabrication.
            val systemMsg = ChatMessageDto("system",
                "Do not fabricate tool outputs, file contents, citations, or completed work.")
            val dto = listOf(systemMsg) + history.map { ChatMessageDto(it.role, it.content) }

            // C-010: trim to token budget, track removed count.
            val (trimmed, removed) = ContextTrimmer.trim(dto)
            if (removed > 0) {
                _state.update { it.copy(truncatedCount = removed) }
            }

            // Imp#3: retry loop with exponential backoff + jitter
            val maxRetries = RetryPolicy.MAX_ATTEMPTS
            var lastError: String? = null

            for (attempt in 1..maxRetries) {
                if (attempt > 1) {
                    _state.update {
                        it.copy(retryProgress = "Retry ($attempt/$maxRetries)")
                    }
                    delay(RetryPolicy.delayMs(attempt))
                }

                // Imp#2: check connectivity before each attempt
                if (!container.connectivityObserver.isConnected) {
                    _state.update {
                        it.copy(
                            waitingForConnection = true,
                            error = "Waiting for connection…",
                            retryProgress = null,
                        )
                    }
                    return@launch
                }

                val assistant = container.chatRepository.addMessage(
                    convId, "assistant",
                    if (attempt > 1) "Retrying…" else ""
                )
                val assistantId = assistant.id
                val acc = StringBuilder()

                _state.update { it.copy(isStreaming = true, streamingText = "") }

                streamJob?.cancel()
                val streamResult = try {
                    val preferNonStream =
                        container.settingsRepository.isStreamBrokenNow(settings.baseUrl)
                    container.openAiClient.streamChat(
                        baseUrl = settings.baseUrl,
                        apiKey = key,
                        model = settings.model,
                        messages = trimmed,
                        temperature = settings.temperature,
                        preferNonStream = preferNonStream,
                    ).collect { event ->
                        when (event) {
                            is StreamEvent.Delta -> {
                                acc.append(event.text)
                                // C-006: throttle UI updates via FeatureFlags
                                val now = System.currentTimeMillis()
                                if (now - lastUiUpdate >= FeatureFlags.streamThrottleMs) {
                                    lastUiUpdate = now
                                    _state.update {
                                        it.copy(streamingText = acc.toString())
                                    }
                                }
                            }
                            is StreamEvent.Error -> {
                                // Imp#3: errors pass through immediately, no throttle
                                lastError = event.message
                                _state.update {
                                    it.copy(error = event.message)
                                }
                            }
                            StreamEvent.FallbackUsed -> {
                                container.settingsRepository.markStreamBroken(
                                    settings.baseUrl,
                                )
                            }
                            StreamEvent.Done -> {
                                // C-006: flush final paint so no deltas are lost
                                _state.update {
                                    it.copy(streamingText = acc.toString())
                                }
                            }
                        }
                    }
                    null // success — null means no error to retry
                } catch (e: Exception) {
                    // Imp#3: catch stream-level errors for retry
                    e.message ?: "Stream failed"
                }

                val finalText = acc.toString()
                when {
                    finalText.isNotEmpty() -> {
                        container.chatRepository.updateMessageContent(
                                                    assistantId, convId, finalText
                                                )
                                                val approxTokens = finalText.length / 4
                                                val cost = approxTokens * 0.0000015  // ~$1.50 per 1M input tokens
                                                _state.update {
                                                    it.copy(isStreaming = false, streamingText = "",
                                                        retryProgress = null,
                                                        lastCost = "≈ $${"%.4f".format(cost)}")
                                                }
                        return@launch // success — exit retry loop
                    }
                    streamResult != null -> {
                        // Stream had errors and no content — retry with non-stream
                        lastError = streamResult
                        container.chatRepository.updateMessageContent(
                            assistantId, convId, "Error: $streamResult"
                        )
                        // Fall through to next attempt
                    }
                    _state.value.error != null -> {
                        container.chatRepository.updateMessageContent(
                            assistantId, convId, "Error: ${_state.value.error}"
                        )
                        if (attempt < maxRetries) continue // retry
                        _state.update {
                            it.copy(isStreaming = false, streamingText = "", retryProgress = null)
                        }
                        return@launch
                    }
                    else -> {
                        container.chatRepository.updateMessageContent(
                            assistantId, convId, "(empty response)"
                        )
                        if (attempt < maxRetries) continue // retry
                        _state.update {
                            it.copy(isStreaming = false, streamingText = "", retryProgress = null)
                        }
                        return@launch
                    }
                }
            }

            // All retries exhausted — C-017: try failover providers.
            val providers = container.settingsRepository.getProviderList()
            if (providers.isNotEmpty()) {
                for (provider in providers) {
                    try {
                        _state.update { it.copy(retryProgress = "Trying ${provider.baseUrl.take(30)}…") }
                        val result = container.openAiClient.completeChat(
                            baseUrl = provider.baseUrl,
                            apiKey = provider.apiKey.ifBlank { key },
                            model = provider.model.ifBlank { settings.model },
                            messages = trimmed,
                            temperature = settings.temperature,
                        )
                        if (result.isNotEmpty()) {
                            container.chatRepository.addMessage(convId, "assistant", result)
                            _state.update { it.copy(isStreaming = false, streamingText = "", retryProgress = null) }
                            return@launch
                        }
                    } catch (_: Exception) { /* try next */ }
                }
            }
            _state.update {
                it.copy(
                    isStreaming = false,
                    streamingText = "",
                    error = lastError ?: "Request failed after $maxRetries attempts",
                    retryProgress = null,
                )
            }
        }
    }

    /** C-006: last UI paint timestamp for throttle gate. */
    private var lastUiUpdate = 0L

    fun saveSettings(
        apiKey: String,
        baseUrl: String,
        model: String,
        temperature: Float,
        finishOnboarding: Boolean = false,
    ) {
        viewModelScope.launch {
            container.settingsRepository.setApiKey(apiKey)
            container.settingsRepository.update(
                baseUrl = baseUrl,
                model = model,
                temperature = temperature,
                onboardingDone = if (finishOnboarding) true else null,
            )
            _state.update {
                it.copy(apiKeyPresent = apiKey.isNotBlank(), error = null)
            }
        }
    }

    // Imp#5: single Pro setter through FeatureFlags
    fun setPro(isPro: Boolean) {
        FeatureFlags.setPro(isPro)
        viewModelScope.launch {
            container.settingsRepository.update(isPro = isPro)
        }
    }

    // C-014: export chat database to a file via SAF.
    fun exportChats(uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val dbFile = container.ctx.getDatabasePath("litechat.db")
                container.ctx.contentResolver.openOutputStream(uri)?.use { out ->
                    dbFile.inputStream().use { it.copyTo(out) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Export failed: ${e.message?.take(60)}") }
            }
        }
    }

    // C-014: import chat database from a file via SAF.
    fun importChats(uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val dbFile = container.ctx.getDatabasePath("litechat.db")
                container.ctx.contentResolver.openInputStream(uri)?.use { input ->
                    dbFile.outputStream().use { input.copyTo(it) }
                }
                _state.update { it.copy(error = "Chats imported — restart to reload") }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Import failed: ${e.message?.take(60)}") }
            }
        }
    }

    // C-016: attach image/file for vision model analysis.
    fun attachImage(uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val bytes = container.ctx.contentResolver.openInputStream(uri)?.readBytes()
                    ?: return@launch
                val b64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                val mime = container.ctx.contentResolver.getType(uri) ?: "image/jpeg"
                setInput("[IMG:data:$mime;base64,$b64] Describe this.")
            } catch (e: Exception) {
                _state.update { it.copy(error = "Attachment failed: ${e.message?.take(60)}") }
            }
        }
    }

    fun shareChat(convId: String?) {
        val id = convId ?: return
        viewModelScope.launch {
            val text = container.chatRepository.exportAsText(id)
            if (text.isNotEmpty()) {
                _state.update { it.copy(lastCost = "Chat exported as text") }
            }
        }
    }

    fun getCurrentChatText(): String? {
        val id = _state.value.activeConversationId ?: return null
        return runBlocking { container.chatRepository.exportAsText(id) }
    }

    fun clearHistory() {
        viewModelScope.launch {
            stopStreaming()
            container.chatRepository.clearAll()
            messagesCollectJob?.cancel()
            _state.update {
                it.copy(activeConversationId = null, messages = emptyList())
            }
        }
    }

    override fun onCleared() {
        stopStreaming()
        container.connectivityObserver.unregister()
        connectivityJob?.cancel()
        super.onCleared()
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(container) as T
        }
    }
}