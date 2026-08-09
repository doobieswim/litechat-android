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
- **Status:** Done
- **Notes:** llm-typewriter 0.1.1 integrated. Assistant messages render with StreamingTypewriter + markdown (bold, italic, code blocks, headings, lists). flowOf(text) for settled messages. User messages stay plain text. Static verify: 47/47.
- **R-006 finding:** `NadeemIqbal/llm-typewriter` (Apache 2.0, `io.github.nadeemiqbal:llm-typewriter:0.1.1`) is a drop-in streaming markdown typewriter for Compose. Also found: `xemantic/markanywhere` (semantic event-stream parser, KMP). Both viable.
- **Depends on:** R-006-1 (APK/RSS measurement) — not yet scheduled
- **Goal:** Add incremental streaming markdown rendering using llm-typewriter (preferred) or markanywhere. Plain text until measurement confirms APK cost is acceptable.  

### C-009 — Streaming height placeholders (from EveryTalk pattern)
- **Status:** Ready
- **Goal:** Combine `animateItemPlacement()` + `AnimatedContent(SizeTransform)` to prevent LazyColumn jumps when streaming markdown finishes. No new deps.
- **Source:** `roseforljh/EveryTalk` `PerformanceConfig.kt` + Reddit research
- **Touch:** `Screens.kt` (ChatScreen, MessageBubble)
- **APK cost:** 0 KB
- **Out of scope:** height estimation for markdown (plain text only until C-008)

### C-010 — Token-budget context compression (from ChatPPP pattern)
- **Status:** Ready
- **Goal:** Auto-truncate conversation history when token budget exceeded (approx 4 chars ≈ 1 token, default threshold 24k). Show "earlier messages truncated" indicator.
- **Source:** `NNCVA/ChatPPP` (★2)
- **Research:** `docs/CONTEXT-WINDOW-MANAGEMENT-C010.md` — full deep-dive with ChatPPP source analysis, code sketch, APK cost (0 KB Tier 1, ~1MB Tier 3 via JTokkit)
- **Touch:** `ChatViewModel.kt`, `OpenAiCompatibleClient.kt`, `Screens.kt` + new `data/context/ContextTrimmer.kt`
- **APK cost:** 0 KB (Tier 1 — pure stdlib approximation)
- **Dev effort:** ~3-4 hours
- **Tier 1 scope:**
  - [ ] New: `ContextTrimmer.kt` — approximate token counting (4 chars ≈ 1 token) + truncation to low watermark
  - [ ] Integration in `ChatViewModel.send()` — trim before API call, keep system prompt, never split turn pairs
  - [ ] UI: `TruncationBanner` composable — "N earlier messages not included" above message list
  - [ ] Settings: configurable token threshold (default 24,000) in DataStore
  - [ ] Unit tests: `ContextTrimmerTest.kt`
- **Out of scope:** LLM-based rolling summary (ChatPPP Tier 2 — separate ticket), JTokkit accurate counting (+1MB APK)

### C-011 — Image generation via /imagine slash command
- **Status:** Done
- **Notes:** Coil 3 deps (~200KB), generateImage() in client (POST /v1/images/generations), /imagine handler in ViewModel, [IMAGE:path] message convention, AsyncImage bubble, generating banner. Static verify: 39/39.
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

### C-012 — Prompt template variables (Pro-gated)
- **Status:** Done
- **Notes:** PromptTemplate data model with [Var] render(), CRUD in SettingsRepository (DataStore JSON), built-in Translate template, template picker row above chat input, Pro-gate: free=1 template, Pro=unlimited. Static verify: 44/44.
- **Goal:** Users create prompt templates with dynamic `[Variable]` fields. Pro users get unlimited; free tier gets 1 built-in. Store in DataStore JSON.
- **Research:** `docs/PREMIUM-STRATEGY.md` (R-008)
- **Touch:** `ChatViewModel.kt`, `Screens.kt`, `SettingsRepository.kt`
- **APK impact:** 0 KB (pure UI + data model)
- **Out of scope:** server-side template sharing, template marketplace

### C-013 — In-app web scraping /browse command (Pro-gated)
- **Status:** Ready
- **Goal:** `/browse <url>` fetches page content, extracts text, injects into conversation. Opera Mini pattern: thin client fetches, remote brain reads. Pro-only.
- **Research:** `docs/PREMIUM-STRATEGY.md` (R-008)
- **Touch:** `OpenAiCompatibleClient.kt`, `ChatViewModel.kt`
- **APK impact:** ~30KB (Jsoup for HTML extraction)
- **Out of scope:** JavaScript rendering, recursive crawling, image extraction

### C-014 — Manual chat backup/restore via SAF (Pro-gated)
- **Status:** Ready
- **Goal:** Export encrypted chat DB to user-chosen folder, import from file. Storage Access Framework. Pro-only. Palm HotSync pattern: user owns their data sync.
- **Research:** `docs/PREMIUM-STRATEGY.md` (R-008)
- **Touch:** `ChatViewModel.kt`, `Screens.kt` (Settings)
- **APK impact:** 0 KB
- **Out of scope:** auto-sync, Google Drive API, multi-device merge conflict resolution

### C-015 — Floating chat overlay (Pro-gated)
- **Status:** Ready
- **Goal:** SYSTEM_ALERT_WINDOW floating bubble → opens minimal Compose chat overlay over any app. Sideloaded users grant manually; Play Store auto-grants on API 29+. Future: Bubbles API for Android 12+.
- **Research:** `docs/DEEP-DIVE-C009-C016.md` (R-009) — Reddit: permission auto-grant on Play Store only, MIUI blocks by default, Bubbles API alternative for 12+
- **APK impact:** 0 KB
- **Touch:** New `OverlayService.kt`, `Screens.kt`, `AndroidManifest.xml`

### C-016 — Image attachment + vision model support (Pro-gated)
- **Status:** Ready  (scope changed from OCR screen capture)
- **Goal:** Users attach images from gallery → base64 encoded → sent to GPT-4V/Claude Vision via existing API key → model analyzes image. Covers OCR, "what's in this image", document reading.
- **Research:** `docs/DEEP-DIVE-C009-C016.md` (R-009)
- **APK impact:** 0 KB (all platform APIs)
- **Why no screen capture:** MediaProjection needs system dialog every time. Gallery attach is zero-permission, zero-friction, more generally useful.
- **Touch:** `OpenAiCompatibleClient.kt`, `ChatViewModel.kt`, `Screens.kt`
- **Out of scope:** on-device OCR (ML Kit), real-time camera, screen recording

### C-017 — Provider failover chain (Kai 9000 steal)
- **Status:** Ready
- **Goal:** Ordered provider list. If primary fails (5xx/timeout), auto-try next. Survives single-provider outages.
- **Source:** Kai 9000 + `docs/COMPETITIVE-STEAL-LIST.md`
- **APK:** 0 KB | **Touch:** `ChatViewModel.kt`, `SettingsRepository.kt`

### C-018 — Per-conversation model binding (AetherisAI steal)
- **Status:** Ready
- **Goal:** Each conversation remembers its model. Returning to old chat auto-switches. Add `model` field to ConversationEntity.
- **Source:** AetherisAI + `docs/COMPETITIVE-STEAL-LIST.md`
- **APK:** 0 KB | **Touch:** `Entities.kt`, `ChatRepository.kt`, `ChatViewModel.kt`

### C-019 — Provider connection test button (ChatCat steal)
- **Status:** Ready
- **Goal:** "Test Connection" button in Settings → GET /v1/models → success/failure before saving.
- **Source:** ChatCat + `docs/COMPETITIVE-STEAL-LIST.md`
- **APK:** 0 KB | **Touch:** `Screens.kt`, `OpenAiCompatibleClient.kt`

### C-020 — Persistent user memory (Kai 9000 steal, Pro-gated)
- **Status:** Ready
- **Goal:** Repeated user facts ("I prefer short answers") auto-promoted to system prompt after N repetitions. Stored in Room. Pro-only.
- **Source:** Kai 9000 + `docs/COMPETITIVE-STEAL-LIST.md`
- **APK:** 0 KB | **Touch:** New `MemoryManager.kt`, `Entities.kt`, `ChatViewModel.kt`

### C-021 — Voice input via Android SpeechRecognizer (EveryTalk steal)
- **Status:** Ready
- **Goal:** Mic button in chat composer → Android built-in SpeechRecognizer → transcribed text. Zero deps, zero APK.
- **Source:** EveryTalk + `docs/COMPETITIVE-STEAL-LIST.md`
- **APK:** 0 KB | **Touch:** `Screens.kt`, `ChatViewModel.kt`

### C-022 — Settings export/import (JSON)
- **Status:** Ready
- **Goal:** Export provider configs + templates as JSON. Import from file. Reduces multi-device setup friction.
- **Source:** Agora key rotation pattern + `docs/COMPETITIVE-STEAL-LIST.md`
- **APK:** 0 KB | **Touch:** `SettingsRepository.kt`, `Screens.kt`

### C-023 — Multi-key per provider (Agora steal)
- **Status:** Ready
- **Goal:** Named API keys per provider with radio-button active selection + masked previews. Users can have "Work OpenAI" and "Personal OpenAI" keys.
- **Source:** Agora subagent deep-dive + `docs/COMPETITIVE-STEAL-LIST.md`
- **APK:** ~15KB | **Touch:** `SettingsRepository.kt`, `SecureStore.kt`, `Screens.kt`

### C-024 — Conversation forks (Agora steal)
- **Status:** Ready
- **Goal:** Branch conversation at any message. Message tree with branch selection. Forked branches are independent. Useful for "try different model" or "try different prompt."
- **Source:** Agora subagent deep-dive + `docs/COMPETITIVE-STEAL-LIST.md`
- **APK:** ~25KB | **Touch:** `Entities.kt`, `ChatRepository.kt`, `Screens.kt`

### D-004 — Distribution pack
- **Status:** Ready
- **Goal:** F-Droid metadata, XDA post template, awesome-byok-apps PR, r/androidafterlife post. Zero code.
- **Source:** `docs/X-TWITTER-COMPETITIVE.md`
- **APK:** 0 KB



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

### R-008 — Premium tier strategy & feature roadmap
- **Status:** Done — C-012, C-013, C-014 Ready; C-015, C-016 Ideas
- **Deliverable:** `docs/PREMIUM-STRATEGY.md`
- **Key findings:** $4.99 one-time validated as impulse-buy sweet spot (100% margin after 15% Play cut). 4 Pro-gated features evaluated against thin-client constraints: prompt variables (★best ROI), web scraping /browse (Opera Mini pattern), cloud sync via SAF (HotSync pattern), floating overlay + cloud OCR (deferred to Ideas). Priority: C-012 first (zero APK, highest Pro value per dev hour).

### R-009 — Deep-dive: C-009/C-010/C-015/C-016
- **Status:** Done — all four promoted to Ready
- **Deliverable:** `docs/DEEP-DIVE-C009-C016.md` + `docs/CONTEXT-WINDOW-MANAGEMENT-C010.md` (subagent)
- **Key findings:** C-009: animateItemPlacement + AnimatedContent (0 KB). C-010: ChatPPP 3-tier, Tier 1 truncation at 24k/14k tokens (0 KB). C-015: SYSTEM_ALERT_WINDOW + Bubbles API future-proofing (0 KB). C-016: image attach + GPT-4V instead of MediaProjection screen capture (0 KB). Reddit-sourced caveats for C-015: Play-only auto-grant, MIUI blocks.

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
