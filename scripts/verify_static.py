#!/usr/bin/env python3
"""Static verification for LiteChat when Android SDK is unavailable."""
from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "app" / "src" / "main"
KT_ROOT = APP / "java"


def main() -> int:
    checks: list[tuple[str, bool, str]] = []

    def ok(name: str, cond: bool, detail: str = "") -> None:
        checks.append((name, cond, detail))

    must_exist = [
        "java/com/litechat/android/data/api/OpenAiCompatibleClient.kt",
        "java/com/litechat/android/util/DeviceCompat.kt",
        "java/com/litechat/android/util/AgentLabGate.kt",
        "java/com/litechat/android/ui/CompatMatrix.kt",
        "java/com/litechat/android/ui/Screens.kt",
        "java/com/litechat/android/ui/ChatViewModel.kt",
        "AndroidManifest.xml",
    ]
    for rel in must_exist:
        p = APP / rel
        ok(f"exists {rel}", p.is_file())

    cli = (KT_ROOT / "com/litechat/android/data/api/OpenAiCompatibleClient.kt").read_text()
    screens = (KT_ROOT / "com/litechat/android/ui/Screens.kt").read_text()
    manifest = (APP / "AndroidManifest.xml").read_text()
    vm = (KT_ROOT / "com/litechat/android/ui/ChatViewModel.kt").read_text()
    settings_repo = (KT_ROOT / "com/litechat/android/data/prefs/SettingsRepository.kt").read_text()

    ok("stream fallback API", "allowNonStreamFallback" in cli and "completeChat" in cli)
    ok("stream failure heuristic", "isStreamClassFailure" in cli)
    ok("awaitClose", "awaitClose" in cli)
    ok("compat matrix onboarding", "CompatMatrixTable" in screens and "DeviceCompat" in screens)
    ok("input cap", "MAX_INPUT_CHARS" in vm)
    ok("largeHeap false", 'largeHeap="false"' in manifest)

    # C-001 regression guards: ads init must be lazy (never at cold start).
    # C-002: ads code lives in the play flavor source set only.
    app_kt = (KT_ROOT / "com/litechat/android/LiteChatApp.kt").read_text()
    play_kt_root = ROOT / "app" / "src" / "play" / "java"
    play_manifest = (ROOT / "app" / "src" / "play" / "AndroidManifest.xml").read_text()
    ad_init = (play_kt_root / "com/litechat/android/data/ads/AdMobLazyInit.kt").read_text()
    ok("lazy ads init (no cold-start MobileAds)", "MobileAds.initialize" not in app_kt)
    ok("lazy ads init (single explicit path)", "MobileAds.initialize" in ad_init and "AtomicBoolean" in ad_init)
    ok("ads auto-init provider stripped", "MobileAdsInitProvider" in play_manifest and 'tools:node="remove"' in play_manifest)

    # C-002 flavor split guards.
    ok("play flavor exists", (ROOT / "app" / "src" / "play").is_dir())
    ok("foss flavor exists", (ROOT / "app" / "src" / "foss").is_dir())
    ok("foss has no GMS code", "com.google.android.gms" not in
       "\n".join(p.read_text() for p in (ROOT / "app" / "src" / "foss").rglob("*.kt")))
    build_kts = (ROOT / "app" / "build.gradle.kts").read_text()
    ok("play/foss productFlavors", "create(\"play\")" in build_kts and "create(\"foss\")" in build_kts)
    ok("GMS deps are play-only", "playImplementation" in build_kts and "implementation(\"com.google.android.gms" not in build_kts)

    # C-006 paint throttle guards.
    flags_kt = (KT_ROOT / "com/litechat/android/core/flags/FeatureFlags.kt").read_text()
    ok("throttle constant", "streamThrottleMs" in flags_kt and "250L" in flags_kt)
    ok("throttle gate in send()", "lastUiUpdate" in vm and "streamThrottleMs" in vm)
    ok("Done flushes final paint", "StreamEvent.Done" in vm and "acc.toString()" in vm
        and "streamingText = acc.toString()" in vm)
    ok("throttle test exists", (ROOT / "app" / "src" / "test" / "java" / "com" / "litechat" /
        "android" / "ui" / "PaintThrottleTest.kt").is_file())
    ok("error path unthrottled", "is StreamEvent.Error ->" in vm and "lastUiUpdate" not in
        vm[vm.find("is StreamEvent.Error"):vm.find("is StreamEvent.Error")+200])

    # C-011 image generation guards.
    ok("Coil 3 dependency", "io.coil-kt.coil3:coil-compose" in build_kts
        and "io.coil-kt.coil3:coil-network-okhttp" in build_kts)
    ok("/imagine handler in ViewModel", "/imagine " in vm and "generateImage" in vm)
    ok("generateImage in client", "fun generateImage" in cli
        and "v1/images/generations" in cli
        and "b64_json" in cli)
    ok("image bubble in Screens", "[IMAGE:" in screens and "AsyncImage" in screens
        and "coil3.compose.AsyncImage" in screens)
    ok("image gen loading state", "isGeneratingImage" in vm
        and "Generating image" in screens)

    # C-012 prompt template guards.
    ok("PromptTemplate data class", "data class PromptTemplate" in
        (KT_ROOT / "com/litechat/android/data/prefs/SettingsRepository.kt").read_text())
    ok("template CRUD in repo", "saveTemplate" in settings_repo
        and "deleteTemplate" in settings_repo
        and "BUILT_IN_TEMPLATES" in settings_repo)
    ok("templates in ChatUiState", "templates: List<PromptTemplate>" in vm)
    ok("insertTemplate in ViewModel", "fun insertTemplate" in vm
        and "template.render()" in vm)
    ok("template picker in chat", "onInsertTemplate" in screens
        and "Pro for more" in screens)

    # C-008 markdown rendering — deferred for v1 (see docs/MARKDOWN-COST.md).
    # llm-typewriter was tried but has no Android target — reverted.
    ok("markdown deferred (no llm-typewriter dep)", "io.github.nadeemiqbal:llm-typewriter" not in build_kts)
    ok("markdown deferred (no StreamingTypewriter import)", "StreamingTypewriter" not in screens
        and "rememberMarkdownTypewriterRenderer" not in screens)
    ok("markdown deferred (assistant renders as plain Text)", "assistant messages render as plain text" in screens)

    # C-009 height placeholder guards.
    ok("height placeholder in LazyColumn", "isLastStreaming" in screens
        and "hasOpenBlock" in screens
        and "hasOpenTable" in screens)
    ok("Spacer for open code block", "32.dp" in screens and "hasOpenBlock" in screens)

    # C-010 context compression guards.
    ok("ContextTrimmer exists", (ROOT / "app" / "src" / "main" / "java" /
        "com" / "litechat" / "android" / "data" / "context" / "ContextTrimmer.kt").is_file())
    ok("trim() in ViewModel", "ContextTrimmer.trim" in vm
        and "truncatedCount" in vm)
    ok("truncation indicator in UI", "truncatedCount > 0" in screens
        and "earlier message(s) not included" in screens)

    all_kt = "\n".join(p.read_text() for p in KT_ROOT.rglob("*.kt"))
    api = cli  # alias for cleaner guard naming

    # C-013 /browse guards.
    ok("Jsoup dep", "org.jsoup:jsoup:1.18.1" in build_kts)
    ok("fetchPage in client", "fetchPage" in api and "Jsoup" in api)
    ok("/browse handler", "/browse " in vm and "fetchPage" in vm)

    # C-014 backup/restore guards.
    ok("exportChats in VM", "exportChats" in vm and "importChats" in vm)

    # C-015 overlay guards.
    overlay_kt_early = (KT_ROOT / "com/litechat/android/ui/OverlayService.kt").read_text()
    ok("OverlayService file", (KT_ROOT / "com/litechat/android/ui/OverlayService.kt").is_file())
    ok("SYSTEM_ALERT_WINDOW in manifest", "SYSTEM_ALERT_WINDOW" in manifest)
    # C-015 fix (2026-08-15): the service must actually show the window and send.
    ok("overlay shows + sends", "onStartCommand" in overlay_kt_early
        and "showOverlay()" in overlay_kt_early and "completeChat" in overlay_kt_early)

    # C-016 attach guards.
    ok("attachImage in VM", "attachImage" in vm and "Base64" in vm)

    # C-017 failover guards.
    ok("ProviderEntry in repo", "ProviderEntry" in settings_repo and "providerListKey" in settings_repo)
    ok("failover in VM", "getProviderList" in vm and "Trying" in vm)

    # C-018 per-conv model guards.
    ok("ConversationEntity.model", "val model: String = """ in all_kt and "per-conversation model binding" in all_kt)

    # C-019 test button guards.
    ok("Test button in Settings", "onClick = {\n                                        testing = true" in screens)

    # C-020 memory guards.
    ok("MemoryManager file", (KT_ROOT / "com/litechat/android/data/context/MemoryManager.kt").is_file())
    # C-020 fix (2026-08-15): memory must be wired into the send flow.
    ok("memory wired in VM", "memoryManager" in vm and "getMemoryPrompt" in vm
        and "Remember " in vm)

    # C-021 voice input guards.
    ok("voice launcher", "rememberLauncherForActivityResult" in screens and "onVoiceInput" in screens)

    # C-022 export/import guards.
    ok("export/import wired", "onExport" in screens and "onImport" in screens and "Backup chats" in screens)
    # C-022 fix (2026-08-15): settings JSON export/import (no secrets).
    ok("settings export/import wired", "exportSettingsJson" in settings_repo
        and "importSettingsJson" in settings_repo and "Export settings" in screens)

    # C-023 NamedKeyStore guards.
    ok("NamedKeyStore file", (KT_ROOT / "com/litechat/android/data/prefs/NamedKeyStore.kt").is_file())
    # C-023 fix (2026-08-15): named keys must be wired + reachable from the UI.
    ok("named keys wired", "namedKeyStore" in vm and "NamedKey" in vm
        and "Saved keys" in screens and "getActiveKey" in all_kt)

    # C-024 conversation forks guards.
    ok("MessageEntity.parentId", "val parentId: String? = null" in all_kt and "conversation forks" in all_kt)
    # C-024 fix (2026-08-15): fork must be more than a schema column.
    ok("forkConversation in repo", "forkConversation" in all_kt
        and "Fork from here" in screens)

    # C-032: Play AI-Generated Content compliance (in-app reporting +
    # acceptable-use + EEA/UK non-personalized ads).
    ok("C-032 report flow", "ACTION_SENDTO" in screens and "Report content" in screens
        and "mailto:litechat@proton.me" in screens)
    ok("C-032 acceptable-use", "AcceptableUseDialog" in screens
        and "acceptableUseAccepted" in settings_repo)
    ok("C-032 non-personalized ads", "PublisherPrivacyPersonalizationState" in ad_init)

    # Gate-gap close (2026-08-15): /browse, attach and backup now enforce Pro.
    ok("Pro gates enforced (browse/attach/backup)", "Web browsing is a Pro feature" in vm
        and "Image attachment is a Pro feature" in vm
        and "Chat backup is a Pro feature" in vm)

    # Image cache guards.
    ok("ImageCacheConfig file", (KT_ROOT / "com/litechat/android/util/ImageCacheConfig.kt").is_file())
    ok("Coil SingletonImageLoader", "SingletonImageLoader" in all_kt and "ImageCacheConfig" in all_kt)

    # C-027 video generation guards.
    ok("createVideo in client", "fun createVideo" in api and "fun pollVideo" in api)
    ok("pollVideo in client", "pollVideo" in api and "job id" in api.lower())
    ok("/video handler in VM", "/video " in vm and "createVideo" in vm)
    ok("VideoView in Screens", "[VIDEO:" in screens and "VideoView" in screens)

    # Kai 9000 honesty rule guard.
    ok("honesty rule in system prompt", '"Do not fabricate tool outputs' in vm
        and 'ChatMessageDto("system"' in vm)

    for bad in ("WebView", "trustAll", "react-native", "io.flutter"):
        ok(f"no {bad}", bad not in all_kt)

    # band boundaries (mirror DeviceCompat.bandFor)
    def band_for(avail_mb: int, total_mb: int = 0) -> str:
        basis = avail_mb if avail_mb > 64 else total_mb // 2
        if basis < 1024:
            return "TIGHT"
        if basis < 2048:
            return "COMFORTABLE"
        if basis < 3584:
            return "ROOMY"
        return "GENEROUS"

    ok("band 800 TIGHT", band_for(800) == "TIGHT")
    ok("band 2048 ROOMY", band_for(2048) == "ROOMY")
    ok("handoff present", (ROOT / "HANDOFF.md").is_file())
    ok("backlog present", (ROOT / "docs" / "BACKLOG.md").is_file())
    ok("team doc present", (ROOT / "docs" / "TEAM.md").is_file())

    # C-031: user-facing brand is BYO AI; package path stays LiteChat.
    strings_xml = (APP / "res" / "values" / "strings.xml").read_text()
    build_kts = (ROOT / "app" / "build.gradle.kts").read_text()
    listing = ROOT / "docs" / "PLAY-LISTING-DRAFT.md"
    listing_txt = listing.read_text() if listing.is_file() else ""
    ok("C-031 app_name BYO AI", ">BYO AI<" in strings_xml)
    ok("C-031 applicationId com.byoai.chat", "applicationId = \"com.byoai.chat\"" in build_kts)
    ok("C-031 namespace stays litechat", "namespace = \"com.litechat.android\"" in build_kts)
    ok("C-031 play listing draft", listing.is_file())
    ok("C-031 listing short line", "Works on 4GB phones" in listing_txt)
    ok("C-031 no SoftRAM claim", "real model on 4GB" not in listing_txt.lower())
    ok("C-031 no Text LiteChat title", 'Text("LiteChat"' not in screens)
    overlay_kt = (KT_ROOT / "com/litechat/android/ui/OverlayService.kt").read_text()
    ok("C-031 overlay no LiteChat", "LiteChat Overlay" not in overlay_kt)

    # C-034: Agent Lab is a door, not a bundled Termux/Node runtime.
    gate = (KT_ROOT / "com/litechat/android/util/AgentLabGate.kt").read_text()
    all_src = "\n".join(p.read_text() for p in KT_ROOT.rglob("*.kt"))
    ok("C-034 AgentLabGate decide", "fun decide(" in gate)
    ok("C-034 refuse weak phones", "Decision.REFUSE" in gate and "Band.TIGHT" in gate)
    ok("C-034 not in this app copy", "not in this app" in gate)
    ok("C-034 mayOpenTermux gate", "fun mayOpenTermux" in gate)
    ok("C-034 Settings agent box", "AgentLabCard" in screens and "Agent box" in screens)
    ok("C-034 manifest sees Termux only", 'package android:name="com.termux"' in manifest)
    ok("C-034 no proot installer", "proot-distro" not in all_src)
    ok("C-034 no curl pipe installer", "curl " not in all_src or "| bash" not in all_src)

    # C-033: pick provider / pick model / paste key.
    catalog = (KT_ROOT / "com/litechat/android/data/prefs/ProviderCatalog.kt").read_text()
    picker = (KT_ROOT / "com/litechat/android/ui/ProviderSetupFields.kt").read_text()
    ok("C-033 catalog exists", "object ProviderCatalog" in catalog)
    ok("C-033 has xai grok", 'id = "xai"' in catalog and "grok-4.6" in catalog)
    ok("C-033 has openrouter deepseek hf", 'id = "openrouter"' in catalog and 'id = "deepseek"' in catalog and 'id = "huggingface"' in catalog)
    ok("C-033 xai paid warning", "paid = true" in catalog and "cost money" in catalog)
    ok("C-033 picker dropdowns", "ExposedDropdownMenuBox" in picker and "Paste your key" in picker)
    ok("C-033 onboarding uses picker", "ProviderSetupFields(" in screens)
    ok("C-033 settings uses picker", screens.count("ProviderSetupFields(") >= 2)
    ok("C-033 URL hidden except custom", "isCustom" in picker)
    ok("host models fill picker", "hostModels" in picker and "fun chatModelIds" in catalog)
    ok("gemini picker is 3.6/3.7/3.1",
       'ModelOption("gemini-3.6-flash"' in catalog
       and 'ModelOption("gemini-3.7-flash"' in catalog
       and 'ModelOption("gemini-3.1-pro-preview"' in catalog)
    ok("gemini picker dropped 2.5-pro option", 'ModelOption("gemini-2.5-pro"' not in catalog)
    ok("gemini picker dropped 2.0-flash option", 'ModelOption("gemini-2.0-flash"' not in catalog)
    ok("groq picker current oss ids", 'ModelOption("openai/gpt-oss-120b"' in catalog and 'ModelOption("groq/compound"' in catalog)
    ok("groq picker dropped llama 3.3 option", 'ModelOption("llama-3.3-70b-versatile"' not in catalog)
    ok("edit door helper", "fun resolveEditModel" in catalog)
    ok("stt groq whisper v3", "whisper-large-v3" in catalog)
    ok("gemini resolve remaps dead ids", "fun resolveModel" in catalog)
    ok("imagine picks host picture model", "fun resolveImageModel" in catalog and "gemini-3.1-flash-image" in catalog)
    ok("video picks host video model", "fun resolveVideoModel" in catalog and "veo-3.1-generate-preview" in catalog)

    # 2026-08-15 REVIEW fixes — regression guards for the confirmed bugs.
    sse = (KT_ROOT / "com/litechat/android/data/api/ChatSseParser.kt").read_text()
    client = (KT_ROOT / "com/litechat/android/data/api/OpenAiCompatibleClient.kt").read_text()
    trimmer = (KT_ROOT / "com/litechat/android/data/context/ContextTrimmer.kt").read_text()
    nks = (KT_ROOT / "com/litechat/android/data/prefs/NamedKeyStore.kt").read_text()
    vm = (KT_ROOT / "com/litechat/android/ui/ChatViewModel.kt").read_text()
    ok("imagine no longer always gpt-image-2", "resolveImageModel(baseUrl)" in client)
    ok("video no longer always sora-2", "resolveVideoModel(baseUrl)" in client)
    ok("openrouter picture door", "openrouterImagesUrl" in client)
    ok("R-020 xai edit json", "fun xaiEditJson" in client and "editXaiImage" in client)
    ok("R-020 xai edit not only multipart", "image_url" in client)
    ok("no destructive room fallback", "fallbackToDestructiveMigration" not in (KT_ROOT / "com/litechat/android/data/AppContainer.kt").read_text())
    ok("picture url streams to file", "createTempFile(\"pic\"" in client or "streamUrlToFile" in client)
    ok("attach copies to file", "attach_" in vm and "decodeFile" in vm)
    ok("slash peel on send", "SlashInput.peel" in vm)
    ok("groq compound cost label", "can cost money" in catalog)
    ok("sora sunset date", "SORA_SUNSET_MS" in catalog)
    ok("openrouter picture slug", "openai/gpt-image-2" in catalog)
    ok("openrouter video not sora", '\"openrouter\", \"custom\" -> \"sora-2\"' not in catalog)
    ok("gemini picture uses generateContent", "fun geminiGenerateContentUrl" in client and "generateContent" in client)
    ok("gemini picture reads inlineData", "fun firstInlineImageB64" in client)
    ok("gemini picture key is x-goog-api-key only", "fun Request.Builder.geminiKey" in client and "friendlyMediaError" in client)
    ok("setup waits for DataStore", "object SetupGate" in (KT_ROOT / "com/litechat/android/ui/SetupGate.kt").read_text())
    ok("root uses SetupGate", "SetupGate.showOnboarding" in screens)
    ok("gemini veo native door", "predictLongRunning" in client and "fun veoStartUrl" in client)
    ok("openai video url no double v1", "fun openaiVideosUrl" in client)
    ok("xai video generations door", "fun xaiVideoStartUrl" in client)
    ok("REVIEW parseEvent accepts stripped payloads", "takeIf { it.isNotEmpty() }" in sse)
    ok("REVIEW stream cancel qualified", "this@OpenAiCompatibleClient.cancel()" in client)
    ok("REVIEW trimmer keeps turn pairs", "trailingUser" in trimmer)
    ok("REVIEW trimmer counts system tokens", "systemTokens" in trimmer)
    ok("REVIEW named key active after insert", "activeIdx" in nks)
    ok("REVIEW listModels off main", "withContext(Dispatchers.IO) {" in screens and "openAiClient.listModels" in screens)
    ok("REVIEW attach fits 32k cap", "MAX_INPUT_CHARS - prefix.length" in vm and "Attachment too large" in vm)

    # P-012 + P-014 — Tier 1 bundle (H-008=A): reply language + pins/drafts.
    entities = (KT_ROOT / "com/litechat/android/data/db/Entities.kt").read_text()
    repo = (KT_ROOT / "com/litechat/android/data/db/ChatRepository.kt").read_text()
    settings = (KT_ROOT / "com/litechat/android/data/prefs/SettingsRepository.kt").read_text()
    ok("P-014 pinned column", "val pinned: Boolean = false" in entities)
    ok("P-014 migration not destructive", "MIGRATION_1_2" in entities and "ADD COLUMN pinned" in entities)
    ok("P-014 pin sort pure + toggle", "ConversationSort.pinnedFirst" in repo and "togglePin" in repo)
    ok("P-014 drafts persisted", "saveDraft" in settings and "drafts_json" in settings)
    ok("P-014 draft restore on switch", "getDraft" in vm and "saveDraft" in vm and "clearDraft" in vm)
    ok("P-014 pin button in drawer", "onTogglePin" in screens and "Icons.Default.Star" in screens)
    ok("P-012 language setting", "LANGUAGE" in settings and "val language: String" in settings)
    ok("P-012 language in system prompt (free)", "setLanguage" in vm and "Reply in" in vm)
    ok("P-012 language picker in settings", "onSetLanguage" in screens and "Reply language" in screens)

    # P-002 — full-text search across chats (Pro).
    ftsq = (KT_ROOT / "com/litechat/android/data/db/FtsQuery.kt").read_text()
    ok("P-002 FtsQuery escape", "object FtsQuery" in ftsq and "fun escape" in ftsq)
    ok("P-002 FTS table", "@Fts4" in entities and "messages_fts" in entities)
    ok("P-002 migration 2-3 not destructive", "MIGRATION_2_3" in entities and "CREATE VIRTUAL TABLE" in entities)
    ok("P-002 index on insert + search", "indexMessage" in repo and "searchMessages" in repo)
    ok("P-002 Pro gate", "Search is a Pro feature" in vm)
    ok("P-002 SearchScreen + grouped", "fun SearchScreen" in screens and "Search chats" in screens)
    ok("P-002 tap opens hit", "openSearchHit" in vm and "highlightMessageId" in vm)

    ok("P-009 folderId + migration", "folderId" in entities and "MIGRATION_3_4" in entities)
    ok("P-009 setFolder + filter", "setFolder" in repo and "inFolder" in (KT_ROOT / "com/litechat/android/data/db/ConversationSort.kt").read_text())
    ok("P-009 FolderBar", "fun FolderBar" in screens or "FolderBar(" in screens)
    ok("P-004 Registered card", "Registered — BYO AI" in screens or "RegisteredCard" in screens)
    ok("P-010 PersonaPacks", "object PersonaPacks" in (KT_ROOT / "com/litechat/android/data/prefs/PersonaPacks.kt").read_text())
    ok("P-010 persona picker", "PersonaRow" in screens and "setPersona" in vm)
    ok("P-013 ChatOptions + knobs", "class ChatOptions" in (KT_ROOT / "com/litechat/android/data/api/OpenAiCompatibleClient.kt").read_text() and "top_p" in (KT_ROOT / "com/litechat/android/data/api/OpenAiCompatibleClient.kt").read_text())
    ok("P-013 Advanced free", "Advanced" in screens and "promptCache" in settings)
    ok("P-005 /search", '"/search "' in vm and "fetchSearch" in (KT_ROOT / "com/litechat/android/data/api/OpenAiCompatibleClient.kt").read_text())
    ok("P-006 recall + edit", '"/recall"' in vm and "fun recall" in (KT_ROOT / "com/litechat/android/data/context/MemoryManager.kt").read_text())
    ok("P-003 BackupCrypto", "object BackupCrypto" in (KT_ROOT / "com/litechat/android/util/BackupCrypto.kt").read_text())
    ok("P-001 voice limit + tts", "VoiceDailyLimit" in vm and "readAloud" in vm)
    ok("REVIEW #1 slot after text", vm.find("Nothing to read yet") is not None and vm.find("Nothing to read yet") < vm.find("if (!consumeVoiceSlot()) return"))
    ok("REVIEW #2 mic does not burn slot", "consumeVoiceSlot" not in screens)
    ok("REVIEW #4 prepare on IO", "prepare()" in vm and "withContext(Dispatchers.IO)" in vm)
    crypto = (KT_ROOT / "com/litechat/android/util/BackupCrypto.kt").read_text()
    export_fn = vm.split("fun exportChats")[1].split("fun importChats")[0] if "fun exportChats" in vm else ""
    import_fn = vm.split("fun importChats")[1].split("fun attachImage")[0] if "fun importChats" in vm else ""
    ok("REVIEW #5 stream backup", "encryptTo" in crypto and "readBytes()" not in export_fn and "readBytes()" not in import_fn)
    ok("REVIEW A no /v1/v1 edits", '"$root/v1/images/edits"' not in client)
    ok("REVIEW B honest 404 only", 'low.contains("unknown")' not in client)
    ok("REVIEW C png imagine", 'gen_${System.currentTimeMillis()}.png' in vm or "CompressFormat.PNG" in vm)
    ok("REVIEW D search streamJob", "streamJob = viewModelScope.launch" in vm.split("fun send")[1].split("/recall")[0] if "fun send" in vm else False)
    ok("REVIEW D edit sets isStreaming", "isStreaming = true" in vm.split("/edit $prompt")[0].split("P-011")[-1] if "P-011" in vm else False)
    ok("REVIEW E armBackupPass", "fun armBackupPass" in vm)
    ok("REVIEW F backup Pro UI", "Chat backup is a Pro feature" in screens)
    ok("REVIEW G strict templates", "decodeTemplatesStrict" in settings)
    ok("REVIEW H folder kotlinx", "ChatFolder.serializer" in settings)
    ok("REVIEW I searchJob cancel", "searchJob?.cancel()" in vm)
    sanitizer = (KT_ROOT / "com/litechat/android/data/prefs/ApiKeySanitizer.kt").read_text()
    ok("B-001 headerSafe on keys", "ApiKeySanitizer.headerSafe" in client)
    ok("B-001 save strips key", "ApiKeySanitizer.headerSafe" in (KT_ROOT / "com/litechat/android/data/prefs/SecureStore.kt").read_text())
    ok("R-019 no addUnsafeNonAscii", "addUnsafeNonAscii" not in client)
    ok("R-019 everyday key line", "This key has a bad character" in sanitizer)
    ok("R-019 chat uses sanitizer", "ApiKeySanitizer.userSafeError" in vm)
    ok("R-019 Test uses sanitizer", "ApiKeySanitizer.BAD_KEY_LINE" in screens or "isIllegalHeader" in screens)
    ok("R-019 imagine catch not raw e.message", "Image generation failed: ${e.message" not in vm)
    ok("browse bare url", "BrowseUrl.normalize" in vm)
    ok("browse fetch uses normalize", "BrowseUrl.normalize" in client)
    ok("stop imagine job", "streamJob = viewModelScope.launch" in vm.split('text.startsWith("/imagine ")) {')[1][:800] if 'text.startsWith("/imagine ")) {' in vm else False)
    ok("stop video job", "streamJob = viewModelScope.launch" in vm.split('text.startsWith("/video ")) {')[1][:800] if 'text.startsWith("/video ")) {' in vm else False)
    ok("stop browse job", "streamJob = viewModelScope.launch" in vm.split('text.startsWith("/browse ")) {')[1][:800] if 'text.startsWith("/browse ")) {' in vm else False)
    ok("FAB stops generate", "isGeneratingImage) onStop()" in screens)
    ok("key field masked", "PasswordVisualTransformation" in (KT_ROOT / "com/litechat/android/ui/ProviderSetupFields.kt").read_text())
    ok("memory decode missing hitCount", "hitCount" in (KT_ROOT / "com/litechat/android/data/context/MemoryManager.kt").read_text() and "decodeList" in (KT_ROOT / "com/litechat/android/data/context/MemoryManager.kt").read_text())
    ok("overlay lifecycle owner", "ViewTreeLifecycleOwner" in (KT_ROOT / "com/litechat/android/ui/OverlayService.kt").read_text())
    ok("failover updates same row", "updateMessageContent(assistantId" in vm)
    ok("named key headerSafe", "ApiKeySanitizer.headerSafe" in (KT_ROOT / "com/litechat/android/data/prefs/NamedKeyStore.kt").read_text())

    ok("REVIEW D search uses activeCall", "fun fetchSearch" in client and "activeCall = call" in client.split("fun fetchSearch")[1].split("fun editImage")[0])
    ok("P-011 /edit", '"/edit "' in vm and "editImage" in (KT_ROOT / "com/litechat/android/data/api/OpenAiCompatibleClient.kt").read_text())

    export_fn = vm.split("fun exportChats")[1].split("fun importChats")[0] if "fun exportChats" in vm else ""
    poll = client.split("fun pollVideo")[1].split("fun streamUrlToFile")[0] if "fun pollVideo" in client else ""
    ok("B-005 refuse blank backup pass", "Type a backup password first." in vm and "Type a backup password first." in screens)
    ok("B-005 no live-db copyTo", "inn.copyTo(out)" not in export_fn)
    ok("B-005 always encryptTo", "BackupCrypto.encryptTo" in export_fn)
    ok("B-006 named key masked", "PasswordVisualTransformation" in screens.split("Key name")[1][:800] if "Key name" in screens else False)
    ok("B-006 backup pass masked", "PasswordVisualTransformation" in screens.split("Backup password")[1][:400] if "Backup password" in screens else False)
    ok("B-006 backup pass not optional", "Backup password (optional)" not in screens)
    ok("B-007 video poll delay", "delay(" in poll and "Thread.sleep" not in poll)
    ok("B-007 poll fails non-2xx", "isSuccessful" in poll and "mediaHttpError" in poll)
    ok("B-007 pollVideo is suspend", "suspend fun pollVideo" in client)
    ok("B-008 clearMemory Pro", "Memory is a Pro feature" in vm.split("fun clearMemory")[1][:400] if "fun clearMemory" in vm else False)
    ok("B-008 confirm memory wipe", "Clear memory?" in screens)
    retry_kt = (KT_ROOT / "com/litechat/android/data/api/RetryInterceptor.kt").read_text()
    ok("B-011 last 429 returned", "attempt >= MAX_ATTEMPTS" in retry_kt and "return response" in retry_kt.split("Last 429")[-1][:500])
    ok("B-011 429 everyday media", "This key cannot make $kind right now" in client)
    ok("B-011 no swallow last 429", 'throw lastException ?: IOException("Max retries' in retry_kt)
    ok("B-012 chat not named override", "getActiveKey()" not in vm)
    ok("B-012 overlay not named override", "getActiveKey()" not in overlay_kt)
    ok("B-012 Test wipes on switch", "LaunchedEffect(base, key, model)" in screens and "testMsg = null" in screens)
    ok("free test toggle in settings", "Free test pictures (no key)" in screens and "onSetFreeTestImages" in screens)
    ok("free test door in client", "fun pollinationsImage" in client and "image.pollinations.ai" in catalog)
    ok("free test labeled not silent", "Free test picture" in vm and "Free test picture" in screens)
    ok("imagine may run keyless", "imagineFreeOk" in vm)
    ok("video free-test honesty", "Video needs a provider key" in vm)

    # Fastlane metadata is part of the build: F-Droid/Play read these files.
    # No Ruby gem. CI static-verify fails the job if listing copy is wrong.
    fl = ROOT / "fastlane" / "metadata" / "android" / "en-US"
    title_p = fl / "title.txt"
    short_p = fl / "short_description.txt"
    full_p = fl / "full_description.txt"
    title = title_p.read_text() if title_p.is_file() else ""
    short = short_p.read_text() if short_p.is_file() else ""
    full = full_p.read_text() if full_p.is_file() else ""
    locked = "Chat with your own key. Works on 4GB phones. No monthly bill."
    vc = "1"
    for line in build_kts.splitlines():
        if "versionCode" in line and "=" in line:
            digits = "".join(c for c in line.split("=", 1)[1] if c.isdigit())
            if digits:
                vc = digits
                break
    changelog = fl / "changelogs" / f"{vc}.txt"
    ok("fastlane title present", title_p.is_file())
    ok("fastlane title BYO AI", title.strip() == "BYO AI")
    ok("fastlane title ≤30", len(title.strip()) <= 30)
    ok("fastlane short present", short_p.is_file())
    ok("fastlane short == locked line", short.strip() == locked)
    ok("fastlane short ≤80", len(short.strip()) <= 80)
    ok("fastlane full present", full_p.is_file())
    ok("fastlane full ≤4000", len(full) <= 4000)
    ok("fastlane full measured 1.6 MB", "1.6 MB" in full)
    ok("fastlane full no size guess", "~2 MB" not in full)
    ok("fastlane full foss no billing", "no billing" in full.lower())
    ok("fastlane changelog for versionCode", changelog.is_file())
    ok("fastlane fdroid.yml present", (fl / "fdroid.yml").is_file())

    failed = 0
    print("=== LiteChat verify_static ===")
    for name, passed, detail in checks:
        if not passed:
            failed += 1
        mark = "PASS" if passed else "FAIL"
        extra = f" ({detail})" if detail else ""
        print(f"{mark}: {name}{extra}")
    print(f"--- {len(checks) - failed}/{len(checks)} passed ---")
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
