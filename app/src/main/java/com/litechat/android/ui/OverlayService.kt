package com.litechat.android.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.litechat.android.LiteChatApp
import com.litechat.android.data.api.ChatMessageDto
import com.litechat.android.data.context.ContextTrimmer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * C-015: Floating chat overlay via SYSTEM_ALERT_WINDOW.
 * A minimal chat input that floats over other apps. Pro-gated — the Settings
 * toggle refuses to enable it for free users (gate enforced in Screens.kt).
 *
 * C-032: ads NEVER run here. This service contains no ad code and the toggle
 * is gated, so no banner can ever appear over other apps (Play hygiene).
 */
class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null
    private var overlayLifecycle: OverlayComposeLifecycle? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startForeground(1, NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Chat overlay")
            .setContentText("Tap to chat with AI from any app")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            // C-015 (REVIEW C5): the PendingIntent must open MainActivity, not
            // point at the Service class.
            .setContentIntent(PendingIntent.getActivity(
                this, 0,
                Intent(this, com.litechat.android.MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                },
                PendingIntent.FLAG_IMMUTABLE
            ))
            .build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // C-015 fix (was broken): the service started but showOverlay() was
        // never called, so the floating window never appeared.
        showOverlay()
        // NOT_STICKY: if the system kills us, don't resurrect an overlay the
        // user may not want — they can flip the toggle again.
        return START_NOT_STICKY
    }

    fun showOverlay() {
        if (overlayView != null) return
        val container = (applicationContext as LiteChatApp).container

        overlayView = ComposeView(this).apply {
            val life = OverlayComposeLifecycle()
            overlayLifecycle = life
            life.attach(this)
            setContent {
                MaterialTheme {
                    var input by remember { mutableStateOf("") }
                    var reply by remember { mutableStateOf<String?>(null) }
                    var busy by remember { mutableStateOf(false) }
                    val scope = rememberCoroutineScope()
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                    ) {
                        Column(Modifier.padding(8.dp)) {
                            OutlinedTextField(
                                value = input,
                                onValueChange = { input = InputPolicy.cap(it) },
                                modifier = Modifier.fillMaxWidth().heightIn(max = 100.dp),
                                placeholder = { Text("Ask AI…") },
                                maxLines = 3,
                            )
                            reply?.let { text ->
                                Text(
                                    text,
                                    modifier = Modifier.padding(top = 6.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 8,
                                )
                            }
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                IconButton(
                                    onClick = {
                                        val text = input.trim()
                                        if (text.isEmpty() || busy) return@IconButton
                                        busy = true
                                        reply = null
                                        scope.launch {
                                            reply = try {
                                                if (!container.isPro()) {
                                                    "Floating overlay is a Pro feature — upgrade in Settings."
                                                } else {
                                                    val key = container.namedKeyStore.getActiveKey()
                                                        .ifBlank { container.settingsRepository.getApiKey() }
                                                    if (key.isBlank()) {
                                                        "Add an API key in Settings first."
                                                    } else {
                                                        val settings =
                                                            container.settingsRepository.settings.first()
                                                        // Reuse one "Overlay" conversation so overlay
                                                        // chats persist in the main chat list.
                                                        val convs = container.chatRepository
                                                            .observeConversations().first()
                                                        var conv = convs.firstOrNull { it.title == "Overlay" }
                                                        if (conv == null) {
                                                            conv = container.chatRepository
                                                                .createConversation(
                                                                    title = "Overlay",
                                                                    model = settings.model,
                                                                )
                                                        }
                                                        container.chatRepository
                                                            .addMessage(conv.id, "user", text)
                                                        val history = container.chatRepository
                                                            .listMessages(conv.id)
                                                        val systemMsg = ChatMessageDto(
                                                            "system",
                                                            "Do not fabricate tool outputs, file contents, citations, or completed work."
                                                        )
                                                        val (trimmed, _) = ContextTrimmer.trim(
                                                            listOf(systemMsg) +
                                                                history.map { ChatMessageDto(it.role, it.content) }
                                                        )
                                                        val answer = withContext(Dispatchers.IO) {
                                                            container.openAiClient.completeChat(
                                                                baseUrl = settings.baseUrl,
                                                                apiKey = key,
                                                                model = settings.model,
                                                                messages = trimmed,
                                                                temperature = settings.temperature,
                                                            )
                                                        }
                                                        container.chatRepository.addMessage(
                                                            conv.id, "assistant",
                                                            answer.ifBlank { "No answer from model." }
                                                        )
                                                        input = ""
                                                        answer.ifBlank { "No answer from model." }
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                "Failed: ${e.message?.take(120)}"
                                            }
                                            busy = false
                                        }
                                    },
                                    enabled = !busy,
                                ) {
                                    Icon(Icons.Filled.Send, contentDescription = "Send")
                                }
                            }
                        }
                    }
                }
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            // FLAG_NOT_FOCUSABLE alone blocks the IME — combine with
            // FLAG_ALT_FOCUSABLE_IM so the text field can open the keyboard
            // without the overlay stealing focus from the app underneath.
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.BOTTOM }

        windowManager.addView(overlayView, params)
    }

    fun hideOverlay() {
        overlayLifecycle?.destroy()
        overlayLifecycle = null
        overlayView?.let { windowManager.removeView(it) }
        overlayView = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() {
        hideOverlay()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "litechat_overlay"
    }

    private fun createNotificationChannel() {
        // C-015 (REVIEW C5): the channel was an empty stub — without a real
        // channel the foreground notification won't show on API 26+.
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Chat overlay",
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)
    }
}

private class OverlayComposeLifecycle : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    init {
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    fun attach(view: android.view.View) {
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        ViewTreeLifecycleOwner.set(view, this)
        ViewTreeViewModelStoreOwner.set(view, this)
        view.setViewTreeSavedStateRegistryOwner(this)
    }

    fun destroy() {
        if (lifecycleRegistry.currentState != Lifecycle.State.INITIALIZED) {
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        }
        store.clear()
    }
}
