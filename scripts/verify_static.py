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
    ok("createVideo in client", "createVideo" in api and "v1/videos" in api)
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

    # 2026-08-15 REVIEW fixes — regression guards for the confirmed bugs.
    sse = (KT_ROOT / "com/litechat/android/data/api/ChatSseParser.kt").read_text()
    client = (KT_ROOT / "com/litechat/android/data/api/OpenAiCompatibleClient.kt").read_text()
    trimmer = (KT_ROOT / "com/litechat/android/data/context/ContextTrimmer.kt").read_text()
    nks = (KT_ROOT / "com/litechat/android/data/prefs/NamedKeyStore.kt").read_text()
    vm = (KT_ROOT / "com/litechat/android/ui/ChatViewModel.kt").read_text()
    ok("REVIEW parseEvent accepts stripped payloads", "takeIf { it.isNotEmpty() }" in sse)
    ok("REVIEW stream cancel qualified", "this@OpenAiCompatibleClient.cancel()" in client)
    ok("REVIEW trimmer keeps turn pairs", "keptTurns.size % 2" in trimmer)
    ok("REVIEW trimmer counts system tokens", "systemTokens" in trimmer)
    ok("REVIEW named key active after insert", "activeIdx" in nks)
    ok("REVIEW listModels off main", "withContext(Dispatchers.IO) {" in screens and "openAiClient.listModels" in screens)
    ok("REVIEW attach fits 32k cap", "MAX_INPUT_CHARS - prefix.length" in vm and "Attachment too large" in vm)

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
