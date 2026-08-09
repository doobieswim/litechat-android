# Lost-repo archaeology — R-006 (new trench finds)

Date: 2026-08-09  
Status: Research → Done  
Previous: docs/LOST-REPOS-R005.md (2026-08-08, distribution surface + SummaryExpressive)

## Summary

Second archaeology pass found **14 new repos** across three tiers, plus one upstream library ecosystem. The biggest discovery is the **NadeemIqbal CMP AI chat starter kit** (prompt-bar + llm-typewriter) and its downstream fork network — these directly impact LiteChat's C-008 (markdown rendering) decision and suggest a path we haven't explored: adopting published KMP streaming-markdown components instead of hand-rolling.

---

## Tier S — Direct LiteChat steal priority

### 1. NadeemIqbal/llm-typewriter (★~20+, Apache 2.0)

- **Repo:** https://github.com/NadeemIqbal/llm-typewriter
- **Maven Central:** `io.github.nadeemiqbal:llm-typewriter:0.1.1`
- **What:** Streaming-text typewriter for Compose Multiplatform. `Flow<String>` driven, live progressive Markdown, syntax-highlighted code blocks built up token-by-token, three speed curves (Linear/EaseOut/Natural), tap-to-skip, screen-reader-friendly.
- **Platforms:** Android (minSdk 24) · iOS · Desktop JVM · Web (wasmJs)
- **Key insight:** Prefix-stable Markdown parser — same prefix yields same tokens, so `**bold` mid-stream renders as plain text, then flips to bold when `**` closes. Code blocks syntax-highlight progressively as lines arrive.
- **LiteChat steal:** This is essentially C-008 pre-built and published to Maven Central. The decision tree is now: (A) stay plain-text for v1 → (B) adopt llm-typewriter as a drop-in dependency → (C) adopt ECSDevs fork (adds LaTeX/performance caching).
- **Steal priority:** ⭐⭐⭐⭐⭐ (direct C-008 solution)

### 2. NadeemIqbal/prompt-bar (★6, Apache 2.0)

- **Repo:** https://github.com/NadeemIqbal/prompt-bar
- **Maven Central:** `io.github.nadeemiqbal:prompt-bar:0.2.0`
- **What:** AI chat composer for CMP — slash commands, @-mentions (async provider), attachment chips, unified Send→Sending→Stop state machine, prompt template chips, voice button, live token counter, smart paste tokenizer.
- **Platforms:** Android (minSdk 24) · iOS · Desktop · Web
- **Key insight:** Pairs naturally with llm-typewriter — ~20 lines from empty screen to ChatGPT-quality streaming on all CMP targets (see dev.to article). The Send/Stop button auto-syncs with the typewriter's streaming state.
- **LiteChat steal:** LiteChat's composer is simpler (no slash commands, no mentions). If we ever want these features, prompt-bar is a drop-in. But for v1 thin chat, our simpler TextField may be better — less APK weight.
- **Steal priority:** ⭐⭐ (overkill for v1, great for v2)

### 3. ECSDevs/llm-typewriter (★2, Apache 2.0) — FORK

- **Repo:** https://github.com/ECSDevs/llm-typewriter
- **Maven Central:** `cc.ptoe:llm-typewriter` (separate artifact)
- **What:** Fork of NadeemIqbal/llm-typewriter, detached from upstream fork network due to substantial divergence:
  - Android-only target (upstream is CMP)
  - Streaming LaTeX math via RaTeX-CMP (pure Rust KaTeX engine)
  - Incremental Markdown re-parse, O(1) reveal tracking
  - Process-wide math-measurement caching
  - 41 commits, active (last commit Jul 31 2026)
- **LiteChat steal:** If we need LaTeX math rendering in streaming chat (unlikely for v1 chat, but relevant for an agent/coding client), this fork is the best option. The O(1) reveal tracking is interesting — means no quadratic re-parse per token.
- **Steal priority:** ⭐⭐ (LaTeX overkill for v1, performance patterns worth studying)

### 4. ECSDevs/Messenger (★2, Apache 2.0)

- **Repo:** https://github.com/ECSDevs/Messenger
- **What:** CMP BYOK AI chat for Phone + Tablet + Desktop + **Wear OS**. Material 3 design, SSE streaming, progressive Markdown + LaTeX, custom AI agents, multiple provider support.
- **Stack:** Kotlin Multiplatform, Jetpack Compose, llm-typewriter (submodule), RaTeX-CMP, 123 commits.
- **Wear OS module:** First BYOK AI chat client we've found with a Wear OS companion. The Wear app syncs agents from mobile and uses the phone as the AI backend — thin-client architecture applied to wearables.
- **LiteChat steal:** The Wear OS module is the key insight. LiteChat could add a Wear companion later using the same "phone as brain" pattern. The KMP shared-core approach means most code is shared across phone/tablet/desktop/wear.
- **Steal priority:** ⭐⭐ (Wear OS is v2 territory, KMP architecture worth noting)

### 5. pjq/ChatCat (★1, MIT)

- **Repo:** https://github.com/pjq/ChatCat
- **What:** KMP AI chat (Android + Desktop + iOS + Web) with MCP support, multimodal (images + image generation), 6 languages, Ktor SSE, Coil 3, Material 3, 5 accent palettes.
- **Key insight — MCP in a thin client:** ChatCat embeds a JSON-RPC 2.0 MCP client that connects to external tool servers. This is the first thin-Android client we've seen with MCP integration — typically MCP lives in agent desktops/VPSes.
- **Provider connection testing:** Full-screen provider editor with "Test Connection" button that hits the endpoint before saving.
- **LiteChat steal:** The MCP integration pattern — proving thin Android CAN host an MCP client without bundling a runtime. Also: connection testing UX, i18n structure (6 languages), Ktor SSE approach (LiteChat uses OkHttp).
- **Steal priority:** ⭐⭐ (MCP interesting for future, connection testing pattern useful now)

### 6. roseforljh/EveryTalk (★176, MIT, 1,134 commits)

- **Repo:** https://github.com/roseforljh/EveryTalk
- **What:** Full-featured Android AI client — multi-provider, streaming, voice, web search, image generation, LaTeX (MathJax 4 SVG), Anthropic Messages API direct, optional backend proxy.
- **Target:** Android 8.1+
- **Key insight — streaming height placeholders:** FAQ Q5 documents a specific streaming UX problem: "The message list jumps when streaming finishes." Their solution: `ENABLE_STREAMING_HEIGHT_PLACEHOLDER` and `ENABLE_SINGLE_SWAP_RENDERING` in `PerformanceConfig.kt`. This is a novel pattern LiteChat could adopt.
- **Tool call rendering:** Citation cards for web search results, tapping opens browser. Native tool call detection across providers (Gemini google_search, Qwen enable_search, DeepSeek search params).
- **22 forks** — indicates community activity and derivative projects.
- **LiteChat steal:** Streaming height placeholders (prevents layout jumps mid-stream), citation-card rendering pattern, tool-call detection per-provider, MathJax 4 SVG approach (offline LaTeX rendering).
- **Steal priority:** ⭐⭐⭐ (streaming height placeholder is production-polish, tool call citation cards are novel)

---

## Tier A — Interesting patterns, not immediate steals

### 7. ThinkOffApp/ClawWatch (★?)

- **Repo:** https://github.com/ThinkOffApp/ClawWatch
- **What:** AI agent running natively on Galaxy Watch — NullClaw + Vosk offline STT + Claude. Zig-based, 2.8MB APK.
- **Not thin-chat** — this is an agent appliance on a watch. But the architecture (offline STT + remote brain) is a pattern reminiscent of Opera Mini.
- **LiteChat takeaway:** Proof that "thin wearable client + remote brain" works. The 2.8MB APK for a watch agent is remarkable — shows how small you can go when you keep the brain remote.

### 8. SimonSchubert/Kai (★?)

- **Repo:** https://github.com/SimonSchubert/Kai
- **What:** "OpenClaw alternative in your pocket" — mobile AI assistant.
- **LiteChat takeaway:** Another data point in the mobile agent space. Not thin-chat.

### 9. beradeep/aiyo (★?)

- **Repo:** https://github.com/beradeep/aiyo
- **What:** BYOK AI Chat App on Android, OpenRouter-focused.
- **Tags:** android, byok, openrouter
- **LiteChat takeaway:** OpenRouter-first BYOK client — suggests OpenRouter is a common entry point for new BYOK apps (LiteChat supports it as a preset).

### 10. esafirm/compose-ai-chat (★?)

- **Repo:** https://github.com/esafirm/compose-ai-chat
- **What:** Simple AI chat using Compose Multiplatform, ChatGPT-like. Minimal.
- **LiteChat takeaway:** Example of the minimal-KMP-chat pattern. Useful reference for comparing KMP complexity vs our native-Compose approach.

### 11. lambiengcode/compose-chatgpt-kotlin-android-chatbot (★?)

- **Repo:** https://github.com/lambiengcode/compose-chatgpt-kotlin-android-chatbot
- **What:** Compose ChatGPT Kotlin chatbot app.
- **LiteChat takeaway:** Another Compose chat reference implementation. Low priority.

### 12. zead333/KleanBot (★?)

- **Repo:** https://github.com/zead333/KleanBot
- **What:** Modern Android chatbot using OpenAI, MVVM, Clean Architecture, Kotlin.
- **LiteChat takeaway:** Clean Architecture reference for chat apps. Low priority — our architecture is simpler/smaller.

### 13. Siddhesh2377/ToolNeuron (★?)

- **Repo:** https://github.com/Siddhesh2377/ToolNeuron
- **What:** Encrypted & Privacy First, On-Device AI App. OpenAI-shaped endpoints, bearer-token auth, rate limit, audit log.
- **LiteChat takeaway:** Privacy-first BYOK with OpenAI-shaped endpoints — similar philosophy. Rate limiting and audit logging patterns worth noting for future.

---

## Tier B — Preservation / adjacent / anti-pattern

### 14. theblazehen/P4OC (★?)

- **Repo:** https://github.com/theblazehen/P4OC
- **What:** OpenCode client for Android. Kotlin 2.3.0, mikepenz markdown, Koin DI, Termux terminal emulator integration, LaunchDarkly EventSource for SSE.
- **Interesting:** Uses okhttp-eventsource (LaunchDarkly) instead of raw OkHttp SSE parsing. Different SSE approach than LiteChat's hand-rolled parser.
- **LiteChat takeaway:** Alternative SSE library option if our hand-rolled parser needs replacement.

### 15. Msr7799/chat-ui-kotlin (★?)

- **Repo:** https://github.com/Msr7799/chat-ui-kotlin
- **What:** HuggingChat Android client — AI chat UI in Kotlin.
- **LiteChat takeaway:** HuggingChat API integration reference.

### 16. wannaphong/android-hostai (★?)

- **Repo:** https://github.com/wannaphong/android-hostai
- **What:** Phone as LLM API server — Android hosts the API endpoint, other devices connect. Uses Ktor for streaming support.
- **LiteChat takeaway:** Reverse pattern of LiteChat (phone is server, not client). Interesting for multi-device home setups.

### 17. samoylenkodmitry/chatgpt-android-fork

- **Repo:** https://github.com/samoylenkodmitry/chatgpt-android-fork-
- **What:** ChatGPT Android fork with Stream Chat SDK for Compose.
- **LiteChat takeaway:** Shows Stream Chat SDK usage pattern. Not relevant — LiteChat doesn't use Stream.

---

## Upstream library ecosystem

The NadeemIqbal CMP AI chat libraries represent a micro-ecosystem:

```
NadeemIqbal/prompt-bar (composer)  ←→  NadeemIqbal/llm-typewriter (renderer)
        ↑                                        ↑
        |                                        |
   AI Chat Starter Kit                    ECSDevs/llm-typewriter (fork)
   "~20 lines from empty screen                   ↑
    to ChatGPT-quality streaming"                 |
                                            ECSDevs/Messenger
                                            (Wear OS + CMP chat app)
```

Both libraries are published to Maven Central, Apache 2.0, and target Android minSdk 24 (within LiteChat's range). The ECSDevs fork adds LaTeX math and performance caching but drops iOS/Desktop/Web targets.

### C-008 decision impact

LiteChat currently has C-008 (markdown rendering) deferred as Idea. The existence of published, Apache 2.0 streaming-markdown libraries changes the cost/benefit calculation:

| Approach | APK cost | Dev effort | Risk |
|----------|----------|------------|------|
| Plain text (current) | 0 KB | 0 | Low — but users expect markdown |
| llm-typewriter (NadeemIqbal) | TBD (need to measure) | Low — drop-in | Medium — 0.1.x maturity |
| llm-typewriter (ECSDevs fork) | TBD + RaTeX (~1MB native lib?) | Low | Medium — single maintainer |
| Hand-rolled (original C-008 plan) | ~300-400 KB | High | Medium-High |
| mikepenz/multiplatform-markdown-renderer | ~500 KB | Medium | Low — mature library |

**Recommendation:** Load NadeemIqbal/llm-typewriter into LiteChat's gradle deps (foss-only test build), measure APK/RSS impact, then decide whether to promote C-008 to Ready with this library as the implementation path.

---

## Forum / non-GitHub finds

### 4PDA — AndGPT01 thread

- **Thread:** https://4pda.to/forum/index.php?showtopic=1068646
- **What:** "Тестирование возможностей API ChatGPT" (Testing ChatGPT API capabilities) — tokens, temperature, request methods, raw responses.
- **Active:** Recent posts Aug 2026
- **Significance:** Different from both numAi's thread (1116157) and the main ChatGPT thread (1073274). This is an API-testing thread, not an app thread — suggests a technically-savvy Russian-language audience on 4PDA interested in BYOK patterns.

### F-Droid AI Chat category

- **URL:** https://f-droid.org/en/categories/ai-chat/
- **Status:** Sparse — few BYOK chat clients, mostly local-model tools and DuckDuckGo AI chat.
- **LiteChat significance:** F-Droid's AI Chat category is underpopulated. LiteChat's foss flavor would be one of the first true BYOK chat clients in the F-Droid ecosystem.

### IzzyOnDroid

- No new BYOK chat clients found beyond what's already documented. The distribution channel remains viable.

### Reddit r/androiddev — Wear OS AI agent (Zig + Vosk)

- **Thread:** https://www.reddit.com/r/androiddev/comments/1rj1hv8/
- **What:** Wear OS app running a real AI agent on-device (Zig + Vosk + TTS, 2.8 MB)
- **Significance:** 2.8 MB APK for an AI agent on a smartwatch is remarkable — proves the thin wearable + remote brain model works. The author's stack (Zig for native speed, Vosk for offline STT) is worth noting for future LiteChat wearable extensions.

---

## What's still unseen

1. **Wayback Machine `github.com/*/ChatGPT-Android` 404s** — not explored. Many early ChatGPT Android clients were deleted (DMCA or abandoned).
2. **Gitee / GitCode `openai android` low-star** — surface not fully checked. Chinese developer ecosystem may have BYOK clients LiteChat hasn't seen.
3. **Private Telegram source drops** — not indexable. numAi's Telegram channels may reference other repos.
4. **Play Store commercial BYOK clients with no source** — UnboundChat noted in R-005, likely more.

---

## Recommendations

| Priority | Action | Backlog ticket |
|----------|--------|----------------|
| 1 | Measure llm-typewriter APK/RSS impact in foss debug build | New R-006-1 |
| 2 | If APK impact < ~400 KB, promote C-008 to Ready with llm-typewriter as implementation path | C-008 update |
| 3 | Adopt streaming height placeholder pattern (EveryTalk) — prevents layout jumps mid-stream | New C-009 or merge into existing |
| 4 | Clone ECSDevs/Messenger for Wear OS architecture reference (do not build now) | Future reference |
| 5 | Post LiteChat FOSS release to r/androidafterlife + 4PDA ChatGPT thread when ready | Distribution |

---

## One-line archaeology status

```
Previously known:  numAi, numAi-plus, ReOldAi, ZeroClawAndroid, maid + 15+ Tier B
New finds R-006:   llm-typewriter (x2), prompt-bar, Messenger (Wear OS), ChatCat (MCP),
                   EveryTalk (height placeholders), ClawWatch (Zig+Vosk), Kai, aiyo,
                   compose-ai-chat, KleanBot, ToolNeuron, P4OC, chat-ui-kotlin, hostai
Key library eco:   NadeemIqbal CMP AI Chat Starter Kit — prompt-bar + llm-typewriter
C-008 path:        llm-typewriter changes the markdown-rendering cost equation
```

**R-006 complete.** The field is now well-mapped. Future archaeology should focus on Gitee/GitCode and Wayback Machine for deleted repos.