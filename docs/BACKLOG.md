# LiteChat backlog

Single task queue for the two-agent team.  
Statuses: `Idea` · `Research` · `Ready` · `Doing` · `Done` · `Blocked`

Coding agent: only take **Ready** (or human-named id). Claim with `Doing`, finish with `Done`.

---

## Now / next (coding)
### C-007 — Wire privacy link and disclaimer in Settings
- **Status:** Done
- **Notes:** Added `app/src/main/assets/privacy.html` (bundled), `privacy.html` (repo root for GitHub Pages), Settings "Privacy Policy" link via `ACTION_VIEW`, upgraded disclaimer to full BYOK wording from research, added Disclaimer section + Privacy Policy link to README.md

### C-008 — Markdown rendering (re-evaluated post R-006)
- **Status:** Idea  (cost equation changed — R-006 found published Maven Central libs)
- **R-006 finding:** `NadeemIqbal/llm-typewriter` (Apache 2.0, `io.github.nadeemiqbal:llm-typewriter:0.1.1`) is a drop-in streaming markdown typewriter for Compose. Also found: `xemantic/markanywhere` (semantic event-stream parser, KMP). Both viable.
- **Depends on:** R-006-1 (APK/RSS measurement) — not yet scheduled
- **Goal:** Add incremental streaming markdown rendering using llm-typewriter (preferred) or markanywhere. Plain text until measurement confirms APK cost is acceptable.  

### C-009 — Streaming height placeholders (from EveryTalk pattern)
- **Status:** Idea
- **Goal:** Pre-allocate space for streaming assistant messages to prevent LazyColumn layout jumps when markdown/code blocks finish rendering.
- **Source:** `roseforljh/EveryTalk` `PerformanceConfig.kt`
- **Touch:** `ChatViewModel.kt`, `Screens.kt`
- **Out of scope:** height estimation for markdown (plain text only until C-008)

### C-010 — Token-budget context compression (from ChatPPP pattern)
- **Status:** Idea
- **Goal:** Auto-truncate conversation history when token budget exceeded (approx 4 chars ≈ 1 token, default threshold 24k). Show "earlier messages truncated" indicator.
- **Source:** `NNCVA/ChatPPP` (★2)
- **Touch:** `ChatViewModel.kt`, `OpenAiCompatibleClient.kt`, `Screens.kt`
- **Out of scope:** LLM-based summary compression (v2)

### C-011 — Image generation via /imagine slash command
- **Status:** Ready
- **Goal:** User types `/imagine <prompt>` → image generated via same BYOK OpenAI key → displayed inline in chat. Uses Coil 3 for display (~200KB APK). Optional Pollinations AI free fallback for FOSS flavor.
- **Research:** `docs/IMAGE-GENERATION-RESEARCH.md` (R-007)
- **AC:**
  - [ ] `/imagine <prompt>` detected in ChatViewModel, triggers image generation
  - [ ] Uses same API key + OkHttpClient as chat (POST /v1/images/generations)
  - [ ] Image appears as styled chat bubble (rounded corners, fill-width)
  - [ ] Loading spinner during generation, error toast on failure
  - [ ] APK growth ≤ 250KB over baseline (Coil 3 only)
  - [ ] Optional: Pollinations AI free fallback for FOSS flavor
- **Touch:** `build.gradle.kts`, `OpenAiCompatibleClient.kt`, `ChatViewModel.kt`, `Screens.kt`
- **Out of scope:** image-to-image editing, multi-turn refinement, separate image gallery, SDXL/Flux via separate keys



### C-006 — UI stream paint throttle (if needed)
- **Status:** Done
- **Notes:** Throttle gate in `ChatViewModel.send()`: 250ms `lastUiUpdate` guard on `StreamEvent.Delta`; `StreamEvent.Done` flushes final paint; errors pass through immediately; `FeatureFlags.streamThrottleMs = 250L`; `PaintThrottleTest` with 8 test cases; 5 verify_static guards. Lowered `gradle.properties` JVM heap from 1536m→768m for the 4GB VPS.  
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

### R-006 — Lost-repo archaeology II (14 new repos + CMP AI chat starter kit)
- **Status:** Done
- **Deliverable:** `docs/LOST-REPOS-R006.md` + `docs/LOST-REPOS-R006-SUPPLEMENT.md`
- **Key finds:** NadeemIqbal/llm-typewriter (C-008 drop-in path), Messenger (Wear OS BYOK), ChatCat (MCP in thin client), EveryTalk (height placeholders), ChatPPP (token-budget context compression), AetherisAI (dual SSE), AndGPT (33KB Android 4+), markanywhere (KMP streaming markdown parser), Ke-Chat (11 providers + skill system). Subagent supplement: AndGPT01 (4PDA ghost, 33KB), ChatPPP token compression, AetherisAI dual SSE, Kai 9000 agent patterns, F-Droid ecosystem map.

### R-007 — Image generation for BYOK chat client
- **Status:** Done → C-011 Ready for coding
- **Deliverable:** `docs/IMAGE-GENERATION-RESEARCH.md`
- **Key findings:** OpenAI `/v1/images/generations` uses SAME API key as chat (no new auth). Coil 3 adds ~200KB APK. Slash-command `/imagine` pattern recommended for v1 (~80 lines of code). Pollinations AI offers free zero-key fallback for FOSS flavor. EveryTalk and ChatCat already implement image gen but LiteChat's slash-command approach would be novel in the BYOK ecosystem.

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
