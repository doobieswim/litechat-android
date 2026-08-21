# LiteChat backlog

Single task queue for the five-agent team.  
Statuses: `Idea` · `Research` · `Ready` · `Doing` · `Done` · `Blocked`

Flow: **DIG (Research) → PROOF Approve → Ready → WIRE → Done → REVIEW**  
Bug lane: **DEBUG → `B-00N` in `docs/BUGS.md` → Ready if cause proven (no PROOF) → WIRE → REVIEW**  
Every **research** ticket needs `LITECHAT-PROOF` Approve before Ready (human may override in one written line).

Coding agent: only take **Ready** (or human-named id). Claim with `Doing`, finish with `Done`.

---

## Now / next (coding)

### C-032 — Play compliance: in-app AI-content reporting + acceptable-use (policy-mandated)
- **Status:** Done — WIRE 2026-08-15. PROOF approved Research B 2026-08-15 (this ticket was flagged for pre-PROOF Ready; re-affirmed post-approval). Not a product choice: Play rejects apps that generate AI content without in-app reporting. Independent of H-008.
- **Fixed (2026-08-15):** long-press → "Report content" on every bubble (text, [IMAGE:], [VIDEO:]) → reason picker → mailto to litechat@proton.me (zero server); one-time acceptable-use dialog after onboarding (no dismiss path); EEA/UK non-personalized ads via RequestConfiguration.PublisherPrivacyPersonalizationState.DISABLED (play-services-ads 23.x removed npa/setNonPersonalizedAds — zero UMP SDK); "no ads in overlay" guard comment. Files: `Screens.kt`, `ChatViewModel.kt`, `SettingsRepository.kt`, `AdMobLazyInit.kt` (play), `OverlayService.kt`. Verify: 92/92 static, both flavors compile, unit tests pass, foss debug APK built.
- **AC:**
  - [x] Long-press → **Report** on every message bubble (text, /imagine, /video results)
  - [x] In-app report flow: reason picker → opens dev contact (mailto or GitHub issue URL) — **no server** (zero-server law)
  - [x] One-time **acceptable-use acceptance** screen on first launch (CSAM/sexual/violence/deception prohibited; note that the user's model provider applies its own safety filters)
  - [x] Ads never run in the overlay service (already true — added guard comment)
  - [x] EEA/UK: UMP consent **or** serve non-personalized ads only — chose non-personalized-only via RequestConfiguration (zero APK cost)
  - [x] Static verify green (92/92). CI not run this pass.
- **Out of scope:** real AdMob IDs (human needs an AdMob account), Play Console Data Safety form answers (human, per audit §2), $25 Play fee.

### C-031 — Rebrand user-facing name to BYO AI
- **Status:** Done — WIRE 2026-08-15. REVIEW Issues 1–6 fixed (user-facing LiteChat copy). Static verify after fix.
- **Touched:** `strings.xml`, `app/build.gradle.kts`, `README.md`, `privacy.html`, `app/src/main/assets/privacy.html`, `docs/PLAY-LISTING-DRAFT.md` (created), `scripts/verify_static.py` (6 C-031 guards)
- **Goal:** Users see **BYO AI**. App id becomes `com.byoai.chat` before first Play upload. Code package stays LiteChat.
- **Research:** `docs/APP-NAMING-RESEARCH.md` · H-001 · `docs/THEME-SHOW-DONT-TELL.md` · `docs/GREY-SALES-GOOD-TWINS.md`
- **Locked copy (everyday only):**
  - Name: **BYO AI** (no “4GB” in the title)
  - Short description (80 max): *Chat with your own key. Works on 4GB phones. No monthly bill.*
  - Banned: fight words, “LiteChat” on the icon/store, “BYOK” as the title, “runs a real model on 4GB”
- **AC (for WIRE after PROOF Approve):**
  - [x] `strings.xml` `app_name` → `BYO AI`
  - [x] `applicationId` in `app/build.gradle.kts` → `com.byoai.chat`; **namespace + Kotlin package stay `com.litechat.android`**
  - [x] README title/tagline; note “internal codename LiteChat” once
  - [x] `docs/PLAY-LISTING-DRAFT.md` name + subtitle (everyday words only)
  - [x] `privacy.html` (assets + repo root) brand text
  - [x] `verify_static.py` user-facing “LiteChat” guards updated if any
  - [x] Static verify green (81/81). CI not run this pass.
- **Out of scope:** icon/logo, domain buy, GitHub repo rename, paid ads, Play upload
- **Cost:** $0 now. Play listing later ~$25 — warn again before anyone pays.

### C-007 — Wire privacy link and disclaimer in Settings
- **Status:** Done
- **Notes:** Added `app/src/main/assets/privacy.html` (bundled), `privacy.html` (repo root for GitHub Pages), Settings "Privacy Policy" link via `ACTION_VIEW`, upgraded disclaimer to full BYOK wording from research, added Disclaimer section + Privacy Policy link to README.md

### C-008 — Markdown rendering (post-v1, DEFERRED)
- **Status:** Idea — **reverted 2026-08-09**
- **Notes:** C-008 was implemented with llm-typewriter 0.1.1 but CI fails: library has no Android target (desktop/iOS/WASM only in Gradle module metadata — no `androidApiElements` variant). Dependency removed; assistant messages reverted to plain `Text` composable. See `docs/MARKDOWN-COST.md` for full re-evaluation criteria and recommended approach (multiplatform-markdown-renderer, not llm-typewriter).
- **Goal:** Add incremental streaming markdown rendering using multiplatform-markdown-renderer (maid-native pattern). Plain text for v1.  

### C-009 — Streaming height placeholders (from EveryTalk pattern)
- **Status:** Done
- **Notes:** EveryTalk Layer 1: detect open code blocks (``` with odd backtick count) and tables (| at line start) in streaming text, inject Spacer(32.dp/8.dp). Prevents LazyColumn jump when fence closes. 0 deps, ~20 lines. Static verify: 50/50.
- **Goal:** Combine `animateItemPlacement()` + `AnimatedContent(SizeTransform)` to prevent LazyColumn jumps when streaming markdown finishes. No new deps.
- **Source:** `roseforljh/EveryTalk` `PerformanceConfig.kt` + Reddit research
- **Touch:** `Screens.kt` (ChatScreen, MessageBubble)
- **APK cost:** 0 KB
- **Out of scope:** height estimation for markdown (plain text only until C-008)

### C-010 — Token-budget context compression (from ChatPPP pattern)
- **Status:** Done
- **Notes:** ContextTrimmer.kt: 4 chars/token, 24k high-water, 14k low-water. Keeps system prompt, never splits turn pairs. Banner shows truncated count. 0 KB APK, ~70 lines. Static verify: 53/53.
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

### C-028 — Stream video download to disk (RAM safety)
- **Status:** Done (2026-08-14)
- **Notes:** `pollVideo()` now streams the MP4 to `cacheDir` via `byteStream().copyTo(FileOutputStream)` — never materializes a full ByteArray in heap. Returns `File`, deletes partial on failure. Caller runs on `Dispatchers.IO`. REVIEW finding C1 (the old code was `body.bytes()`), plus C2 (all blocking client calls wrapped in `withContext(IO)`), C3 (Stop actually stops: `streamJob` assigned + `stopRequested` flag + rethrow CancellationException), C9 (no ghost "Retrying…"/"Error:" rows — one assistant row reused across retries).

### C-029 — Generated media disk cap (storage safety)
- **Status:** Done (2026-08-14)
- **Notes:** New `util/MediaCleanup.kt` — FIFO (by `lastModified()`) eviction of `gen_*`/`vid_*` files in `cacheDir`, band-tuned caps (TIGHT=20MB, COMFORTABLE=50MB, ROOMY/GENEROUS=150MB). Called after every successful `/imagine` and `/video`. Never touches chat DB or Coil disk cache.

### C-030 — Image display size uses band-tuned config
- **Status:** Done (2026-08-14)
- **Notes:** `Screens.kt` MessageBubble image request now reads size from `ImageCacheConfig.displaySize(DeviceCompat.snapshot(...).band)` instead of hardcoded `.size(540, 540)`. TIGHT decodes 360×360 (~56% less bitmap memory than 540).

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
- **Status:** Done
- **Notes:** Jsoup dep, /browse handler, fetchPage(). 53/53. 2026-08-14: REVIEW Part D fix — page content now fed to the model via completeChat (previously stored as assistant text, model never called → no answer).
- **Goal:** `/browse <url>` fetches page content, extracts text, injects into conversation. Opera Mini pattern: thin client fetches, remote brain reads. Pro-only.
- **Research:** `docs/PREMIUM-STRATEGY.md` (R-008)
- **Touch:** `OpenAiCompatibleClient.kt`, `ChatViewModel.kt`
- **APK impact:** ~30KB (Jsoup for HTML extraction)
- **Out of scope:** JavaScript rendering, recursive crawling, image extraction

### C-014 — Manual chat backup/restore via SAF (Pro-gated)
- **Status:** Done
- **Notes:** SAF export/import for chat database. 0 KB.
- **Goal:** Export encrypted chat DB to user-chosen folder, import from file. Storage Access Framework. Pro-only. Palm HotSync pattern: user owns their data sync.
- **Research:** `docs/PREMIUM-STRATEGY.md` (R-008)
- **Touch:** `ChatViewModel.kt`, `Screens.kt` (Settings)
- **APK impact:** 0 KB
- **Out of scope:** auto-sync, Google Drive API, multi-device merge conflict resolution

### C-015 — Floating chat overlay (Pro-gated)
- **Status:** Done — **fixed 2026-08-15 (WIRE):** the service started but never showed the window (`showOverlay()` was never called) and the overlay had no send path. Now: `onStartCommand` → `showOverlay()`, input + Send + reply via `completeChat` into a persistent "Overlay" conversation, IME-friendly window flags, and the Settings toggle enforces the Pro gate (was ungated). Files: `OverlayService.kt`, `Screens.kt`. Verify: 92/92 static + both flavors compile.
- **Notes:** OverlayService + SYSTEM_ALERT_WINDOW, foreground. 0 KB.
- **Goal:** SYSTEM_ALERT_WINDOW floating bubble → opens minimal Compose chat overlay over any app. Sideloaded users grant manually; Play Store auto-grants on API 29+. Future: Bubbles API for Android 12+.
- **Research:** `docs/DEEP-DIVE-C009-C016.md` (R-009) — Reddit: permission auto-grant on Play Store only, MIUI blocks by default, Bubbles API alternative for 12+
- **APK impact:** 0 KB
- **Touch:** New `OverlayService.kt`, `Screens.kt`, `AndroidManifest.xml`

### C-016 — Image attachment + vision model support (Pro-gated)
- **Status:** Done
- **Notes:** Image/file attachment, base64 for vision. 0 KB.
- **Goal:** Users attach images from gallery → base64 encoded → sent to GPT-4V/Claude Vision via existing API key → model analyzes image. Covers OCR, "what's in this image", document reading.
- **Research:** `docs/DEEP-DIVE-C009-C016.md` (R-009)
- **APK impact:** 0 KB (all platform APIs)
- **Why no screen capture:** MediaProjection needs system dialog every time. Gallery attach is zero-permission, zero-friction, more generally useful.
- **Touch:** `OpenAiCompatibleClient.kt`, `ChatViewModel.kt`, `Screens.kt`
- **Out of scope:** on-device OCR (ML Kit), real-time camera, screen recording

### C-017 — Provider failover chain (Kai 9000 steal)
- **Status:** Done
- **Notes:** Provider list in SettingsRepository, failover in send(). 0 KB.
- **Goal:** Ordered provider list. If primary fails (5xx/timeout), auto-try next. Survives single-provider outages.
- **Source:** Kai 9000 + `docs/COMPETITIVE-STEAL-LIST.md`
- **APK:** 0 KB | **Touch:** `ChatViewModel.kt`, `SettingsRepository.kt`

### C-018 — Per-conversation model binding (AetherisAI steal)
- **Status:** Done
- **Notes:** ConversationEntity.model field, per-conversation switching. 0 KB.
- **Goal:** Each conversation remembers its model. Returning to old chat auto-switches. Add `model` field to ConversationEntity.
- **Source:** AetherisAI + `docs/COMPETITIVE-STEAL-LIST.md`
- **APK:** 0 KB | **Touch:** `Entities.kt`, `ChatRepository.kt`, `ChatViewModel.kt`

### C-019 — Provider connection test button (ChatCat steal)
- **Status:** Done
- **Notes:** Test Connection button in SettingsScreen. 0 KB.
- **Goal:** "Test Connection" button in Settings → GET /v1/models → success/failure before saving.
- **Source:** ChatCat + `docs/COMPETITIVE-STEAL-LIST.md`
- **APK:** 0 KB | **Touch:** `Screens.kt`, `OpenAiCompatibleClient.kt`

### C-020 — Persistent user memory (Kai 9000 steal, Pro-gated)
- **Status:** Done — **fixed 2026-08-15 (WIRE):** MemoryManager existed but nothing called it (dead code). Now wired into `send()`: "Remember …" lines are recorded (5 hits → immediate promotion) and promoted memories are prepended to the system prompt — Pro only. Settings gains "Clear memory". Files: `ChatViewModel.kt`, `AppContainer.kt`, `MemoryManager.kt`, `Screens.kt`.
- **Notes:** MemoryManager with hit-count promotion (Kai 9000). 0 KB.
- **Goal:** Repeated user facts ("I prefer short answers") auto-promoted to system prompt after N repetitions. Stored in Room. Pro-only.
- **Source:** Kai 9000 + `docs/COMPETITIVE-STEAL-LIST.md`
- **APK:** 0 KB | **Touch:** New `MemoryManager.kt`, `Entities.kt`, `ChatViewModel.kt`

### C-021 — Voice input via Android SpeechRecognizer (EveryTalk steal)
- **Status:** Done
- **Notes:** Voice input mic button in chat composer. 0 KB.
- **Goal:** Mic button in chat composer → Android built-in SpeechRecognizer → transcribed text. Zero deps, zero APK.
- **Source:** EveryTalk + `docs/COMPETITIVE-STEAL-LIST.md`
- **APK:** 0 KB | **Touch:** `Screens.kt`, `ChatViewModel.kt`

### C-022 — Settings export/import (JSON)
- **Status:** Done — **fixed 2026-08-15 (WIRE):** no settings JSON export existed (the SAF buttons only did chat-DB backup). Now `exportSettingsJson`/`importSettingsJson` (no secrets: keys stay in SecureStore, Pro state and compliance flags excluded) + Settings "Export/Import settings" buttons. Files: `SettingsRepository.kt`, `ChatViewModel.kt`, `Screens.kt`.
- **Notes:** Settings export/import as JSON. 0 KB.
- **Goal:** Export provider configs + templates as JSON. Import from file. Reduces multi-device setup friction.
- **Source:** Agora key rotation pattern + `docs/COMPETITIVE-STEAL-LIST.md`
- **APK:** 0 KB | **Touch:** `SettingsRepository.kt`, `Screens.kt`

### C-023 — Multi-key per provider (Agora steal)
- **Status:** Done — **fixed 2026-08-15 (WIRE):** NamedKeyStore existed but nothing used it (dead code). Now the active named key overrides the primary key in every send; Settings gains a "Saved keys" add/list/Use/Delete section. Files: `AppContainer.kt`, `ChatViewModel.kt`, `Screens.kt`.
- **Notes:** NamedKeyStore for multi-key per provider. Encrypted.
- **Goal:** Named API keys per provider with radio-button active selection + masked previews. Users can have "Work OpenAI" and "Personal OpenAI" keys.
- **Source:** Agora subagent deep-dive + `docs/COMPETITIVE-STEAL-LIST.md`
- **APK:** ~15KB | **Touch:** `SettingsRepository.kt`, `SecureStore.kt`, `Screens.kt`

### C-024 — Conversation forks (Agora steal)
- **Status:** Done — **fixed 2026-08-15 (WIRE):** forks were schema-only (a `parentId` column with zero logic). Now `forkConversation()` copies the prefix into an independent branch (fork-point copy keeps `parentId`); long-press any message → "Fork from here". Files: `ChatRepository.kt`, `ChatViewModel.kt`, `Screens.kt`.
- **Notes:** Conversation forks: parentId in MessageEntity.
- **Goal:** Branch conversation at any message. Message tree with branch selection. Forked branches are independent. Useful for "try different model" or "try different prompt."
- **Source:** Agora subagent deep-dive + `docs/COMPETITIVE-STEAL-LIST.md`
- **APK:** ~25KB | **Touch:** `Entities.kt`, `ChatRepository.kt`, `Screens.kt`

### D-004 — Distribution pack
- **Status:** Done
- **Notes:** F-Droid metadata YAML + XDA post template + awesome-byok-apps prep.

### C-025 — Wire stubs (C-021/C-022/C-016/C-015)
- **Status:** Done
- **Notes:** All 4 stubs wired: voice (SpeechRecognizer intent), attach (gallery picker), backup/restore (SAF), overlay toggle. 71/71 verify.
- **Goal:** Connect mic button to SpeechRecognizer intent, export/import to SAF file picker, attach button to gallery picker, overlay to settings toggle.
- **Source:** R-010 gap analysis
- **APK:** 0 KB | **Touch:** `Screens.kt`, `ChatViewModel.kt`, `AndroidManifest.xml`

### C-026 — Add verify_static guards for C-013 through C-024
- **Status:** Done
- **Notes:** 18 new guards + fixed all_kt ordering. 71/71 passed.
- **Goal:** Prevent regressions on all batch 1-3 tickets. 12 new guards.
- **Source:** R-010 gap analysis
- **APK:** 0 KB | **Touch:** `scripts/verify_static.py`

### D-005 — Complete distribution: Play Store + awesome-byok-apps + README
- **Status:** Done — WIRE 2026-08-15. README rewritten with full feature set + real measured APK sizes (foss 1.6 MB, play 3.2 MB from CI artifacts); fastlane F-Droid copy aligned to locked brand line + real size; XDA template made honest (dev framing, accurate features); r/androidafterlife draft + awesome-byok-apps entry finalized in `docs/LAUNCH-PACK.md`; awesome-byok-apps PR opened (yatsyk/awesome-byok-apps#11). Verify: 92/92 static.
- **Goal:** Play Store short+full description, README update with all features, awesome-byok-apps PR, r/androidafterlife post.
- **Source:** R-010 gap analysis
- **APK:** 0 KB | **Docs only**
- **Remaining (human):** post r/androidafterlife + XDA/4PDA threads, pay $25 + upload to Play, tag v1.0.0 + create GitHub Release, submit fdroiddata MR.

### D-006 — Wire Fastlane listing files into the build
- **Status:** Done — WIRE 2026-08-15. Metadata-only (no Ruby gem, no Play upload, **$0**). Added `title.txt` + `changelogs/1.txt` + `fastlane/README.md`. 13 guards in `verify_static.py` so CI `static-verify` fails if listing copy is wrong (title, locked short line, 80/4000 limits, measured 1.6 MB, foss "no billing", changelog matches `versionCode`). Suite: **105/105**.
- **Out of scope:** installing the Fastlane Ruby gem; Play `supply` upload (needs the **$25** account + service-account JSON — ask first).

### C-033 — Caveman provider + model picker
- **Status:** Done — WIRE 2026-08-15. Onboarding + Settings use `ProviderSetupFields`: pick provider, pick model, paste key. URL fills itself. Custom still shows the URL box. Catalog: Gemini, Groq, OpenRouter, Hugging Face, Grok/xAI, OpenAI, DeepSeek, Mistral, Ollama, Custom. Paid providers (Grok/OpenAI/DeepSeek/Mistral) show a money warning. **No Grok website login** — that needs a paid SuperGrok/X account and is not a paste-key flow.
- **APK:** ~0 KB extra

### C-034 — Agent Lab door (no Termux inside the APK)
- **Status:** Done — WIRE 2026-08-15. Opt-in Settings card only. `AgentLabGate` refuses TIGHT/COMFORTABLE and low storage (<400 MB). ROOMY = warn, GENEROUS = door. Open Termux **only if already installed**. No Node/Python/proot download. Tests: `AgentLabGateTest`.
- **APK:** ~0 KB extra (no runtime)

### C-035 — REVIEW fixes: streaming + named keys + trimmer + ANR + attach
- **Status:** Done — WIRE 2026-08-15. Fixed all parent-confirmed REVIEW issues: (1) `ChatSseParser.parseEvent` now accepts already-stripped SSE payloads (the stream→UI pipeline was dropping every event); (2) `callbackFlow` `cancel()` qualified to the client (`this@OpenAiCompatibleClient.cancel()`) so it cancels the HTTP call, not its own channel; (3) `NamedKeyStore.withKey` resolves the index after upsert so a new active key stays active; (4) `ContextTrimmer` drops oldest turn pairs as a unit and counts system tokens; (5) Fetch models / Test wrapped in `withContext(Dispatchers.IO)`; (6) attachImage loops quality+sample down so base64 always fits the 32k input cap. New tests: parser chain (2), ContextTrimmer (3), NamedKeyStoreLogic (4). 7 regression guards added. Suite **129/129**.
- **Tests:** `ChatSseParserTest`, `ContextTrimmerTest`, `NamedKeyStoreLogicTest`

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

### R-010 — Final gap analysis: everything missed
- **Status:** Done — C-025/C-026/D-005 Ready
- **Deliverable:** `docs/GAP-ANALYSIS-R010.md`
- **Key findings:** 4 stubs need wiring (C-021/022/016/015). No verify guards for batch tickets. Competitor patterns: scheduled tasks, stream block parser, i18n, model caching, stream pause, DDG scraper, draft persistence, backup encryption. Distribution: Play Store listing + awesome-byok-apps PR + README update still needed. Zero tests for new features.

### R-011 — Historic sales & positioning playbook
- **Status:** Done — research only. **No Ready child.** PROOF 2026-08-15 Issues addressed (backstage stamp + everyday launch copy). Waiting second PROOF pass.
- **Deliverable:** `docs/SALES-POSITIONING-HISTORIC.md`
- **Key findings:** Tired aisle; honesty / small / no-rent as mechanism. User-facing words now defer to `THEME-SHOW-DONT-TELL.md`. Name map still waits on H-004 + H-001. C-031 stays Blocked.
- **Out of scope:** coding, Ready tickets, fight-talk listing copy.

### R-012 — Unconventional shine (fight promoters + carnival)
- **Status:** Done — research only. **No Ready child.** Steal list stamped backstage after H-006.
- **Deliverable:** `docs/SHINE-UNCONVENTIONAL.md`
- **Key findings:** Shine = real numbers + calm first screenshot + pay-once after use. No bout title, no named enemy on Play. H-005 rewritten as logistics.
- **Out of scope:** coding, paid ads, Ready tickets.

### R-013 — King's Road underdog theme
- **Status:** Done — human locked **H-006 quieter**
- **Deliverable:** `docs/KINGS-ROAD-THEME.md` (backstage) + `docs/THEME-SHOW-DONT-TELL.md` (**user-facing law**)
- **Key findings:** Underdog vs fat/expensive apps lives in **bones** (small APK, honest plain “low memory” note, no account, $4.99 once, refuse heavy work). Users never hear fight words. Everyday chat app on the face. Banned: champ/kick-out/underdog/weigh-in on listing or UI. Coding: a feature that makes us louder or fatter fails the theme.
- **Out of scope:** wrestler mascot, bout-card Play listing, coding.

### R-014 — Grey sales, honest twins
- **Status:** Research — waiting `LITECHAT-PROOF` (no Ready child)
- **Deliverable:** `docs/GREY-SALES-GOOD-TWINS.md`
- **Key findings:** Keep the human button (demo, one next step, which-not-if, real scarcity). Throw away the lie, fake clock, and trap door. Best twin we already have: $4.99 once (kills the whole subscription maze). Never write boiler-room scripts, fake urgency, confirmshame, or SoftRAM claims.
- **Out of scope:** coding, Ready tickets, user-facing fight/pressure copy.

### R-015 — Overnight tech myths + real roadmap
- **Status:** Research — waiting `LITECHAT-PROOF` (no Ready child)
- **Deliverable:** `docs/OVERNIGHT-ROADMAP.md`
- **Key findings:** Overnight is a myth. Hits were years + one simple loop + still being around. Roadmap: demo is the product → one true share picture → underground then Play → stay thin (Opera Mini slot) → don’t pull if it spikes. No fake waitlist, no paid streamers, no Hotmail stamp on user chat.
- **Out of scope:** coding, name list, Ready tickets.

### R-016 — Pro incentives: 3-dig deep dive (competitive / trends / latent wants)
- **Status:** Research — waiting `LITECHAT-PROOF` + human H-008 (no Ready child)
- **Deliverables:** `docs/PRO-SCAN-COMPETITIVE.md` · `docs/PRO-TRENDS-NEXT-BIG.md` · `docs/PRO-LATENT-WANTS.md` · **synthesis:** `docs/PRO-ROADMAP.md`
- **Key findings:** The bundle = **Voice + Memory + Ownership at $4.99 once** ("Your key. Your voice. It remembers. No monthly bill."). Tier 1 (v1.1, ~0 KB): voice mode, memory+/recall, full-text search, encrypted backup upgrade, template deep, quiet+registration screen, web search. Tier 2 (v1.2+): BYO-Sync, tasks mode, overlay v2, usage dashboard, custom look, profiles. **Never gate:** core chat, failover, compat matrix, key security, /imagine & /video, i18n, LAN/Ollama, basic export, community.
- **Proposed tickets (Research, need PROOF+human):** P-001 voice · P-002 search · P-003 backup upgrade · P-004 quiet/registration · P-005 web search · P-006 memory+ · P-007 sync · P-008 tasks
- **Out of scope:** coding, subscription models (never), Ready tickets.
- **Status update (2026-08-15):** `LITECHAT-PROOF` **Approve** (full-queue re-verification) + human **H-008 = A (full Tier 1 bundle)**. P-001–P-006 + P-009–P-014 promoted to **Ready** below. P-007 (BYO-Sync) + P-008 (tasks) stay Research (Tier 2, v1.2+).

---

## Tier 1 Pro bundle — Ready (H-008 = A, human 2026-08-15)

Flow-complete: PROOF Approve (2026-08-15 full-queue) + human decision. All tickets: Pro = $4.99 once (existing `BYO_pro`), ~0 KB APK each, API-side. Never-gate list unchanged (core chat, failover, languages, /imagine & /video, multi-key, compat matrix).

### P-001 — Voice mode (Pro; free = 1 voice exchange/day)
- **Status:** Done — WIRE 2026-08-16. **v1 STT is the phone speech box** (lands in the composer, never auto-send, does **not** burn the daily slot, does **not** use the key). `transcribeAudio()` exists for later. Read-aloud uses `/v1/audio/speech` (counts as the 1/day free use). Slot is only taken after there is text to read. `MediaPlayer.prepare()` is on IO. Quiet cost line.
- **Goal:** Mic → Whisper STT (Groq free tier OK) → chat → TTS read-aloud. Extends C-021 voice stub.
- **AC:**
  - [ ] STT via OpenAI-compatible `/v1/audio/transcriptions` with the active key + baseUrl
  - [ ] Transcription lands in the composer (user presses Send — never auto-send)
  - [ ] Read-aloud TTS (`/v1/audio/speech`): play / pause / stop, stream to file (C-028 pattern, no heap blowups)
  - [ ] Pro gate: free tier = 1 voice exchange/day, Pro = unlimited (count-limit, roadmap row 1)
  - [ ] Quiet cost line near the mic: "voice uses your key — can cost money" (Groq free tier noted)
  - [ ] All audio on `Dispatchers.IO`; cancel on Stop
  - [ ] Unit test for the daily-limit gate; verify_static guards
- **Files:** `OpenAiCompatibleClient.kt`, `ChatViewModel.kt`, `Screens.kt`, Pro-gate/FeatureFlags
- **Out of scope:** on-device STT, wake word, bundled voices, speaker diarization
- **Research:** `docs/PRO-ROADMAP.md` row 1 · `docs/PRO-TRENDS-NEXT-BIG.md`
- **Cost:** bills the user's own provider key (like chat); app itself stays $4.99 once

### P-002 — Full-text search across chats (Pro)
- **Status:** Done — WIRE 2026-08-16. Room FTS4 + Pro search UI. REVIEW I (2026-08-16): `searchJob?.cancel()` + ignore stale hits so a slow "a" cannot overwrite "ab".
- **Goal:** Search every message. Room FTS4/5 virtual table (~0 KB).
- **AC:**
  - [x] FTS table + sync on message insert (Room migration on upgrade)
  - [x] Search entry in Settings (or top bar) → results grouped by conversation
  - [x] Tap result opens the conversation at that message
  - [x] Pro gate
  - [x] FTS query-escaping unit test (quotes/dashes) + verify_static guards
- **Files:** `Entities.kt`, `ChatRepository.kt`, `ChatViewModel.kt`, `Screens.kt`
- **Out of scope:** semantic/vector search, filters (per-conversation) in v1
- **Research:** `docs/PRO-ROADMAP.md` row 3 · `docs/PRO-LATENT-WANTS.md` P-003

### P-003 — Encrypted backup upgrade (Pro)
- **Status:** Done — WIRE 2026-08-16. AES-GCM + PBKDF2, streamed 8 KB chunks. REVIEW E/F (2026-08-16): password armed on the ViewModel before the file picker; Backup/Restore buttons stay closed until Pro.
- **Goal:** AES-encrypted chat export, scheduled local backups, restore-to-new-phone. Extends C-014 SAF backup.
- **AC:**
  - [ ] Export DB → AES-GCM encrypted file; key from user passphrase (PBKDF2, salt stored in file header)
  - [ ] Import: decrypt + WAL-safe restore (reuse C-008 pattern)
  - [ ] Optional quiet "back up?" reminder (one, dismissible — never a nag)
  - [ ] Pro gate (C-014 already Pro)
  - [ ] Restore-to-new-phone end-to-end test path (debug build)
  - [ ] No plaintext keys on disk
- **Files:** `ChatViewModel.kt`, new `util/BackupCrypto.kt`, `Screens.kt` (Settings)
- **Out of scope:** vendor cloud, auto-sync (that is P-007, Tier 2)
- **Research:** `docs/PRO-ROADMAP.md` row 4 · `docs/PRO-LATENT-WANTS.md` #1/#4

### P-004 — Quiet + registration screen (Pro)
- **Status:** Done — WIRE 2026-08-16. Settings shows "Registered — BYO AI · date · no renewal, ever". Upgrade button hidden for Pro. First Pro day is stamped.
- **Goal:** Lifetime "Registered" screen; paid users never see a sale prompt again.
- **AC:**
  - [ ] After Pro purchase: calm screen "Registered — BYO AI · <date> · no renewal, ever"
  - [ ] Zero remaining upsell prompts for Pro users (grep all sale strings; gate check)
  - [ ] Banner removal already `isPro`-gated — verify
  - [ ] Theme-law everyday words only (`docs/THEME-SHOW-DONT-TELL.md`)
  - [ ] verify_static guards
- **Files:** `Screens.kt`, `SettingsRepository.kt`, `BillingRepository.kt`
- **Out of scope:** accounts, cloud registration, receipts
- **Research:** `docs/PRO-ROADMAP.md` row 6 · `docs/PRO-LATENT-WANTS.md` P-001/P-006

### P-005 — Web search (Pro)
- **Status:** Done — WIRE 2026-08-16. `/search` → DDG HTML → model keeps URLs. REVIEW D (2026-08-16): lives on `streamJob`, Stop FAB shows.
- **Goal:** `/search <query>` → DDG HTML search → top results → model answers with source links. Extends C-013 /browse.
- **AC:**
  - [ ] `/search` handler: DDG results via Jsoup (reuse C-013 stack — no second HTTP client)
  - [ ] Top results fed to model via `completeChat` (C-013 pattern)
  - [ ] Answer carries plain-text source URLs
  - [ ] Pro gate
  - [ ] Timeout + `Dispatchers.IO`; cancel on Stop
- **Files:** `OpenAiCompatibleClient.kt` (`fetchSearch`), `ChatViewModel.kt`, `Screens.kt`
- **Out of scope:** Google/Bing paid APIs, JS rendering, result caching
- **Research:** `docs/PRO-ROADMAP.md` row 7

### P-006 — Memory+ (Pro)
- **Status:** Done — WIRE 2026-08-16. `/recall`, Settings list/edit/delete, rolling summary when history is trimmed.
- **Goal:** `/recall` command, rolling summaries, memory visible + editable. Extends C-020 MemoryManager.
- **AC:**
  - [ ] `/recall <topic>` surfaces promoted memories
  - [ ] Rolling summary: when history exceeds budget, oldest turns summarized via model into a memory row (Pro)
  - [ ] Settings "Memory" screen: list / edit / delete (C-020 "Clear memory" grows up)
  - [ ] C-020 hit-promotion logic kept
  - [ ] Summary call on `Dispatchers.IO`, cancels on Stop; summary stored as memory, not chat spam
- **Files:** `MemoryManager.kt`, `ChatViewModel.kt`, `Screens.kt`, `Entities.kt`
- **Out of scope:** embedding memory, cross-device sync, auto-recall in every prompt
- **Research:** `docs/PRO-ROADMAP.md` row 2

### P-009 — Chat folders (Pro)
- **Status:** Done — WIRE 2026-08-16. folderId + drawer. REVIEW H (2026-08-16): folder names use kotlinx JSON so a newline cannot wipe every folder.
- **Goal:** Organize conversations into folders.
- **AC:**
  - [ ] `ConversationEntity.folderId` (nullable) + Room migration
  - [ ] Folder CRUD (DataStore or Room table)
  - [ ] Drawer/screen: "All" + folders list; long-press conversation → move to folder
  - [ ] Pro gate
- **Files:** `Entities.kt`, `ChatRepository.kt`, `ChatViewModel.kt`, `Screens.kt`
- **Out of scope:** nested folders, smart/auto folders
- **Research:** `docs/PRO-ROADMAP.md` row 8

### P-010 — Template deep + AI persona packs (Pro)
- **Status:** Done — WIRE 2026-08-16. Personas + template export. REVIEW G (2026-08-16): import uses a strict decoder; a settings file or garbage JSON no longer wipes templates.
- **AC:**
  - [ ] Template export/import as JSON (no secrets — keys stay in SecureStore)
  - [ ] 5–8 built-in personas (e.g. Plain-English explainer, Translator, Teacher, Code helper) as system-prompt templates
  - [ ] Persona picker row above the composer
  - [ ] Pro gate (C-012 already Pro)
  - [ ] Persona names in theme-law everyday words
- **Files:** `SettingsRepository.kt`, `ChatViewModel.kt`, `Screens.kt`
- **Out of scope:** community template marketplace
- **Research:** `docs/PRO-ROADMAP.md` row 5 · TypingMind "agents" precedent

### P-011 — Image editing (Pro)
- **Status:** Done — WIRE 2026-08-16. `/edit` last image. REVIEW A/B/C/D (2026-08-16): URL is `$root/images/edits` (no /v1/v1); only HTTP 404/405 say "cannot edit"; JPEG saved as real PNG; Stop works.
- **AC:**
  - [ ] `/edit` uses last generated image + prompt → `POST /v1/images/edits`
  - [ ] Result appended as a new bubble (original stays)
  - [ ] Stream to disk (C-028 pattern) + `MediaCleanup` (C-029)
  - [ ] Pro gate
  - [ ] Honest error when the provider lacks an edits endpoint (no fake retry loop)
  - [ ] Boundary law: generation itself stays free — only the edit call is Pro (PROOF issue-3 line)
- **Files:** `OpenAiCompatibleClient.kt`, `ChatViewModel.kt`, `Screens.kt`
- **Out of scope:** in-app image editor UI, multi-image edit, masks
- **Research:** `docs/PRO-ROADMAP.md` row 12 · boundary line row 42

### P-012 — Language output control (FREE — H-009, never gate)
- **Status:** Done — WIRE 2026-08-15. Settings picker (12 choices incl. Research C market languages) → `AppSettings.language` → injected into the system prompt on every send ("Reply in <language>.") — main chat AND /browse. FREE, no Pro gate (H-009 lock; 2 verify_static guards that it stays ungated). Files: `SettingsRepository.kt`, `ChatViewModel.kt`, `Screens.kt`. Verify: 138/138 static, both flavors compile, 54/54 unit tests.
- **AC:**
  - [ ] Settings picker with the Research C market languages
  - [ ] Injected into the system prompt on send (first line: "Reply in <language>")
  - [ ] FREE — no Pro gate (verify_static guard that it never gates)
  - [ ] Per-conversation override optional (v1: global setting)
- **Files:** `SettingsRepository.kt`, `ChatViewModel.kt`, `Screens.kt`
- **Out of scope:** UI translation (separate i18n ticket), locale detection
- **Research:** `docs/PRO-ROADMAP.md` row 9 · H-009 · `docs/GLOBAL-MARKETS-RESEARCH.md`

### P-013 — Model knobs + prompt caching toggle (FREE)
- **Status:** Done — WIRE 2026-08-16. Advanced sliders in Settings. Extra fields sent only when not default. Prompt-cache toggle. FREE.
- **AC:**
  - [ ] Params in DataStore, sent only when non-default (no request bloat)
  - [ ] Prompt-caching toggle (provider-supported header/flag where available; documented no-op otherwise)
  - [ ] FREE — no Pro gate
  - [ ] Collapsed "Advanced" section in provider settings (quiet, not a power-user shrine)
- **Files:** `SettingsRepository.kt`, `OpenAiCompatibleClient.kt`, `Screens.kt`
- **Out of scope:** temperature per-message UI, JSON-schema output
- **Research:** `docs/PRO-ROADMAP.md` row 10

### P-014 — Pin chats + save draft (FREE)
- **Status:** Done — WIRE 2026-08-15. `ConversationEntity.pinned` (Room v1→v2 migration — no destructive wipe) + pure `ConversationSort.pinnedFirst` (pins on top, then recency) + star toggle in the drawer; per-conversation drafts in DataStore (`drafts_json`) saved on switch/new-chat/delete and cleared on send/browse/imagine/video. Files: `Entities.kt`, `AppContainer.kt`, `ChatRepository.kt`, new `ConversationSort.kt`, `SettingsRepository.kt`, `ChatViewModel.kt`, `Screens.kt`. Tests: `ConversationSortTest` (4), `DraftStoreLogicTest` (4). Verify: 138/138 static, both flavors compile, 54/54 unit tests.
- **AC:**
  - [ ] `pinned` flag on ConversationEntity + pin sort (pinned first, then recency)
  - [ ] Draft per conversation saved on pause/leave, restored on open (Room or DataStore)
  - [ ] FREE — no Pro gate
  - [ ] Unit tests for pin sort + draft restore
- **Files:** `Entities.kt`, `ChatRepository.kt`, `ChatViewModel.kt`, `Screens.kt`
- **Out of scope:** cross-device draft sync (P-007), pinned-folder UI
- **Research:** `docs/PRO-ROADMAP.md` row 11

**Build order suggestion (WIRE):** P-012/P-014 first (tiny, free, zero risk) → P-002/P-009 (pure Room) → P-004/P-010 (pure UI) → P-005/P-006 (model calls) → P-003 (crypto) → P-001/P-011 (audio + image APIs). All compile-safe individually; one Gradle task at a time on the 4GB host.

---

### R-017 — HenWorks Hermes Agent Android (competitive dig)
- **Status:** Research — written 2026-08-15, waiting `LITECHAT-PROOF` (no Ready child)
- **Deliverable:** `docs/HENWORKS-HERMES-AGENT-DIG.md` + `COMPETITIVE-STEAL-LIST.md` HenWorks row + `DIG-FINDINGS.md` §7. Clones: `/opt/data/workspace/Hermes-agent-android-PC-companion-app` (official, AGPL-3.0), `/opt/data/workspace/Hermes-Agent-On-Android` (community, ★189).
- **Key findings:** App shell (`com.hermesagent.android`) is closed-source; open companion proves a phone↔PC mesh (NaCl E2E, QR pairing, idempotent handoff) — a design reference for future BYO-Sync (P-007). One-time "Hermes Pro" removes ads = same lane as our $4.99. Agent runtime + proot (~200MB env) stays on the avoid list (Tier B/D).
- **Proposed follow-ups (need PROOF+human):** P-007 BYO-Sync design pass using the mesh/merge patterns.

### R-018 — Opclaw / OpenClaw-on-Android (competitive dig)
- **Status:** Research — written 2026-08-15, waiting `LITECHAT-PROOF` (no Ready child)
- **Deliverable:** `docs/OPENCLAW-ANDROID-DIG.md` + `COMPETITIVE-STEAL-LIST.md` Opclaw row + `DIG-FINDINGS.md` §8. Clones: `/opt/data/workspace/openclaw-android` (AidanPark ★1734, no-proot), `/opt/data/workspace/openclaw-termux` (mithun50 ★1678, Flutter).
- **Key findings:** HenWorks Opclaw = closed shell, **~263 MB APK**; category underneath is open (openclaw/openclaw ★386K; clawhub ★9.3K; awesome-openclaw-skills ★52K). "No proot, ~200MB, 3–10 min" = glibc-ld.so-only trick (native shell + WebView dashboard + terminal emulator + Node patches). Agent lane is a crowded gold rush; thin-client lane stays ours. 263 MB vs 1.6 MB = show-don't-tell contrast.
- **No Ready child proposed** — research-only validation of the Tier A lane.

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
