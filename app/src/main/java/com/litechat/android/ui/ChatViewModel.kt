package com.litechat.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.litechat.android.core.flags.FeatureFlags
import com.litechat.android.data.AppContainer
import com.litechat.android.data.api.BrowseUrl
import com.litechat.android.data.api.ChatMessageDto
import com.litechat.android.data.api.SlashInput
import com.litechat.android.data.api.RetryPolicy
import com.litechat.android.data.api.StreamEvent
import com.litechat.android.data.connectivity.ConnectivityObserver
import com.litechat.android.data.context.ContextTrimmer
import com.litechat.android.data.db.ConversationEntity
import com.litechat.android.data.db.MessageEntity
import com.litechat.android.data.db.SearchHit
import com.litechat.android.data.prefs.ApiKeySanitizer
import com.litechat.android.data.prefs.AppSettings
import com.litechat.android.data.prefs.NamedKeyStore
import com.litechat.android.data.prefs.ProviderCatalog
import com.litechat.android.data.prefs.PromptTemplate
import com.litechat.android.data.prefs.SettingsRepository
import com.litechat.android.data.api.ChatOptions
import com.litechat.android.data.prefs.ChatFolder
import com.litechat.android.data.prefs.PersonaPacks
import com.litechat.android.util.BackupCrypto
import com.litechat.android.util.VoiceDailyLimit
import com.litechat.android.util.DeviceCompat
import com.litechat.android.util.ImageCacheConfig
import com.litechat.android.util.MediaCleanup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    /** C-023: named keys (encrypted) for multi-key per provider. */
    val namedKeys: List<NamedKeyStore.NamedKey> = emptyList(),
    /** P-002: last search box text. */
    val searchQuery: String = "",
    /** P-002: hits grouped in the UI by conversation. */
    val searchResults: List<SearchHit> = emptyList(),
    /** P-002: scroll-to + highlight after opening a hit. */
    val highlightMessageId: String? = null,
    /** P-009: folders + which one the drawer is showing (null = All). */
    val folders: List<com.litechat.android.data.prefs.ChatFolder> = emptyList(),
    val activeFolderId: String? = null,
    /** P-006: visible memory list for Settings. */
    val memories: List<com.litechat.android.data.context.MemoryManager.MemoryEntry> = emptyList(),
    /** P-001: TTS is playing. */
    val isSpeaking: Boolean = false,
    /** P-003: one quiet backup reminder. */
    val showBackupReminder: Boolean = false,
    /** False until DataStore has emitted once. Do not treat defaults as "never set up". */
    val settingsReady: Boolean = false,
)

class ChatViewModel(
    private val container: AppContainer,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private var streamJob: Job? = null
    private var searchJob: Job? = null
    private var messagesCollectJob: Job? = null
    private var connectivityJob: Job? = null
    private var stopRequested = false
    private var ttsPlayer: android.media.MediaPlayer? = null
    private var pendingBackupPass: String = ""

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
                val prevActive = _state.value.activeConversationId
                val nextActive = SetupGate.restoreConversationId(
                    prevActive,
                    convos.map { c -> c.id },
                )
                _state.update {
                    it.copy(
                        settings = merged,
                        settingsReady = true,
                        conversations = convos,
                        apiKeyPresent = container.settingsRepository.getApiKey().isNotBlank(),
                        showBackupReminder = merged.isPro && !merged.backupReminderDone,
                        activeConversationId = nextActive,
                    )
                }
                if (nextActive != null && nextActive != prevActive) {
                    selectConversation(nextActive)
                }
            }
        }

        // C-023: load named keys into state once at startup.
        refreshNamedKeys()

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

        viewModelScope.launch {
            container.settingsRepository.folders.collect { list ->
                _state.update { it.copy(folders = list) }
            }
        }
        refreshMemories()
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
        val prev = _state.value
        val prevId = prev.activeConversationId
        val prevInput = prev.input
        messagesCollectJob?.cancel()
        // C-018: switch to the conversation's saved model if present.
        val convModel = _state.value.conversations.find { it.id == id }?.model
        _state.update {
            it.copy(
                activeConversationId = id,
                messages = emptyList(),
                streamingText = "",
                error = null,
                // P-014: unsent text belongs to its own chat — draft restored below.
                input = "",
                settings = if (!convModel.isNullOrBlank())
                    it.settings.copy(model = convModel) else it.settings,
            )
        }
        // P-014: save the old chat's draft, then load this chat's draft.
        if (prevId != null && prevId != id) {
            viewModelScope.launch {
                container.settingsRepository.saveDraft(prevId, prevInput)
            }
        }
        viewModelScope.launch {
            val draft = container.settingsRepository.getDraft(id)
            if (_state.value.activeConversationId == id && !_state.value.isStreaming) {
                _state.update { it.copy(input = InputPolicy.cap(draft)) }
            }
        }
        messagesCollectJob = viewModelScope.launch {
            container.chatRepository.observeMessages(id).collect { list ->
                _state.update { s -> s.copy(messages = list) }
            }
        }
    }

    fun newChat() {
        // P-014: save the current chat's unsent text before opening a new one.
        val prev = _state.value
        val prevId = prev.activeConversationId
        if (prevId != null && prev.input.isNotBlank()) {
            viewModelScope.launch {
                container.settingsRepository.saveDraft(prevId, prev.input)
            }
        }
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
            container.settingsRepository.clearDraft(id)
            if (_state.value.activeConversationId == id) {
                messagesCollectJob?.cancel()
                _state.update {
                    it.copy(activeConversationId = null, messages = emptyList())
                }
            }
        }
    }

    /** P-014: pin/unpin a conversation (pinned chats sort to the top). */
    fun togglePin(id: String) {
        viewModelScope.launch { container.chatRepository.togglePin(id) }
    }

    /** P-002: search every saved message. Pro only. */
    fun searchChats(raw: String) {
        if (!container.isPro()) {
            _state.update {
                it.copy(error = "Search is a Pro feature — pay once to unlock")
            }
            return
        }
        _state.update { it.copy(searchQuery = raw) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val hits = withContext(Dispatchers.IO) {
                container.chatRepository.searchMessages(raw)
            }
            if (raw != _state.value.searchQuery) return@launch
            _state.update { it.copy(searchResults = hits) }
        }
    }

    /** P-002: open the chat and land on that message. */
    fun openSearchHit(hit: SearchHit) {
        selectConversation(hit.conversationId)
        _state.update { it.copy(highlightMessageId = hit.messageId) }
    }

    fun clearSearch() {
        _state.update { it.copy(searchQuery = "", searchResults = emptyList()) }
    }

    fun clearHighlight() {
        _state.update { it.copy(highlightMessageId = null) }
    }

    fun selectFolder(folderId: String?) {
        _state.update { it.copy(activeFolderId = folderId) }
    }

    fun createFolder(name: String) {
        if (!container.isPro()) {
            _state.update { it.copy(error = "Folders are a Pro feature — pay once to unlock") }
            return
        }
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            container.settingsRepository.saveFolder(
                ChatFolder(id = java.util.UUID.randomUUID().toString(), name = trimmed.take(40)),
            )
        }
    }

    fun deleteFolder(id: String) {
        if (!container.isPro()) return
        viewModelScope.launch {
            container.settingsRepository.deleteFolder(id)
            if (_state.value.activeFolderId == id) {
                _state.update { it.copy(activeFolderId = null) }
            }
        }
    }

    fun moveChatToFolder(conversationId: String, folderId: String?) {
        if (!container.isPro()) {
            _state.update { it.copy(error = "Folders are a Pro feature — pay once to unlock") }
            return
        }
        viewModelScope.launch { container.chatRepository.setFolder(conversationId, folderId) }
    }

    fun setPersona(id: String) {
        if (id.isNotBlank() && !container.isPro()) {
            _state.update { it.copy(error = "Personas are a Pro feature — pay once to unlock") }
            return
        }
        viewModelScope.launch {
            container.settingsRepository.update(activePersonaId = id)
        }
    }

    fun setModelKnobs(
        topP: Float? = null,
        presencePenalty: Float? = null,
        frequencyPenalty: Float? = null,
        maxTokens: Int? = null,
        promptCache: Boolean? = null,
    ) {
        viewModelScope.launch {
            container.settingsRepository.update(
                topP = topP,
                presencePenalty = presencePenalty,
                frequencyPenalty = frequencyPenalty,
                maxTokens = maxTokens,
                promptCache = promptCache,
            )
        }
    }

    fun refreshMemories() {
        _state.update { it.copy(memories = container.memoryManager.list()) }
    }

    fun editMemory(oldFact: String, newFact: String) {
        if (!container.isPro()) return
        container.memoryManager.update(oldFact, newFact)
        refreshMemories()
    }

    fun deleteMemory(fact: String) {
        if (!container.isPro()) return
        container.memoryManager.delete(fact)
        refreshMemories()
    }

    fun dismissBackupReminder() {
        viewModelScope.launch {
            container.settingsRepository.update(backupReminderDone = true)
            _state.update { it.copy(showBackupReminder = false) }
        }
    }

    private fun chatOptionsOf(s: AppSettings): ChatOptions = ChatOptions(
        topP = s.topP.takeIf { it != 1f },
        presencePenalty = s.presencePenalty.takeIf { it != 0f },
        frequencyPenalty = s.frequencyPenalty.takeIf { it != 0f },
        maxTokens = s.maxTokens.takeIf { it > 0 },
        promptCache = s.promptCache,
    )

    /** P-012: reply-language setting — FREE, never gated (H-009). */
    fun setLanguage(language: String) {
        viewModelScope.launch {
            container.settingsRepository.update(language = language)
        }
    }

    fun stopStreaming() {
        stopRequested = true
        streamJob?.cancel()
        container.openAiClient.cancel()
        _state.update { it.copy(isStreaming = false, isGeneratingImage = false, retryProgress = null) }
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
        val text = SlashInput.peel(_state.value.input)
        if (text.isEmpty() || _state.value.isStreaming || _state.value.isGeneratingImage) return

        // Imp#2: block send when disconnected
        if (!container.connectivityObserver.isConnected) {
            _state.update { it.copy(waitingForConnection = true, error = "Waiting for connection…") }
            return
        }

        // Settings box is the live key. A named "active" Gemini key must not
        // ride along after they paste Groq and tap Back (B-012).
        val key = container.settingsRepository.getApiKey()
        val settings = _state.value.settings
        val localEndpoint = settings.baseUrl.contains("127.0.0.1") ||
            settings.baseUrl.contains("localhost")
        // /imagine may run keyless when the free-test toggle is on (Pollinations).
        val imagineFreeOk = text.startsWith("/imagine ") && settings.freeTestImages
        if (key.isBlank() && !localEndpoint && !imagineFreeOk) {
            val line = if (text.startsWith("/video ")) {
                "Video needs a provider key — free test covers pictures only. Gemini or Grok can make videos."
            } else {
                "Add an API key in Settings"
            }
            _state.update { it.copy(error = line) }
            return
        }

        // C-013: /browse command — fetch web page, inject into context.
        if (text.startsWith("/browse ")) {
            // Gate-gap close: the ticket said Pro-gated; enforce it here.
            if (!container.isPro()) {
                _state.update { it.copy(error = "Web browsing is a Pro feature — pay once to unlock") }
                return
            }
            val url = BrowseUrl.normalize(text.removePrefix("/browse ").trim())
            if (url.isEmpty()) {
                _state.update { it.copy(error = "Usage: /browse example.com") }
                return
            }
            stopRequested = false
            streamJob = viewModelScope.launch {
                var convId = _state.value.activeConversationId
                if (convId == null) {
                    val c = container.chatRepository.createConversation(
                        title = "/browse ${url.take(40)}",
                        model = settings.model,
                    )
                    convId = c.id
                    selectConversation(convId)
                }
                _state.update { it.copy(input = "", error = null, isStreaming = true) }
                container.chatRepository.addMessage(convId, "user", "/browse $url")
                // P-014: text consumed — no draft left behind.
                viewModelScope.launch { container.settingsRepository.clearDraft(convId) }
                try {
                    // C-028: Jsoup fetch is blocking (15s) — never on Main.
                    val pageText = withContext(Dispatchers.IO) {
                        container.openAiClient.fetchPage(url)
                    }
                    // C-013 fix (REVIEW Part D): the page content must reach the MODEL —
                    // previously it was stored as an "assistant" message and the model
                    // was never called, so /browse produced no answer.
                    container.chatRepository.addMessage(convId, "user",
                        "[Content from $url]\n$pageText")
                    val history = container.chatRepository.listMessages(convId)
                    // P-012: reply language rides the system prompt here too.
                    val browseLanguage = _state.value.settings.language
                    val systemMsg = ChatMessageDto("system",
                        if (browseLanguage.isNotBlank())
                            "Reply in $browseLanguage. Do not fabricate tool outputs, file contents, citations, or completed work."
                        else
                            "Do not fabricate tool outputs, file contents, citations, or completed work.")
                    val dto = listOf(systemMsg) + history.map { ChatMessageDto(it.role, it.content) }
                    val (trimmed, _) = ContextTrimmer.trim(dto)
                    val answer = withContext(Dispatchers.IO) {
                        container.openAiClient.completeChat(
                            baseUrl = settings.baseUrl,
                            apiKey = key,
                            model = settings.model,
                            messages = trimmed,
                            temperature = settings.temperature,
                        )
                    }
                    container.chatRepository.addMessage(convId, "assistant",
                        answer.ifBlank { "No answer from model." })
                } catch (e: java.util.concurrent.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    val line = ApiKeySanitizer.userSafeError(e, "Browse")
                    _state.update { it.copy(error = line) }
                    container.chatRepository.addMessage(convId, "assistant", line)
                } finally {
                    _state.update { it.copy(isStreaming = false) }
                }
            }
            return
        }

        // P-005: /search — DuckDuckGo then the model answers with source URLs.
        if (text.startsWith("/search ")) {
            if (!container.isPro()) {
                _state.update { it.copy(error = "Web search is a Pro feature — pay once to unlock") }
                return
            }
            val query = text.removePrefix("/search ").trim()
            if (query.isEmpty()) {
                _state.update { it.copy(error = "Usage: /search <words>") }
                return
            }
            stopRequested = false
            streamJob = viewModelScope.launch {
                var convId = _state.value.activeConversationId
                if (convId == null) {
                    val c = container.chatRepository.createConversation(
                        title = "/search ${query.take(40)}",
                        model = settings.model,
                    )
                    convId = c.id
                    selectConversation(convId)
                }
                _state.update { it.copy(input = "", error = null, isStreaming = true) }
                container.chatRepository.addMessage(convId, "user", "/search $query")
                container.settingsRepository.clearDraft(convId)
                try {
                    val results = withContext(Dispatchers.IO) {
                        container.openAiClient.fetchSearch(query)
                    }
                    if (stopRequested) return@launch
                    container.chatRepository.addMessage(
                        convId, "user",
                        "[Search results for $query]\n$results\n\nAnswer using these sources. Keep the URLs in the answer.",
                    )
                    val history = container.chatRepository.listMessages(convId)
                    val language = _state.value.settings.language
                    val systemMsg = ChatMessageDto(
                        "system",
                        buildString {
                            if (language.isNotBlank()) append("Reply in $language. ")
                            append("Do not fabricate citations. Keep source URLs.")
                        },
                    )
                    val dto = listOf(systemMsg) + history.map { ChatMessageDto(it.role, it.content) }
                    val (trimmed, _) = ContextTrimmer.trim(dto)
                    val answer = withContext(Dispatchers.IO) {
                        container.openAiClient.completeChat(
                            baseUrl = settings.baseUrl,
                            apiKey = key,
                            model = settings.model,
                            messages = trimmed,
                            temperature = settings.temperature,
                            options = chatOptionsOf(settings),
                        )
                    }
                    if (stopRequested) return@launch
                    container.chatRepository.addMessage(
                        convId, "assistant",
                        answer.ifBlank { "No answer from model." },
                    )
                } catch (e: java.util.concurrent.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    if (stopRequested) return@launch
                    _state.update { it.copy(error = "Search failed: ${e.message?.take(100)}") }
                } finally {
                    _state.update { it.copy(isStreaming = false) }
                }
            }
            return
        }

        // P-006: /recall — show stored memories matching a topic.
        if (text.startsWith("/recall")) {
            if (!container.isPro()) {
                _state.update { it.copy(error = "Memory is a Pro feature — pay once to unlock") }
                return
            }
            val topic = text.removePrefix("/recall").trim()
            viewModelScope.launch {
                var convId = _state.value.activeConversationId
                if (convId == null) {
                    val c = container.chatRepository.createConversation(title = "/recall")
                    convId = c.id
                    selectConversation(convId)
                }
                _state.update { it.copy(input = "", error = null) }
                container.chatRepository.addMessage(convId, "user", text)
                val hits = container.memoryManager.recall(topic)
                val body = if (hits.isEmpty()) "Nothing stored about that."
                else hits.joinToString("\n") { "• ${it.fact}" }
                container.chatRepository.addMessage(convId, "assistant", body)
                refreshMemories()
            }
            return
        }

        // C-027: /video command — generate video via Sora-compatible API.
        if (text.startsWith("/video ")) {
            val prompt = text.removePrefix("/video ").trim()
            if (prompt.isEmpty()) {
                _state.update { it.copy(error = "Usage: /video <prompt>") }
                return
            }
            stopRequested = false
            streamJob = viewModelScope.launch {
                var convId = _state.value.activeConversationId
                if (convId == null) {
                    val c = container.chatRepository.createConversation(
                        title = "/video ${prompt.take(40)}",
                        model = settings.model,
                    )
                    convId = c.id
                    selectConversation(convId)
                }
                _state.update { it.copy(input = "", error = null, isGeneratingImage = true, isStreaming = true) }
                container.chatRepository.addMessage(convId, "user", "/video $prompt")
                // P-014: text consumed — no draft left behind.
                viewModelScope.launch { container.settingsRepository.clearDraft(convId) }
                try {
                    // C-028: create + poll + stream-to-disk all on IO; never on Main.
                    val jobId = withContext(Dispatchers.IO) {
                        container.openAiClient.createVideo(
                            baseUrl = settings.baseUrl,
                            apiKey = key,
                            prompt = prompt,
                        )
                    }
                    val file = java.io.File(container.ctx.filesDir,
                        "vid_${System.currentTimeMillis()}.mp4")
                    withContext(Dispatchers.IO) {
                        container.openAiClient.pollVideo(
                            baseUrl = settings.baseUrl,
                            apiKey = key,
                            jobId = jobId,
                            destFile = file,
                        )
                    }
                    // C-029: run FIFO media cleanup after every successful generation.
                    MediaCleanup.run(container.ctx)
                    container.chatRepository.addMessage(convId, "assistant",
                        "[VIDEO:${file.absolutePath}]")
                } catch (e: java.util.concurrent.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    val line = ApiKeySanitizer.userSafeError(e, "Video")
                    _state.update { it.copy(error = line) }
                    container.chatRepository.addMessage(convId, "assistant", line)
                } finally {
                    _state.update { it.copy(isGeneratingImage = false, isStreaming = false) }
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
            stopRequested = false
            streamJob = viewModelScope.launch {
                var convId = _state.value.activeConversationId
                if (convId == null) {
                    val c = container.chatRepository.createConversation(
                        title = "/imagine ${prompt.take(40)}",
                        model = settings.model,
                    )
                    convId = c.id
                    selectConversation(convId)
                }
                _state.update { it.copy(input = "", error = null, isGeneratingImage = true, isStreaming = true) }
                // Insert user's prompt as a regular message.
                container.chatRepository.addMessage(convId, "user", "/imagine $prompt")
                // P-014: text consumed — no draft left behind.
                viewModelScope.launch { container.settingsRepository.clearDraft(convId) }
                try {
                    val freeTest = settings.freeTestImages
                    val hostModel = ProviderCatalog.resolveImageModel(settings.baseUrl)
                    var freeLabel: String? = null
                    val imageBytes: ByteArray = if (key.isBlank() || hostModel == null) {
                        if (!freeTest) {
                            throw java.io.IOException(
                                if (key.isBlank()) "Add an API key in Settings"
                                else ProviderCatalog.cannotMakePicturesLine(settings.baseUrl)
                            )
                        }
                        freeLabel = "[Free test picture — no key needed]"
                        // C-028: network on IO, never Main.
                        withContext(Dispatchers.IO) {
                            container.openAiClient.pollinationsImage(prompt)
                        }
                    } else {
                        try {
                            // C-028: network + decode + compress all on IO, never Main.
                            withContext(Dispatchers.IO) {
                                container.openAiClient.generateImage(
                                    baseUrl = settings.baseUrl,
                                    apiKey = key,
                                    prompt = prompt,
                                    model = settings.model,
                                )
                            }
                        } catch (e: java.util.concurrent.CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            if (!freeTest) throw e
                            freeLabel = "[Free test picture — the host could not make it]"
                            withContext(Dispatchers.IO) {
                                container.openAiClient.pollinationsImage(prompt)
                            }
                        }
                    }
                    // Save to cache dir, downscaled for weak devices.
                    val maxDim = ImageCacheConfig.maxSaveDimension(
                        DeviceCompat.snapshot(container.ctx).band
                    )
                    val file = java.io.File(
                        container.ctx.filesDir,
                        "gen_${System.currentTimeMillis()}.png"
                    )
                    withContext(Dispatchers.IO) {
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
                        file.outputStream().use { out ->
                            scaled?.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                        }
                        scaled?.recycle()
                        bitmap?.recycle()
                    }
                    // C-029: FIFO cleanup after every successful generation.
                    MediaCleanup.run(container.ctx)
                    val label = freeLabel?.let { "$it\n" } ?: ""
                    container.chatRepository.addMessage(
                        convId, "assistant",
                        "${label}[IMAGE:${file.absolutePath}]"
                    )
                } catch (e: java.util.concurrent.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    val line = ApiKeySanitizer.userSafeError(e, "Image generation")
                    _state.update { it.copy(error = line) }
                    container.chatRepository.addMessage(convId, "assistant", line)
                } finally {
                    _state.update { it.copy(isGeneratingImage = false, isStreaming = false) }
                }
            }
            return
        }

        // P-011: /edit — edit the last generated image. Generation stays free.
        if (text.startsWith("/edit ")) {
            if (!container.isPro()) {
                _state.update { it.copy(error = "Image edit is a Pro feature — pay once to unlock") }
                return
            }
            val prompt = text.removePrefix("/edit ").trim()
            if (prompt.isEmpty()) {
                _state.update { it.copy(error = "Usage: /edit <what to change>") }
                return
            }
            stopRequested = false
            streamJob = viewModelScope.launch {
                var convId = _state.value.activeConversationId
                if (convId == null) {
                    _state.update { it.copy(error = "Make an image first with /imagine") }
                    return@launch
                }
                val lastImage = container.chatRepository.listMessages(convId)
                    .asReversed()
                    .firstOrNull { it.content.startsWith("[IMAGE:") }
                if (lastImage == null) {
                    _state.update { it.copy(error = "No image in this chat to edit") }
                    return@launch
                }
                val path = lastImage.content.removePrefix("[IMAGE:").removeSuffix("]")
                val src = java.io.File(path)
                if (!src.exists()) {
                    _state.update { it.copy(error = "The last image file is gone") }
                    return@launch
                }
                _state.update { it.copy(input = "", error = null, isGeneratingImage = true, isStreaming = true) }
                container.chatRepository.addMessage(convId, "user", "/edit $prompt")
                try {
                    val bytes = withContext(Dispatchers.IO) {
                        val upload = if (src.name.endsWith(".png", true)) src else {
                            val png = java.io.File(container.ctx.cacheDir, "edit_${System.currentTimeMillis()}.png")
                            val bmp = android.graphics.BitmapFactory.decodeFile(src.absolutePath)
                                ?: throw IllegalArgumentException("Could not read the image")
                            png.outputStream().use { out ->
                                bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                            }
                            bmp.recycle()
                            png
                        }
                        container.openAiClient.editImage(
                            baseUrl = settings.baseUrl,
                            apiKey = key,
                            imageFile = upload,
                            prompt = prompt,
                        )
                    }
                    val file = java.io.File(
                        container.ctx.filesDir,
                        "gen_${System.currentTimeMillis()}.png",
                    )
                    withContext(Dispatchers.IO) { file.writeBytes(bytes) }
                    MediaCleanup.run(container.ctx)
                    container.chatRepository.addMessage(
                        convId, "assistant", "[IMAGE:${file.absolutePath}]",
                    )
                } catch (e: java.util.concurrent.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    if (stopRequested) return@launch
                    val line = ApiKeySanitizer.userSafeError(e, "Edit")
                    _state.update { it.copy(error = line) }
                    container.chatRepository.addMessage(convId, "assistant", line)
                } finally {
                    _state.update { it.copy(isGeneratingImage = false, isStreaming = false) }
                }
            }
            return
        }

        stopRequested = false
        streamJob = viewModelScope.launch {
            // C-028: reset stale per-send state so a new send doesn't inherit the
            // previous attempt's truncation count or error banner (REVIEW finding C9).
            _state.update { it.copy(truncatedCount = 0, error = null, lastCost = null) }
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
            // P-014: text consumed — no draft left behind.
            viewModelScope.launch { container.settingsRepository.clearDraft(convId) }

            // C-020: persistent memory (Pro) — explicit "Remember …" lines are
            // stored and promoted into the system prompt immediately (5 hits).
            val isPro = container.isPro()
            if (isPro && text.startsWith("Remember ", ignoreCase = true)) {
                val fact = text.removePrefix("Remember ").trim()
                if (fact.isNotEmpty()) {
                    repeat(5) { container.memoryManager.record(fact) }
                }
            }
            val memoryPrompt = if (isPro) container.memoryManager.getMemoryPrompt() else ""

            val history = container.chatRepository.listMessages(convId)
            // Kai 9000 "honesty rule": measurably reduces model fabrication.
            val honestyRule = "Do not fabricate tool outputs, file contents, citations, or completed work."
            // P-012: reply language (FREE — H-009) rides the system prompt.
            val language = _state.value.settings.language
            val persona = if (isPro) PersonaPacks.byId(settings.activePersonaId) else null
            val systemText = buildString {
                if (memoryPrompt.isNotBlank()) append("$memoryPrompt ")
                if (persona != null) append("${persona.system} ")
                if (language.isNotBlank()) append("Reply in $language. ")
                append(honestyRule)
            }
            val systemMsg = ChatMessageDto("system", systemText)
            val dto = listOf(systemMsg) + history.map { ChatMessageDto(it.role, it.content) }

            // C-010: trim to token budget, track removed count.
            val (trimmed, removed) = ContextTrimmer.trim(dto)
            if (removed > 0) {
                _state.update { it.copy(truncatedCount = removed) }
                if (isPro) {
                    val dropped = dto.filter { it.role != "system" }.take(removed)
                    val blob = dropped.joinToString("\n") { "${it.role}: ${it.content.take(400)}" }
                    viewModelScope.launch {
                        try {
                            val summary = withContext(Dispatchers.IO) {
                                container.openAiClient.completeChat(
                                    baseUrl = settings.baseUrl,
                                    apiKey = key,
                                    model = settings.model,
                                    messages = listOf(
                                        ChatMessageDto(
                                            "system",
                                            "Summarize in 2 short sentences. No extras.",
                                        ),
                                        ChatMessageDto("user", blob.take(4000)),
                                    ),
                                    temperature = 0.3f,
                                )
                            }
                            container.memoryManager.addSummary(summary)
                            refreshMemories()
                        } catch (_: Exception) { /* keep chat going */ }
                    }
                }
            }

            // Imp#3: retry loop with exponential backoff + jitter
            val maxRetries = RetryPolicy.MAX_ATTEMPTS
            var lastError: String? = null

            // C-028: create ONE assistant row up-front and reuse it across retries so
            // failed attempts don't leave duplicate "Retrying…"/"Error:" ghost rows
            // (REVIEW finding C9).
            val assistant = container.chatRepository.addMessage(convId, "assistant", "")
            val assistantId = assistant.id

            for (attempt in 1..maxRetries) {
                if (stopRequested) { stopStreaming(); return@launch }
                if (attempt > 1) {
                    _state.update {
                        it.copy(retryProgress = "Retry ($attempt/$maxRetries)")
                    }
                    delay(RetryPolicy.delayMs(attempt))
                    if (stopRequested) { stopStreaming(); return@launch }
                }

                // Imp#2: check connectivity before each attempt
                if (!container.connectivityObserver.isConnected) {
                    _state.update {
                        it.copy(
                            waitingForConnection = true,
                            error = "Waiting for connection…",
                            retryProgress = null,
                            isStreaming = false,
                        )
                    }
                    return@launch
                }

                if (attempt > 1) {
                    container.chatRepository.updateMessageContent(assistantId, convId, "Retrying…")
                }
                val acc = StringBuilder()

                _state.update { it.copy(isStreaming = true, streamingText = "") }

                // NOTE: no streamJob?.cancel() here — streamJob now points at THIS
                // coroutine (fixed C3), so calling cancel() would abort the send itself.
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
                        options = chatOptionsOf(settings),
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
                } catch (e: java.util.concurrent.CancellationException) {
                    throw e // user hit Stop — do not treat as retryable error
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
                        if (attempt < maxRetries) {
                            if (stopRequested) { stopStreaming(); return@launch }
                            continue // retry
                        }
                        _state.update {
                            it.copy(isStreaming = false, streamingText = "", retryProgress = null)
                        }
                        return@launch
                    }
                    else -> {
                        container.chatRepository.updateMessageContent(
                            assistantId, convId, "(empty response)"
                        )
                        if (attempt < maxRetries) {
                            if (stopRequested) { stopStreaming(); return@launch }
                            continue // retry
                        }
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
                    if (stopRequested) { stopStreaming(); return@launch }
                    try {
                        _state.update { it.copy(retryProgress = "Trying ${provider.baseUrl.take(30)}…") }
                        // C-028: completeChat is blocking — never on Main.
                        val result = withContext(Dispatchers.IO) {
                            container.openAiClient.completeChat(
                                baseUrl = provider.baseUrl,
                                apiKey = provider.apiKey.ifBlank { key },
                                model = provider.model.ifBlank { settings.model },
                                messages = trimmed,
                                temperature = settings.temperature,
                            )
                        }
                        if (result.isNotEmpty()) {
                            container.chatRepository.updateMessageContent(assistantId, convId, result)
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

    // C-014 / P-003: export chat database. Encrypted if passphrase is set.
    fun armBackupPass(pass: String) {
        pendingBackupPass = pass
    }

    fun exportChats(uri: android.net.Uri, passphrase: String = pendingBackupPass) {
        if (!container.isPro()) {
            _state.update { it.copy(error = "Chat backup is a Pro feature — pay once to unlock") }
            return
        }
        if (passphrase.isBlank()) {
            // B-005: empty pass used to copy the live SQLite file. Never do that.
            _state.update { it.copy(error = "Type a backup password first.") }
            return
        }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val dbFile = container.ctx.getDatabasePath("litechat.db")
                    container.database.openHelper.writableDatabase
                        .query("PRAGMA wal_checkpoint(TRUNCATE)")
                        .use { it.moveToFirst() }
                    container.ctx.contentResolver.openOutputStream(uri)?.use { out ->
                        dbFile.inputStream().use { inn ->
                            BackupCrypto.encryptTo(inn, passphrase, out)
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Export failed: ${e.message?.take(60)}") }
            }
        }
    }

    // C-014 / P-003: import. Decrypts if the file starts with BYO1.
    fun importChats(uri: android.net.Uri, passphrase: String = pendingBackupPass) {
        if (!container.isPro()) {
            _state.update { it.copy(error = "Chat restore is a Pro feature — pay once to unlock") }
            return
        }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    container.database.close()
                    val dbFile = container.ctx.getDatabasePath("litechat.db")
                    val incoming = java.io.File.createTempFile("bak", ".bin", container.ctx.cacheDir)
                    try {
                        container.ctx.contentResolver.openInputStream(uri)?.use { inn ->
                            incoming.outputStream().use { inn.copyTo(it) }
                        } ?: throw IllegalArgumentException("Could not read file")
                        val header = ByteArray(4)
                        incoming.inputStream().use { inn ->
                            val n = inn.read(header)
                            if (n < 4) header.fill(0)
                        }
                        if (BackupCrypto.looksEncrypted(header)) {
                            if (passphrase.isBlank()) {
                                throw IllegalArgumentException("This backup needs a password")
                            }
                            incoming.inputStream().use { inn ->
                                dbFile.outputStream().use { out ->
                                    BackupCrypto.decryptTo(inn, passphrase, out)
                                }
                            }
                        } else {
                            incoming.inputStream().use { inn ->
                                dbFile.outputStream().use { inn.copyTo(it) }
                            }
                        }
                    } finally {
                        incoming.delete()
                    }
                    java.io.File("${dbFile.path}-wal").delete()
                    java.io.File("${dbFile.path}-shm").delete()
                }
                _state.update { it.copy(error = "Chats imported — restart to reload") }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Import failed: ${e.message?.take(60)}") }
            }
        }
    }

    // C-016: attach image/file for vision model analysis.
    fun attachImage(uri: android.net.Uri) {
        // Gate-gap close: the ticket said Pro-gated; enforce it here.
        if (!container.isPro()) {
            _state.update { it.copy(error = "Image attachment is a Pro feature — pay once to unlock") }
            return
        }
        viewModelScope.launch {
            try {
                // C-028: decode + downscale on IO (Bitmap decode is expensive).
                val prefix = "[IMG:data:image/jpeg;base64,"
                val budget = InputPolicy.MAX_INPUT_CHARS - prefix.length - " Describe this.".length
                val b64 = withContext(Dispatchers.IO) {
                    val tmp = java.io.File(container.ctx.cacheDir, "attach_${System.currentTimeMillis()}.bin")
                    try {
                        container.ctx.contentResolver.openInputStream(uri)?.use { inn ->
                            tmp.outputStream().use { inn.copyTo(it) }
                        } ?: return@withContext ""
                        val opts = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        android.graphics.BitmapFactory.decodeFile(tmp.absolutePath, opts)
                        val maxDim = ImageCacheConfig.maxSaveDimension(
                            DeviceCompat.snapshot(container.ctx).band
                        )
                        var sample = 1
                        while (opts.outWidth / sample > maxDim || opts.outHeight / sample > maxDim) {
                            sample *= 2
                        }
                        var encoded = ""
                        for (quality in intArrayOf(80, 60, 40, 25)) {
                            val decodeOpts = android.graphics.BitmapFactory.Options().apply { inSampleSize = sample }
                            val bitmap = android.graphics.BitmapFactory.decodeFile(tmp.absolutePath, decodeOpts)
                                ?: continue
                            val out = java.io.ByteArrayOutputStream()
                            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, out)
                            bitmap.recycle()
                            val candidate = android.util.Base64.encodeToString(out.toByteArray(), android.util.Base64.NO_WRAP)
                            if (candidate.length <= budget) {
                                encoded = candidate
                                break
                            }
                            sample *= 2
                        }
                        encoded
                    } finally {
                        tmp.delete()
                    }
                }
                if (b64.isBlank()) {
                    _state.update { it.copy(error = "Attachment too large to send — try a smaller photo") }
                    return@launch
                }
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

    suspend fun getCurrentChatText(): String? {
        val id = _state.value.activeConversationId ?: return null
        // C-028: Room read is suspending — never wrapped in runBlocking on Main.
        return withContext(Dispatchers.IO) { container.chatRepository.exportAsText(id) }
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

    // C-032: one-time acceptable-use acceptance (Play AI-Generated Content policy).
    fun acceptAcceptableUse() {
        viewModelScope.launch {
            container.settingsRepository.update(acceptableUseAccepted = true)
        }
    }

    /** Free /imagine test via Pollinations (no key). Bubbles stay labeled. */
    fun setFreeTestImages(on: Boolean) {
        viewModelScope.launch {
            container.settingsRepository.update(freeTestImages = on)
        }
    }

    // C-020: clear all stored memory facts. B-008: Pro-only.
    fun clearMemory() {
        if (!container.isPro()) {
            _state.update { it.copy(error = "Memory is a Pro feature — pay once to unlock") }
            return
        }
        container.memoryManager.clear()
        refreshMemories()
        _state.update { it.copy(error = "Memory cleared") }
    }

    fun exportTemplates(uri: android.net.Uri) {
        if (!container.isPro()) {
            _state.update { it.copy(error = "Template export is a Pro feature — pay once to unlock") }
            return
        }
        viewModelScope.launch {
            try {
                val json = container.settingsRepository.exportTemplatesJson()
                withContext(Dispatchers.IO) {
                    container.ctx.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Export failed: ${e.message?.take(60)}") }
            }
        }
    }

    fun importTemplates(uri: android.net.Uri) {
        if (!container.isPro()) {
            _state.update { it.copy(error = "Template import is a Pro feature — pay once to unlock") }
            return
        }
        viewModelScope.launch {
            try {
                val json = withContext(Dispatchers.IO) {
                    container.ctx.contentResolver.openInputStream(uri)
                        ?.readBytes()?.toString(Charsets.UTF_8) ?: ""
                }
                val err = container.settingsRepository.importTemplatesJson(json)
                _state.update { it.copy(error = err ?: "Templates imported") }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Import failed: ${e.message?.take(60)}") }
            }
        }
    }

    fun consumeVoiceSlot(): Boolean {
        val s = _state.value.settings
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            .format(java.util.Date())
        val used = if (s.voiceDay == today) s.voiceUsedToday else 0
        if (!VoiceDailyLimit.allowed(container.isPro(), used)) {
            _state.update { it.copy(error = "Free plan: 1 voice use per day. Pay once for more.") }
            return false
        }
        if (!container.isPro()) {
            viewModelScope.launch {
                container.settingsRepository.update(voiceDay = today, voiceUsedToday = used + 1)
            }
        }
        return true
    }

    fun readAloud() {
        val text = _state.value.messages.lastOrNull { it.role == "assistant" }?.content
            ?.takeIf { it.isNotBlank() && !it.startsWith("[IMAGE:") }
        if (text.isNullOrBlank()) {
            _state.update { it.copy(error = "Nothing to read yet") }
            return
        }
        if (!consumeVoiceSlot()) return
        val key = container.settingsRepository.getApiKey()
        val settings = _state.value.settings
        viewModelScope.launch {
            try {
                val dest = java.io.File(container.ctx.cacheDir, "tts_${System.currentTimeMillis()}.mp3")
                val player = withContext(Dispatchers.IO) {
                    container.openAiClient.speakToFile(settings.baseUrl, key, text, dest)
                    android.media.MediaPlayer().apply {
                        setDataSource(dest.absolutePath)
                        setOnCompletionListener {
                            it.release()
                            dest.delete()
                            _state.update { st -> st.copy(isSpeaking = false) }
                        }
                        prepare()
                    }
                }
                ttsPlayer?.release()
                ttsPlayer = player
                _state.update { it.copy(isSpeaking = true) }
                player.start()
            } catch (e: java.util.concurrent.CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isSpeaking = false, error = "Read aloud failed: ${e.message?.take(80)}") }
            }
        }
    }

    fun stopAloud() {
        ttsPlayer?.stop()
        ttsPlayer?.release()
        ttsPlayer = null
        _state.update { it.copy(isSpeaking = false) }
        container.openAiClient.cancel()
    }

    // C-022: export settings (no secrets) via SAF.
    fun exportSettings(uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val json = container.settingsRepository.exportSettingsJson()
                withContext(Dispatchers.IO) {
                    container.ctx.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toByteArray())
                    }
                }
                _state.update { it.copy(error = "Settings exported") }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Export failed: ${e.message?.take(60)}") }
            }
        }
    }

    // C-022: import settings JSON via SAF.
    fun importSettings(uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val json = withContext(Dispatchers.IO) {
                    container.ctx.contentResolver.openInputStream(uri)
                        ?.readBytes()?.toString(Charsets.UTF_8) ?: ""
                }
                val err = container.settingsRepository.importSettingsJson(json)
                _state.update { it.copy(error = err ?: "Settings imported") }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Import failed: ${e.message?.take(60)}") }
            }
        }
    }

    // C-023: named keys per provider (encrypted, Agora pattern).
    fun saveNamedKey(name: String, key: String, setActive: Boolean = false) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || key.isBlank()) {
            _state.update { it.copy(error = "Key name and value are required") }
            return
        }
        container.namedKeyStore.save(NamedKeyStore.NamedKey(trimmed, key, setActive))
        if (setActive) {
            viewModelScope.launch {
                container.settingsRepository.setApiKey(key)
                _state.update { it.copy(apiKeyPresent = key.isNotBlank()) }
            }
        }
        refreshNamedKeys()
    }

    fun deleteNamedKey(name: String) {
        container.namedKeyStore.delete(name)
        refreshNamedKeys()
    }

    fun setActiveNamedKey(name: String) {
        val found = container.namedKeyStore.getAll().find { it.name == name } ?: return
        container.namedKeyStore.save(found.copy(isActive = true))
        viewModelScope.launch {
            container.settingsRepository.setApiKey(found.key)
            _state.update { it.copy(apiKeyPresent = found.key.isNotBlank()) }
        }
        refreshNamedKeys()
    }

    private fun refreshNamedKeys() {
        _state.update { it.copy(namedKeys = container.namedKeyStore.getAll()) }
    }

    // C-024: fork the active conversation at a message.
    fun forkFrom(messageId: String) {
        val convId = _state.value.activeConversationId ?: return
        viewModelScope.launch {
            try {
                val branch = container.chatRepository.forkConversation(
                    conversationId = convId,
                    fromMessageId = messageId,
                    model = _state.value.settings.model,
                )
                selectConversation(branch.id)
                _state.update { it.copy(error = "Forked — you're now in a new branch") }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Fork failed: ${e.message?.take(60)}") }
            }
        }
    }

    override fun onCleared() {
        stopStreaming()
        stopAloud()
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