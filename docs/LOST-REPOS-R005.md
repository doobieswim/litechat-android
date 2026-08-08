# Further lost-repo archaeology — R-005

Date: 2026-08-08  
Status: Research → Done  
Previous: docs/LOST-REPOS.md (2026-08-08, initial dig)

## New finds (post-initial-dig)

### 1. numAi on r/androidafterlife

- **Reddit thread**: `r/androidafterlife` — "I made an AI client for Android 1.0+ — numAi"
- **Significance**: The numAi author is actively promoting on Reddit in the retro-Android community. This is the **exact same audience** LiteChat's FOSS arm targets: users running old phones, looking for BYOK AI clients.
- **Takeaway**: r/androidafterlife is a distribution channel worth noting alongside 4PDA/XDA/OldMarket.

### 2. kid1412621/SummaryExpressive

- **Repo**: https://github.com/kid1412621/SummaryExpressive ★210
- **What**: Modern BYOK FOSS Android app (Jetpack Compose) for summarizing videos, articles, images with AI/LLM
- **minSdk**: Android 13 (API 33)
- **Why relevant**: Same BYOK pattern as LiteChat, but targets summaries (not chat) and requires modern Android. Demonstrates there's an audience for BYOK Android apps beyond pure chat.
- **Not a direct competitor** — different use case, high minSdk.

### 3. 4PDA ChatGPT thread (topic 1073274)

- **URL**: https://4pda.to/forum/index.php?showtopic=1073274
- **Content**: Russian-language discussion about official ChatGPT app + alternatives
- **Status**: Active (st=3540 means 3500+ posts)
- **Significance**: Different from numAi's 4PDA topic (1116157). The ChatGPT thread is where general users discuss AI clients; numAi's topic is the developer's own thread. Two different audiences on the same forum.
- **Takeaway**: When publishing FOSS arm, a post in the ChatGPT thread (not just a new topic) reaches users already looking for AI clients.

### 4. UnboundChat (Play Store)

- **App**: `com.marko.unboundchat` — "UnboundChat: Private AI (BYOK)"
- **Platform**: Play Store (not open source)
- **What**: BYOK chat app, connects to OpenAI/Google/OpenRouter
- **Significance**: Commercial BYOK competitor on Play Store. Validates the market for Play-distributed BYOK chat.
- **No source available** — closed-source competitor only.

### 5. Nothing else new in the trenches

Additional searches for:
- Gitee low-star `openai android` — no notable finds
- Wayback `github.com/*/ChatGPT-Android` 404s — not explored (requires Wayback API)
- Codeberg AI chat clients — surface not checked

The initial LOST-REPOS.md already captured the most important repos (numAi, numAi-plus, ReOldAi, ZeroClawAndroid, maid). The field is well-mapped.

## Distribution surface update

| Channel | numAi | LiteChat FOSS target |
|---------|-------|---------------------|
| GitHub Releases | Used | ✓ (C-002 build) |
| 4PDA (topic 1116157) | ✓ | Worth a post |
| 4PDA ChatGPT thread (1073274) | — | Higher-traffic audience |
| Reddit r/androidafterlife | ✓ | ✓ |
| XDA thread | ✓ | Worth a post |
| OldMarket (id=410) | ✓ | Niche, low priority |
| Telegram @AppDataEN | ✓ | Niche |
| F-Droid | — | ✓ (eventual goal) |
| IzzyOnDroid | — | Easier than F-Droid |
| Obtanium (direct GitHub) | — | ✓ (simplest) |

## Recommendations

### Priority 1: GitHub Releases + Obtanium
Already done — fossRelease APK builds. Users can sideload directly from GitHub Releases. Obtanium auto-updates from GitHub.

### Priority 2: F-Droid
Requires: reproducible build, no GMS deps (already done for foss), F-Droid metadata YAML. The foss flavor is F-Droid-ready by design.

### Priority 3: 4PDA / XDA posts
Low-effort, high-reach in the retro-Android audience. Post format: APK link + feature list + compatibility matrix.

### No new backlog ticket needed
R-003 already covers distribution. These findings augment DISTRIBUTION-FOSS.md.

---

## One-line archaeology status

```
Tier S (true weak-RAM):     numAi, numAi-plus, ReOldAi    → all cloned, all analyzed
Tier A (agent/on-device):   ZeroClawAndroid, maid         → analyzed, no further action
Tier B (obscure/0★):        LOST-REPOS.md covers 15+ repos → no new trench finds
Play commercial BYOK:       UnboundChat                   → market validation only
Forum surface:               4PDA (2 topics), XDA, Reddit  → distribution notes
```

**R-005 complete.** No further lost-repo archaeology needed unless human orders a Wayback crawl of deleted repos.