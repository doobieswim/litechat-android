package com.litechat.android.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import java.io.File
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.litechat.android.BuildConfig
import com.litechat.android.LiteChatApp
import com.litechat.android.R
import com.litechat.android.data.db.MessageEntity
import com.litechat.android.data.prefs.PromptTemplate
import com.litechat.android.util.AgentLabGate
import com.litechat.android.util.DeviceCompat
import com.litechat.android.util.ImageCacheConfig
import com.litechat.android.util.LanDetector

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiteChatRoot(vm: ChatViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    var showSettings by remember { mutableStateOf(false) }
    var showOnboarding by remember {
        mutableStateOf(!state.settings.onboardingDone)
    }
    val context = LocalContext.current
    val activity = context as? Activity
    // C-028: scope for the share handler (getCurrentChatText is suspending now).
    val shareScope = androidx.compose.runtime.rememberCoroutineScope()

    // C-021: voice input launcher.
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) vm.setInput(matches[0])
        }
    }

    // C-016: image/file picker launcher.
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { vm.attachImage(it) } }

    // C-022: SAF export launcher.
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri -> uri?.let { vm.exportChats(it) } }

    // C-022: SAF import launcher.
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { vm.importChats(it) } }

    // C-022: settings JSON export/import (no secrets — keys never leave the device).
    val settingsExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { vm.exportSettings(it) } }
    val settingsImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { vm.importSettings(it) } }

    // C-015: overlay toggle.
    var overlayOn by remember { mutableStateOf(false) }

    LaunchedEffect(state.settings.onboardingDone) {
        if (!state.settings.onboardingDone) showOnboarding = true
    }

    when {
        showOnboarding -> OnboardingScreen(
            initialKey = (LocalContext.current.applicationContext as LiteChatApp)
                .container.settingsRepository.getApiKey(),
            initialBase = state.settings.baseUrl,
            initialModel = state.settings.model,
            onDone = { key, base, model ->
                vm.saveSettings(key, base, model, state.settings.temperature, finishOnboarding = true)
                showOnboarding = false
            }
        )
        showSettings -> SettingsScreen(
            state = state,
            onBack = { showSettings = false },
            onSave = { key, base, model, temp ->
                vm.saveSettings(key, base, model, temp)
            },
            onClearHistory = vm::clearHistory,
            onSetPro = vm::setPro,
            onExport = { exportLauncher.launch("litechat_backup.db") },
            onImport = { importLauncher.launch(arrayOf("*/*")) },
            onExportSettings = { settingsExportLauncher.launch("litechat_settings.json") },
            onImportSettings = { settingsImportLauncher.launch(arrayOf("application/json")) },
            onSaveNamedKey = vm::saveNamedKey,
            onDeleteNamedKey = vm::deleteNamedKey,
            onSetActiveNamedKey = vm::setActiveNamedKey,
            onClearMemory = vm::clearMemory,
            overlayOn = overlayOn,
            onToggleOverlay = { enabled ->
                if (enabled) {
                    // C-015 (REVIEW C5): check SYSTEM_ALERT_WINDOW first to avoid
                    // BadTokenException on addView.
                    if (android.provider.Settings.canDrawOverlays(context)) {
                        overlayOn = true
                        val svc = Intent(context, OverlayService::class.java)
                        context.startForegroundService(svc)
                    } else {
                        overlayOn = false
                        val overlayIntent = Intent(
                            android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            android.net.Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(overlayIntent)
                    }
                } else {
                    overlayOn = false
                    context.stopService(Intent(context, OverlayService::class.java))
                }
            },
        )
        else -> ChatScreen(
            state = state,
            onOpenSettings = { showSettings = true },
            onFork = vm::forkFrom,
            onNewChat = vm::newChat,
            onSelect = vm::selectConversation,
            onDelete = vm::deleteConversation,
            onInput = vm::setInput,
            onSend = vm::send,
            onStop = vm::stopStreaming,
            onClearError = vm::clearError,
            onInsertTemplate = vm::insertTemplate,
                        onAttachImage = { imagePicker.launch("image/*") },
                        onVoiceInput = {
                            val intent = Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                    android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            }
                            voiceLauncher.launch(intent)
                        },
                        onShare = {
                            shareScope.launch {
                                // C-028: getCurrentChatText is now suspending (Room on IO).
                                val text = vm.getCurrentChatText() ?: "No messages yet"
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share chat"))
                            }
                        },
                    )
    }

    // C-032: one-time acceptable-use acceptance (Play AI-Generated Content
    // policy — no BYOK carve-out, shown after onboarding, must be accepted).
    if (state.settings.onboardingDone && !state.settings.acceptableUseAccepted) {
        AcceptableUseDialog(onAccept = vm::acceptAcceptableUse)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    state: ChatUiState,
    onOpenSettings: () -> Unit,
    onNewChat: () -> Unit,
    onSelect: (String) -> Unit,
    onDelete: (String) -> Unit,
    onInput: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onClearError: () -> Unit,
    onInsertTemplate: (PromptTemplate) -> Unit,
    onAttachImage: () -> Unit,
    onVoiceInput: () -> Unit,
    onShare: () -> Unit,
    onFork: (String) -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // C-032/C-024: long-press actions — Report (Play AI-Generated Content
    // policy) and Fork from here.
    var actionMsg by remember { mutableStateOf<MessageEntity?>(null) }
    var showReportReasons by remember { mutableStateOf(false) }

    val displayMessages = buildList {
        addAll(state.messages)
        if (state.isStreaming && state.streamingText.isNotEmpty()) {
            // Replace last empty/partial assistant bubble visually
            val last: MessageEntity? = lastOrNull()
            if (last?.role == "assistant") {
                removeAt(lastIndex)
                add(last.copy(content = state.streamingText.ifEmpty { "…" }))
            }
        }
    }

    LaunchedEffect(displayMessages.size, state.streamingText.length) {
        if (displayMessages.isNotEmpty()) {
            listState.animateScrollToItem(displayMessages.lastIndex)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
            ) {
                Text(
                    "Chats",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                TextButton(onClick = {
                    onNewChat()
                    scope.launch { drawerState.close() }
                }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("New chat")
                }
                HorizontalDivider()
                LazyColumn {
                    items(state.conversations, key = { it.id }) { c ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(c.id)
                                    scope.launch { drawerState.close() }
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                c.title,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                color = if (c.id == state.activeConversationId)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface,
                            )
                            IconButton(onClick = { onDelete(c.id) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(stringResource(R.string.app_name), fontWeight = FontWeight.SemiBold)
                            Text(
                                state.settings.model,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                                            IconButton(onClick = onNewChat) {
                                                Icon(Icons.Default.Add, contentDescription = "New chat")
                                            }
                                            IconButton(onClick = onShare) {
                                                Icon(Icons.Default.Send, contentDescription = "Share")
                                            }
                                            IconButton(onClick = onOpenSettings) {
                                                Icon(Icons.Default.Settings, contentDescription = "Settings")
                                            }
                                        },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                )
            },
            bottomBar = {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    // C-012: template picker row — Pro-gated beyond free limit.
                    if (state.templates.isNotEmpty()) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            state.templates.forEach { tpl ->
                                Surface(
                                    onClick = { onInsertTemplate(tpl) },
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(8.dp),
                                ) {
                                    Text(
                                        tpl.name,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                            }
                            if (!state.settings.isPro) {
                                Text(
                                    "Pro for more",
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    state.error?.let { err ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                err,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp,
                            )
                            IconButton(onClick = onClearError) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss")
                            }
                        }
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        // C-021: mic button for voice input (Android SpeechRecognizer).
                                                IconButton(
                                                    onClick = onVoiceInput,
                                                    modifier = Modifier.size(40.dp),
                                                ) {
                                                    Icon(
                                                        Icons.Default.Send, // Replace with mic icon in resources
                                                        contentDescription = "Voice input",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                }
                                                // C-016: attach image/file button.
                                                IconButton(
                                                    onClick = onAttachImage,
                                                    modifier = Modifier.size(40.dp),
                                                ) {
                                                    Icon(
                                                        Icons.Default.Add,
                                                        contentDescription = "Attach image",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                }
                        OutlinedTextField(
                            value = state.input,
                            onValueChange = onInput,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp, max = 140.dp),
                            placeholder = { Text("Message") },
                            maxLines = 5,
                        )
                        Spacer(Modifier.width(8.dp))
                        FloatingActionButton(
                            onClick = { if (state.isStreaming) onStop() else onSend() },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(52.dp),
                        ) {
                            Icon(
                                if (state.isStreaming) Icons.Default.Close
                                else Icons.Default.Send,
                                contentDescription = if (state.isStreaming) "Stop" else "Send",
                            )
                        }
                    }
                    if (!state.settings.isPro) {
                        BannerAd()
                    }
                }
            }
        ) { padding ->
            Column(Modifier.padding(padding)) {
                // Imp#2: connectivity lost banner
                if (state.waitingForConnection) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            "Waiting for connection…",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
                // Imp#3: retry progress — show attempt count during backoff
                state.retryProgress?.let { label ->
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            label,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            // C-011: image generation progress banner.
                if (state.isGeneratingImage) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                "Generating image…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                        }
                    }
                }
            // C-010: context truncation indicator.
                if (state.truncatedCount > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            "${state.truncatedCount} earlier message(s) not included (token budget)",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            // Cost display: radical transparency.
                state.lastCost?.let { cost ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            cost,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            if (displayMessages.isEmpty()) {
                val ctx = LocalContext.current
                val snap = remember(ctx) { DeviceCompat.snapshot(ctx) }
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "BYOK AI chat",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Medium,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Your key · Your endpoint · Stays on device",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(16.dp))
                        DeviceStatusCard(snap, compact = true)
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(displayMessages, key = { it.id }) { msg ->
                        // C-009: height placeholder for last streaming message
                        val isLastStreaming = state.isStreaming &&
                            msg.id == displayMessages.lastOrNull()?.id &&
                            msg.role == "assistant"
                        MessageBubble(msg, onLongPress = { actionMsg = it })
                        if (isLastStreaming) {
                            val text = msg.content
                            val hasOpenBlock = text.contains("```") &&
                                text.count { it == '`' } % 2 != 0
                            val hasOpenTable = text.lines().any {
                                it.contains("|") && it.trimStart().startsWith("|")
                            }
                            if (hasOpenBlock || hasOpenTable) {
                                Spacer(Modifier.height(if (hasOpenBlock) 32.dp else 0.dp))
                                if (hasOpenTable) Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
            } // close Column(Modifier.padding(padding)) from Imp#2/#3 banners
        }
    }

    // C-032: in-app AI-content reporting (Play requires this for any app that
    // generates AI content — chat, /imagine, /video). Zero server: opens a
    // pre-filled email to the developer.
    actionMsg?.let { msg ->
        val reportContext = LocalContext.current
        if (!showReportReasons) {
            AlertDialog(
                onDismissRequest = { actionMsg = null },
                title = { Text("Message actions") },
                text = {
                    Column {
                        TextButton(onClick = {
                            onFork(msg.id)
                            actionMsg = null
                        }) { Text("Fork from here") }
                        TextButton(onClick = { showReportReasons = true }) {
                            Text("Report content", color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { actionMsg = null }) { Text("Cancel") }
                },
            )
        } else {
            AlertDialog(
                onDismissRequest = {
                    actionMsg = null
                    showReportReasons = false
                },
                title = { Text("Report content") },
                text = {
                    Column {
                        Text(
                            "Why are you reporting this? This opens an email to the developer — nothing is sent automatically.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        listOf(
                            "Illegal content",
                            "Sexual content",
                            "Violence or gore",
                            "Harassment or abuse",
                            "Misinformation",
                            "Other",
                        ).forEach { reason ->
                            TextButton(onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:litechat@proton.me")
                                    putExtra(Intent.EXTRA_SUBJECT, "AI content report — BYO AI")
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Reason: $reason\n\nRole: ${msg.role}\n\nMessage snippet:\n${msg.content.take(500)}"
                                    )
                                }
                                try {
                                    reportContext.startActivity(intent)
                                } catch (_: Exception) {
                                    // No mail app installed — the report stays local.
                                }
                                actionMsg = null
                                showReportReasons = false
                            }) { Text(reason) }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        actionMsg = null
                        showReportReasons = false
                    }) { Text("Cancel") }
                },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(msg: MessageEntity, onLongPress: (MessageEntity) -> Unit) {
    val isUser = msg.role == "user"

    // C-011: render image messages with Coil AsyncImage.
    if (!isUser && msg.content.startsWith("[IMAGE:")) {
        val path = msg.content.removePrefix("[IMAGE:").removeSuffix("]")
        val file = File(path)
        if (file.exists()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .widthIn(max = 520.dp)
                        .combinedClickable(onClick = {}, onLongClick = { onLongPress(msg) }),
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(file)
                            .crossfade(true)
                            // C-030: band-tuned decode size — never decode larger than the
                            // device's free-RAM band needs (was a hardcoded 540x540).
                            .size(ImageCacheConfig.displaySize(
                                DeviceCompat.snapshot(LocalContext.current).band
                            ))
                            .build(),
                        contentDescription = "Generated image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.FillWidth,
                    )
                }
            }
            return
        }
        // File not found — fall through to text rendering.
    }

    // C-027: render video messages with built-in VideoView.
    if (!isUser && msg.content.startsWith("[VIDEO:")) {
        val path = msg.content.removePrefix("[VIDEO:").removeSuffix("]")
        val file = File(path)
        if (file.exists()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .widthIn(max = 520.dp)
                        .combinedClickable(onClick = {}, onLongClick = { onLongPress(msg) })) {
                    AndroidView(
                        factory = { android.widget.VideoView(it).apply {
                            setVideoPath(path)
                            setOnPreparedListener { start() }
                        }},
                        modifier = Modifier.fillMaxWidth().height(300.dp).padding(8.dp)
                    )
                }
            }
            return
        }
    }

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            color = if (isUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp,
            ),
            modifier = Modifier
                .widthIn(max = 520.dp)
                .combinedClickable(onClick = {}, onLongClick = { onLongPress(msg) }),
        ) {
            // C-008 (deferred): assistant messages render as plain text for v1.
            // Markdown deferred — see docs/MARKDOWN-COST.md.
            if (!isUser) {
                SelectionContainer {
                    Text(
                        text = msg.content.ifEmpty { "…" },
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = if (msg.content.contains("```")) FontFamily.Monospace
                        else FontFamily.Default,
                        lineHeight = 22.sp,
                    )
                }
            } else {
                SelectionContainer {
                    Text(
                        text = msg.content.ifEmpty { "…" },
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = if (msg.content.contains("```")) FontFamily.Monospace
                        else FontFamily.Default,
                        lineHeight = 22.sp,
                    )
                }
            }
        }
    }
}

/**
 * BannerAd is flavor-specific (C-002): the play build shows an AdMob banner,
 * the foss build renders nothing. Defined in each flavor's ui source set.
 */

@Composable
private fun AcceptableUseDialog(onAccept: () -> Unit) {
    AlertDialog(
        // C-032: no dismiss path — the user must accept once before using chat.
        onDismissRequest = { },
        title = { Text("Before you start") },
        text = {
            Column {
                Text("BYO AI connects to AI services with your own key. Please read this once.")
                Spacer(Modifier.height(8.dp))
                Text(
                    "You must not use BYO AI to create or share: child sexual abuse material; sexual content involving minors; non-consensual sexual content; graphic violence or gore meant to shock; or content that deceives or impersonates real people for harm.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "The AI provider you connect to applies its own safety filters too. BYO AI does not host or moderate your conversations.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onAccept) { Text("I understand") }
        },
    )
}

@Composable
fun OnboardingScreen(
    initialKey: String,
    initialBase: String,
    initialModel: String,
    onDone: (key: String, base: String, model: String) -> Unit,
) {
    var step by remember { mutableIntStateOf(0) }
    var key by remember { mutableStateOf(initialKey) }
    var base by remember { mutableStateOf(initialBase) }
    var model by remember { mutableStateOf(initialModel) }
    val ctx = LocalContext.current
    val snap = remember(ctx) { DeviceCompat.snapshot(ctx) }
    val scroll = rememberScrollState()

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Text(
                if (step == 0) "Will this phone run it?" else "Connect your key",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (step == 0) "Step 1 of 2 · Honest fit check (ReOldAi-style matrix)"
                else "Step 2 of 2 · BYOK — nothing is sent to us",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))

            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(scroll),
            ) {
                if (step == 0) {
                    DeviceStatusCard(snap)
                    Spacer(Modifier.height(16.dp))
                    CompatMatrixTable(highlight = snap.band)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "This app only turns on the green path by default. " +
                            "Agent runtimes and local 7B models are different products — not silent bloat.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        "Pick a name from the list. Paste your key. That is all.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(16.dp))
                    ProviderSetupFields(
                        key = key,
                        onKeyChange = { key = it },
                        base = base,
                        onBaseChange = { base = it },
                        model = model,
                        onModelChange = { model = it },
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            if (step == 0) {
                Button(
                    onClick = { step = 1 },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Continue") }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { step = 0 }) { Text("Back") }
                    Button(
                        onClick = { onDone(key, base, model) },
                        modifier = Modifier.weight(1f),
                        enabled = base.isNotBlank() && model.isNotBlank(),
                    ) { Text("Start chatting") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: ChatUiState,
    onBack: () -> Unit,
    onSave: (key: String, base: String, model: String, temp: Float) -> Unit,
    onClearHistory: () -> Unit,
    onSetPro: (Boolean) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onExportSettings: () -> Unit,
    onImportSettings: () -> Unit,
    onSaveNamedKey: (String, String) -> Unit,
    onDeleteNamedKey: (String) -> Unit,
    onSetActiveNamedKey: (String) -> Unit,
    onClearMemory: () -> Unit,
    overlayOn: Boolean,
    onToggleOverlay: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val app = context.applicationContext as LiteChatApp
    val activity = context as? Activity
    var key by remember {
        mutableStateOf(app.container.settingsRepository.getApiKey())
    }
    var base by remember { mutableStateOf(state.settings.baseUrl) }
    var model by remember { mutableStateOf(state.settings.model) }
    var temp by remember { mutableStateOf(state.settings.temperature.toString()) }
    var confirmClear by remember { mutableStateOf(false) }
    var billingMsg by remember { mutableStateOf<String?>(null) }
    var showMatrix by remember { mutableStateOf(false) }
    var showAgentLab by remember { mutableStateOf(false) }
    // C-005: /models picker state
    var fetchedModels by remember { mutableStateOf<List<String>?>(null) }
    var modelsMsg by remember { mutableStateOf<String?>(null) }
    var modelsLoading by remember { mutableStateOf(false) }
    var showModelsMenu by remember { mutableStateOf(false) }
    // C-023: add-key form state (declared here — composable calls can't live
    // directly inside the LazyColumn scope).
    var newKeyName by remember { mutableStateOf("") }
    var newKeyValue by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snap = remember(context) { DeviceCompat.snapshot(context) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = {
                        onSave(key, base, model, temp.toFloatOrNull() ?: 0.7f)
                        onBack()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Back")
                    }
                },
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                DeviceStatusCard(snap, compact = true)
            }
            item {
                TextButton(onClick = { showMatrix = !showMatrix }) {
                    Text(if (showMatrix) "Hide compatibility matrix" else "Show compatibility matrix")
                }
            }
            if (showMatrix) {
                item {
                    CompatMatrixTable(highlight = snap.band)
                }
            }
            item {
                TextButton(onClick = { showAgentLab = !showAgentLab }) {
                    Text(if (showAgentLab) "Hide agent box" else "Agent box (not in this app)")
                }
            }
            if (showAgentLab) {
                item {
                    AgentLabCard(
                        snap = snap,
                        freeStorageMb = AgentLabGate.freeStorageMb(context),
                        termuxInstalled = AgentLabGate.isTermuxInstalled(context),
                        onOpenTermux = {
                            context.packageManager
                                .getLaunchIntentForPackage(AgentLabGate.TERMUX_PACKAGE)
                                ?.let { context.startActivity(it) }
                        },
                    )
                }
            }
            item {
                Text("Provider", fontWeight = FontWeight.SemiBold)
            }
            item {
                ProviderSetupFields(
                    key = key,
                    onKeyChange = { key = it },
                    base = base,
                    onBaseChange = { base = it },
                    model = model,
                    onModelChange = { model = it },
                )
            }
            // C-005: optional GET /models picker — failures show a short message, never crash.
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                TextButton(
                                                                    onClick = {
                                                                        modelsLoading = true
                                                                        modelsMsg = null
                                                                        scope.launch {
                                                                            val lanBase = LanDetector.scan()
                                                                            if (lanBase != null) {
                                                                                base = lanBase
                                                                                modelsMsg = "LAN Ollama found"
                                                                            } else {
                                                                                val ids = app.container.openAiClient.listModels(base, key)
                                                                                fetchedModels = ids
                                                                                modelsMsg = when {
                                                                                    ids.isEmpty() -> "No models returned (check base URL)"
                                                                                    else -> "${ids.size} models found"
                                                                                }
                                                                            }
                                                                            modelsLoading = false
                                                                        }
                                                                    },
                                                                    enabled = !modelsLoading,
                                                                ) {
                                                                    Text(if (modelsLoading) "Fetching…" else "Fetch models")
                                                                }
                                // C-019: Test Connection button — quick validation before saving.
                                var testMsg by remember { mutableStateOf<String?>(null) }
                                var testing by remember { mutableStateOf(false) }
                                TextButton(
                                    onClick = {
                                        testing = true
                                        testMsg = null
                                        scope.launch {
                                            try {
                                                val ids = app.container.openAiClient.listModels(base, key)
                                                testMsg = if (ids.isNotEmpty()) "Connected ✓" else "No models"
                                            } catch (e: Exception) {
                                                testMsg = "Failed: ${e.message?.take(40)}"
                                            }
                                            testing = false
                                        }
                                    },
                                    enabled = !testing,
                                ) {
                                    Text(if (testing) "Testing…" else "Test")
                                }
                                testMsg?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                                modelsMsg?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                            }
                        }
            fetchedModels?.let { ids ->
                if (ids.isNotEmpty()) {
                    item {
                        ExposedDropdownMenuBox(
                            expanded = showModelsMenu,
                            onExpandedChange = { showModelsMenu = it },
                        ) {
                            OutlinedTextField(
                                value = model,
                                onValueChange = { model = it },
                                label = { Text("Pick a model") },
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showModelsMenu)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                            )
                            ExposedDropdownMenu(
                                expanded = showModelsMenu,
                                onDismissRequest = { showModelsMenu = false },
                            ) {
                                ids.forEach { id ->
                                    DropdownMenuItem(
                                        text = { Text(id, maxLines = 1) },
                                        onClick = {
                                            model = id
                                            showModelsMenu = false
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                OutlinedTextField(temp, { temp = it }, label = { Text("Temperature") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
            item {
                Button(
                    onClick = {
                        onSave(key, base, model, temp.toFloatOrNull() ?: 0.7f)
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Save") }
            }
            // C-023: named keys per provider (encrypted, Agora pattern).
            item {
                Text("Saved keys", fontWeight = FontWeight.SemiBold)
            }
            if (state.namedKeys.isEmpty()) {
                item {
                    Text(
                        "No named keys yet. Add one to keep multiple API keys.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            state.namedKeys.forEach { named ->
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            named.name,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            if (named.isActive) "active" else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        TextButton(onClick = { onSetActiveNamedKey(named.name) }) {
                            Text("Use")
                        }
                        TextButton(onClick = { onDeleteNamedKey(named.name) }) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            item {
                OutlinedTextField(
                    newKeyName, { newKeyName = it },
                    label = { Text("Key name (e.g. Work OpenAI)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                )
            }
            item {
                OutlinedTextField(
                    newKeyValue, { newKeyValue = it },
                    label = { Text("API key") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                )
            }
            item {
                TextButton(onClick = {
                    onSaveNamedKey(newKeyName, newKeyValue)
                    newKeyName = ""
                    newKeyValue = ""
                }) { Text("Add key") }
            }
            item { HorizontalDivider() }
            item {
                Text("Pro", fontWeight = FontWeight.SemiBold)
                Text(
                    if (state.settings.isPro) "Pro active — ads removed"
                    else "One-time purchase removes ads",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (!state.settings.isPro) {
                item {
                    Button(
                        onClick = {
                            if (activity == null) {
                                billingMsg = "Billing needs an Activity"
                                return@Button
                            }
                            app.container.billingRepository.launchPurchase(activity) { ok, err ->
                                if (ok) {
                                    onSetPro(true)
                                    billingMsg = "Pro unlocked"
                                } else {
                                    billingMsg = err ?: "Purchase failed"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Upgrade to Pro") }
                }
            }
            item {
                TextButton(onClick = {
                    app.container.billingRepository.queryOwned()
                    if (app.container.billingRepository.proOwned.value) {
                        onSetPro(true)
                        billingMsg = "Purchases restored"
                    } else {
                        billingMsg = "No Pro purchase found"
                    }
                }) { Text("Restore purchases") }
            }
            // Debug-only foot-gun guard (C-003): the local Pro flag must never
            // appear in release binaries.
            if (BuildConfig.DEBUG) {
                item {
                    TextButton(onClick = {
                        onSetPro(true)
                        billingMsg = "Dev: Pro flagged locally (debug builds)"
                    }) { Text("Dev: mark Pro (local)") }
                }
            }
            billingMsg?.let {
                item { Text(it, color = MaterialTheme.colorScheme.primary) }
            }
            item { HorizontalDivider() }
            item {
                TextButton(onClick = { confirmClear = true }) {
                    Text("Clear all chats", color = MaterialTheme.colorScheme.error)
                }
            }
            item {
                Text(
                    "BYO AI is an unofficial, open-source client for OpenAI-compatible APIs (OpenAI, OpenRouter, Groq, Ollama, and others). It is not affiliated with, endorsed by, or connected to OpenAI, Google, Anthropic, or any AI provider. You bring your own API key — BYO AI does not provide, proxy, or resell API access. All chat data travels directly between your device and the API server you configure.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                val privacyUrl = "https://flamingspade1995-coder.github.io/litechat-android/privacy.html"
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyUrl))
                    context.startActivity(intent)
                }) {
                    Text("Privacy Policy")
                }
            }
            // C-022: Settings export/import as JSON.
                        item { HorizontalDivider() }
                        item {
                            Text("Data", fontWeight = FontWeight.SemiBold)
                        }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = onExport) {
                                    Text("Backup chats")
                                }
                                TextButton(onClick = onImport) {
                                    Text("Restore chats")
                                }
                            }
                        }
                        // C-022: settings JSON export/import (never contains keys).
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = onExportSettings) {
                                    Text("Export settings")
                                }
                                TextButton(onClick = onImportSettings) {
                                    Text("Import settings")
                                }
                            }
                        }
                        // C-020: persistent memory (Pro) — clear stored facts.
                        item {
                            TextButton(onClick = onClearMemory) {
                                Text("Clear memory", color = MaterialTheme.colorScheme.error)
                            }
                        }
                        // C-015: floating overlay toggle.
                        item { HorizontalDivider() }
                        item {
                            Text("Floating overlay (Pro)", fontWeight = FontWeight.SemiBold)
                        }
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.material3.Switch(
                                    checked = overlayOn,
                                    onCheckedChange = { enabled ->
                                        // C-015 gate: overlay is Pro — refuse to
                                        // enable it for free users (was ungated).
                                        if (enabled && !state.settings.isPro) {
                                            billingMsg = "Floating overlay is a Pro feature — pay once to unlock"
                                        } else {
                                            onToggleOverlay(enabled)
                                        }
                                    },
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (overlayOn) "Overlay active" else "Chat from any app",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear all chats?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    onClearHistory()
                    confirmClear = false
                }) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Cancel") }
            },
        )
    }
}
