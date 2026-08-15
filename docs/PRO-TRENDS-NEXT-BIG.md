# R-00X — PRO-TRENDS-NEXT-BIG: What's next in AI chat (2026–2027) for a thin BYOK 4GB client

**Date:** 2026-08-15
**Agent:** LITECHAT-DIG (trends research)
**Status:** Research (needs LITECHAT-PROOF Approve before any ticket goes Ready)
**Cost:** $0 (web_search / web_extract only; no builds, no gradle)
**Audience:** internal. User-facing copy rules stay in `docs/THEME-SHOW-DONT-TELL.md` — every feature below is flagged **user-facing** or **backstage**.

---

## 0. TL;DR — the answer first

The next big feature for BYO AI is **voice** (talk to your own brain), with **memory** (the app that knows you) as the retention engine that makes voice worth using. Ranked top 5 for *our specific app*:

| # | Feature | Trend heat | Thin-client feasibility | APK cost | Pro fit | Differentiation vs BYOK rivals |
|---|---------|-----------|------------------------|----------|---------|--------------------------------|
| 1 | **Voice mode** (STT → chat → TTS; realtime as stretch) | 🔥 hottest (GPT-Live, Gemini Live; 150M people/week use ChatGPT voice) | High — all API-side, OkHttp WebSocket already in deps | ~0 KB (MediaRecorder + player) | Excellent — voice is THE premium flag in every AI app | None of the tiny BYOK clients do realtime BYOK voice |
| 2 | **Memory** (on-device fact store injected into context) | 🔥 baseline expectation 2026 ("without it you're in the wrong category") | Very high — Room DB + prompt injection, near-zero tokens | ~0 KB | Excellent — retention + Pro story | Rare in thin BYOK; ChatGPT's is tiny (~1.2k words) |
| 3 | **Files & vision** (PDF/photo/doc chat + cloud OCR) | baseline table-stakes | High — send bytes to multimodal API | ~0 KB | Good — everyday use (contracts, receipts, homework) | Chatbox already has it → parity, not moat |
| 4 | **Tasks mode** (one-shot agent loop w/ visible budget) | 🔥 2026–27 wave ("chat → action"; Gartner: 40% of enterprise apps w/ agents by end-2026) | Medium — repeated tool calls, needs cost guardrail | ~0 KB | Strong — "give it a task, check back" | Few BYOK mobile apps do real loops |
| 5 | **Cross-device sync** (user's own Drive/WebDAV; SAF export already Ready = base) | baseline for power users | Medium — OAuth/WebDAV, but $0 server | ~0–200 KB | Good | Chatbox syncs; parity play |

**2027 prediction (Section 8):** voice becomes the default input surface, "memory + tasks" becomes the standard pairing every chat app ships, and the platform giants (Gemini Intelligence, Apple Intelligence, ad-funded ChatGPT) keep gating the good stuff to new flagships and subscriptions — which is exactly the market BYO AI owns. The winning thin-BYOK bundle by mid-2027 is **Voice + Memory + Tasks at a one-time $4.99**, on phones the giants abandoned.

---

## 1. The macro picture: three waves, one strategy

Three things are true in August 2026 that were not true a year ago:

**Wave 1 — Chat is becoming voice and action.** ChatGPT reached ~900M weekly active users (a16z 6th ed., Mar 2026: https://a16z.com/100-gen-ai-apps-6/); 150M+ people/week talk to ChatGPT with Voice/Dictation (OpenAI GPT-Live launch, Jul 2026: https://openai.com/index/introducing-gpt-live/). a16z's headline for 2026 is literally "AI is moving from chat to action." Gartner: 40% of enterprise applications will include task-specific AI agents by end of 2026, up from <5% in 2025 (cited at https://www.reddit.com/r/AIVoice_Agents/comments/1s0fity/7_ai_voice_agent_trends_you_must_know_in_2026/ and https://www.insentragroup.com/us/insights/not-geek-speak/generative-ai/agentic-ai-takes-the-wheel-a-deep-dive-into-2026/).

**Wave 2 — Memory is the new baseline, and the giants are becoming landlords.** Vellum's 2026 roundup: "Memory is no longer a differentiator, it is a baseline expectation… any assistant without it is competing in the wrong category" (https://www.vellum.ai/blog/best-personal-ai-assistants-with-memory). a16z: "context compounds — the more an LLM knows about you, the better results… and the more you use it" — i.e., memory = lock-in. Meanwhile the platform owners are gating: **Gemini Intelligence** (agentic Android AI, announced at Android Show, May 12 2026) rolls out only to latest Samsung Galaxy + Pixel and "most Android phones released before 2026 likely won't support it" (https://blog.google/products-and-platforms/platforms/android/gemini-intelligence/ ; https://www.facebook.com/TechRadar/posts/most-android-phones-released-before-2026-likely-wont-support-gemini-intelligence/1408686024629090/). Google is shutting down Google Assistant on Android/Wear OS Sept 4, 2026 (https://the-decoder.com/google-will-shut-down-google-assistant-starting-september-2026-as-gemini-takes-over-on-android-and-wear-os/). OpenAI is testing ads in ChatGPT (Aug 11, 2026: https://openai.com/index/testing-ads-in-chatgpt/) and is 2.5–2.7x the size of every rival (a16z).

**Wave 3 — BYOK is going mainstream.** JetBrains shipped BYOK in its IDEs as a subscription alternative (Dec 2025: https://blog.jetbrains.com/ai/2025/12/bring-your-own-key-byok-is-now-live-in-jetbrains-ides/); commentary: "BYOK: the subtle shift that could reshape how we pay for AI" (https://medium.com/enrique-dans/byok-the-subtle-shift-that-could-reshape-how-we-pay-for-ai-9e165d9e63cd); the BYOK directory site lists dozens of tools incl. multi-provider, encrypted-sync, OpenRouter-based clients (https://byoklist.com/); the "I pay $60/mo across ChatGPT+Claude+Gemini" fatigue thread is a running genre on Reddit (https://www.reddit.com/r/ArtificialNtelligence/comments/1rfh1la/is_anyone_else_feeling_subscription_fatigue_with/).

**Strategy consequence (backstage):** the giants are winning the *new-phone, subscription* market and abandoning the *old-phone, pay-once* market. BYO AI's thesis — thin client, user's own key, $4.99 once, works on a 4GB phone — is not just viable; it is the counter-position to Gemini Intelligence gating and ad-invasion. Every feature below is chosen to reinforce that position, not to chase the giants' hardware.

---

## 2. Historical frame (the house lens): what the 90s/2000s already decided

The user loves this frame and it keeps being right. Map 2026 features onto the old thin-client playbook:

| 2026 thing | 1990s/2000s ancestor | Lesson for BYO AI |
|---|---|---|
| **Voice cascade** (STT → LLM → TTS) | J2ME Generic Connection Framework / Opera Mini: phone = dumb terminal, brains remote | Voice is just another remote capability. The phone records audio and streams it; it never needs a model. Same as Opera Mini never needed a renderer. |
| **Full-duplex realtime voice** (GPT-Live) | 1995 Dragon NaturallySpeaking's dream — speech UI on a PC | It took 30 years and it is still a *server* capability. The client's job is just a WebSocket + mic + speaker. A 4GB phone can host it; only the brain is fat. |
| **Memory** (facts on device, paged into context) | Palm OS: tiny dynamic heap + storage heap; HotSync keeps truth where RAM is cheap | Palm programmers who put big structs in the dynamic heap shipped crashy apps. So: never hold history in RAM — keep *facts* on disk (Room) and page the relevant ~1–2K tokens into context. Memory is the storage heap; context is the dynamic heap. |
| **Files / vision / PDF chat** | J2ME "record stores, not DOM"; SMS bots with zero client RAM | Send bytes, not processing. The phone's job is a file picker; the brain reads the PDF. Zero local OCR (ML Kit = 8MB, anti-thin, already rejected in R-008). |
| **Agent Tasks mode** | Demoscene 64K intros: "you can't ship assets, you ship generators" | Don't ship a knowledge base; ship tools + prompts + retrieval, and let the remote model generate the work. Also DOS multi-boot menus: Chat mode / Task mode are just boot personalities of one thin app. |
| **Cross-device sync** | Palm HotSync: the PC held the truth, the handheld was a cache | User-owned cloud (Drive/WebDAV) = HotSync. The vendor never holds data; the user's own storage is the truth. |
| **Browser automation / computer-use agents** | Opera Mini's big brother (a server browsing for you) | On a thin Android client, a screenshot-click-loop agent is like running a 1995 browser on a 386 — the server must do it. The honest thin version is `/browse` (already Ready, C-013). |
| **Gemini Intelligence / AI phones** | AOL walled garden, then the 2000s carrier portals; Cortana → the platform assistant graveyard | Platform AI gets gated to new hardware and subscriptions; old phones get nothing (Google Assistant shutdown Sept 2026 is the graveyard in real time). BYO AI is the used Honda: old hardware, no rent. |
| **Offline queue** | SMS store-and-forward; Palm's outbox | Pure client, WhatsApp-style outbox: queue prompts, flush when connected. No on-device model needed. |

Recurring law (from `docs/WEAK-RAM-DEEP-HISTORY.md`): **RESIDENT = latency-critical, OVERLAY = loaded on demand, REMOTE = brain where RAM is cheap, GENERATE = tools+prompts instead of data, LIE = never.** Every ranked feature below is RESIDENT-light and REMOTE/GENERATE-heavy.

---

## 3. Trend-by-trend: the full field

Each entry: (a) what it is in plain words, (b) evidence it's trending, (c) feasibility for a 4GB thin BYOK client, (d) Pro-gating fit, plus a user-facing/backstage flag.

### 3.1 Realtime voice (GPT-Live-style full-duplex) + Whisper STT — 🔥 TOP TREND

(a) **Plain words:** You talk; the AI hears you mid-sentence, thinks, and talks back — and can interrupt/be interrupted like a person. The simpler "cascade" version: your speech becomes text (STT), the model answers, text becomes speech (TTS).

(b) **Trending:** GPT-Live launched Jul 8, 2026 — full-duplex voice model, GPT-5.5 in the background, live translation, SynthID audio watermarking added Jul 31 (https://openai.com/index/introducing-gpt-live/). 150M people/week already talk to ChatGPT (same page). Gemini 3.1 Flash Live (Mar 2026) and Gemini Live API compete head-on (https://flowtivity.ai/blog/gemini-3-1-flash-live-vs-gpt-realtime-1-5-voice-agent-comparison-2026/). OpenAI Realtime API is now gpt-realtime-2.1 / 2.1-mini (https://developers.openai.com/api/docs/pricing). "Are voice agents the next big computing platform?" is an active 2026 debate (e.g., https://www.youtube.com/watch?v=8FVyTIUytVw). Enterprise: 80% of businesses plan to integrate voice tech in customer service by 2026 (https://nextlevel.ai/voice-ai-trends-enterprise-adoption-roi/).

(c) **Feasibility (4GB thin client): HIGH, API-side.**
- **Cascade now (recommended v1):** MediaRecorder → upload/stream to any OpenAI-compatible `/audio/transcriptions` (OpenAI gpt-4o-transcribe ≈ $0.006/min; **Groq free tier runs whisper-large-v3, 20 RPM/2k RPD** — https://console.groq.com/docs/speech-to-text, https://www.grizzlypeaksoftware.com/articles/p/groq-api-free-tier-limits-in-2026-what-you-actually-get-uwysd6mb) → existing chat completions → TTS via Groq Orpheus or OpenAI. All HTTP, zero new concepts, works with the user's existing key (even a Groq-only user gets voice for ~free).
- **Realtime (stretch):** OkHttp already in deps and ships WebSocket in core; the client just needs mic capture + audio playback + a WS pipe. Cost is user-paid: Realtime 2.1 ≈ $0.06–$0.11/min, mini ≈ $0.02–$0.05/min; gpt-realtime-whisper live STT $0.017/min; gpt-realtime-translate $0.034/min (https://www.forasoft.com/blog/article/openai-realtime-api-pricing, https://developers.openai.com/api/docs/pricing). GPT-Live itself is not in the API yet ("API soon" signup) — build the cascade now, add realtime when GPT-Live/Realtime matures.
- On-device STT = NOT recommended (Tier C toy per house history; ~1GB+ models, LMK risk).

(d) **Pro fit: EXCELLENT.** Voice is the flagship premium feature of ChatGPT/Gemini; "talk to your brain, pay once" is a one-line Pro pitch and a screenshot story. Free tier: 1 short voice exchange/day as the trial hook (same pattern that worked for /imagine and video). **User-facing** (the "mic button"), with a tiny **backstage** cost concern: voice bills faster than text — the quiet-receipt rule applies.

### 3.2 Vision / OCR (camera + screenshots + photos)

(a) **Plain words:** Point the camera (or pick a screenshot/photo) and ask — the model reads the image: text, receipts, whiteboards, error screens.

(b) **Trending:** Table stakes — every mainstream client has it (ChatGPT, Gemini, Chatbox's "AI Vision": https://play.google.com/store/apps/details?id=chatgpt.ai.chatbot.open.chat.gpt.bot.writer.assistant). Multimodal is default in frontier models; vision is what makes "AI phone" features (screen reading, photo answers) work.

(c) **Feasibility: HIGH.** Client work = image picker + camera intent + base64/multipart upload to the user's multimodal-capable key. Zero APK cost. This *is* C-016 (cloud OCR via vision model, already an Idea) generalized to "photo chat."

(d) **Pro fit: GOOD** — everyday use (receipts, contracts, homework, "what's wrong with this screen"). Not a moat (parity), but closes the gap vs Chatbox and makes the app feel complete. **User-facing.** On-device OCR (ML Kit) stays off: 8MB and anti-thin (R-008 verdict).

### 3.3 MCP / tools / function-calling

(a) **Plain words:** The model can call tools — search, files, your calendar — via a standard protocol (MCP) instead of just talking. Function calling = same idea, per-provider JSON.

(b) **Trending:** MCP went from an Anthropic announcement (Nov 2024, https://www.anthropic.com/news/model-context-protocol) to cross-vendor infrastructure: **97M+ monthly SDK downloads, 5,800–10,000+ servers, ~900% YoY growth; 80% of Fortune 500 deploying AI agents, 28% with MCP servers** (https://www.digitalapplied.com/blog/mcp-adoption-statistics-2026-model-context-protocol, https://www.synvestable.com/model-context-protocol.html, https://nevermined.ai/blog/model-context-protocol-adoption-statistics). OpenAI exposes MCP as a tool type in the Responses API; Google has A2A for agent-to-agent; the Linux Foundation's Agentic AI Foundation now hosts the spec. a16z notes connector ecosystems are the new lock-in (ChatGPT: 220 apps; Claude: ~160 connectors + ~50 community MCP servers — https://a16z.com/100-gen-ai-apps-6/).

(c) **Feasibility: MEDIUM-HIGH, but scope carefully.** Function-calling is API-side (declare tools as JSON schema, render calls) and works with any OpenAI-compatible provider — high feasibility. A *full MCP client* (remote JSON-RPC over HTTP/SSE to user-configured servers) is more work and is a power-user backstage feature; on a thin app the honest first step is **3–5 curated built-in tools** (web fetch — `/browse` already Ready; image/video gen — done; memory lookup — see 3.4; calculator/unit convert). MCP server *hosting* on the phone = anti-thin (that's the Tier B "agent lab" the house rejected).

(d) **Pro fit: GOOD as curated tools, WEAK as raw MCP config.** Power users love raw MCP; regular people (the theme's audience) will never configure servers. **User-facing:** "Ask BYO AI to look things up / make a picture / remember" via tools. **Backstage:** the MCP plumbing. Pro-gate: tools (not raw config).

### 3.4 Agentic loops / "Tasks" mode

(a) **Plain words:** Instead of one question → one answer, you give the AI a job ("plan a 4th birthday party: menu, shopping list, and a script") and it works through steps, using tools, and reports back.

(b) **Trending:** The single biggest 2026 theme. a16z Big Ideas 2026: "AI is moving from chat to action" (https://www.youtube.com/watch?v=ULszsXDyjMY&vl=en). Gartner: 40% of enterprise apps with task-specific agents by end-2026 (up from <5%). Adobe 2026 Digital Trends: agents go mainstream in 18 months (https://business.adobe.com/resources/digital-trends-report.html). Agentic commerce: Gartner expects AI agents to handle 20% of digital storefront interactions by 2028; 70% of consumers already use agents for travel bookings (https://svitla.com/blog/agentic-ai-market-trends-2025-2026-5-shifts-that-matter). Google's Gemini Intelligence is precisely this on Android (https://blog.google/products-and-platforms/platforms/android/gemini-intelligence/).

(c) **Feasibility: MEDIUM for a thin BYOK client.** Two routes: (i) server-side agent modes where the provider runs the loop (e.g., deep-research-style APIs) — trivial client, but provider-dependent; (ii) a lightweight client-side loop: the client repeatedly calls chat completions with tool definitions until the model emits a "done" — doable with existing SSE/OkHttp stack, but each loop step costs the user tokens, and runaway loops are a real bill-shock risk. **Mandatory guardrail:** a visible step/token budget — the theme's "quiet receipt" rule makes this a feature, not a penalty. Reliability: keep loops short (3–8 tool steps), always let the user cancel.

(d) **Pro fit: STRONG.** "Give it a task" is the most demonstrable premium upgrade over plain chat, and few BYOK mobile clients ship it. **User-facing** (label: "Task", never "Agent" — theme), with **backstage** loop plumbing + budget math. Pro-gate: yes (free tier: 1 short task/day).

### 3.5 Persistent memory

(a) **Plain words:** The app remembers things about you across chats — your name, your job, "I'm vegetarian," your kids' names — and quietly uses them, so it stops asking the same questions. Two flavors: (i) facts/preferences you or the AI store; (ii) automatic summarization of past chats.

(b) **Trending:** "Memory is a baseline expectation" (https://www.vellum.ai/blog/best-personal-ai-assistants-with-memory). Mem0's 2026 State of AI Agent Memory report treats memory as a first-class architectural component (LoCoMo 92.5, LongMemEval 94.4 — https://mem0.ai/blog/state-of-ai-agent-memory-2026); a local-first memory paper appeared Mar 2026 (MemX, arXiv 2603.16171: https://arxiv.org/html/2603.16171v1). ChatGPT ships cross-session memory (~1,200–1,400 words — https://www.vellum.ai/blog/best-personal-ai-assistants-with-memory); Gemini has "personal context." a16z: context compounds → lock-in (https://a16z.com/100-gen-ai-apps-6/).

(c) **Feasibility: VERY HIGH and the cheapest big win.** On-device Room table of memory facts; at chat start, retrieve relevant facts (keyword/embedding via the user's key, or plain substring match for v1) and inject top-K (~800–1,500 tokens) into the system prompt. Cost to user: near zero (prompt-injection tokens only). APK: 0 KB. No embeddings model required for v1 — simple term matching is surprisingly good for facts like "vegetarian," "works at X." Palm storage-heap discipline applies: facts on disk, paged into context, never all in RAM.

(d) **Pro fit: EXCELLENT — it is the retention engine.** Memory makes the app feel smarter every week and raises switching cost without any server. Pro-gate the *auto-learn* mode; free tier gets manual "remind it" notes. **User-facing** but quiet — the app should show "I remembered: …" subtly, with a visible, editable memory list (trust = control). **Backstage:** the retrieval/injection logic.

### 3.6 Local hybrid (tiny on-device model + cloud brain)

(a) **Plain words:** A small model on the phone handles easy/offline stuff; the big model handles everything else.

(b) **Trending:** Real and growing: Gemini Nano on Pixels (https://www.articsledge.com/post/on-device-ai), Apple Intelligence's on-device + Private Cloud Compute (https://www.apple.com/newsroom/2026/06/apple-intelligence-brings-powerful-ai-capabilities-into-everyday-experiences/), Google AI Edge Gallery offline demos, Wear OS local assistants.

(c) **Feasibility on a 4GB phone: HARD and explicitly against house law.** `docs/WEAK-RAM-DEEP-HISTORY.md` §7.3: on 4GB, only ~0.5B–0.6B class models run, "expect slow tokens, heat, LMK when user switches apps"; LiteRT even on flagships peaks ~1.7GB CPU. A bundled 0.5B would add hundreds of MB to the APK/download and RAM pressure — a direct violation of the 2MB APK and "refuse fat work" theme law. **Verdict: do not build.** The thin alternative that captures 80% of the user need is the **offline queue** (3.14) — store-and-forward, not on-device brains.

(d) **Pro fit: n/a (rejected).** If the owner ever wants an offline story, it's the queue + a "you'll get the answer when you're back online" promise — a used-Honda reliability feature, possibly free-tier (trust).

### 3.7 Wear OS / watch companion

(a) **Plain words:** A tiny watch app: tap → talk → your phone relays to your API key → the answer appears on the wrist.

(b) **Trending:** Gemini is rolling out to Wear OS watches (Pixel, Samsung, OPPO, OnePlus, Xiaomi — https://blog.google/products-and-platforms/platforms/wear-os/gemini-wear-os-watches/); Wear OS 7 adds Gemini AI (https://www.cnet.com/tech/mobile/your-smartwatch-is-about-to-get-more-productive-thanks-to-gemini-ai/); Google Assistant's Sept 2026 shutdown pushes everyone to AI assistants on wrist (https://the-decoder.com/google-will-shut-down-google-assistant-starting-september-2026-as-gemini-takes-over-on-android-and-wear-os/). A privacy-focused indie Wear OS assistant with local LLM support already exists (https://www.reddit.com/r/LocalLLaMA/comments/1ol8zo5/i_built_a_privacy_focused_ai_assistant_for_wearos/).

(c) **Feasibility: MEDIUM.** A Wear OS module is a separate small APK; the watch is an *even thinner* client (mic → phone relay → text). But: it's a new surface to maintain, Wear OS debug/test costs, and the 4GB-phone-first audience skews to older hardware that may not pair with modern watches. Doable, cheap-ish (~2–4 dev weeks), but it does not serve the core user first.

(d) **Pro fit: GOOD as a differentiator** ("your own AI on your wrist, one-time price") — no BYOK watch chat exists today. **User-facing** but optional; **backstage** relay plumbing. Verdict: watch-list item, not next big.

### 3.8 Cross-device sync

(a) **Plain words:** Same chats on your phone, tablet, and PC — history follows you, encrypted.

(b) **Trending:** Baseline expectation in BYOK land — the byoklist directory's first entry already advertises "synced across iOS, Android, and web" (https://byoklist.com/); Chatbox syncs everywhere (https://chatboxai.app/en).

(c) **Feasibility: MEDIUM.** Zero-server constraint means the user's own cloud: SAF export/import (C-014, already Ready) is the v1; auto-sync via user's Google Drive (OAuth) or WebDAV is the v2. Palm HotSync pattern: the user's storage is the truth, the phone is a cache.

(d) **Pro fit: GOOD** — it's what power users eventually demand, and it pairs with memory (3.5) so memory can ride along encrypted. Parity play (not a moat). **User-facing.** Pro-gate auto-sync; manual SAF export could stay Pro (per C-014) or go free as a trust feature.

### 3.9 Offline queue

(a) **Plain words:** No signal? Your prompt waits in an outbox and sends itself when you're back online. (Not "AI that works offline" — the *message* works offline.)

(b) **Trending:** WhatsApp-style message queues are the reference pattern (https://dev.to/satyasootar/how-whatsapp-works-without-internet-offline-messaging-and-sync-explained-3nle); the on-device offline-AI market is growing (LokalMind, Google AI Edge Gallery) but is a different (Tier C) product.

(c) **Feasibility: VERY HIGH.** Pure client: Room outbox + WorkManager retry on connectivity. 0 KB APK. Android 8+ friendly. This is the honest thin-client answer to "offline AI" — store-and-forward, like SMS/J2ME before it.

(d) **Pro fit: MEDIUM.** It's a reliability/trust feature — the kind of thing the theme says should *feel* free ("Same app next year. More reliable."). Suggest: free-tier (builds the everyday-tool trust), or Pro if we need another bullet. **User-facing** ("Will send when you're back online" — a calm note, no speech).

### 3.10 Translation (text + live voice)

(a) **Plain words:** Chat translates across languages; live voice translates a spoken conversation in near-real-time.

(b) **Trending:** Google Translate added live back-and-forth conversation in 70+ languages (https://blog.google/products-and-platforms/products/translate/language-learning-live-translate/); OpenAI sells live translation natively: gpt-realtime-translate $0.034/min (https://developers.openai.com/api/docs/pricing); ChatGPT's GPT-Live does live translation (https://openai.com/index/introducing-gpt-live/). People already use ChatGPT Pro voice as a live interpreter (https://www.reddit.com/r/ArtificialInteligence/comments/1qpidzm/best_ai_app_to_use_for_spoken_live_translation/).

(c) **Feasibility: HIGH** — "translate this" is a prompt; live voice translation = the voice cascade (3.1) with a translate call. Zero new deps.

(d) **Pro fit: MEDIUM.** It's a commodity (Google Translate is free); as a Pro feature it's weak, as a free utility it's fine. Bundle it with voice mode rather than selling it alone. **User-facing.** Verdict: ship as part of voice, not as a headline.

### 3.11 Document / PDF chat

(a) **Plain words:** Upload a PDF (contract, manual, essay) and ask questions; the model answers from the document.

(b) **Trending:** Fully mainstream — ChatPDF, AskYourPDF, NotebookLM, ChatGPT, Claude all do it (2026 roundups: https://www.atlasworkspace.ai/blog/pdf-chat-ai-tools, https://paperguide.ai/blog/ai-tools-to-chat-with-pdf/, https://www.hebbia.com/resources/best-ai-for-document-analysis). Chatbox advertises "summarize files" and "document parsing" (https://chatboxai.app/en).

(c) **Feasibility: MEDIUM-HIGH.** Thin path: send the file (or its text) to a multimodal/long-context model via the user's key — most frontier models read PDFs/Office files directly; fallback: client-side text extraction for plain PDFs (no heavy deps — e.g., PdfBox-lite ~1–2MB, or push bytes and let the API handle it). Context-window discipline already exists (C-010 ContextTrimmer) — page the doc, don't dump it. APK impact small if we lean on the API.

(d) **Pro fit: GOOD** — "chat with your papers" is a real everyday use case on cheap phones (students, job seekers, small businesses), and it supersets C-016 (cloud OCR). Parity (not moat) but closes the last big Chatbox gap. **User-facing.**

### 3.12 Browser automation / computer-use agents

(a) **Plain words:** An AI that browses the web *by clicking*, fills forms, books things — acting like a person with a browser.

(b) **Trending:** 2026 is the year consumer browser agents shipped: OpenAI Operator/ChatGPT Agent, Perplexity Comet, ChatGPT Atlas, Gemini in Chrome with Auto Browse, Manus (https://zylos.ai/research/2026-02-08-computer-use-gui-agents/, https://www.firecrawl.dev/blog/best-browser-agents, https://agentconn.com/blog/best-ai-computer-use-agents-2026/, https://www.turingpost.com/p/computer-use-ai-agents). Open source: Browser Use, Stagehand, Skyvern, UI-TARS.

(c) **Feasibility on a thin Android client: LOW (and anti-thin).** Full computer-use needs a screenshot→click loop; on Android that means either a WebView shell (banned by theme law) or accessibility-service robot (heavy, permission-hostile, Play-review risk). The thin, honest version is `/browse` (fetch + extract + inject — C-013, Ready) and, later, "search-then-summarize" inside Tasks mode (3.4).

(d) **Pro fit: SKIP as a feature; keep /browse as the Pro web story.** **User-facing** (the /browse button), **backstage** (any future loop logic).

### 3.13 Coding assistant modes

(a) **Plain words:** A chat mode tuned for code: syntax, diffs, file context — "fix this function."

(b) **Trending:** Massive on desktop — Claude Code hit 18% workplace adoption (6x YoY), tied with Cursor, behind Copilot at 29%; ChatGPT used for coding by 28% (JetBrains Jan 2026 survey, n=10,000+: https://tech-insider.org/ie/ai-coding-assistants-2026/, https://uvik.net/blog/ai-coding-assistant-statistics/). a16z notes Anthropic is betting on prosumers.

(c) **Feasibility: HIGH** (a "Code" system prompt + markdown + file attach — all API-side), but the *audience* fit is wrong: the theme explicitly bans "for developers only" positioning. A light "code" prompt preset is cheap; a full mobile coding agent is a different product.

(d) **Pro fit: MEDIUM.** It would please devs (who are early adopters of BYOK anyway) but it risks the everyday voice. Verdict: ship as a *prompt preset* (C-012 template territory), not a mode. **User-facing** (preset), **backstage** (no new machinery).

### 3.14 Image / video generation — already owned

(a) **Plain words:** Ask for an image or a short video; the model makes it. BYO AI already ships /imagine (C-011 done) and has the video plan researched (`docs/VIDEO-MONETIZATION.md`) — the *only* BYOK chat client with video gen is the stated moat.

(b) **Trending:** Nano Banana (Gemini) generated 200M images and pulled 10M users in its first week; Veo 3 "the breakthrough moment for AI video" (a16z 6th ed.). Creative tools are the growth category (CapCut 736M MAU; Canva/Notion AI attach rates — https://a16z.com/100-gen-ai-apps-6/).

(c) **Feasibility: already proven** in-repo. Next step: let *voice* (3.1) and *tasks* (3.4) call generation — "make me a cover image for this" becomes a tool call.

(d) **Pro fit: already the Pro anchor.** Keep. **User-facing.**

### 3.15 "AI phone" features (Gemini Intelligence, Apple Intelligence)

(a) **Plain words:** The OS itself gets AI: proactive agents, screen understanding, form filling, cross-app actions. Google's Gemini Intelligence (May 2026) automates multi-step tasks, summarizes in Chrome, fills forms, and builds widgets from plain language; Apple Intelligence does on-device + Private Cloud Compute.

(b) **Trending:** This is the platform wave (https://blog.google/products-and-platforms/platforms/android/gemini-intelligence/; https://www.apple.com/newsroom/2026/06/apple-intelligence-brings-powerful-ai-capabilities-into-everyday-experiences/). The load-bearing fact for us: **the best of it is gated to new flagships** — "most Android phones released before 2026 likely won't support Gemini Intelligence" (TechRadar). Google Assistant dies Sept 4, 2026, leaving old phones with *less* assistant than they had.

(c) **Feasibility: N/A to build — this is competitive weather, not a feature.** We cannot and should not mimic OS-level agents on 4GB (Tier B "agent lab" — rejected).

(d) **Pro fit: indirect and huge.** Every giant move that gates AI to new phones/subscriptions is a sales pitch for BYO AI. **Backstage** (positioning narrative + Play listing honesty), never user-facing as "we beat Gemini."

### 3.16 Subscription-fatigue-driven BYOK growth

(a) **Plain words:** People are tired of $20/month×N; BYOK (your key, pay per use) is the counter-move.

(b) **Trending:** Documented above (3.0 Wave 3): JetBrains BYOK (https://blog.jetbrains.com/ai/2025/12/bring-your-own-key-byok-is-now-live-in-jetbrains-ides/), Reddit fatigue threads (https://www.reddit.com/r/ArtificialNtelligence/comments/1rfh1la/is_anyone_else_feeling_subscription_fatigue_with/), "BYOK: the subtle shift" (https://medium.com/enrique-dans/byok-the-subtle-shift-that-could-reshape-how-we-pay-for-ai-9e165d9e63cd), byoklist.com, and even OpenAI *testing ads* — the giants are monetizing attention; BYOK monetizes usage at cost.

(c) **Feasibility: n/a (market force).** Actionable version: a **cost receipt** (per-message token cost, a quiet line under the answer) — theme-compliant ("If we ever show cost, it is a quiet receipt, not a flex") and genuinely useful to BYOK users. Optionally a monthly spend estimate view. All client-side math (C-010 already does char/token approximation).

(d) **Pro fit: MEDIUM** (receipt could be free to build trust; the spend-dashboard could be Pro). Mostly **backstage** narrative + **user-facing** quiet receipt.

---

## 4. Ranked "next big feature" top 5 — for BYO AI specifically

Scoring: trend heat (2026–27), thin-client feasibility, APK/RAM cost, Pro-gating fit, differentiation vs BYOK rivals (Chatbox, Enchanted, EveryTalk, TypingMind), theme fit (everyday, small-first, show-don't-tell). 1–5 stars each; total /25.

| Rank | Feature | Trend | Feasibility | APK cost | Pro fit | Differentiation | Theme | Total |
|---|---|---|---|---|---|---|---|---|
| 1 | **Voice mode** (cascade → realtime) | ★★★★★ | ★★★★★ | ★★★★★ | ★★★★★ | ★★★★☆ | ★★★★★ | **29/30** |
| 2 | **Memory** (on-device facts → context) | ★★★★★ | ★★★★★ | ★★★★★ | ★★★★★ | ★★★★☆ | ★★★★★ | **29/30** |
| 3 | **Files & vision** (PDF/photo/doc chat) | ★★★★☆ | ★★★★★ | ★★★★★ | ★★★★☆ | ★★☆☆☆ (parity) | ★★★★★ | **27/30** |
| 4 | **Tasks mode** (agent loop + budget) | ★★★★★ | ★★★☆☆ | ★★★★★ | ★★★★☆ | ★★★★☆ | ★★★★☆ | **26/30** |
| 5 | **Cross-device sync** (user-owned cloud) | ★★★☆☆ | ★★★★☆ | ★★★★☆ | ★★★★☆ | ★★☆☆☆ (parity) | ★★★★☆ | **24/30** |

Watch list (not top 5): Wear OS companion (differentiator but wrong-first user), offline queue (ship free), translation pack (bundle with voice), curated tools/MCP-lite (inside Tasks), cost receipt (free trust feature).

### Why #1 and #2 are effectively a single bundle ("Talk to the one that knows you")

Voice without memory is a party trick; memory without voice is a settings page. Together they make the *first* BYOK client that feels like a personal assistant rather than an API frontend — at ~0 KB of APK, ~0 server cost, and both are pure REMOTE/GENERATE plays (no on-device models), which is exactly the Palm/Opera-Mini discipline the house keeps rediscovering. They also compound: memory makes voice cheaper (fewer repeated questions → fewer tokens) and voice makes memory effortless ("remind me…" spoken). Ship as **Pro "Voice + Memory"** with the free tier getting one voice exchange/day and manual memory notes.

**Recommended build order (each release = one free/trust feature + one Pro feature, per R-008 cadence):**
1. Memory v1 (manual + auto-learn) — smallest, highest retention ROI. **Pro.**
2. Voice cascade (STT→chat→TTS, Groq/OpenAI-compatible) — biggest wow per dev-hour. **Pro** (free: 1/day).
3. Files & vision (supersets C-016 cloud OCR) — parity closure. **Pro.**
4. Tasks mode v1 (2–4 tool steps: /browse + memory + gen, hard budget, cancel button). **Pro.**
5. Auto-sync (Drive/WebDAV) on top of C-014 SAF. **Pro.** / Cost receipt + offline queue → **free** (trust).

---

## 5. What NOT to build (and why)

| Temptation | Verdict | Reason |
|---|---|---|
| On-device 0.5B model for offline | **No** | Tier C toy on 4GB; violates 2MB-APK + refuse-fat-work law (WEAK-RAM §7.3). Offline queue is the honest substitute. |
| Full computer-use / browser robot | **No** | Needs WebView shell or accessibility robot → banned by theme law; Play-review risk. /browse (C-013) is the thin version. |
| Raw MCP server configuration UI | **No** (curated tools only) | Power-user backstage; regular people won't configure servers; hosting MCP on the phone = Tier B agent lab. |
| ML Kit on-device OCR | **No** | 8MB, anti-thin (R-008 verdict). Cloud vision via user's key instead. |
| "AI phone"-style OS agents | **No** | Cannot and must not imitate Gemini Intelligence on 4GB; it's competitive weather, not a feature. |
| "Runs a real model on 4GB" marketing | **No** | SoftRAM class lie; banned in Play copy (C-031) and by house history. |

---

## 6. User-facing vs backstage summary

- **User-facing (ship carefully themed):** Voice mic button, memory ("I remembered…" + editable list), photo/PDF chat, Task button (label "Task", never "Agent"), sync toggle, offline-queue note, cost receipt line, /browse button.
- **Backstage (never in user copy):** MCP plumbing, tool schemas, loop orchestration + budget math, retrieval/injection logic, provider negotiation (which provider supports realtime/vision), positioning vs Gemini Intelligence, "BYOK" as a word.

---

## 7. Evidence file (load-bearing URLs)

- OpenAI GPT-Live (voice, full-duplex, 150M weekly voice users, API soon): https://openai.com/index/introducing-gpt-live/
- OpenAI API pricing (Realtime 2.1 $32/$64 per 1M audio tokens; whisper $0.006/min; realtime-whisper $0.017/min; realtime-translate $0.034/min): https://developers.openai.com/api/docs/pricing
- Realtime cost-per-minute analysis (Jul 2026, $0.02–$0.11/min realistic): https://www.forasoft.com/blog/article/openai-realtime-api-pricing
- Groq free-tier STT (whisper-large-v3, 20 RPM / 2k RPD) + Orpheus TTS: https://console.groq.com/docs/speech-to-text ; https://www.grizzlypeaksoftware.com/articles/p/groq-api-free-tier-limits-in-2026-what-you-actually-get-uwysd6mb
- a16z Top 100 Gen AI Consumer Apps, 6th ed. (Mar 2026; 900M ChatGPT WAU; connector ecosystems; Nano Banana/Veo 3; "context compounds"): https://a16z.com/100-gen-ai-apps-6/
- Gemini Intelligence (May 2026, flagship-gated): https://blog.google/products-and-platforms/platforms/android/gemini-intelligence/ ; gating: https://www.facebook.com/TechRadar/posts/most-android-phones-released-before-2026-likely-wont-support-gemini-intelligence/1408686024629090/
- Google Assistant shutdown Sept 4, 2026: https://the-decoder.com/google-will-shut-down-google-assistant-starting-september-2026-as-gemini-takes-over-on-android-and-wear-os/
- Gemini on Wear OS: https://blog.google/products-and-platforms/platforms/wear-os/gemini-wear-os-watches/ ; https://www.cnet.com/tech/mobile/your-smartwatch-is-about-to-get-more-productive-thanks-to-gemini-ai/
- Apple Intelligence (on-device + Private Cloud Compute): https://www.apple.com/newsroom/2026/06/apple-intelligence-brings-powerful-ai-capabilities-into-everyday-experiences/
- Memory as baseline (Vellum 2026; ChatGPT memory ~1,200–1,400 words): https://www.vellum.ai/blog/best-personal-ai-assistants-with-memory
- Mem0 State of AI Agent Memory 2026: https://mem0.ai/blog/state-of-ai-agent-memory-2026 ; local-first memory (MemX, Mar 2026): https://arxiv.org/html/2603.16171v1
- MCP adoption (97M+ monthly SDK downloads; 5,800+/10,000+ servers; 80% Fortune 500 agents; 28% MCP): https://www.digitalapplied.com/blog/mcp-adoption-statistics-2026-model-context-protocol ; https://www.synvestable.com/model-context-protocol.html ; https://nevermined.ai/blog/model-context-protocol-adoption-statistics ; spec origin: https://www.anthropic.com/news/model-context-protocol
- Agentic AI 2026 (Gartner 40% by end-2026): https://www.insentragroup.com/us/insights/not-geek-speak/generative-ai/agentic-ai-takes-the-wheel-a-deep-dive-into-2026/ ; https://business.adobe.com/resources/digital-trends-report.html ; https://svitla.com/blog/agentic-ai-market-trends-2025-2026-5-shifts-that-matter
- Browser agents 2026 (Operator, Comet, Atlas, Gemini in Chrome; Browser Use/UI-TARS): https://zylos.ai/research/2026-02-08-computer-use-gui-agents/ ; https://agentconn.com/blog/best-ai-computer-use-agents-2026/ ; https://www.turingpost.com/p/computer-use-ai-agents
- Coding assistants 2026 (Claude Code 18%, Cursor 18%, Copilot 29%, ChatGPT 28%): https://tech-insider.org/ie/ai-coding-assistants-2026/ ; https://uvik.net/blog/ai-coding-assistant-statistics/
- PDF chat mainstream 2026: https://www.atlasworkspace.ai/blog/pdf-chat-ai-tools ; https://paperguide.ai/blog/ai-tools-to-chat-with-pdf/
- BYOK mainstream (JetBrains Dec 2025; commentary; directory): https://blog.jetbrains.com/ai/2025/12/bring-your-own-key-byok-is-now-live-in-jetbrains-ides/ ; https://medium.com/enrique-dans/byok-the-subtle-shift-that-could-reshape-how-we-pay-for-ai-9e165d9e63cd ; https://byoklist.com/
- Subscription fatigue: https://www.reddit.com/r/ArtificialNtelligence/comments/1rfh1la/is_anyone_else_feeling_subscription_fatigue_with/
- OpenAI testing ads (Aug 2026): https://openai.com/index/testing-ads-in-chatgpt/
- Live translation (Google 70+ languages; ChatGPT-as-interpreter): https://blog.google/products-and-platforms/products/translate/language-learning-live-translate/
- Offline messaging/queue pattern: https://dev.to/satyasootar/how-whatsapp-works-without-internet-offline-messaging-and-sync-explained-3nle
- House history (thin-client law): `docs/WEAK-RAM-DEEP-HISTORY.md` ; `docs/PREMIUM-STRATEGY.md` ; `docs/THEME-SHOW-DONT-TELL.md`

---

## 8. 2027 prediction

1. **Voice becomes the default input.** GPT-Live-class full-duplex voice goes API-wide in 2027 (OpenAI already signals "API soon"), Gemini Live and Groq keep prices falling (Realtime mini ≈ $0.02–0.05/min today). Every serious chat app ships a mic button; the ones that make it *work on your own key* win the BYOK crowd. Expect BYOK voice rates to be a headline comparison in app-store reviews.
2. **"Memory + Tasks" is the standard pairing.** By late 2027 a chat app without persistent memory will read like a 2024 app. Memory becomes a privacy battleground: on-device/local-first memory (MemX-class) is where BYOK and privacy-first apps win against cloud memory.
3. **The giants lock the new phones; the old phones become the BYOK market.** Gemini Intelligence stays flagship-gated; Apple Intelligence stays premium-hardware; ChatGPT adds ads (already testing, Aug 2026). Subscription prices drift up while BYOK per-use prices fall (Groq free tiers, cheap OpenRouter models). The "one-time $4.99, works on your old phone, no ads, your key" position gets *stronger* every quarter — a rare structural tailwind.
4. **BYO AI's 2027 shape (prediction):** Voice + Memory + Tasks as the Pro bundle, files/vision as parity, sync as glue, and generation (image/video) as the moat — all API-side, all ~0 KB APK growth, on 4GB phones the giants stopped supporting. The app's one-liner by then: "Your key. Your voice. It remembers. No monthly bill." — and every word of it is true.

---

## 9. Proposed backlog tickets (stay Research until PROOF Approves)

- **R-00X-A — Memory v1 (Pro):** on-device Room fact store; manual "Remind it" + auto-learn (user-visible, editable list); inject top-K facts into system prompt; ~0 KB APK; AC: facts persist across sessions, appear in prompt, editable/deletable, no PII sent to providers beyond prompt injection.
- **R-00X-B — Voice mode v1 (Pro, free 1/day):** cascade STT→chat→TTS over any OpenAI-compatible endpoint incl. Groq; mic via MediaRecorder; playback via MediaPlayer; cost receipt line; AC: works with OpenAI + Groq keys, streaming text still first-class, graceful no-key/no-mic errors.
- **R-00X-C — Files & vision (Pro):** image picker/camera + PDF/file attach → multimodal model via user's key; supersedes C-016; AC: photo chat + PDF text answers, size limits enforced, context budget via C-010.
- **R-00X-D — Tasks mode v1 (Pro):** lightweight agent loop (≤4 steps, tools: /browse, memory, image gen), visible step/token budget + cancel; AC: cancels cleanly, bills only user's key, no infinite loops (hard cap), labeled "Task" in UI.
- **R-00X-E — Cost receipt (free):** quiet per-message token-cost line + monthly estimate; AC: uses C-010 char/token math, no network, theme-compliant copy.

---

*Report complete. All claims above are from the cited URLs or in-repo docs; no builds were run (rule: web_search/web_extract/grep only). Next step: LITECHAT-PROOF review.*
