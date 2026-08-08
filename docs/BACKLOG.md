# LiteChat backlog

Single task queue for the two-agent team.  
Statuses: `Idea` · `Research` · `Ready` · `Doing` · `Done` · `Blocked`

Coding agent: only take **Ready** (or human-named id). Claim with `Doing`, finish with `Done`.

---

## Now / next (coding)

### C-002 — Product flavors `play` vs `foss`
- **Status:** Ready  
- **Goal:** `play` keeps GMS ads+billing; `foss` builds without Play/AdMob deps for sideload/F-Droid-shaped builds.  
- **AC:**
  - [ ] `productFlavors { play {…}; foss {…} }` (or equivalent) in `app/build.gradle.kts`  
  - [ ] `foss` compile does not require Play Billing / Ads libraries (stubs or source sets)  
  - [ ] `play` retains current monetization behavior  
  - [ ] README documents `assemblePlayRelease` / `assembleFossRelease` (names as implemented)  
  - [ ] CI builds at least one flavor (prefer both if time-cheap)  
- **Touch:** Gradle, source sets `play/` `foss/`, billing/ads call sites  
- **Out of scope:** actual F-Droid submission  
- **Note:** Keep applicationId strategy documented (suffix or separate id)  

### C-003 — Debug-only “Dev: mark Pro”
- **Status:** Ready  
- **Goal:** Remove foot-gun from release builds.  
- **AC:**
  - [ ] “Dev: mark Pro” UI only if `BuildConfig.DEBUG` (or foss debug)  
  - [ ] Release play binary has no visible unlock cheat  
- **Touch:** `Screens.kt` Settings  

### C-004 — Per-baseUrl stream-broken cache
- **Status:** Ready  
- **Goal:** After stream-class failure + successful non-stream fallback, remember baseUrl prefers non-stream for a while (numAi-plus idea).  
- **AC:**
  - [ ] DataStore (or similar) map/set of baseUrl → “prefer non-stream”  
  - [ ] `streamChat` skips SSE or tries non-stream first when flagged  
  - [ ] Success path can clear flag OR TTL documented (pick one; document in KDoc)  
  - [ ] No API key material in the cache  
- **Touch:** `OpenAiCompatibleClient.kt`, `SettingsRepository` or small store, maybe ViewModel  
- **Research:** `docs/DIG-FINDINGS.md` pattern 2  

### C-005 — GET `/models` picker
- **Status:** Ready  
- **Goal:** Optional fetch of model ids from `{base}/models` for OpenAI-compatible servers.  
- **AC:**
  - [ ] Settings or onboarding: “Fetch models” button  
  - [ ] OkHttp GET with same auth headers; parse `data[].id` leniently  
  - [ ] Failure shows short error; does not crash  
  - [ ] Empty key + local Ollama still attempted  
  - [ ] No huge dependency added  
- **Touch:** client + Settings UI  
- **Out of scope:** full model capability matrix  

### C-006 — UI stream paint throttle (if needed)
- **Status:** Idea  
- **Goal:** Conflate SSE deltas to ≤10–20 UI updates/sec on weak devices.  
- **AC:** TBD after coding checks jank; research notes numAi-plus UPDATE_DELAY  
- **Touch:** `ChatViewModel`  

---

## Research queue

### R-001 — Play listing pack (privacy + disclaimer copy)
- **Status:** Research  
- **Goal:** Draft privacy policy HTML outline + unofficial BYOK disclaimer (HenWorks-style, not legal advice).  
- **Deliverable:** `docs/PLAY-LISTING-DRAFT.md` + mark **C-007** Ready for wiring links in Settings  
- **Depth:** modern Play Data safety + short historical “unofficial client” norms  

### R-002 — Markdown renderer cost
- **Status:** Idea  
- **Goal:** Decide plain text vs incremental markdown; APK/RSS cost (maid-native lesson ~0.4 MB class).  
- **Deliverable:** ticket C-00x Ready or explicit “defer”  

### R-003 — 4PDA / XDA / OldMarket distribution notes (FOSS arm)
- **Status:** Idea  
- **Goal:** Where weak-device users actually get APKs; store listing hygiene.  
- **Deliverable:** `docs/DISTRIBUTION-FOSS.md` + backlog line for README  

### R-004 — OpenAI-Client-Android streaming library notes
- **Status:** Idea  
- **Goal:** Vendor-neutral SSE parser test fixtures if useful to LiteChat unit tests.  
- **Deliverable:** optional `app/src/test` fixtures ticket  

### R-005 — Further lost-repo archaeology
- **Status:** Idea  
- **Goal:** Wayback / Gitee / APK-only clients — only if human orders.  
- **Deliverable:** append `docs/LOST-REPOS.md`  

---

## Done (recent)

| ID | What | Notes |
|----|------|--------|
| C-001 | Lazy-init MobileAds when `!isPro` | Removed cold-start `MobileAds.initialize`; `AdMobLazyInit` one-time init in `BannerAd`; stripped `MobileAdsInitProvider` via manifest `tools:node="remove"`; verify_static guards |
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
