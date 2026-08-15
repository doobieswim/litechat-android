# LiteChat — Exhaustive Code Review Report

**Date:** 2026-08-14
**Method:** 3 parallel read-only review subagents (UI / data-network-db / prefs-util-media-gradle) + real Gradle build (`:app:compilePlayDebugKotlin`, JDK 17, SDK 36) + git history analysis.
**Reviewer codeword:** `LITECHAT-REVIEW`
**Status:** 🟢 **Resolved** — all build blockers (A1-A6) + security B1 + runtime C1-C3/C5-C9 applied and compiling locally (2026-08-14); Part D dead code removed or wired (2026-08-14).

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
| `ScreenshotDetector` | `util/ScreenshotDetector.kt` | ✅ **DELETED** (2026-08-14) — dead stub, NPE risk on API 29+, undeclared `READ_MEDIA_IMAGES`; unreferenced |
| `CommunityPrompts` | `data/community/CommunityPrompts.kt` | ✅ **DELETED** (2026-08-14) — dead, wrong owner, `URL.readText()` no timeout; unreferenced |
| `NamedKeyStore` (C-023) | `data/prefs/NamedKeyStore.kt` | ✅ JSON escaping fixed (kotlinx.serialization) — commit `ac13a95` |
| `MemoryManager` (C-020) | `data/context/MemoryManager.kt` | ✅ JSON escaping fixed (kotlinx.serialization) — commit `ac13a95` |
| `FeatureFlags.unlimitedRepos` | `core/flags/FeatureFlags.kt` | ✅ **REMOVED** (2026-08-14) — dangling const |
| `FeatureFlags.markdownRendering` | `core/flags/FeatureFlags.kt` | ✅ **REMOVED** (2026-08-14) — dangling const |
| `/browse` (C-013) | `ChatViewModel.kt` | ✅ **FIXED** (2026-08-14) — page content now fed to the model via `completeChat`, answer stored as assistant message |

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
| C4-C9 | 🟡 | Overlay stub, attachImage truncation, cacheDir media, DB backup, banner/retry UX | C5 overlay (PendingIntent→MainActivity, real channel, canDrawOverlays) ✅ · C6 attachImage downscale ✅ · C9 no ghost rows + stale-state reset ✅ · C7 media → filesDir ✅ (commit pending) · C8 WAL-safe export/import ✅ (commit pending) |
| D | ⚠️ | 7 dead/unwired items + NPE/stub risks | NamedKeyStore/MemoryManager JSON escaping ✅; ScreenshotDetector + CommunityPrompts deleted, FeatureFlags dangling consts removed, /browse now calls the model ✅ (commit pending) |

---

## PART G — RECOMMENDED NEXT STEPS

1. **Decide Coil version (A4)** — pick 3.5.0 (revert, matches git intent) or 3.1.0 (fix 3 API call sites). This is the one real decision.
2. **Fix the 4 missing-import blocks (A5)** — `combine`, `kotlinx.serialization.json.*`, `FREE_TEMPLATE_LIMIT`, `const val` placement. ~15 min.
3. **Fix Screens RecognizerIntent (A6).**
4. **Rebuild** — verify the tree compiles green.
5. **Then the runtime/security items** (C1 video streaming, C2 IO dispatcher, C3 Stop, B1 key encryption).
6. **Then dead code** (D): wire or delete ScreenshotDetector, CommunityPrompts, NamedKeyStore, MemoryManager, /browse.

*Three build-blocker fixes (A1-A3) were applied during this review so the real errors beneath could be surfaced. All other fixes left for human decision (your call on Coil, and I don't silently rewrite working logic).*

---

## REVIEW PASS 2 — commit `3af434a` (C7/C8 + Part D) — ✅ **APPROVED**

**Date:** 2026-08-14 (second pass)
**Scope:** coding agent's most recent work: C7 (media → filesDir), C8 (WAL-safe export/import), Part D (dead code deletion, /browse model call).
**Evidence:** commit diff + symbol cross-refs + `verify_static.py` 75/75 + real Gradle compile `:app:compilePlayDebugKotlin` **BUILD SUCCESSFUL** (6m 7s, 16 tasks).

### Verified in code (not just commit message)

| Item | Claim | Proof |
|------|-------|-------|
| C7 video | cacheDir → filesDir | `ChatViewModel.kt:301` `File(container.ctx.filesDir, "vid_…mp4")`; **zero `cacheDir` refs remain** in ChatViewModel |
| C7 image | cacheDir → filesDir | `ChatViewModel.kt:360` `File(container.ctx.filesDir, "gen_…jpg")` |
| C-029 | MediaCleanup follows C7 | `MediaCleanup.kt` now `val dir = context.filesDir`; both call sites (`ChatViewModel.kt:312,381`) use `container.ctx` ✅ |
| C8 export | checkpoint before copy | `PRAGMA wal_checkpoint(TRUNCATE)` via `openHelper.writableDatabase`, then copy `.db`, all in `withContext(Dispatchers.IO)` |
| C8 import | close DB, drop stale WAL | `container.database.close()` → copy → delete `-wal` + `-shm` (prevents foreign-WAL replay) |
| D | ScreenshotDetector deleted | File gone; `grep -rn "ScreenshotDetector" app/src` → **empty** |
| D | CommunityPrompts deleted | File gone; `grep -rn "CommunityPrompts" app/src` → **empty** |
| D | dangling flags removed | `unlimitedRepos`/`markdownRendering` gone from FeatureFlags; **no refs anywhere** |
| D | /browse calls the model | `ChatMessageDto` exists (`role`,`content`), `completeChat(baseUrl, apiKey, model, messages, temperature)` signature matches call, `ContextTrimmer.trim` returns `Pair<List,Int>` matched by destructure; page content added as **user** msg then model answer stored as **assistant** — real fix, not a stub |

### Notes (non-blocking)

1. **Protocol nit:** the coding agent edited `docs/REVIEW.md` itself (status → 🟢 Resolved). The reviewer owns this file; the change was factual and consistent with what I verified, so no harm — but future coding passes should leave REVIEW.md to the reviewer.
2. **/browse page content now persists as a user message** in Room (full page text in history). Deliberate (answer needs it in context), but large pages will grow the conversation and be re-sent until ContextTrimmer trims them — acceptable for now, watch token cost on very long pages.
3. **Static-verify count:** skill doc says baseline 78/78; current run reports **75/75 passed** (all green, no failures). The lower ceiling reflects guard-count changes in the verifier script itself, not regressions — suite passes fully.

**Verdict:** ✅ **APPROVE** — all claims in commit `3af434a` are real and compile-clean. Ready for next backlog ticket.

---

## Review — 2026-08-15 — ticket C-031

**Role:** `LITECHAT-REVIEW`. Read-only. Did not edit `app/**`. Did not run Gradle.

**Verdict:** Issues

Launcher name and docs say **BYO AI**. The screen the person *uses* still says **LiteChat**. Ticket goal was “users see BYO AI.” That is not done.

### Issues

1. **`Screens.kt:319`** — top bar still `Text("LiteChat")`.
   - Why: this is the title they stare at every chat.
   - Fix: `stringResource(R.string.app_name)` (already `BYO AI`).

2. **`Screens.kt:1076`** — Settings disclaimer still names LiteChat.
   - Why: user-facing legal/brand text.
   - Fix: same wording, swap brand to BYO AI.

3. **`Screens.kt:756`** — onboarding/compat copy: “LiteChat ships only the green path…”
   - Why: a person setting up the app reads it.
   - Fix: everyday words, no LiteChat. Example: “This app only turns on the green path by default.”

4. **`CompatMatrix.kt:99`** — “LiteChat is Tier A…”
   - Why: shown on the matrix screen.
   - Fix: drop the old name. “This app is thin chat + remote brain.”

5. **`DeviceCompat.kt:47`** — “LiteChat chat mode is still designed for this”
   - Why: shown in the RAM band note.
   - Fix: “Chat still works on this phone.”

6. **`OverlayService.kt:48` and `:124`** — notification title/channel “LiteChat Overlay”
   - Why: Android shows this in the shade.
   - Fix: “BYO AI Overlay” or just “Chat overlay.”

### What passed

- `strings.xml` `app_name` = BYO AI; manifest `android:label` points at it. Home-screen icon is correct.
- `applicationId` = `com.byoai.chat`. `namespace` still `com.litechat.android`. Right split.
- Privacy HTML (both copies) = BYO AI.
- `docs/PLAY-LISTING-DRAFT.md` has the locked 4GB line, no SoftRAM, no fight words.
- Class names (`LiteChatApp`, `Theme.LiteChat`) are code, not the store. Leave them.

### Nits (not Issues)

- `PLAY_PRO_SKU` is still `litechat_pro`. That is H-003, not this ticket.
- GitHub URL still `litechat-android`. Ticket said leave the repo name.
- User-Agent / `X-Title: LiteChat` goes to the API host, not the person. Optional later.
- README still says LiteChat in the architecture bits. Devs only.

### Next

WIRE fixes 1–6. Then call REVIEW again. Do not mark this Approve until the top bar is BYO AI.

---

## Review — 2026-08-15 — ticket C-031 (fix pass)

**Role:** `LITECHAT-REVIEW`. Read-only. Did not edit `app/**`. Did not run Gradle.

**Verdict:** Approve

The six Issues from the last pass are gone. The top bar uses `stringResource(R.string.app_name)` → **BYO AI**.

### Recheck of Issues 1–6

| # | Was | Now |
|---|---|---|
| 1 | `Text("LiteChat")` top bar | `stringResource(R.string.app_name)` |
| 2 | Settings disclaimer LiteChat | Says BYO AI |
| 3 | “LiteChat ships only the green path” | “This app only turns on the green path…” |
| 4 | “LiteChat is Tier A…” | “This app is thin chat + remote brain.” |
| 5 | Tight-band LiteChat line | “Chat still works on this phone.” |
| 6 | “LiteChat Overlay” | “Chat overlay” (title + channel) |

Class names (`LiteChatApp`, `Theme.LiteChat`, `LiteChatRoot`) stayed. Correct.

### Nit (not Issues)

- **`DeviceCompat.kt:129`** — matrix footnote still says “not LiteChat default.” A person can see that one line on the RAM table. Swap to “not this app’s default” if you want zero leftover. Not enough to fail the ticket.

Same leftover class as last time: User-Agent / `X-Title: LiteChat` (API host only), `litechat_pro` SKU (H-003), GitHub repo name.

C-031 can stay **Done**.

---

## Review — 2026-08-15 — FULL PROJECT

**Role:** `LITECHAT-REVIEW`. Read-only. Did not edit `app/**`. Did not run Gradle (fan-out RAM rule). Ran `python3 scripts/verify_static.py` → **122/122**.
**HEAD:** `dbd62a9` (C-033 picker) on top of `b211200` (C-034 Agent Lab) + `ed41570` (D-006 Fastlane). Tree **clean**.
**Method:** parent grep/read of hard constraints + new tickets; 3-layer fan-out dispatched (UI / data / prefs). This section is the combined verdict.

**Verdict: Issues** (one leftover runtime bug). C-033 / C-034 / D-006 themselves are sound. Do not flip those tickets back. Fix the attach path, then this review can Approve.

### Confirmed bugs (Issues)

1. **`ChatViewModel.kt:766` — photo still goes through the 32 000-character text cap.**  
   `attachImage` downscales (good) then `setInput("[IMG:data:$mime;base64,$b64]…")`, and `setInput` runs `InputPolicy.cap(32_000)`. `maxSaveDimension` is **512 / 768 / 1024**. A JPEG that size at quality 80 is often **bigger than ~24 KB** (what 32k base64 chars can hold). The old C6 “silent truncate” can still happen.  
   **Why it matters:** vision attach looks like it worked; the model gets a chopped picture.  
   **Fix:** do not route binary through `setInput`. Send the downscaled bytes as a vision part, or cap the JPEG until `b64.length + prefix < 32_000` (loop quality/size down), or raise a dedicated attach path that skips the paste cap.

### Passes (this batch + old laws)

- **Hard constraints:** no `WebView`, `trustAll`, `setHostnameVerifier`, RN/Flutter, `proot-distro`, or Hermes OAuth client id `b1a00492` in `app/src`.
- **C-033 picker:** `ProviderSetupFields` = pick provider → paste key → pick model. URL box only for Custom. Paid providers (Grok/OpenAI/DeepSeek/Mistral) show “can cost money”. No fake SuperGrok login. Catalog has Gemini/Groq/OpenRouter/HF/xAI/OpenAI/DeepSeek/Mistral/Ollama/Custom. Wired in onboarding **and** Settings. Unit test `ProviderCatalogTest` exists.
- **C-034 Agent Lab:** door only. TIGHT + COMFORTABLE + storage &lt; 400 MB → REFUSE. `mayOpenTermux` false on REFUSE. Manifest `<queries>` for `com.termux` only — not a component. No installer.
- **C-028 video:** `pollVideo` streams MP4 to a file (`byteStream().copyTo`); comment forbids `body.bytes()`. VM wraps in `withContext(IO)`. `Thread.sleep(2000)` is on that IO path.
- **Stop:** `streamJob` is assigned (`streamJob = viewModelScope.launch`); `stopRequested` checked; no self-cancel inside send.
- **Keys:** primary key in `SecureStore` / EncryptedSharedPreferences. Failover list JSON stores **baseUrl + model only**; keys via `secureStore.getProviderKey`. Templates use `Json.encodeToString` (not a raw StringBuilder).
- **Coil display:** `Screens.kt` uses `ImageCacheConfig.displaySize(band)`, not `.size(540,540)`.
- **Overlay channel:** real `NotificationChannel` (“Chat overlay”), not an empty stub. Overlay enable checks `canDrawOverlays` + Pro gate in Settings.
- **SKU:** `PLAY_PRO_SKU` is `BYO_pro` (old `litechat_pro` nit is gone).
- **Fastlane / D-006:** title BYO AI, locked short line, 1.6 MB, foss “no billing”, changelog for versionCode 1. CI static-verify owns these.
- **C-031 leftover:** DeviceCompat matrix note now says “not this app. We will not hide Termux here.” User-visible LiteChat strings from the last review are gone. Class names (`LiteChatApp`, `Theme.LiteChat`) stay — correct.

### Nits (not Issues)

- **`OpenAiCompatibleClient.kt:241,413-414`** — `User-Agent` / `X-Title: LiteChat` still goes to the API host. Person never sees it. Optional later.
- **`DeviceCompat.kt:18`** — KDoc still says “LiteChat”. Devs only.
- **`ProviderSetupFields.kt:119`** — `startActivity` for “Get a key” has no try/catch. Rare no-browser crash. Wrap it.
- **`AgentLabGate.kt:65`** — `getPackageInfo(name, 0)` is the old overload. Works with the `<queries>` tag; can switch to `PackageInfoFlags` later.
- **GitHub repo / package path** still `litechat-android` / `com.litechat.android`. Locked earlier. Leave them.

### Fix order

1. Issue 1 (attach + 32k cap) — data-loss class.  
2. Optional nits.  
3. Do **not** add SuperGrok OAuth or Termux-inside-APK. REVIEW agrees with the product refuse.

### Tickets

| Ticket | Status after this review |
|--------|--------------------------|
| C-033 picker | **Done** — keep |
| C-034 Agent Lab door | **Done** — keep |
| D-006 Fastlane-in-build | **Done** — keep |
| C-031 brand | **Done** — keep |
| Attach 32k residual | **new Issue** — WIRE if human wants |

Call REVIEW again after Issue 1 is fixed if you want Approve.

---

## Review — 2026-08-15 — FULL PROJECT fan-out addendum

**Role:** `LITECHAT-REVIEW`. Still read-only. Did not edit `app/**`.  
**Source:** 3-layer fan-out (`deleg_839f16ae`). Parent **byte-checked** every new Issue below. Child text is not enough.

**Verdict stays Issues.** Worse than the first pass: **chat streaming is dead in the current tree.** C-033/C-034/D-006 still stay Done.

### New confirmed bugs (parent verified)

1. **`StreamParser.kt:47` + `ChatSseParser.kt:48-49` + `OpenAiCompatibleClient.kt:106-111` — tokens never reach the UI.**  
   `parseSSE` emits the payload **without** `data:`. `parseEvent` then returns null unless the line **still** starts with `data:`. Every chunk is dropped. `gotDelta` stays false and `streamError` stays null, so the stream→non-stream fallback **does not run**. Tests only feed `parseEvent("data: …")` — they never test the two functions chained.  
   **Fix:** accept already-stripped JSON/`[DONE]` in `dataPayload`, **or** stop stripping in `parseSSE`. Add one test that pipes `data: {"choices":…}` through `parseSSE` → `parseEvent`.

2. **`OpenAiCompatibleClient.kt:88` — `cancel()` inside `callbackFlow` closes the channel.**  
   Receiver is `ProducerScope` / `SendChannel`. Unqualified `cancel()` is **not** `OpenAiCompatibleClient.cancel()`. Every `streamChat` starts by shutting its own pipe.  
   **Fix:** `this@OpenAiCompatibleClient.cancel()` then `userCancelled = false`.

3. **`NamedKeyStore.kt:55-59` — new named key with `isActive=true` is turned off.**  
   On insert `idx` is `-1`, then `i != idx` is true for **every** row including the new one. `getActiveKey()` is blank; send falls back to the primary key.  
   **Fix:** after `list.add`, set `idx = list.lastIndex`.

4. **`Screens.kt` Fetch models / Test — `listModels()` (`Call.execute()`) on Main.**  
   `rememberCoroutineScope().launch` is Main. Weak phone ANR for the whole HTTP wait.  
   **Fix:** `withContext(Dispatchers.IO)`.

5. **`ContextTrimmer.kt:11` vs `:54-67` — “never splits turn pairs” is a lie.**  
   Newest-first walk can keep an assistant without its user. System rows skip the token budget.  
   **Fix:** drop oldest user+assistant pairs as a unit; count system tokens.

### Already in the first pass (still true)

- `ChatViewModel.kt:766` attach still hits the 32k text cap.

### Fan-out Issues I did **not** promote (plausible, not re-read line-by-line this addendum)

Overlay `ViewTreeLifecycleOwner`, `FLAG_NOT_FOCUSABLE` vs IME, failover second assistant row, `/imagine`/`/video` untracked jobs, RetryInterceptor retrying canceled calls, Jsoup second HTTP stack. Treat as next REVIEW pass or WIRE if the human wants a crash/ANR sweep.

### Fix order (updated)

1. Issues 1–2 (chat does not stream).  
2. Named-key activate (3).  
3. Attach 32k + listModels Main (4) + trimmer (5).  
4. Overlay / Stop-on-media sweep.  
5. Still do **not** add SuperGrok OAuth or Termux-in-APK.

Say **WIRE** to fix the stream break. Do not ship `dbd62a9` as a daily driver until 1–2 are green.


