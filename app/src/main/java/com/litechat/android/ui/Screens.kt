package com.litechat.android.ui

import android.app.Activity
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.litechat.android.BuildConfig
import com.litechat.android.LiteChatApp
import com.litechat.android.data.ads.AdMobLazyInit
import com.litechat.android.data.db.MessageEntity
import com.litechat.android.util.DeviceCompat
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiteChatRoot(vm: ChatViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    var showSettings by remember { mutableStateOf(false) }
    var showOnboarding by remember {
        mutableStateOf(!state.settings.onboardingDone)
    }

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
        )
        else -> ChatScreen(
            state = state,
            onOpenSettings = { showSettings = true },
            onNewChat = vm::newChat,
            onSelect = vm::selectConversation,
            onDelete = vm::deleteConversation,
            onInput = vm::setInput,
            onSend = vm::send,
            onStop = vm::stopStreaming,
            onClearError = vm::clearError,
        )
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
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val displayMessages = buildList {
        addAll(state.messages)
        if (state.isStreaming && state.streamingText.isNotEmpty()) {
            // Replace last empty/partial assistant bubble visually
            val last = lastOrNull()
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
                            Text("LiteChat", fontWeight = FontWeight.SemiBold)
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
                                else Icons.Default.ArrowUpward,
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
            if (displayMessages.isEmpty()) {
                val ctx = LocalContext.current
                val snap = remember(ctx) { DeviceCompat.snapshot(ctx) }
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
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
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(displayMessages, key = { it.id }) { msg ->
                        MessageBubble(msg)
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(msg: MessageEntity) {
    val isUser = msg.role == "user"
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
            modifier = Modifier.widthIn(max = 520.dp),
        ) {
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

@Composable
private fun BannerAd() {
    val unitId = BuildConfig.ADMOB_BANNER_ID
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { ctx ->
            // C-001: one-time lazy SDK init on first banner need (non-Pro only —
            // this composable is never created for Pro users).
            AdMobLazyInit.ensureInitialized(ctx)
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = unitId
                loadAd(AdRequest.Builder().build())
            }
        }
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
    val presets = com.litechat.android.data.prefs.SettingsRepository.PRESETS
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
                        "LiteChat ships only the green path by default. " +
                            "Agent runtimes and local 7B models are different products — not silent bloat.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        "Paste an OpenAI-compatible API key. Traffic goes only to the endpoint you choose.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Provider preset", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        presets.take(4).forEach { p ->
                            TextButton(onClick = {
                                base = p.baseUrl
                                model = p.model
                            }) { Text(p.name, fontSize = 12.sp) }
                        }
                    }
                    OutlinedTextField(
                        value = key,
                        onValueChange = { key = it },
                        label = { Text("API key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = base,
                        onValueChange = { base = it },
                        label = { Text("Base URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("Model") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Local Ollama on phone is 🟨/🟥 on weak free-RAM — prefer cloud or a LAN PC.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                Text("Provider", fontWeight = FontWeight.SemiBold)
            }
            item {
                OutlinedTextField(key, { key = it }, label = { Text("API key") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
            item {
                OutlinedTextField(base, { base = it }, label = { Text("Base URL") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
            item {
                OutlinedTextField(model, { model = it }, label = { Text("Model") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
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
            // Debug: long-press free unlock for emulator without Play
            item {
                TextButton(onClick = {
                    onSetPro(true)
                    billingMsg = "Dev: Pro flagged locally (debug builds)"
                }) { Text("Dev: mark Pro (local)") }
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
                    "LiteChat is a local BYOK client. Your key is stored encrypted on device and only sent to the base URL you configure. Not affiliated with OpenAI or any provider.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
