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

    all_kt = "\n".join(p.read_text() for p in KT_ROOT.rglob("*.kt"))
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
