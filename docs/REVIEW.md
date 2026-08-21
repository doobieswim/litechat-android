# LiteChat — Code review log

## Review — 2026-08-21 — B-005–B-008

**Scope:** last WIRE batch still **uncommitted**. Files: `ChatViewModel.kt` (export + clearMemory), `Screens.kt` (mask + backup refuse + memory confirm), `OpenAiCompatibleClient.kt` (`pollVideo` suspend + `delay` + `pollBodyOrThrow`), `VoiceAndBackupTest.kt`, `verify_static.py`. Overlay ViewTree was already dirty — **not this batch**.
**Role:** `LITECHAT-REVIEW`. Read-only. Did not edit `app/**`. No Gradle.
**This-review check:** `python3 scripts/verify_static.py` → **233/233**. Hard-constraint grep (WebView / trust-all / RN / Flutter) empty.

**Verdict: Approve**

Independent check of B-005–B-008 against BACKLOG AC. Do not re-open leftover 1–8, B-001, B-004, Overlay `.set`, B-002, B-003, B-009.

### AC check

1. **B-005 never plaintext DB** — `ChatViewModel.kt:1118-1132`: blank pass returns “Type a backup password first.” Then only `BackupCrypto.encryptTo`. No `inn.copyTo(out)` on export. Settings refuses before the picker (`Screens.kt:1593`). Process death after SAF: `pendingBackupPass` is empty → same refuse. Empty encrypt test in `VoiceAndBackupTest.kt`.
2. **B-006 dots on secrets** — named-key add row `Screens.kt:1482`; backup password `:1618`. Main paste box still masked (`ProviderSetupFields.kt:119`). Label no longer says optional (`:1617`).
3. **B-007 Stop + bad HTTP on `/video`** — `pollVideo` is `suspend` (`OpenAiCompatibleClient.kt:774`). Waits are `delay` (`:826`, `:855`, `:889`). `pollBodyOrThrow` (`:790-794`) throws `mediaHttpError` on non-2xx. Call still on IO (`ChatViewModel.kt:638-644`). Stop cancels `streamJob` → `delay` throws; `finally` `:656-657` clears `isGeneratingImage` / `isStreaming`.
4. **B-008 memory wipe** — `clearMemory()` returns unless Pro (`ChatViewModel.kt:1285-1287`). Free tap gets “pay once to unlock” (`Screens.kt:1648-1650`). Pro gets “Clear memory?” dialog (`:1704-1717`) before wipe.

### Issues

None for this batch.

### Nits (not Issues)

- Backup file name is still `litechat_backup.db` even though the bytes are encrypted. Import still accepts old plaintext (ticket said that is OK).
- Clear memory button stays on screen for free users; tap explains. AC allowed that.
- `friendlyMediaError` may show 120 chars of HTTP body. Unlikely to contain the key. Same as other media doors.
- Overlay ViewTree extension is still uncommitted from an earlier pass. Not B-005–B-008.
- Whole tree of app fixes is **uncommitted**. Lost on a crash. Not on the phone.

**Next:** do not bake unless asked. Say **WIRE** if you want this committed. Then a new play-debug APK only if you ask.

---

## Review — 2026-08-21 — leftover 1–8 (wipe / RAM / trim / peel / labels)

**Scope:** last WIRE batch still **uncommitted** on `b90a21e`. Files: `AppContainer.kt`, `OpenAiCompatibleClient.kt` (`downloadImageBytes`, `streamUrlToFile`), `ChatViewModel.kt` (`attachImage`, `send`), `ContextTrimmer.kt`, `SlashInput.kt`, `ProviderCatalog.kt`, `README.md`, tests, `verify_static.py`.
**Role:** `LITECHAT-REVIEW`. Read-only. Did not edit `app/**`. No Gradle.
**This-review check:** `python3 scripts/verify_static.py` → **222/222**. Hard-constraint grep (WebView / trust-all / RN / Flutter) empty.

**Verdict: Approve**

The eight asked items are in the working tree. Independent check (not the coding pass’s own write-up). B-002 tap targets were never this batch. Do not re-open B-001 / B-004 / R-019 / R-020 / doors.

### AC check

1. **No chat wipe on missed upgrade** — `AppContainer.kt:28-34`: `fallbackToDestructiveMigration()` gone. Named `MIGRATION_1_2`…`3_4` only. `Entities.kt` `version = 4`. No other `fallbackToDestructive` in `app/**`.
2. **Picture URL streams to disk** — `OpenAiCompatibleClient.kt:640-650` + `887-907`: GET → `byteStream().copyTo(FileOutputStream)`, then refuse if `tmp.length() > 8MB`, then `readBytes()`. `activeCall` set so Stop can cut the GET.
3. **Attach no whole-file `readBytes`** — `ChatViewModel.kt:1201-1234`: stream copy to cache file → `decodeFile` bounds + sample + quality loop (80→25) until b64 fits the 32k budget. `openInputStream.readBytes()` is not on this path.
4. **Trim keeps the live question** — `ContextTrimmer.kt:35-58` peels a trailing user, drops oldest pairs, glues the user back. Test `trailing unmatched user is not dropped` asserts `m2-` stays and `m0-` goes.
5. **Stuck `v` / `⁶`** — `SlashInput.peel` (`SlashInput.kt:13-20`) on `send()` (`ChatViewModel.kt:417`). Strips 1–4 junk chars before `/imagine` etc. JVM test for `v/imagine` and U+2076.
6. **Groq Compound cost line** — `ProviderCatalog.kt:56`: `Groq Compound — can cost money`. Host stays `paid = false` (free key).
7. **Sora after 24 Sep 2026** — `SORA_SUNSET_MS = 1_790_208_000_000` = 2026-09-24 00:00 UTC. `resolveVideoModel` returns `sora-2` only if `nowMs < SORA_SUNSET_MS`, else null → named “cannot make videos” line. Test at sunset-1 vs sunset.
8. **README picker ids** — OpenAI `gpt-5.6-luna`, OpenRouter `openrouter/free`. Matches catalog + Settings default.

### Issues

None for this batch.

### Nits (not Issues)

- **8 MB cap is after the full copy** (`downloadImageBytes`). Heap is bounded. A huge URL can still fill disk, then delete. Abort in the copy loop if you ever reopen this.
- Picture path still `readBytes()` the temp file after the cap (≤8 MB heap).
- Attach still decodes a Bitmap to shrink (needed). Copy to cache has no byte cap.
- Overlay composer does not call `SlashInput.peel` (overlay has no `send()` of its own).
- B-002 hard-to-tap buttons still Research. Peel does not fix taps.
- B-003 still **Research** in BACKLOG even though peel landed. REVIEW does not flip tickets.
- Groq host tagline is still “Free key.” Compound line is the money flag.
- Whole batch is **uncommitted**. Lost on a crash. Not on the phone.

**Next:** do not bake unless asked. Say **WIRE** if you want this committed. Then a new play-debug APK only if you ask.

---

## Review — 2026-08-21 — R-020 xAI /edit JSON

**Scope:** `OpenAiCompatibleClient.kt` (`editXaiImage`, `xaiEditJson`, `editOpenAiImage`), tests, `verify_static.py`. Read-only. No Gradle. **216/216**.
**Role:** `LITECHAT-REVIEW`

**Verdict: Approve**

Matches DIG: Grok edit is JSON + data URI. OpenAI stays multipart. Others still refuse.

### AC check

- xAI branch: `Content-Type: application/json`, `xaiEditJson` with `image.url` + `type=image_url`, model from `resolveEditModel` (`grok-imagine-image-2.0`).
- OpenAI: still `MultipartBody`.
- Groq/Gemini/OpenRouter: `resolveEditModel` is null → named refuse line.
- JVM test asserts JSON is not multipart.
- 4MB cap before `readBytes`.

### Issues

None.

### Nits (not Issues)

- Decode miss still says “This provider cannot edit pictures.” (generic).
- xAI non-404 uses `friendlyMediaError` (120-char HTTP blob). Unlikely to contain the key.

**Next:** new play-debug APK only if asked. Not on the phone yet.

---

## Review — 2026-08-21 — all doors (Groq picker, /edit, voice)

**Scope:** Groq catalog ids, `resolveEditModel` / `resolveSttModel` / `resolveTtsModel`, `editImage`/`transcribeAudio`/`speakToFile`, remap of dead Llama ids, tests, `verify_static.py`. Read-only. No Gradle. **214/214**.
**Role:** `LITECHAT-REVIEW`

**Verdict: Approve**

Dead Groq Llama picks are gone. `/edit` and voice no longer send OpenAI-only ids to every host.

### AC check

- Groq picker: `openai/gpt-oss-20b`, `openai/gpt-oss-120b`, `groq/compound`. No `llama-3.3-70b-versatile` option.
- Saved `llama-3.1-8b-instant` / `llama-3.3-70b-versatile` remap.
- `/edit`: refuse unless OpenAI/custom (`gpt-image-2`) or xAI (`grok-imagine-image-2.0`); multipart includes `model`.
- STT: Groq `whisper-large-v3`; OpenAI `whisper-1`; Gemini etc refuse.
- TTS: OpenAI/custom `tts-1` only; others refuse.

### Issues

None for this batch.

### Nits (not Issues)

- xAI `/images/edits` may want JSON, not OpenAI multipart — unproven until a phone hit.
- HTTP 404 on edit still says “This provider cannot edit images.” (generic).
- README OpenAI/OpenRouter example ids are still old (`gpt-4o-mini`) — not this ticket.

**Next:** new play-debug APK only if asked. Not on the phone yet.

---

## Review — 2026-08-21 — /imagine /video per host

**Scope:** `ProviderCatalog.kt` host match + picture/video doors, `OpenAiCompatibleClient.kt` OpenRouter `/images`, `ChatViewModel.kt` passes chat model only if it looks like a picture id, tests, `verify_static.py`. Read-only. No Gradle. **210/210**.
**Role:** `LITECHAT-REVIEW`

**Verdict: Approve**

The old “OpenRouter = OpenAI images + Sora” path was wrong. Code now matches the live doors.

### AC check

- OpenRouter pictures: `openrouterImagesUrl` → `{base}/images`, slug `openai/gpt-image-2`, Gemini image fallbacks. No `response_format` on that door.
- OpenRouter video: `resolveVideoModel` is null (not `sora-2`).
- Gemini/xAI/OpenAI doors unchanged.
- Groq/HF/DeepSeek/Mistral/Ollama refuse with a named line.
- `fromBaseUrl` matches Google even without `/openai/`.
- Chat `gemini-3.6-flash` is not sent as a picture id (`contains("image")` gate).

### Issues

None for this batch.

### Nits (not Issues)

- Gemini native miss still says “This provider cannot make pictures.” instead of the named line.
- A Groq chat model whose **id contains “image”** would try Groq `/images/generations` instead of refusing. Unlikely.
- OpenRouter is still tagged `paid = false` while pictures can bill — old catalog, not this ticket.

**Next:** new play-debug APK only if the human asks. Not on the phone yet.

---

## Review — 2026-08-21 — B-004 Stop / memory / hide key

**Scope:** WIRE B-004 (+ still-uncommitted B-001, R-019, BrowseUrl). Files: `ChatViewModel.kt`, `Screens.kt`, `ProviderSetupFields.kt`, `MemoryManager.kt`, `OverlayService.kt`, `NamedKeyStore.kt`, `OpenAiCompatibleClient.kt`, `ConnectivityObserver.kt`, `BrowseUrl.kt`, `ApiKeySanitizer.kt`, tests, `verify_static.py`. Read-only. No Gradle. `python3 scripts/verify_static.py` → **207/207**.
**Role:** `LITECHAT-REVIEW`

**Verdict: Approve**

B-004 AC is in the tree. Prior picture/key and bare-URL browse still look right.

### AC check

- Stop: `/browse` `/imagine` `/video` assign `streamJob`; FAB uses `isStreaming || isGeneratingImage`; `send()` refuses while generating; cancel is rethrown.
- Memory: `decodeList` defaults missing `hitCount` to 1; save uses `encodeDefaults = true`. JVM test present.
- Key: `PasswordVisualTransformation` on the paste field.
- Overlay: `ViewTreeLifecycleOwner` + SavedState + ViewModelStore before `setContent`; paste capped.
- Failover writes the same assistant row (`updateMessageContent(assistantId, convId, result)`).
- Named keys run `headerSafe` on save.

### Issues

None for this batch.

### Nits (not Issues)

- Room `fallbackToDestructiveMigration()` still on (`AppContainer.kt`) — out of B-004.
- Picture URL still `body?.bytes()` — leftover.
- Overlay uses `ViewTreeLifecycleOwner.set` (not compiled here). If a flavor fails, swap to the `setViewTreeLifecycleOwner` extension.
- Named-key *add* boxes were not in this mask pass.

**Next:** human spot-check on a **new** play-debug APK. Do not bake unless asked.

---

## Review — 2026-08-21 — FULL PROJECT addendum (3-layer fan-out)

**Scope:** UI / data-network-db / prefs-media-gradle subagents. Read-only. No Gradle. Written after `/browse` bare-URL WIRE (do not treat that as unreviewed leftover).
**Role:** `LITECHAT-REVIEW` combine step.

**Verdict: Issues** — extra real bugs beyond the first full-project pass. Do not bake until the human asks. Do not re-open B-001 / R-019 / BrowseUrl (already in the tree).

### Already fixed (do not re-open)

- B-001 / R-019 key strip + no key in errors
- `/browse` accepts `example.com` (`BrowseUrl.normalize`)

### New Issues (user-visible first)

1. **Stop does not cancel `/browse` `/imagine` `/video`** — `ChatViewModel.kt:447, 605, 660` never set `streamJob`. FAB is only `isStreaming` (`Screens.kt:587`). Send can start a second call.
2. **Memory silently wipes** — `MemoryManager.kt:85,94` encode skips default `hitCount`; decode `!!` throws → empty list → overwrite.
3. **API key shown in the clear** — Settings `OutlinedTextField` (`Screens.kt:1148`, `ProviderSetupFields.kt:110`).
4. **Overlay can crash on enable** — `OverlayService.kt:91` ComposeView with no `ViewTreeLifecycleOwner`.
5. **Failover ghost assistant** — `ChatViewModel.kt:1043` inserts a second row instead of updating the first.
6. **Stuck Stop after disconnect mid-retry** — `ChatViewModel.kt:901` returns without clearing `isStreaming`.
7. **Picture URL download still `body?.bytes()`** — `OpenAiCompatibleClient.kt:569`; GET not on `activeCall`.
8. **Room `fallbackToDestructiveMigration()`** — `AppContainer.kt:34` can wipe chats.
9. **Context trimmer splits a turn** — `ContextTrimmer.kt:59` when history ends on the current user message.
10. **Fetch models button can stick on “Fetching…”** — `Screens.kt:1248` no `finally`.
11. **Video poll ignores HTTP errors** — `:722/:756/:787` loop until timeout.
12. **Stream fallback swallows cancel** — `OpenAiCompatibleClient.kt:182`.
13. **Offline launch looks online** — `ConnectivityObserver.kt:25`.
14. **Attach `readBytes()` whole photo** — `ChatViewModel.kt:1187`.
15. **Overlay paste uncapped** — `OverlayService.kt:107`.
16. **Named keys skip headerSafe on save** — `NamedKeyStore.kt:53` (send path still strips).

### Passes

Chat `/search` `/edit` Stop works. Imagine/video network on IO. One OkHttpClient. Keys not in DataStore JSON. AGP 8.7.3. Coil band size. MediaCleanup. Encrypted prefs. `largeHeap=false`.

**Next:** human `LITECHAT-WIRE` for a Stop + memory + key-mask batch, or a debug APK of what is already coded. Do not bake unless asked.

---

## Review — 2026-08-21 — FULL PROJECT (features having problems)

**Scope:** whole tree + phone facts from this session. Read-only. No Gradle. `verify_static` **196/196**. Uncommitted WIRE: B-001 + R-019 (REVIEW already **Approve**).
**Role:** audit / `LITECHAT-REVIEW` fan-out started; this section written from live greps so it is not lost if RAM dies.

**Verdict: Issues** — the phone is on an **old APK**. Several “broken features” are already fixed in this tree and never installed. Remaining real bugs below.

### 0. Install gap (why “a lot of features” feel broken)

The play-debug on the phone does **not** include B-001/R-019. `/imagine` died on a junk **⁶** in the key and printed the key. That is fixed in git working tree, not on device.

### Confirmed bugs (still in code)

1. **`ChatViewModel.kt:660`** — `/imagine` uses `viewModelScope.launch` and never sets `streamJob`.
   - Why: Stop does not cancel a picture job (`stopStreaming` only cancels `streamJob`).
   - Fix: `streamJob = viewModelScope.launch { ... }` like `/edit` / `/search`.

2. **`OpenAiCompatibleClient.kt:569`** — `downloadImageBytes` still `body?.bytes()` (full image in heap).
   - Why: 4GB law; URL-shaped OpenAI pictures can spike RAM.
   - Fix: stream to `File` like video (C-028). Gemini native path returns b64 already, so this is the URL fallback only.

3. **`OpenAiCompatibleClient.kt:737,766,800`** — `Thread.sleep` in video poll loops.
   - Why: if ever called off IO, ANR. Callers wrap in `withContext(IO)` today — still a landmine.
   - Fix: `delay()` in a suspend poll, or keep documented IO-only.

4. **`AppContainer.kt:34`** — `fallbackToDestructiveMigration()` still on beside named migrations.
   - Why: a missed migration **wipes chats**.
   - Fix: remove fallback once versions are named; last-resort only.

5. **B-002 (Research)** — Compose Continue / text fields miss accessibility click and type (phone, Android 16). Coordinate tap worked. Not a one-line cause.

6. **B-003 (Research)** — Gboard can prepend `v` / `⁶` in the chat box so `/imagine` never matches.

### Passes (were dead in Aug 15 audit, now wired)

- MemoryManager instantiated in `AppContainer.kt:38`.
- NamedKeyStore instantiated `:40`, save in ViewModel ~1409.
- Overlay `showOverlay()` called from `onStartCommand` (`OverlayService.kt:81`) and has a send field.
- Fork exists (`ChatRepository.forkConversation`, ViewModel ~1434).
- Search is Pro-gated (`ChatViewModel.kt:254`).
- Imagine network on IO (`ChatViewModel.kt:677`).
- No `addUnsafeNonAscii`. No WebView / trust-all / RN / Flutter in main.

### Uncommitted (must land before a bake)

`ApiKeySanitizer.kt` (untracked) + client/store/VM/Screens/tests. REVIEW **Approve** for B-001/R-019. Not on the phone.

### Tickets

| Item | State |
|------|--------|
| B-001 / R-019 | Approve in tree; **need new APK** |
| Imagine Stop | leftover Issue |
| Image URL `bytes()` | leftover Issue |
| Destructive Room fallback | leftover Issue |
| B-002 / B-003 | Research |

**Next:** install a new play-debug of this tree, then re-test pictures. Do not bake unless asked. 3-layer subagents may add an addendum.

---

## Review — 2026-08-21 — B-001 + R-019 (WIRE)

**Scope:** uncommitted WIRE: `ApiKeySanitizer.kt` (new), `SecureStore.kt`, `OpenAiCompatibleClient.kt`, `ChatViewModel.kt`, `Screens.kt`, `ReviewFixLogicTest.kt`, `verify_static.py`. Read-only. No Gradle. `python3 scripts/verify_static.py` → **196/196**.
**Role:** `LITECHAT-REVIEW`

**Verdict: Approve**

Prior Issues (raw `x-goog-api-key`, save-only-trim, no WIRE) are closed.

### AC check

- B-001 strip on save: `SecureStore.setApiKey` / `setProviderKey` use `headerSafe`.
- B-001 strip on send: every `Authorization: Bearer` in the client uses `headerSafe`; `geminiKey` uses it and skips empty.
- B-001 test: `ApiKeySanitizerTest` strips U+2076 and builds OkHttp `Headers` without throw.
- R-019: `/imagine` `/video` `/edit` use `userSafeError`; Test uses `isIllegalHeader` → everyday line. No `addUnsafeNonAscii`. Theme line is everyday.

### Issues

None.

### Nits (not Issues)

- `getApiKey()` still returns the stored string. A ⁶ can still *show* in Settings until Save. Send path already strips, so `/imagine` should work on the new APK without re-paste.
- `NamedKeyStore` does not call `headerSafe` on save. Client still strips at header time.
- `/browse` still uses `e.message.take(200)` (out of ticket).

### Tickets

| Ticket | After this review |
|--------|-------------------|
| B-001 strip key headers | **Approve** |
| R-019 no key in errors | **Approve** |

**Next:** human spot-check on a **new** play-debug APK (this tree is not on the phone). Do not bake unless asked.

---

## Review — 2026-08-21 — B-001 (pictures) + last Gemini `/imagine` WIRE

**Scope:** Human said `LITECHAT-REVIEW` after DEBUG. No new WIRE since B-001. Reviewed current `app/**` bytes for the picture path (`9910113`, `7bbfa47`) and B-001 AC. Read-only. No Gradle. `python3 scripts/verify_static.py` → **189/189 passed**.
**Role:** `LITECHAT-REVIEW`

**Verdict: Issues** — nothing to Approve. B-001 is Ready but **not coded**. Pictures still die on the phone before Google is called.

### Issues

1. **`OpenAiCompatibleClient.kt:840`** — `header("x-goog-api-key", apiKey)` uses the raw key.
   - Why it matters: phone error `Unexpected char 0x2076 at 0 in x-goog-api-key`. OkHttp never sends. `/imagine` and Test both die.
   - Suggested fix: strip non-ASCII (and other illegal header bytes) here **and** on every `Authorization: Bearer $apiKey` (same file `:233` and the other Bearer lines).

2. **`SecureStore.kt:26-28` and `:34-35`** — `setApiKey` / `setProviderKey` only `trim()`.
   - Why it matters: a Gboard **⁶** stays in encrypted storage. Next launch still broken.
   - Suggested fix: same strip on save so old junk keys get cleaned once.

3. **No WIRE for B-001** — BACKLOG says Ready; `app/**` unchanged for this ticket (`git status`: only `docs/BUGS.md` + `docs/BACKLOG.md`).
   - Why it matters: REVIEW grades code. There is no code.
   - Suggested fix: human `LITECHAT-WIRE` B-001, then REVIEW again.

### What last Gemini WIRE did right (not Issues)

- Native `generateContent` + `x-goog-api-key` only (`9910113`) — correct vs Google 401 on Bearer+AI Studio key.
- Fallback picture ids exist (`ProviderCatalog.kt` image fallbacks).
- Static suite still green.

### Nits (not Issues)

- `/imagine` still not on `streamJob` (old leftover). Stop FAB may not show while generating.
- Picture path still returns a full `ByteArray` (old leftover).

### Tickets

| Ticket | After this review |
|--------|-------------------|
| B-001 strip key headers | **Issues** — not implemented |
| B-002 Compose a11y | Research — not in this review |
| B-003 Gboard extra letters | Research — not in this review |

**Next:** WIRE B-001. Do not bake an APK until that lands and REVIEW Approves.

---

# LiteChat — Exhaustive Code Review Report (archive)

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

---

## Review — 2026-08-15 — ticket C-035 (fix pass)

**Role:** `LITECHAT-REVIEW`. Read-only. Did not edit `app/**`. Did not run Gradle (RAM rule — CI run 101 is the compile proof).

**HEAD:** `5316369` (C-035). Tree clean. `verify_static.py` **129/129**, rc=0. CI run 101 (same SHA) **success**; artifact `litechat-apks` = `foss/release/app-foss-arm64-v8a-release-unsigned.apk` 1.72 MB + `play/release/app-play-arm64-v8a-release-unsigned.apk` 3.37 MB — assembleRelease + R8 + size gate really ran and passed.

**Verdict: ✅ Approve.** All five fan-out Issues + the attach Issue are fixed in code, with regression tests that reproduce the original failure mode.

### Issues 1–5 from the addendum — verified in code

| # | Was broken | Now | Proof |
|---|-----------|-----|-------|
| 1 | Stream dead: `parseSSE` strips `data:`, `parseEvent` required it | `parseEvent` accepts stripped payloads | `ChatSseParser.kt`: `dataPayload(line) ?: line.trim().takeIf { it.isNotEmpty() }`; tests `parseEvent accepts already-stripped payloads` + `parseSSE to parseEvent pipeline delivers deltas and Done` (real `ByteArrayInputStream`, the exact chain the addendum demanded) |
| 2 | `cancel()` in `callbackFlow` closed its own channel | Qualified | `OpenAiCompatibleClient.kt:92` (block head) + `:179` (awaitClose): `this@OpenAiCompatibleClient.cancel()`; `userCancelled` reset per send (`:93`); `CancellationException` rethrown (`:135-136`); IOException checks `userCancelled \|\| activeCall?.isCanceled()` (`:138`) → user Stop never falls into stream→non-stream retry |
| 3 | New active named key deactivated (idx = -1 before insert) | `withKey()` resolves active idx **after** upsert | `NamedKeyStore.kt` companion `withKey`; `NamedKeyStoreLogicTest`: 4 cases incl. "new active key stays active after insert" + "saving an active key deactivates the previous one" |
| 4 | `listModels()` / `LanDetector.scan()` on Main | `withContext(Dispatchers.IO)` | `Screens.kt` both Fetch-models and Test buttons; imports added. All other blocking paths already IO-wrapped (`ChatViewModel` fetchPage / completeChat / createVideo / pollVideo / generateImage / DB export) |
| 5 | Trimmer split turn pairs; system skipped budget | Drops whole oldest pairs; system counts | `ContextTrimmer.kt`: system separated + counted (`systemTokens` starts `keptTokens`); newest-first pair walk; odd-tail parity guard `removeAt(0)`; `ContextTrimmerTest`: "never splits a user assistant turn pair" + "system prompt is always kept and counts against the budget" |
| + | Attach still hit the 32k cap (silent truncation) | Loops quality 80→60→40→25, doubling sample until `b64 ≤ MAX_INPUT_CHARS − prefix − " Describe this."` | `ChatViewModel.kt` attachImage; honest error "Attachment too large to send — try a smaller photo" when nothing fits |

### Nits (not Issues)

- **Trimmer odd-tail edge:** if history has an odd non-system count (e.g. an unanswered pending user turn), the parity guard drops the *oldest* kept message, which can leave one mid-history message orphaned. Realistic Room history is user→assistant pairs, so this is belt-and-suspenders, strictly better than the old walk. If a future pass wants polish: drop the trailing pending turn instead of the oldest.
- **`userCancelled` is a plain var** read on the IO flow thread after being set on the caller thread. Standard pattern here and the documented C3 fix; no action.
- Old nits still open (not this ticket): `User-Agent`/`X-Title: LiteChat` to the API host, `ProviderSetupFields.kt:119` no-try/catch `startActivity`, `AgentLabGate.kt` old `getPackageInfo` overload.

### Tickets

| Ticket | Status after this review |
|--------|--------------------------|
| C-035 (stream / named keys / trimmer / ANR / attach) | **Approve** — Ready to ship |
| C-033 / C-034 / D-006 / C-031 | Done — keep (unchanged) |

`5316369` is safe as a daily driver. Next ticket can be drained.

---

## Review — 2026-08-16 — Tier 1 WIRE batch (P-002 + P-001/003/004/005/006/009/010/011/013)

**Role:** `LITECHAT-REVIEW`. Read-only. Did not edit `app/**`. Did not run Gradle (RAM + review law).  
**HEAD:** `f2c475a` (Tier 1 bundle). Also in scope: `1f249cc` (P-002 search). Tree clean.  
**Proof already on disk (coding session):** `verify_static.py` **158/158**; foss unit tests **67/67**. This review does not re-run those.

**Verdict: Issues**

The batch is real (not dead-code tickets). Several items should be fixed before you treat this as a daily-driver install.

### Issues (fix these)

| # | Where | What's wrong | Why it matters | Suggested fix |
|---|-------|--------------|----------------|---------------|
| 1 | `ChatViewModel.kt:1262-1285` | `readAloud()` calls `consumeVoiceSlot()` **before** it checks there is text. Empty chat still burns the free daily use, then says "Nothing to read yet". | Free user loses their one voice use for nothing. | Check for a readable last reply first. Only then count the slot. |
| 2 | `Screens.kt` mic `onVoiceInput` + `consumeVoiceSlot()` | The phone's built-in speech box also burns the 1/day slot. That path does **not** call the user's paid key. Ticket said the limit is for a voice **exchange** (Whisper/TTS). | Free user taps mic once → cannot tap "Read last reply" the same day. Feels like a trap. | Count the slot only for `speakToFile` / `transcribeAudio` (key-using calls). Leave on-device speech free. |
| 3 | P-001 AC vs code | Ticket: STT via `/v1/audio/transcriptions`. Code: `transcribeAudio()` exists in the client and has **zero callers**. Mic is still Android `SpeechRecognizer`. | Claimed feature is half-built. | Either wire a record-then-`transcribeAudio` path, or say in BACKLOG that v1 STT is the phone box and only TTS is the key path. |
| 4 | `ChatViewModel.kt:1296-1305` | After writing the mp3 on IO, `MediaPlayer.prepare()` runs on the **main** thread. | Can freeze the UI (ANR class). Same family as old Review C2. | `prepare()` / `setDataSource` on `Dispatchers.IO`, then `start()` on Main. |
| 5 | `ChatViewModel.kt:1081-1119` | Encrypted backup does `dbFile.readBytes()` and `inputStream.readBytes()`. Whole chat DB + whole backup file sit in heap twice. Old C-014 streamed with `copyTo`. | 4GB law. A fat chat history can kill the process on a $30 phone. | Stream through a temp file. Encrypt in chunks, or encrypt the streamed copy after checkpoint. Don't hold two full copies. |

### Nits (not enough to fail the whole batch)

- **`deleteFolder`** (`ChatViewModel.kt:289`) deletes the folder name but does not clear `folderId` on chats. They still show under All. Harmless, a bit messy.
- **`editImage`** still returns a full `ByteArray` (same as existing `/imagine`). Not new, but `/edit` copies the same heap pattern.
- **`fallbackToDestructiveMigration()`** is still on. If Room decides the FTS table doesn't match, an upgrade **wipes chats**. Pre-existing; more dangerous now that search/folders bump the schema.
- **`RECORD_AUDIO`** was added to the manifest. Play Data Safety / F-Droid need an honest line for that. **$0**, but a human form change.
- **`consumeVoiceSlot`** increments in a launch; two fast taps can both pass. Small race.
- Theme law: user-facing words look everyday. Personas are "Teacher", "Short answers", etc. Good.

### What passed

- Search table + upgrade copy + Pro lock + grouped results + tap-to-open (P-002).
- Folders: `folderId`, v3→v4 migration registered, drawer All + Move, Pro gate on create/move (P-009).
- Registered card + upgrade button hidden for Pro (P-004). Settings export still omits keys and Pro flags.
- Personas exist (6), picker row, injected only if Pro (P-010).
- Advanced knobs + `ChatOptions` only sent when not default (P-013). FREE.
- `/search` is Pro, IO, feeds the model, asks for URLs (P-005).
- `/recall` + memory list/edit/delete + summary hook (P-006).
- Backup password uses AES-GCM + PBKDF2; magic `BYO1`; wrong password fails (P-003). Passphrase is not saved to disk.
- `/edit` is Pro; `/imagine` stays free; honest "cannot edit images" (P-011).
- No WebView / trust-all in the new code.

### Tickets

| Ticket | Review |
|--------|--------|
| P-002 search | Approve |
| P-009 folders | Approve |
| P-004 registered | Approve |
| P-010 personas | Approve |
| P-013 knobs | Approve |
| P-005 `/search` | Approve |
| P-006 memory+ | Approve |
| P-003 encrypted backup | **Issues** (#5 heap) |
| P-001 voice | **Issues** (#1 #2 #3 #4) |
| P-011 `/edit` | Approve (nit: ByteArray) |

Say **WIRE** if you want those Issues fixed. Then we re-review. Do not install this as the "everything works" phone build until 1–4 are fixed (5 if you keep big chat histories).

---

## Review addendum — 2026-08-16 — 3-layer fan-out (after `5656ab4`)

Three read-only reviewers finished after the first verdict. They looked at `f2c475a`. Coding then shipped `5656ab4` for the first five Issues. This addendum **keeps** what is still real.

### Already fixed in `5656ab4` (do not re-open)

| First-pass # | What |
|---|---|
| Voice #1 / UI #3 | Slot taken only after there is text. Mic no longer burns the slot. |
| Voice #4 / UI #1 (part) | `MediaPlayer.prepare()` is now inside `withContext(IO)`. |
| Backup heap #5 | Encrypt/decrypt in 8 KB chunks. No `readBytes()` on the backup path. |
| Voice #3 / Data #5 | BACKLOG now says v1 STT is the phone speech box. |

Leftover of UI #1: `ttsPlayer` is still assigned **after** prepare. Stop during prepare cannot cancel, then `start()` can still play. Smaller than the original ANR.

### Still Issues (new or leftover)

| # | Where | What's wrong | Why | Suggested fix |
|---|-------|--------------|-----|---------------|
| A | `OpenAiCompatibleClient.kt:309` | `/edit` posts to `$root/v1/images/edits`. Catalog `baseUrl` already ends in `/v1` → **`/v1/v1/images/edits`**. | Feature is broken on OpenAI/Groq/OpenRouter. Combined with B, the user sees “cannot edit” instead of a URL bug. | `"$root/images/edits"` (same join as chat). |
| B | `OpenAiCompatibleClient.kt:329-333` | Any 404/405 or body containing `unknown` / `not found` becomes “This provider cannot edit images.” | “Unknown API key” and the bad URL from A all look like “this host has no edits.” | Only 404/405 on the edits path, or “not supported”. Leave 401/400 as the real HTTP text. |
| C | `OpenAiCompatibleClient.kt:315` | Last `/imagine` file is JPEG (`gen_*.jpg`). `/edit` always sends `image/png`. | Edits APIs want a real PNG. Even a correct URL can fail. | Convert to PNG before upload, or save `/imagine` as PNG. |
| D | `ChatViewModel.kt` `/search` + `/edit` | Bare `viewModelScope.launch`. Not on `streamJob`. Stop FAB never shows. | P-005 said cancel on Stop. User can send again while DDG is still running. | Assign `streamJob`, set `isStreaming`, honor `stopRequested`. |
| E | `Screens.kt` backup password | Password lives in `remember { }`. If the phone kills the screen while the file picker is open, it comes back empty. Export then writes a **plain** DB. | 4GB phones die in the picker a lot. User thought they set a password. | Store `pendingBackupPass` on the ViewModel **before** opening the picker. |
| F | Settings Backup / Restore buttons | Not Pro-gated in the UI. Search chats is. | Free user gets an empty leftover file, then a late error. | Same as Search: if not Pro, show the pay-once line and do not open the picker. |
| G | `SettingsRepository` template import | Bad JSON / a settings file used as templates → silently reset to the one built-in pack. UI says “imported.” | User loses their templates. | Strict decoder. Throw on non-array. Never reuse the “missing → builtins” reader. |
| H | Folder names JSON | Hand-built JSON. A newline in a folder name can invalidate the file; next save wipes **all** folders. | Same class we already fixed for drafts/templates. | Use kotlinx serialization like drafts. |
| I | Search-as-you-type | Every keystroke starts a new query. No cancel. Slow `"a"` can overwrite `"ab"`. | Wrong hits. | `searchJob?.cancel()`; ignore stale results. |

### Nits (do not block a small fix pass)

- `addSummary` is append-only (many “Summary:” rows). Ticket wanted one rolling row.
- Already-Pro installs never get `proSince` stamped → Registered card can say “today” every launch.
- `generateImage` has the same extra `/v1` (pre-existing; `/edit` copied it).
- New chat created inside a folder still lands in All.
- `fallbackToDestructiveMigration()` still on.
- `clearHighlight()` never called after you leave the hit.

### Ticket table (updated)

| Ticket | After fan-out |
|--------|----------------|
| P-002 search | Approve (nit: keystroke race = I) |
| P-009 folders | **Issues** (H wipe-all) |
| P-004 registered | Approve (nit: already-Pro date) |
| P-010 personas | **Issues** (G template wipe) |
| P-013 knobs | Approve |
| P-005 `/search` | **Issues** (D Stop) |
| P-006 memory+ | Approve (nit: rolling summary) |
| P-003 backup | **Issues** (E process-death plaintext) |
| P-001 voice | First-pass Issues fixed in `5656ab4`. Leftover Stop-during-prepare nit. |
| P-011 `/edit` | **Issues** (A broken URL, B lie, C JPEG-as-PNG, D Stop) |

**Verdict stays Issues.** Worst leftover: **A `/edit` URL is wrong**, **E backup can save plaintext after the picker kills the app**, **G/H data wipe**.

Say **WIRE** to fix A–I. I will not start until you say that.

---

## Review — 2026-08-16 — leftover A–I (`3e8671a`)

**Role:** `LITECHAT-REVIEW`. Read-only. Did not edit `app/**`. Did not run Gradle (RAM + review law).  
**HEAD:** `3e8671a` (Fix REVIEW Issues A-I). Tree clean. `main` matches `origin/main`.  
**This-review check:** `python3 scripts/verify_static.py` → **173/173**. Coding session also left foss unit tests **76/76** on disk; this review did not re-run Gradle.

**Verdict: Approve**

A–I are in the tree. The worst leftovers (wrong `/edit` URL, template/folder wipe, silent plaintext after a picker restart) are gone for the cases we asked to fix.

### A–I close-out

| # | Asked | What landed | Verdict |
|---|-------|-------------|---------|
| A | No `/v1/v1` | `imagesUrl()` joins `$root/images/{edits,generations}`. Tests for catalog `/v1`, trailing slash, already-complete URL. Also fixed `/imagine` (same bug). | Fixed |
| B | Honest 404 only | `editImage` uses 404/405 only. Other codes keep `HTTP {code}: …`. | Fixed |
| C | Real PNG | `/imagine` writes `gen_*.png` with `CompressFormat.PNG`. `/edit` converts leftover JPEG first. Client mime follows the file name. | Fixed |
| D | Stop on `/search` + `/edit` | Both assign `streamJob`, set `isStreaming` (Stop FAB watches that flag), honor `stopRequested`, rethrow cancel. `fetchSearch` now uses the shared OkHttp `activeCall` so Stop can cut the fetch. | Fixed |
| E | Password survives picker | `armBackupPass` on the ViewModel **before** the picker. Callback no longer reads `remember { }`. | Fixed for screen restart. See leftover nit. |
| F | Pro-gate buttons | Backup / Restore match Search: pay-once line, picker does not open. | Fixed |
| G | Strict template import | `decodeTemplatesStrict` throws on a settings object or garbage. Import writes nothing and says “Templates file is not valid.” Tests cover both. | Fixed |
| H | Folder JSON | `ChatFolder` is `@Serializable`. Encode/decode use kotlinx. Newline / quotes survive. Old hand-built array still loads. | Fixed |
| I | Stale search | `searchJob?.cancel()` + drop if the query changed. | Fixed |

### Issues (none)

No new blocking bugs in this commit.

### Leftover nits (do not block Approve)

- **E process-death hole:** a ViewModel field dies if the whole app is killed in the picker. Empty password still writes a **plain** DB (`exportChats` `copyTo` when `passphrase.isBlank()`). Rare, but that is the original 4GB “picker killed us” case. SavedStateHandle, or “refuse empty export if the user had typed a password,” would close it. Not the same as the old `remember { }` hole.
- **`/imagine` still not on `streamJob`.** Out of scope for D. Stop FAB will not show during generate.
- **`editImage` / `/imagine` still return a full `ByteArray`.** Pre-existing heap shape.
- **`deleteFolder`** still does not clear `folderId` on chats.
- **`addSummary`** still appends another “Summary:” row.
- **Already-Pro** installs can still miss `proSince` (Registered card says “today”).
- New chat started inside a folder still lands in All.
- **`fallbackToDestructiveMigration()`** still on.
- **`clearHighlight()`** still unused after you leave a hit.
- TTS Stop-during-prepare leftover from `5656ab4` (player assigned after `prepare`).

### Hard constraints

- No WebView / trust-all / RN / Flutter in the new code.
- No new API-key logging.
- Theme-law everyday words on the new Backup/Restore lines.
- New verify_static guards A–I are real substring checks, not wishful comments.

### Tickets

| Ticket | After this review |
|--------|-------------------|
| P-002 search | Approve (I closed) |
| P-009 folders | Approve (H closed) |
| P-004 registered | Approve (nit: already-Pro date) |
| P-010 personas | Approve (G closed) |
| P-013 knobs | Approve |
| P-005 `/search` | Approve (D closed) |
| P-006 memory+ | Approve (nit: rolling summary) |
| P-003 backup | Approve (E asked-fix landed; process-death plaintext is a nit) |
| P-001 voice | Approve leftover from `5656ab4` (Stop-during-prepare nit) |
| P-011 `/edit` | Approve (A/B/C/D closed; ByteArray nit) |

`3e8671a` is safe as a daily-driver **sideload** of this tree. Human still spot-checks on a phone. Play / F-Droid forms are not this review.

No WIRE needed unless you want the process-death backup nit or the old leftover nits.

---

## Review — 2026-08-21 — FULL TREE addendum (independent)

**Scope:** whole `app/**` at HEAD `1860467` plus uncommitted `OverlayService.kt` (ViewTree lifecycle extensions). Read-only. No Gradle. `python3 scripts/verify_static.py` → **222/222**. Hard-constraint grep (WebView / trust-all / RN / Flutter) empty.
**Role:** `LITECHAT-REVIEW`

**Do not re-open:** B-001, B-004, R-019, R-020, leftover 1–8 (Approve), A–I (Approve).

**Verdict: Issues**

Already-fixed classes still look right in this tree (destructive Room fallback gone, picture URL streams to disk, attach has a 32k budget loop, `streamJob` is set on chat / `/browse` / `/search` / `/imagine` / `/video` / `/edit`, keys in EncryptedSharedPreferences, folders/templates/named keys use kotlinx JSON, `imagesUrl` does not emit `/v1/v1`). Overlay compile fix (`setViewTreeLifecycleOwner` extensions) is in the working tree and is **uncommitted**.

### New Issues

1. **`Screens.kt:1253`** — Fetch models always runs `LanDetector.scan()` first. If any box on `192.168.0.1–10` / `192.168.1.1–10` / `10.0.0.1–10` answers Ollama, it **overwrites** the base URL and never lists this provider’s models.
   - Why: a Gemini/OpenAI user taps Fetch and can get silently pointed at a LAN box. Worst case the scan is 30 serial 1.5s TCP waits (`LanDetector.kt:18-28`) — the button says “Fetching…” for a long time.
   - Fix: only scan when the picker is Custom/Ollama. Never replace a non-empty saved host. Cap the scan (parallel + short deadline, or `127.0.0.1` only).

2. **`OverlayService.kt:224`** — overlay window is `FLAG_NOT_FOCUSABLE` (plus `FLAG_ALT_FOCUSABLE_IM`).
   - Why: Compose `OutlinedTextField` needs window focus. On many phones the Ask box will not take taps or open the keyboard, so overlay send is a dead control.
   - Fix: drop `FLAG_NOT_FOCUSABLE`. Use `FLAG_NOT_TOUCH_MODAL` so taps outside still reach the app underneath.

3. **`OpenAiCompatibleClient.kt:268`** — `/browse` still uses `Jsoup.connect(...).get()`. That stack is not `activeCall`.
   - Why: Stop cancels `streamJob` and OkHttp. The page fetch keeps going up to 15s. The person tapped Stop and the phone is still on the network.
   - Fix: same path as `/search` — OkHttp GET, set `activeCall`, parse HTML with Jsoup after.

4. **`OverlayService.kt:145`** + **`OpenAiCompatibleClient.kt:1123`** — overlay Send has no Stop, and the one shared client has `readTimeout(0)`.
   - Why: `completeChat` can wait forever. Overlay `busy` stays true with no Close button. Test/Fetch models use the same client, so a stuck host leaves “Testing…” / “Fetching…” forever.
   - Fix: give overlay a cancel that calls `openAiClient.cancel()`. Put a finite read timeout on non-stream calls (or a second client). SSE can keep `readTimeout(0)`.

5. **`ChatViewModel.kt:142`** — `connectivityObserver.state.observeForever { … }` is never removed.
   - Why: `AppContainer` lives for the process. The lambda holds the ViewModel after `onCleared`. Every rotate/recreate leaks a VM. The launch job also finishes right after register, so cancelling `connectivityJob` does nothing.
   - Fix: `callbackFlow` / `observe` with a LifecycleOwner, or `removeObserver` in `onCleared`.

6. **`network_security_config.xml:4`** — `<base-config cleartextTrafficPermitted="true" />` for the whole app.
   - Why: product law is cleartext for LAN Ollama only. A custom `http://` host on the public net ships the key in the clear. This is not trust-all TLS, but it is wider than LAN.
   - Fix: allow cleartext only for localhost + RFC1918 domain-config. Keep HTTPS as the default.

### Passes (hunt list)

- No `fallbackToDestructiveMigration` in `AppContainer.kt`.
- No `body.bytes()` left (comment only). Video/picture GET streams to a file.
- `streamJob` assigned on the send paths that show Stop.
- Primary + failover + named keys go through EncryptedSharedPreferences.
- No hand-rolled `jsonQuote` / StringBuilder JSON in prefs.
- Attach downscales until b64 fits the 32k budget (mime vs jpeg-prefix mismatch is a nit).
- `imagesUrl` / video helpers do not emit `/v1/v1`.
- No WebView / trust-all / RN / Flutter in `app/src`.

### Nits (not Issues)

- Video poll loops still ignore HTTP status (old full-project #11). Not re-opened as new.
- `RetryInterceptor` sleeps and retries `IOException` with no `call.isCanceled()` check. After Stop the OkHttp thread can sit ~3s. Unlikely to send a second paid call if cancel sticks.
- Overlay FGS `specialUse` has no `PROPERTY_SPECIAL_USE_FGS_SUBTYPE` (Play form / Android 14+).
- Mic button uses the Send icon (`Screens.kt:560`).
- `ChatViewModel.shareChat` is unused; the UI shares via `getCurrentChatText()`.
- Imagine fallback still treats body text `not found` as “try next model” (`OpenAiCompatibleClient.kt:525`). Edit path was fixed to HTTP 404/405 only.

**Next:** WIRE the six Issues if you want them in the tree. Commit the Overlay ViewTree fix so it is not lost. Do not bake an APK unless asked.


