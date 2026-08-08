# LiteChat backlog

Single task queue for the two-agent team.  
Statuses: `Idea` · `Research` · `Ready` · `Doing` · `Done` · `Blocked`

Coding agent: only take **Ready** (or human-named id). Claim with `Doing`, finish with `Done`.

---

## Now / next (coding)
### C-007 — Wire privacy link and disclaimer in Settings
- **Status:** Ready  
- **Goal:** Add privacy policy link and unofficial BYOK disclaimer to Settings/About so the app is Play Store submission-ready.  
- **AC:**
  - [ ] Settings screen has "Privacy Policy" link opening hosted HTML (or bundled HTML as fallback)
  - [ ] Settings screen shows the BYOK disclaimer text (one paragraph)
  - [ ] README.md includes the disclaimer wording
- **Touch:** `Screens.kt` (Settings section), `README.md`  
- **Research:** `docs/PLAY-LISTING-DRAFT.md` — privacy HTML template + disclaimer text  
- **Out of scope:** actual Play Store submission, hosting the privacy page


### C-008 — Markdown rendering (post-v1, DEFERRED)
- **Status:** Idea  
- **Depends on:** User demand data from v1 (Play reviews, feedback channels)  
- **Goal:** Add incremental streaming markdown rendering using `com.mikepenz:multiplatform-markdown-renderer-m3` v0.43+ with `StreamingMarkdownState`, following the maid-native integration pattern.  
- **AC:**
  - [ ] APK growth ≤ 500 KB over baseline (measure before/after with `assembleRelease` diff)
  - [ ] Streaming replies use `StreamingMarkdownState` (O(n) parse, not O(n²))
  - [ ] Parse cache bounded by character count (512 KB budget ≈ 5 MB retained)
  - [ ] Markdown state hoisted above LazyColumn in ChatScreen, keyed on streamingId
  - [ ] Settled messages render from synchronous `parseMarkdown()` with LRU cache
  - [ ] Toolchain: compileSdk 37+, Kotlin 2.4.x+, AGP 9.x+, Compose BOM 2026.06+
  - [ ] Tested on 4 GB device: streaming jank ≤ 1 skipped frame per 100 tokens
  - [ ] Plain-text fallback: if renderer init fails, degrade to BasicText (never crash)
- **Touch:** `build.gradle.kts`, `libs.versions.toml`, `ui/markdown/Markdown.kt` (new), `ui/chat/ChatScreen.kt`, `ui/chat/MessageItem.kt`, `ChatViewModel.kt`
- **Research:** `docs/MARKDOWN-COST.md` — APK/RSS library comparison, maid-native integration pattern, streaming parse audit, constrained-device history
- **Out of scope:** Markdown images (needs Coil), syntax-highlighted code blocks, LaTeX math, HTML rendering, WYSIWYG composer



### C-006 — UI stream paint throttle (if needed)
- **Status:** Ready  
- **Goal:** Throttle SSE delta UI updates to ≤4 fps (250ms interval) to prevent jank on weak devices. Pattern from numAi-plus `UPDATE_DELAY_MS`.  
- **AC:**
  - [ ] `ChatViewModel.send()` throttles UI updates to ≤4 fps (250ms via `System.currentTimeMillis()`)
  - [ ] Final text always flushed on `StreamEvent.Done` / stream end (no lost deltas)
  - [ ] Error events pass through immediately (no throttle)
  - [ ] No new dependencies (pure stdlib timer, not Flow-based sample/debounce)
  - [ ] Unit test: 20 rapid deltas within 250ms produce ≤2 UI state updates
- **Touch:** `ChatViewModel.kt`, `ChatViewModelTest.kt`  
- **Research:** `docs/PAINT-THROTTLE-RESEARCH.md` — 50-year pattern from Palm/J2ME to Compose
- **Out of scope:** per-device adaptive throttle, Settings UI for interval, token-count throttle

---

## Research queue

### R-001 — Play listing pack (privacy + disclaimer copy)
- **Status:** Done — C-007 Ready for coding  
- **Deliverable:** `docs/PLAY-LISTING-DRAFT.md` — privacy HTML template, BYOK disclaimer text, Play Data Safety form, historical unofficial-client lineage (ICQ clones → Palm conduits → Winamp → modern BYOK)  

### R-002 — Markdown renderer cost
- **Status:** Done — decision: **DEFER for v1**, open C-008 as Idea
- **Deliverable:** `docs/MARKDOWN-COST.md` — full APK/RSS library comparison, maid-native ~300-400 KB cost, streaming feasibility, 40-year constrained-device rich-text history. Recommendation: plain text for v1; add markdown post-v1 when user demand justifies it.

### R-003 — 4PDA / XDA / OldMarket distribution notes (FOSS arm)
- **Status:** Done — distribution is operational (no code ticket needed)  
- **Deliverable:** `docs/DISTRIBUTION-FOSS.md` — 10-channel hierarchy, pre-Play Android distribution history (SlideME→F-Droid lineage), store listing hygiene comparison, XDA thread template  

### R-004 — OpenAI-Client-Android streaming library notes
- **Status:** Done  
- **Deliverable:** `docs/OPENAI-CLIENT-ANDROID-NOTES.md` — SSE parser comparison, no new fixture needed

### R-005 — Further lost-repo archaeology
- **Status:** Done  
- **Deliverable:** `docs/LOST-REPOS-R005.md` — r/androidafterlife, 4PDA ChatGPT thread, SummaryExpressive noted; no new trench repos found

---

## Done (recent)

| ID | What | Notes |
|----|------|--------|
| C-001 | Lazy-init MobileAds when `!isPro` | Removed cold-start `MobileAds.initialize`; `AdMobLazyInit` one-time init in `BannerAd`; stripped `MobileAdsInitProvider` via manifest `tools:node="remove"`; verify_static guards |
| C-002 | Product flavors `play` vs `foss` | `productFlavors { play, foss }`, play-only GMS deps via `add("playImplementation",…)`, foss has `applicationIdSuffix .foss`, R8 fix: `-dontwarn com.google.errorprone.annotations.**` for tink transitive dep. Both flavors build/tests pass. |
| C-003 | Debug-only "Dev: mark Pro" | `if (BuildConfig.DEBUG)` guard in Screens.kt line 708; release APKs verified clean of "Dev: mark Pro" string. |
| C-004 | Per-baseUrl stream-broken cache | SettingsRepository `markStreamBroken()`/`isStreamBrokenNow()` with 24h TTL in DataStore JSON map; ChatViewModel checks flag before `streamChat()`, marks on `FallbackUsed`. |
| C-005 | GET `/models` picker | `listModels()` in client, "Fetch models" button + dropdown picker in Settings; lenient parse, no crash on failure. |
| D-001 | App scaffold Compose BYOK chat | Room, prefs, billing stubs, CI |
| D-002 | Stream + non-stream fallback | `OpenAiCompatibleClient` numAi-plus pattern |
| D-003 | Free-RAM compat matrix UX | `DeviceCompat`, onboarding step 1, Settings |

---

## Parking lot (do not build)

- Bundle Hermes/OpenClaw/Node in APK  
- Default on-device 7B  
- Trust-all SSL  
- WebView wrapper of chat.openai.com  
- Interstitial ad on every send  
- Trademark-confusing “Hermes chat” branding  

---

## Claiming protocol

```markdown
### C-00X — title
- **Status:** Doing
- **Agent:** coding
- **Started:** YYYY-MM-DD
```

When finished, move to Done table with files touched.
