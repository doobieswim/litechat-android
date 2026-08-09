# R-006 supplement — subagent finds (Aug 9 2026)

Finds from 3 parallel subagents that weren't in my direct search.

---

## Tier S — Extremely high steal priority

### anqtst/AndGPT (33KB APK, Android 4.0+)

- **Source:** 4PDA thread (closed/archived): `4pda.to/forum/index.php?showtopic=1068646`
- **GitHub:** https://github.com/anqtst/AndGPT
- **What:** The most extreme thin BYOK client ever found — 33KB APK. Pure Java + Android SDK, zero external dependencies, API key in app folder, token control, temperature, raw response display. Written entirely on a phone using ChatGPT (no PC).
- **Dev quote:** "прога написана при помощи ее самой, то есть при помощи исключительно самого чата прямо на телефоне без использования РС" (app was written using ChatGPT itself, directly on the phone, no PC)
- **LiteChat steal:** This is the absolute lower bound for what a BYOK chat client can be. 33KB. No Compose. No deps. LiteChat targets ~2MB — AndGPT proves we could go even thinner for extreme low-end devices. The architecture (pure Java, no streaming, raw API responses) is a viable "LiteChat Lite" fallback path for sub-512MB devices.
- **Steal priority:** ⭐⭐⭐⭐⭐ (architectural lower bound proof)

### NNCVA/ChatPPP (★2) — Token-budget context compression

- **Repo:** https://github.com/NNCVA/ChatPPP
- **What:** OpenAI-compatible chat with SSE, but the killer feature is **token-budget context assembly**: triggers context compression at 24,000 tokens, compresses conversation history to 14,000 tokens via rolling summary. Also: hidden reasoning blocks (DeepSeek R1), per-conversation preset binding.
- **LiteChat steal:** This is the first thin client we've seen with automatic context management. LiteChat currently has no context window handling — it just sends all history. For long conversations on 4GB phones, token-budget compression prevents both API cost explosion and LMK kills from huge message lists. The 24k→14k threshold is an existence proof; we can tune for LiteChat's target models.
- **Steal priority:** ⭐⭐⭐⭐⭐ (novel pattern, direct LiteChat gap)

### rahulmasal/AetherisAI (★4) — Dual SSE framing

- **Repo:** https://github.com/rahulmasal/AetherisAI
- **What:** Multi-provider chat with the most sophisticated SSE parser we've found: handles BOTH OpenAI `data: [DONE]` framing AND Anthropic typed SSE events (`message_start`, `content_block_delta`, `message_stop`) in a single client. Also: live model discovery via GET /v1/models, encrypted API keys, per-conversation model+system prompt binding.
- **LiteChat steal:** LiteChat's SSE parser handles OpenAI-compatible only. AetherisAI proves a single `StreamParser` can handle both protocols. If LiteChat ever adds Anthropic direct API support, this is the reference pattern.
- **Steal priority:** ⭐⭐⭐⭐ (Anthropic support is v2, but dual-SSE architecture is well-documented here)

### xemantic/markanywhere — KMP streaming markdown parser

- **Repo:** https://github.com/xemantic/markanywhere
- **What:** KMP incremental markdown parser designed specifically for LLM streaming. Unlike llm-typewriter (which renders progressively), markanywhere emits **semantic event streams** — paragraphs, code blocks, lists as typed events. Eliminates flicker from unclosed tokens. Pure KMP, no Android dependency.
- **Key difference from llm-typewriter:** markanywhere is a **parser** (emits structured events), llm-typewriter is a **renderer** (emits Compose nodes). They could work together: markanywhere for parse stage, llm-typewriter for render.
- **LiteChat steal:** If we want markdown that's semantically correct (not just visually correct), markanywhere's event-stream approach is superior. For simple chat with bold/code/headings, llm-typewriter is fine. For an agent/coding client where code block semantics matter, markanywhere wins.
- **Steal priority:** ⭐⭐⭐ (relevant if C-008 needs semantic correctness over visual-only)

---

## Tier A — Strong patterns, not immediate steals

### KeKe0904/Ke-Chat (★1) — Feature density champion

- **Repo:** https://github.com/KeKe0904/Ke-Chat
- **What:** 11 AI providers, streaming with reasoning display, image generation, TTS/ASR, BLE heart rate monitoring, skill system with Markdown+YAML frontmatter, dual cache (LRU memory + GZip disk). Absurd feature density for 1 star.
- **LiteChat steal:** The skill system (Markdown+YAML frontmatter for defining AI behaviors) is a novel approach to BYOK personalization. Cache architecture (LRU+GZip) is production-grade. Multi-provider architecture with 11 providers proves it scales.
- **Steal priority:** ⭐⭐⭐ (skill system is novel, cache pattern is useful)

### DDxfy/GPT-FIG (★2) — OpenAI Responses API

- **Repo:** https://github.com/DDxfy/GPT-FIG
- **What:** Uses OpenAI's newer **Responses API** (not Chat Completions). This is the first Android client we've seen on the Responses API — everyone else uses Chat Completions. Chinese college project.
- **LiteChat steal:** Responses API is OpenAI's future direction. Having a reference implementation for Android is valuable. But LiteChat targets OpenAI-compatible, not OpenAI-specific.
- **Steal priority:** ⭐⭐ (forward-looking, but niche)

### traveler3022/Hermes-Pocket (★2) — WebSocket streaming

- **Repo:** https://github.com/traveler3022/Hermes-Pocket
- **What:** Hermes Agent Android client — WebSocket streaming (not SSE), tool-call cards in UI, reasoning blocks, foreground service for persistent connection keepalive.
- **LiteChat steal:** WebSocket-based streaming is an alternative to SSE. Foreground service pattern for persistent connections. Tool-call card UI pattern.
- **Steal priority:** ⭐⭐ (WebSocket is v2, foreground service is agent territory)

---

## Tier B — Interesting but low priority

| Repo | Stars | What | LiteChat value |
|------|-------|------|----------------|
| I-ShivamSingh/ChatGPT-Android-App | 0 | Ultra-minimal Java/Kotlin Retrofit, minSdk 21, no streaming | Baseline for "what's the minimum" |
| giofahreza/OVQ-Scholar | 1 | PDF text extraction (PDFBox), image upload | PDF/inline attachment pattern |
| snowa11/snow-studio | 1 | Lightweight chat, recently active | Active development, fresh patterns |
| mindylab/lmsmob_chat | 9 | LM Studio local server client, MCP tools, LAN | LAN local LLM pattern |
| jsericksk/ChatGPT | 3 | Clean Architecture, Retrofit, Room, minSdk 21 | Low minSdk approach |
| UmairOye/Chat-AI | 2 | GPT-3.5 chat, text+voice, minSdk 24 | Voice integration pattern |
| PascalAllekotte/OpenAI_Chatbot_MVVM | 7 | Raw OkHttp + Moshi, no Retrofit | Minimal dependency approach |
| la-colinares/AndroidChatGPT | 4 | Compose, Clean Arch, Hilt, Lottie | Animation in chat UI |

---

## Forum ecosystem update

### AndGPT01 — the 4PDA ghost

The AndGPT01 thread on 4PDA is **closed/archived**. The developer wrote the entire app on a phone using ChatGPT as the coding assistant — no PC involved. The APK is 33KB. This is the most extreme "thin client" existence proof in the ecosystem. The GitHub repo (anqtst/AndGPT) has the source.

**LiteChat lesson:** Even if we ship a 2MB Compose APK as default, we should document that a pure-Java 33KB build is possible for devices that can't run Compose. This keeps the "weak-RAM honesty" promise.

### F-Droid BYOK ecosystem

The subagent found a richer F-Droid ecosystem than previously documented:

| App | APK | Android | What |
|-----|-----|---------|------|
| Aiyo | 3.6MB | 7+ | OpenRouter proxy, 500+ models, Apache 2.0 |
| GPTMobile | 5.6MB | 12+ | Jetpack Compose, multi-LLM, GPL-3.0 |
| Maskan | 24MB | 8+ | 11 providers, AES-256-GCM key storage, SQLCipher |
| Agora | 51MB | 7+ | Most feature-complete BYOK client, MIT |

Maskan's AES-256-GCM key storage via Android Keystore is worth studying — LiteChat uses EncryptedSharedPreferences which is simpler but less configurable.

---

## Pattern ecosystem: the markdown renderer landscape

The streaming markdown space now has THREE viable approaches, not one:

| Library | Approach | Pros | Cons |
|---------|----------|------|------|
| **NadeemIqbal/llm-typewriter** | Progressive Compose renderer, Flow<String> | Drop-in, published (Maven Central), syntax highlighting, 3 speed curves | 0.1.x maturity, no semantic events |
| **ECSDevs/llm-typewriter** (fork) | Above + LaTeX + O(1) tracking | Math support, performance caching | Android-only, single maintainer |
| **xemantic/markanywhere** | Semantic event-stream parser, KMP | Structured output, no flicker, pure parser (composable with any renderer) | No built-in Compose rendering, newer |

For C-008: llm-typewriter is the pragmatic choice (drop-in, works). markanywhere is the architectural choice (better design, less mature). The two could compose: markanywhere parse → llm-typewriter render.

---

## One new BACKLOG ticket from subagent findings

### C-010 — Token-budget context compression (from ChatPPP pattern)

- **Status:** Idea
- **Goal:** Implement automatic conversation history truncation when token budget is exceeded. Pattern from NNCVA/ChatPPP: trigger at ~24k tokens, compress to ~14k via rolling summary. Use tiktoken or equivalent for token counting.
- **Source:** NNCVA/ChatPPP (★2)
- **AC:**
  - [ ] Token counter for conversation history (approximate is fine — 4 chars ≈ 1 token)
  - [ ] Configurable threshold (default: 24,000 tokens ≈ 96KB text)
  - [ ] When exceeded, trim oldest messages to bring total under threshold
  - [ ] Show "earlier messages truncated" indicator in chat
  - [ ] Optional: rolling summary instead of pure truncation (v2)
  - [ ] No regression: short conversations unaffected
- **Touch:** `ChatViewModel.kt`, `OpenAiCompatibleClient.kt` (token counting), `Screens.kt` (truncation indicator)
- **Out of scope:** actual LLM-based summary compression (v2), per-model threshold customization

---

*Supplement to R-006. These finds came from the 3-subagent fan-out and were missed in the direct search.*