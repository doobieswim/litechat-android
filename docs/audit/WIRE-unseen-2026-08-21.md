# WIRE unseen audit — 2026-08-21

**Role:** `LITECHAT-WIRE` (coding lens only)  
**Mode:** read the tree. Do **not** patch `app/**`. No Gradle. No tickets.  
**Repo:** `/opt/data/workspace/byok-chat-android`  
**HEAD:** `1860467`  
**Dirty:** `OverlayService.kt` (ViewTree swap, uncommitted) plus docs.

Talk like a person. This is “would a bake die, or would a phone freeze?” not product research.

`python3 scripts/verify_static.py` on this working tree: **222/222**. That is **not** a compile.

---

## One-page verdict

A coding agent who trusts `verify_static` and last-commit HEAD can still ship a **red bake** or a **frozen phone**.

| Risk | Bake or phone? | In this tree |
|------|----------------|--------------|
| Overlay ViewTree API | Bake (unresolved) **or** overlay crash | **Fixed in working copy. Not committed.** |
| `Thread.sleep` in `/video` poll | Phone: Stop lies; 5 min hang on bad HTTP | Still there. Call is on IO, so not an ANR by itself. |
| Blocking `Call.execute()` | Phone ANR **if** someone drops the IO wrap | Chat/Test/video paths wrap IO today. Overlay Room still starts on Main. |
| `streamJob` | Phone: Stop no-op on a new slash | `/browse` `/search` `/video` `/imagine` `/edit` chat: assigned. `/recall` is not (local, fine). |
| Flavor gap | Foss bake dies if main starts using Play-only fields | Stubs match **today**. Easy to break. |
| Static vs real compile | Green Python, red Gradle | Many traps below. |

Do not treat 222/222 as “safe to install.”

---

## 1. Overlay ViewTree — already fixed, still uncommitted

**Working copy (this disk):**

- `OverlayService.kt:42-43` import `setViewTreeLifecycleOwner` / `setViewTreeViewModelStoreOwner`
- `OverlayService.kt:277-281` `view.setViewTreeLifecycleOwner(this)` (and ViewModel + SavedState)

**Last commit still has:**

- `import androidx.lifecycle.ViewTreeLifecycleOwner`
- `ViewTreeLifecycleOwner.set(view, this)`

Compose 2024.12 + lifecycle 2.8 wants the **extension on the View**. The old `.set` object is the thing REVIEW already flagged (“if a flavor fails, swap”). A bake from **HEAD** can die with `Unresolved reference: ViewTreeLifecycleOwner` (or `.set`). A bake that **forgets this uncommitted file** is the same bake.

If the overlay **does** compile with the old call but `attach()` is skipped, the phone dies when you flip the float switch: ComposeView with no owner → crash, not a polite error.

**Static check cannot see the difference.**  
`scripts/verify_static.py:382` only asks: is the string `ViewTreeLifecycleOwner` in the file? Both APIs contain that word. HEAD would pass. The fix would pass. A broken mix would pass.

**For a bake:** include `app/src/main/java/com/litechat/android/ui/OverlayService.kt` as it sits on disk. Do not revert to HEAD’s `.set`.

---

## 2. `Thread.sleep` in video poll — phone, not bake

`OpenAiCompatibleClient.kt`

- `:817` Veo: `Thread.sleep(10_000)` every loop
- `:846` Grok: `Thread.sleep(5_000)`
- `:880` Sora: `Thread.sleep(2_000)`
- `:778` default timeout **300_000** ms (five minutes)
- `:802` Veo poll: `call.execute()` then parse JSON. **No** `isSuccessful` check. A 401 body with no `done` just sleeps again.

Who calls it: `ChatViewModel.kt:638-644` inside `withContext(Dispatchers.IO)`. So the **UI thread is not** sleeping today. Good.

Why the phone still feels broken:

1. `Thread.sleep` is not a coroutine cancel. Stop (`ChatViewModel.kt:389-392`) cancels `streamJob` + `activeCall`. During the sleep there is **no** call. The job may sit until the sleep ends (up to 10s on Gemini). Then a **new** poll starts.
2. If interrupt *does* fire, `InterruptedException` is a normal `Exception`. The video `catch` (`ChatViewModel.kt:652-655`) can paint “Video: …” instead of a clean Stop.
3. Bad HTTP on Veo loops until the five-minute cap. The spinner stays. Stop looks dead.
4. A later agent who copies `pollVideo()` onto Main (Settings “test video”, overlay, etc.) **will** ANR. `execute()` + `sleep` share the caller thread. `RetryInterceptor.kt:41,46` also `Thread.sleep` on that same thread.

`RetryInterceptor` sleeps are fine **only** because current HTTP is on OkHttp/IO. Do not call `listModels` / `completeChat` / `pollVideo` from a click without `Dispatchers.IO`.

---

## 3. Main-thread `execute()` — mostly wrapped; two traps left

Blocking HTTP lives on `OpenAiCompatibleClient` as plain `fun` + `call.execute()` (`:244` listModels, `:975` executeChat, poll loops `:802/:836/:867`, plus image/video/search).

**Safe today (IO wrap on the call site):**

- Settings Fetch / Test — `Screens.kt:1252-1259` and `:1287-1288` (`withContext(Dispatchers.IO)`)
- Overlay send HTTP — `OverlayService.kt:181-188`
- `/browse` fetch + complete — `ChatViewModel.kt:466, 484`
- `/search` — `:534, 553`
- `/video` create+poll — `:629, 638`
- `/imagine` — `:688`
- failover complete — `:1048`
- TTS `prepare()` — `:1356-1365` on IO; `player.start()` `:1371` on Main (correct)

**Still on Main (not HTTP, but a 4GB hitch):**

- Overlay send (`OverlayService.kt:145-180`) uses `rememberCoroutineScope()` (Main). Before the IO hop it does DataStore `.first()`, Room `observeConversations().first()`, `createConversation` / `addMessage` / `listMessages`. Room suspend usually hops off Main. Encrypted key read + first Flow collect can still jank the float window.
- `ChatViewModel.kt:142` `observeForever` inside `viewModelScope.launch` with **no** `removeObserver`. Not a freeze on day one. Leak + duplicate observers if the VM is recreated. Phone gets dumber over time.

**Agent trap:** `verify_static.py:310` only checks that `withContext(Dispatchers.IO)` and `listModels` both appear **somewhere** in Screens.kt. A new Test button that calls `listModels` on Main still passes static.

---

## 4. `streamJob` — Stop wiring

Assigned (`ChatViewModel.kt`):

| Line | Path |
|------|------|
| 450 | `/browse` |
| 520 | `/search` |
| 613 | `/video` |
| 671 | `/imagine` |
| 752 | `/edit` |
| 816 | normal send |

`stopStreaming()` `:389-392` cancels that job + the OkHttp call. FAB uses `isStreaming \|\| isGeneratingImage` (static guard `FAB stops generate` passed).

`send()` `:418` refuses while streaming or generating. Good.

**Do not put `streamJob?.cancel()` inside the send coroutine.** `:934` already warns: that would kill the send itself.

**`/recall` (`:587`)** launches a **bare** `viewModelScope.launch` — no `streamJob`, no `isStreaming`. It only reads memory on disk. Stop does not apply. Leave it unless someone adds network there.

**Agent trap:** a new slash (`/foo`) that sets `isStreaming = true` but forgets `streamJob =` makes Stop a dummy. Static only greps imagine/video/browse (and a brittle search slice). It will not catch `/foo`.

Video Stop is extra-weak because of §2 sleep, even though `streamJob` **is** set.

---

## 5. Flavor gaps — foss bake vs play bake

Used from **main** (must exist in **both** flavors):

- `BannerAd()` — foss stub `BannerAd.kt:7` = `Unit`; play real AdView
- `AdMobLazyInit.ensureInitialized` — foss no-op; play real
- `BillingRepository(context)` + `proOwned` + `startConnection` + `queryOwned` + `launchPurchase` + `endConnection` — foss stub matches those **names**

Play-only extra: `BillingRepository.kt:29` `productDetails`. Main never reads it. **Foss has no such field.** If Screens starts collecting `billingRepository.productDetails`, **foss compile dies**. Play still builds. CI runs **both** (`assembleFossRelease` then `assemblePlayRelease`). One flavor green is not enough.

Play billing `:36` `enablePendingPurchases()` with no args is valid on `billing-ktx:7.1.1`. Bump to Billing 8 and this line (and maybe `queryProductDetailsAsync` `:66`) fail compile. Static does not pin the Billing API shape.

`app/build.gradle.kts:138-140` uses `add("playImplementation", …)` on purpose (`create()` flavors do not grow a `playImplementation()` helper). “Cleaning up” to `playImplementation("…")` can break the **Gradle** file itself.

Sample AdMob ids (`app/build.gradle.kts:21-22` and play manifest `:11`) are debug samples. Play **release** still bakes them. Store reject, not kotlinc.

Foss has **no** extra manifest. Overlay FGS lives in main (`AndroidManifest.xml:45-48`) so both flavors get it.

---

## 6. `verify_static` vs a real compile

Python **222/222** on this disk. CI `static-verify` is the same script. Then Gradle.

What static **cannot** do:

| Lie | Why a bake still dies |
|-----|------------------------|
| Overlay owner string | Matches HEAD `.set` and the extension fix (`verify_static.py:382`) |
| `listModels` off Main | Same file, not the same click (`:310`) |
| `streamJob` on slash | Only imagine/video/browse slices (`:376-378`) |
| Coil / icons / Room KSP | Never runs kotlinc |
| AGP 8.7.3 + `compileSdk = 36` | `gradle.properties` has **no** `android.suppressUnsupportedCompileSdk=36`. AGP 8.7 was tested to 35. Some machines turn that into a hard fail. Static never opens AGP. |
| Heap | `gradle.properties:4` is `-Xmx768m` for the VPS. CI **build** job rewrites it. CI **publish** job (tag `v*`) does **not** (`build.yml:70-102`). Tag bake can OOM on play R8 while `static-verify` was green. |
| Signing env | Gradle wants `KEYSTORE_FILE` (`app/build.gradle.kts:36-42`). Publish sets `STORE_FILE` / `KEY_ALIAS` (`build.yml:91-94`). Secrets never reach Gradle. “Signed GitHub Release” is unsigned. F-Droid likes unsigned. Play does not. |
| Uncommitted Overlay | CI checks out **git**. The ViewTree fix is **not** on `main` until someone commits it. |

Kotlin is **2.2.0**. AGP is **8.7.3**. Coil is **3.1.0**. That mix has compiled here before; it is still a “don’t bump one number” minefield. Static will stay green while you bump Coil to 3.5 or Billing to 8.

`versionCode` / `versionName` in `app/build.gradle.kts:17-18` are still **1 / 1.0.0**. A side bake named “1.0.5-wire / 6” is **not** this file. Don’t ship two stories.

---

## 7. Other bake/phone hits a coding agent would trip

**Overlay FGS on a new phone (`OverlayService.kt:74`, manifest `:10-11, :48`)**  
`startForeground(1, notification)` two-arg. Type `specialUse` is only on the `<service>` tag. No `PROPERTY_SPECIAL_USE_FGS_SUBTYPE`. Play 14+ review can bounce. Some OEMs kill the service. Not kotlinc.

**Overlay window flags (`OverlayService.kt:224-225`)**  
`FLAG_NOT_FOCUSABLE or FLAG_ALT_FOCUSABLE_IM`. Keyboard/Send is OEM luck. That is a phone bug (B-009 research), not a bake.

**`callbackFlow` cancel (`OpenAiCompatibleClient.kt:110, 203`)**  
Qualified `this@OpenAiCompatibleClient.cancel()`. Do **not** write a bare `cancel()` inside `callbackFlow`. That compiles and then every stream kills itself.

**Room (`AppContainer.kt:29-33`, `Entities.kt:154`)**  
Named migrations 1→4. No destructive fallback. A new column without a `MIGRATION_4_5` **crashes the phone on upgrade** (illegal state), and may fail KSP if the entity and SQL disagree. Static only checks the old three names.

**`Icons` (`Screens.kt` + overlay Send)**  
Core set only (`app/build.gradle.kts:109`). `ArrowUpward` and friends are **not** in core. Unresolved at compile. Static does not grep icons.

**Connectivity forever observer (`ChatViewModel.kt:141-146`)**  
Compiles. Leaks. Not an install blocker.

**Hard-constraint grep** (WebView / trust-all / RN / Flutter) in `app/src`: empty. Good.

---

## 8. What looks safe for a coding pass

- Foss/play stubs match the methods main actually calls.
- `listModels` / Test are on IO in Settings.
- Chat, browse, search, imagine, video, edit assign `streamJob`.
- SSE `cancel()` is qualified; fallback is on IO.
- Coil 3.1 `SingletonImageLoader.Factory` + okio disk path match the 3.1 AAR (MemoryCache still has `trimToSize` in 3.1.0).
- No WebView chat shell.

---

## What a bake needs (no tickets, just facts)

1. Commit or cherry-pick the Overlay ViewTree **extension** file. Do not bake HEAD’s `.set`.
2. Do not trust 222/222. Real proof is `assemblePlayDebug` / `assembleFossRelease` (not run this pass).
3. `/video` Stop will still feel broken on a phone until poll uses a cancellable delay and fails non-2xx.
4. Keep flavor public APIs twins. Don’t read `productDetails` from main.
5. Tag/publish CI heap + `KEYSTORE_*` env are still wrong; a “release” tag can OOM or ship unsigned.

No `app/**` edits. No Gradle this pass.
