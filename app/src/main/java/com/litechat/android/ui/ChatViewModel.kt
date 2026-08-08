package com.litechat.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.litechat.android.data.AppContainer
import com.litechat.android.data.api.ChatMessageDto
import com.litechat.android.data.api.StreamEvent
import com.litechat.android.data.db.ConversationEntity
import com.litechat.android.data.db.MessageEntity
import com.litechat.android.data.prefs.AppSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
)

class ChatViewModel(
    private val container: AppContainer,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private var streamJob: Job? = null
    private var messagesCollectJob: Job? = null

    init {
        viewModelScope.launch {
            combine(
                container.settingsRepository.settings,
                container.chatRepository.observeConversations(),
                container.billingRepository.proOwned,
            ) { settings, convos, billingPro ->
                Triple(settings, convos, billingPro || settings.isPro)
            }.collect { (settings, convos, isPro) ->
                if (isPro && !settings.isPro) {
                    container.settingsRepository.update(isPro = true)
                }
                val merged = settings.copy(isPro = isPro)
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
        container.billingRepository.startConnection()
    }

    fun setInput(value: String) {
        // Cap paste size: Compose Constraints overflow / OOM on huge single-line measure
        // (see gpt_mobile#226: "Can't represent a width of … height of 369898 in Constraints")
        val capped = if (value.length > MAX_INPUT_CHARS) value.take(MAX_INPUT_CHARS) else value
        _state.update { it.copy(input = capped) }
    }

    companion object {
        /** Soft ceiling keeps layout measure and binder IPC safe on 4GB devices. */
        const val MAX_INPUT_CHARS = 32_000
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun selectConversation(id: String) {
        messagesCollectJob?.cancel()
        _state.update {
            it.copy(
                activeConversationId = id,
                messages = emptyList(),
                streamingText = "",
                error = null,
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
            val c = container.chatRepository.createConversation()
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
        _state.update { it.copy(isStreaming = false) }
    }

    fun send() {
        val text = _state.value.input.trim()
        if (text.isEmpty() || _state.value.isStreaming) return

        val key = container.settingsRepository.getApiKey()
        val settings = _state.value.settings
        val localEndpoint = settings.baseUrl.contains("127.0.0.1") ||
            settings.baseUrl.contains("localhost")
        if (key.isBlank() && !localEndpoint) {
            _state.update { it.copy(error = "Add an API key in Settings") }
            return
        }

        viewModelScope.launch {
            var convId = _state.value.activeConversationId
            if (convId == null) {
                val c = container.chatRepository.createConversation(
                    title = text.take(48).ifBlank { "New chat" }
                )
                convId = c.id
                selectConversation(convId)
            } else if (_state.value.messages.isEmpty()) {
                container.chatRepository.renameConversation(convId, text.take(48))
            }

            _state.update { it.copy(input = "", error = null) }
            container.chatRepository.addMessage(convId, "user", text)

            val history = container.chatRepository.listMessages(convId)
            val dto = history.map { ChatMessageDto(it.role, it.content) }

            val assistant = container.chatRepository.addMessage(convId, "assistant", "")
            val assistantId = assistant.id
            val acc = StringBuilder()

            _state.update { it.copy(isStreaming = true, streamingText = "") }

            streamJob?.cancel()
            streamJob = viewModelScope.launch {
                try {
                    container.openAiClient.streamChat(
                        baseUrl = settings.baseUrl,
                        apiKey = key,
                        model = settings.model,
                        messages = dto,
                        temperature = settings.temperature,
                    ).collect { event ->
                        when (event) {
                            is StreamEvent.Delta -> {
                                acc.append(event.text)
                                _state.update { it.copy(streamingText = acc.toString()) }
                            }
                            is StreamEvent.Error -> {
                                _state.update { it.copy(error = event.message) }
                            }
                            StreamEvent.Done -> Unit
                        }
                    }
                } finally {
                    val finalText = acc.toString()
                    when {
                        finalText.isNotEmpty() ->
                            container.chatRepository.updateMessageContent(
                                assistantId, convId, finalText
                            )
                        _state.value.error != null ->
                            container.chatRepository.updateMessageContent(
                                assistantId, convId, "Error: ${_state.value.error}"
                            )
                        else ->
                            container.chatRepository.updateMessageContent(
                                assistantId, convId, "(empty response)"
                            )
                    }
                    _state.update {
                        it.copy(isStreaming = false, streamingText = "")
                    }
                }
            }
        }
    }

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

    fun setPro(isPro: Boolean) {
        viewModelScope.launch {
            container.settingsRepository.update(isPro = isPro)
        }
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
        super.onCleared()
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(container) as T
        }
    }
}
