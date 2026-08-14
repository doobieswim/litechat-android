# LiteChat — Exhaustive Code Review Report

**Date:** 2026-08-14
**Method:** 3 parallel read-only review subagents (UI / data-network-db / prefs-util-media-gradle) + real Gradle build (`:app:compilePlayDebugKotlin`, JDK 17, SDK 36) + git history analysis.
**Reviewer codeword:** `LITECHAT-REVIEW`
**Status:** 🟡 **Mostly resolved** — all build blockers (A1-A6) + security B1 + runtime C1-C3/C5/C6/C9 applied and compiling locally (2026-08-14). Remaining: C7 (media in evictable cacheDir), C8 (WAL backup), dead-code items in Part D.

---

## PART A — BUILD BLOCKERS (fix these first, in order)

### A1. 🔴 AGP version corrupted → `2.1.2` (doesn't exist)
- **File:** `build.gradle.kts:2`
- **What:** `id("com.android.application") version "2.1.2"` — not a valid AGP version (2016-era; no such modern build plugin).
- **Proof:** real build fails: `Plugin [id: 'com.android.application', version: '2.1.2'] was not found`. Gradle 8.11.1 + compileSdk 36 + Kotlin 2.2.0 all require AGP 8.x.
- **Origin:** commit `874b30b` (Aug 10) changed it from `"8.7.3"`.
- **Fix:** ✅ **APPLIED** → `version "8.7.3"`. Build now resolves AGP.

### A2. 🔴 Room 2.6.1 + KSP 2.2.0 → `unexpected jvm signature V`
- **File:** `app/build.gradle.kts:131-134` (Room 2.6.1) + `build.gradle.kts:6` (KSP `2.2.0-2.0.2`)
- **What:** KSP2 compiler crashes on Room 2.6.1 codegen: `kspPlayDebugKotlin FAILED: java.lang.IllegalStateException: unexpected jvm signature V`. Room 2.6.1's compiler is not KSP2-compatible.
- **Proof:** real build failure; KSP1 mode (`-Pksp.useKSP2=false`) gets past Room with zero data-layer errors.
- **Fix:** ✅ **APPLIED** → `val room = "2.7.1"` (KSP2-ready). Build now passes KSP/Room; exposes the remaining Kotlin errors.

### A3. 🔴 Reversed constructor args (Coil loader)
- **File:** `LiteChatApp.kt:28` vs `ImageCacheConfig.kt:47`
- **What:** `createImageLoader(context, band)` is called as `createImageLoader(snap.band, this)` — args swapped.
- **Origin:** commit `03ff687`.
- **Fix:** ✅ **APPLIED** → `createImageLoader(this, snap.band)`.

### A4. 🔴 Coil 3.1.0 API mismatch (after 3.5.0 → 3.1.0 downgrade)
- **Files:** `ImageCacheConfig.kt:59,63`; `LiteChatApp.kt:5,13,44,50`
- **Errors:** `Argument type mismatch: File but Path expected` (disk cache directory), `Unresolved reference 'bitmapConfig'`, `Unresolved reference 'ImageLoaderFactory'`, `Unresolved reference 'Coil'`, `'newImageLoader' overrides nothing`.
- **Cause:** commit `874b30b` downgraded Coil **3.5.0 → 3.1.0** but left 3.5.0 API usage. Coil 3.1.0's `DiskCache.Builder.directory()` wants a `Path`; `ImageLoader.Builder.bitmapConfig()` signature differs; `ImageLoaderFactory` lives in a different package.
- **Fix (2 options):**
  - **Option 1 (preferred, matches git intent):** revert Coil to `3.5.0` (`coil-compose`, `coil-network-okhttp`, `coil-core`). Then the current code compiles unchanged. (compileSdk 36 already supports it — commit `a3996f8` bumped SDK for exactly this.)
  - **Option 2 (keep 3.1.0):** change disk-cache `directory()` to a `Path`, fix `bitmapConfig` API, and correct the `ImageLoaderFactory`/`Coil` imports.
  - ⚠️ **Not yet applied** — needs a decision (see bottom).

### A5. 🔴 Missing imports (deleted by prior agent)
- **`ChatViewModel.kt`:** `Unresolved reference 'combine'` (L67) → add `import kotlinx.coroutines.flow.combine`. This single missing import cascades into the `component3()/isPro/copy` errors at L71-86.
- **`NamedKeyStore.kt` + `SettingsRepository.kt`:** `Unresolved reference 'jsonArray'/'jsonObject'/'jsonPrimitive'` (NamedKeyStore L32-38; SettingsRepository L150-198) → add `import kotlinx.serialization.json.*`.
- **`ChatViewModel.kt:109`:** `Unresolved reference 'FREE_TEMPLATE_LIMIT'` — symbol deleted; re-add the constant (e.g. in `FeatureFlags` or companion).
- **`SettingsRepository.kt:269`:** `Const 'val' only allowed on top level / named objects / companion` — misplaced `const val` inside a function.
- ⚠️ **Not yet applied.**

### A6. 🔴 Voice recognizer API misuse (Screens)
- **File:** `Screens.kt:184-188`
- **Errors:** `RecognizerIntent` constructor is package-private; `putExtra` unresolved; `RecognizerIntent` vs `Intent` mismatch.
- **Cause:** prior-agent deletion/corruption of the speech-recognition intent construction.
- **Fix:** build the recognition intent via `Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)` + `putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, ...)`.
- ⚠️ **Not yet applied.**

---

## PART B — SECURITY (fix as priority-2)

### B1. 🟠 Failover provider API keys stored in PLAINTEXT
- **File:** `SettingsRepository.kt:184-212`
- **What:** `saveProviderList()` writes `"apiKey": "${p.apiKey}"` into unencrypted DataStore Preferences (`provider_list_json`), bypassing the encrypted `SecureStore`. `getProviderList()` is live (consumed `ChatViewModel.kt:521-528`).
- **Why it matters:** a BYOK key for any failover provider sits in cleartext on disk — violates the no-plaintext-key law.
- **Fix:** route provider keys through `SecureStore`/`NamedKeyStore` (encrypted); store only an id in DataStore.

### B2. ✅ Verified SECURE (no action)
- `SecureStore.kt` — correctly uses `EncryptedSharedPreferences` + `MasterKey AES256_GCM`. Primary key path is encrypted.
- No trust-all SSL anywhere (`no setHostnameVerifier`/`X509TrustManager`/`SSLContext`).
- No API-key logging anywhere in `app/src`.
- Intentional cleartext only for localhost Ollama preset (`http://127.0.0.1:11434`).

---

## PART C — RUNTIME / MEMORY BUGS (4GB-first violations)

### C1. 🟠 C-028 video heap bug — NOT actually fixed (the big one)
- **File:** `OpenAiCompatibleClient.kt:349`; `ChatViewModel.kt:272-279`
- **What:** `pollVideo` does `it.body?.bytes()` — loads the **entire MP4 (5-20MB)** into heap, then `file.writeBytes(videoBytes)` makes a second copy.
- **Key finding:** commit `1a290d6` is titled "C-028: Stream video download to disk (ByteArray → Okio sink)" but its **only change is `docs/BACKLOG.md`** — zero code changed. The fix was never implemented.
- **Fix:** stream to disk: `dlCall.execute().use { r -> FileOutputStream(file).use { f -> r.body?.byteStream()?.copyTo(f) } }`.

### C2. 🟠 Main-thread blocking → ANR / NetworkOnMainThreadException
- **File:** `OpenAiCompatibleClient.kt:352` (`Thread.sleep(2000)` poll loop), plus `ChatViewModel.kt` calls `fetchPage` (Jsoup), `generateImage`, `createVideo`/`pollVideo`, failover `completeChat`, `listModels` — all on Main dispatcher.
- **What:** zero `withContext(Dispatchers.IO)` in `ChatViewModel.kt`. Blocking calls on Main → ANR / crash on Android 7+.
- **Fix:** wrap every blocking client call in `withContext(Dispatchers.IO)`; make `pollVideo` suspend.

### C3. 🟠 "Stop" does not stop — triggers retries
- **File:** `ChatViewModel.kt:169-173`; `OpenAiCompatibleClient.kt:63-66,129-134`
- **What:** `streamJob` is declared but **never assigned** (always null) → `stopStreaming()` cancels nothing. Client `cancel()` nulls `activeCall` before the check → user-cancel misread as network error; `catch (e: Exception)` swallows `CancellationException` → Stop auto-retries and duplicates assistant rows.
- **Fix:** track the `send()` job; rethrow `CancellationException`; don't null `activeCall` before the cancel check.

### C4. 🟠 `/video` polling blocks UI + `.bytes()` heap (ties to C1)
- Combined with C1/C2 — the video path is the worst offender on 4GB.

### C5. 🟡 Overlay (C-015) is a gutted, inert stub
- **File:** `OverlayService.kt:66,70-88,118-120`; `Screens.kt:1088-1094`
- **Problems:** dead `ChatViewModel` created but never used (leaks collectors); overlay has no send wiring; notification channel never created (empty stub → foreground notification won't show); `SYSTEM_ALERT_WINDOW` never checked (`BadTokenException` risk); `PendingIntent.getActivity` points at the Service class; "Pro" label not gated by Pro.

### C6. 🟡 `attachImage` truncates images via the 32k input cap
- **File:** `ChatViewModel.kt:614-626`
- **What:** full image → base64 → `setInput` → `InputPolicy.cap` (32_000 chars) silently truncates any image whose base64 exceeds 32k → corrupt image sent. Full `readBytes` spikes heap.
- **Fix:** downscale before encoding; don't route binary through the text cap.

### C7. 🟡 Generated media stored in evictable `cacheDir`
- **File:** `ChatViewModel.kt:335-339, 277-281`
- **What:** `/imagine` and `/video` write to `cacheDir` (OS-evictable) and store the absolute path as message content. After eviction, messages render raw `[IMAGE:…]` text.
- **Fix:** copy into `filesDir` (app-private).

### C8. 🟡 `exportChats`/`importChats` copy a live Room DB (WAL) — corrupt backup risk
- **File:** `ChatViewModel.kt:585-611`

### C9. 🟡 `truncatedCount` never resets; stale error banner persists; retries duplicate "Retrying…"/"Error:" rows
- **File:** `ChatViewModel.kt:387-389, 415-418, 476-492`

---

## PART D — DEAD / UNWIRED CODE (was it wired? No.)

| Item | File | Status |
|------|------|--------|
| `ScreenshotDetector` | `util/ScreenshotDetector.kt` | Dead stub, **also NPE-risk** (`getExternalStoragePublicDirectory` null on API 29+), undeclared `READ_MEDIA_IMAGES` |
| `CommunityPrompts` | `data/community/CommunityPrompts.kt` | Dead; hardcodes wrong owner `flamingspade1995-coder/...`; `URL.readText()` no timeout |
| `NamedKeyStore` (C-023) | `data/prefs/NamedKeyStore.kt` | Entire class unwired; manual-JSON-no-escaping bug (key with `"` wipes all) |
| `MemoryManager` (C-020) | `data/context/MemoryManager.kt` | Unwired; same manual-JSON-no-escaping bug (fact with `"` drops all memories) |
| `FeatureFlags.unlimitedRepos` | `core/flags/FeatureFlags.kt:24` | Dangling (never referenced) |
| `FeatureFlags.markdownRendering` | `core/flags/FeatureFlags.kt:27` | Dangling |
| `/browse` (C-013) | `ChatViewModel.kt:215-244` | Fetches page but **never calls the model** → no AI answer produced |

---

## PART E — CLEAN / CONFIRMED-CORRECT (no action)

- **Room entities** (`Entities.kt`) — valid PKs, no type issues. **ChatRepository** CRUD correct, cascades correct.
- **Play/FOSS flavor split** — fully symmetric, NOT a Grok deletion. Both carry `BannerAd`/`BillingRepository`/`AdMobLazyInit`; play = real, foss = intentional stubs. Manifest strips GMS from foss.
- **Single shared OkHttpClient** — honored everywhere in data code (one deviation: `fetchPage` uses Jsoup's own stack).
- **RetryInterceptor** — correct: retries only 429/5xx, no auth retries, bounded (3 attempts, backoff+jitter). Minor: doc claims "Respects Retry-After" but doesn't.
- **`DeviceCompat`** — 8-row matrix, all 4 bands intact, 4GB-honesty (availMem-first) preserved. Bands match tests.
- **`ImageCacheConfig`** — band-tuned (2/5/10/20MB), RGB_565 for weak bands, downscaling intact (only call-site bug A3 + Coil API A4).
- **`ContextTrimmer`** — token-budget logic intact and wired (`ChatViewModel.kt:386`).
- **`InputPolicy`** — `MAX_INPUT_CHARS = 32_000`, `cap()` correct; tests pass.
- **`CompatMatrix`, `Theme` (dark-only), `MainActivity`** — clean. Coil singleton band-tuned; `onLowMemory`/`onTrimMemory` trim cache.
- **`ConnectivityObserver`** — no leak (unregister on `onCleared`). Minor: reports Connected before `NET_CAPABILITY_VALIDATED`.
- **Both SSE parsers** live (neither dead).
- **No WebView, no RN/Flutter, no agent runtime, no trust-all** — Tier A law upheld.

---

## PART F — SEVERITY SUMMARY

| # | Severity | Finding | Status |
|---|----------|---------|--------|
| A1 | 🔴 Build | AGP 2.1.2 → 8.7.3 | ✅ Applied (commit eb96cfc) |
| A2 | 🔴 Build | Room 2.6.1 → 2.7.1 (KSP2) | ✅ Applied (commit eb96cfc) |
| A3 | 🔴 Build | LiteChatApp reversed args | ✅ Applied (commit eb96cfc) |
| A4 | 🔴 Build | Coil 3.1.0 API mismatch | ✅ Applied (commit 7ab0003 — kept 3.1.0, fixed 3 call sites per DO-NOT) |
| A5 | 🔴 Build | Missing imports (combine, json.*, FREE_TEMPLATE_LIMIT, const) | ✅ Applied (commit 7ab0003) |
| A6 | 🔴 Build | Screens RecognizerIntent misuse | ✅ Applied (commit 7ab0003) |
| B1 | 🟠 Security | Provider keys in plaintext | ✅ Applied (keys → SecureStore, DataStore stores id only) |
| C1 | 🟠 Memory | C-028 video heap (fix never implemented) | ✅ Applied (pollVideo streams to File) |
| C2 | 🟠 Runtime | Main-thread blocking / ANR | ✅ Applied (withContext(IO) on all blocking client calls) |
| C3 | 🟠 Runtime | Stop doesn't stop (retries) | ✅ Applied (streamJob assigned + stopRequested + rethrow CancellationException) |
| C4-C9 | 🟡 | Overlay stub, attachImage truncation, cacheDir media, DB backup, banner/retry UX | C5 overlay (PendingIntent→MainActivity, real channel, canDrawOverlays) ✅ · C6 attachImage downscale ✅ · C9 no ghost rows + stale-state reset ✅ · C7/C8 (cacheDir media, DB WAL backup) ⚠️ Open |
| D | ⚠️ | 7 dead/unwired items + NPE/stub risks | NamedKeyStore/MemoryManager JSON escaping ✅; ScreenshotDetector/CommunityPrompts/FeatureFlags dangle + /browse still ⚠️ Open |

---

## PART G — RECOMMENDED NEXT STEPS

1. **Decide Coil version (A4)** — pick 3.5.0 (revert, matches git intent) or 3.1.0 (fix 3 API call sites). This is the one real decision.
2. **Fix the 4 missing-import blocks (A5)** — `combine`, `kotlinx.serialization.json.*`, `FREE_TEMPLATE_LIMIT`, `const val` placement. ~15 min.
3. **Fix Screens RecognizerIntent (A6).**
4. **Rebuild** — verify the tree compiles green.
5. **Then the runtime/security items** (C1 video streaming, C2 IO dispatcher, C3 Stop, B1 key encryption).
6. **Then dead code** (D): wire or delete ScreenshotDetector, CommunityPrompts, NamedKeyStore, MemoryManager, /browse.

*Three build-blocker fixes (A1-A3) were applied during this review so the real errors beneath could be surfaced. All other fixes left for human decision (your call on Coil, and I don't silently rewrite working logic).*
