# TypingMind deep dig — full feature map + steal list for BYO AI

**Date:** 2026-08-15
**Agent:** LITECHAT-DIG
**Sources:** typingmind.com/buy · typingmind.com · docs.typingmind.com/feature-list · docs.typingmind.com (llms.txt index) · Reddit backlash thread (r/openrouter "Paid $99 for TypingMind lifetime license… now memory/sync is…")
**Status:** Research — feeds R-016 / H-008. Backstage; user-facing copy must pass `docs/THEME-SHOW-DONT-TELL.md`.

---

## 1. What TypingMind is

A **BYOK LLM frontend** (web + desktop + PWA + mobile-friendly): you bring your own API keys, it's the shell. Same product family as BYO AI — but desktop/power-user, not "$30 phone". It monetizes with **one-time lifetime licenses** ($39/$79/$99) + a **separate recurring cloud subscription** (sync/memory/knowledge-base) — and that split is exactly what triggered the backlash (see §5).

## 2. Their tiers (from typingmind.com/buy, 2026-08-15)

| Tier | Price (once) | Includes |
|------|-------------|----------|
| Free | $0 | Basic chat, ads/popups, limited features |
| Standard | $39 | Remove ads, basic chat, **AI agents**, voice input, share chats |
| Extended | $79 | + **Image gen & editing, web search, TTS, vision, upload documents** |
| Premium | $99 | + **Multi-model chats, unlimited plugins, projects & folders, artifacts, free updates** |
| Cloud (recurring) | separate sub | **Sync/backup, memory, knowledge base (RAG)** ← the backlash |
| Teams/Bulk | $395+/custom | Admin, KB, analytics, branding, roles, SSO, usage limits, reseller |

**Validation for us:** their "remove ads + features" one-time ladder ($39–99) confirms one-time works in BYOK; our $4.99 sits at the bottom of the band because our market is cheap phones, not desktops.

## 3. Full feature inventory → BYO AI verdict

Legend: ✅ **HAVE** · ➕ **ADD (cheap, fits thin client)** · 🔜 **FUTURE (v1.2+, bigger)** · ❌ **SKIP (wrong for 4GB thin client / zero-server law / not our market)** · Pro/free per our R-016 roadmap.

### Models & provider config
| TypingMind feature | Verdict | Note |
|---|---|---|
| All OpenAI/Claude/Gemini/Mistral/Grok/DeepSeek/Kimi models | ✅ have | OpenAI-compatible base URL = most of these; add presets later |
| Custom models / custom endpoint & proxy | ✅ have | Custom base URL exists (proxy out of scope — trust) |
| OpenRouter/Groq/Ollama/LM Studio | ✅ have | Presets exist |
| **Temperature / top P / presence & frequency penalty / max tokens** | ➕ add (free) | These are plain API params; small Settings expansion. Power users on XDA expect them |
| **Stream response control** (instant vs word-by-word) | ➕ add (free) | We stream; a "fast vs typewriter" toggle is ~10 lines |
| **Automatic prompt caching** (Claude/OpenAI/Gemini) | ➕ add (free) | Saves the USER money on repeated context — honesty brand; expose as toggle |
| **Custom system instruction** | ➕ add (Pro-advanced) | We have a system prompt internally; expose a user-editable one |

### Chat experience
| TypingMind feature | Verdict | Note |
|---|---|---|
| Multi-conversations in parallel | ✅ have | Room conversations |
| **Context limit / context summary** | ✅ have (C-010 trimmer) + 🔜 P-010 auto-summaries | Trimmer free; rolling summaries Pro |
| **Prompt library + templates + variables** | ✅ have (C-012, Pro) | Ours already gated — matches their Pro |
| **60+ built-in AI agents (personas)** | ➕ add (Pro) | Curated system-prompt packs = zero-APK template packs (extends C-012). Strong "finish the game" Pro hook |
| **Language output control** | ➕ add (free) | "Reply in Spanish" setting via system prompt; pairs with Research C (global markets) |
| Upload documents / video | 🔜 future | Attach exists (C-016); PDF text extraction later |
| **RAG knowledge base** | 🔜 future (Pro) | "Ask your files" — API-side chunk+inject; medium build; strong Pro |
| Multi-model responses (one convo, 2 models) | 🔜 future (Pro) | We have per-conversation model (C-018); parallel dual-model = Premium-class |
| Message syntax / prompt chaining | 🔜 future (Pro) | Chain templates together — power-users pay |

### Plugins / tools
| TypingMind feature | Verdict | Note |
|---|---|---|
| **Web search (SerpAPI/Perplexity/DDG)** | ➕ add (Pro) — planned P-005 | /browse exists (Pro); add search-then-answer with DuckDuckGo (zero API cost) |
| Image generation (DALL-E/Stable Diffusion/GPT editor) | ✅ have /imagine | **Editing** (gpt-image-1 edit) = cheap add, Pro-worthy |
| URL reader (Firecrawl) | ✅ have /browse | Jsoup already does this |
| **Artifacts (interactive canvas: code/docs/prototypes)** | ❌ skip | Needs rich WebView-ish runtime — violates thin-client + 4GB law |
| **Custom plugins (HTTP/JS/MCP)** | 🔜 future (Pro, backstage) | MCP = power-user; our roadmap flagged it; not v1 |
| Zapier/Calendar/Slack automations | ❌ skip | Server/accounts — zero-server law; not our audience |
| Plugin marketplace | ❌ skip | Server cost; community packs via GitHub JSON can be free later |

### Chat management
| TypingMind feature | Verdict | Note |
|---|---|---|
| Chat history search | ➕ add (Pro) — planned P-002 | Room FTS, ~0 KB. Their Thomas Frank testimonial literally praises folders+search |
| **Chat folders / projects / tags** | ➕ add (Pro) | Room `folderId` column; "organize" is the Pro layer (latent-wants #7) |
| **Pin chats** | ➕ add (free) | Tiny; sticky favorites |
| **Save draft** | ➕ add (free) | Tiny; input persistence |
| Edit & fork conversations | ✅ have (C-024 forks) | Edit message = small add |
| Share chats | ✅ have (share text) | **Secret-link share = ❌ (needs their cloud server — we never run servers)** |
| Import/export chats | ✅ have (C-014 SAF) | |
| **Migrate from ChatGPT/Claude** (import your official-app history) | 🔜 future (Pro) | JSON import of ChatGPT export; HotSync pattern; good Pro story |
| **Custom profiles with custom API keys** | 🔜 future (Pro) — planned P-007 | Work/personal setups |
| Side chat / quote | ❌ skip v1 | Desktop convenience |

### UI / UX
| TypingMind feature | Verdict | Note |
|---|---|---|
| Light/dark mode | ➕ add (free or Pro) | We're OLED-dark-first (feature for our audience); light = later |
| Voice input | ✅ have (C-021, free) | |
| **Text-to-speech read-aloud** | ➕ add (Pro) — planned voice | Android TTS, ~0 KB |
| Sound notification | ➕ add (free) | Tiny |
| Hotkeys / wide screen | ❌ skip | Desktop-only |
| Custom avatar | ❌ skip | Identity play for character apps, not us |
| **Search keyword suggestions** | ➕ add (free, later) | After each answer, suggest DDG keywords — cheap |
| PWA / macOS app | ❌ skip | Android-only product |

### Security / privacy
| TypingMind feature | Verdict | Note |
|---|---|---|
| Private by default / encrypted key | ✅ have (SecureStore AES256) | |
| **API key encryption with password** | 🔜 future | App-lock (UnboundChat gates it) — Pro privacy toggle |
| **API tokens cost estimation / usage stats** | ➕ add (Pro) — scan #7 | Per-message cost line stays FREE (honesty brand); history dashboard Pro |
| OAuth for plugins | ❌ skip | |

### Teams / enterprise — ❌ ALL SKIP
Admin panel, KB, analytics, branding, roles/SSO/SCIM, usage limits, reseller — not our market (consumer $30 phone). TypingMind's Teams is a separate product; we have zero server cost by law.

## 4. Ranked steal list for BYO AI (effort × Pro value)

| # | Steal | Effort | Pro/free | Why |
|---|-------|--------|----------|-----|
| 1 | **Chat history search (FTS)** | Low (~0 KB) | Pro | Their most-praised org feature; our P-002; "protect my work" want |
| 2 | **AI persona packs (60+ agents → curated templates)** | Low (0 KB) | Pro | Extends C-012; "finish the game" episode model; zero code beyond packs |
| 3 | **Web search (DDG search-then-answer)** | Low–Med | Pro | Planned P-005; Chatbox/UnboundChat gate it too |
| 4 | **TTS read-aloud + full voice mode** | Med | Pro | Planned; the next-big-feature bundle |
| 5 | **Parameter settings (top_p, penalties, max_tokens, prompt caching)** | Low | Free | Power-user knobs; caching saves users money = honesty |
| 6 | **Chat folders + pin + save draft** | Low | Pro/free mix | Organization = Pro layer; pin/draft free |
| 7 | **System instruction + language output control** | Low | Free | Trivial system-prompt settings; global-market lever |
| 8 | **Migrate from ChatGPT** | Med | Pro | Import official-app export; Palm-conduit story |
| 9 | **Image editing (gpt-image-1)** | Low–Med | Pro | Extends /imagine |
| 10 | **App lock / key password** | Low | Pro | UnboundChat precedent; privacy toggle |

## 5. The backlash lesson (must never repeat)

TypingMind sells "lifetime" then puts **sync, memory, and knowledge base behind a separate recurring subscription**. Reddit: *"Paid $99 for TypingMind lifetime license… now memory/sync is…"* — public anger ("daylight robbery" in our scan). **Our rule (already in PRO-ROADMAP): pay once → everything visible, forever. No asterisks.** If we ever add sync (P-007 BYO-Sync), it's user-owned storage (WebDAV/Drive via SAF) — the user's account, not our server — so it costs us nothing and never needs a "cloud subscription."

## 6. What we must NOT copy

- Secret-link chat sharing (needs a server — zero-server law)
- Artifacts/interactive canvas (WebView-class runtime on 4GB)
- Plugin marketplace / Zapier-style automations (server/accounts)
- Teams/enterprise tier (not our market)
- The "lifetime + cloud subscription" split (trust killer)

## 7. Sources

- https://www.typingmind.com/buy (tiers, 2026-08-15)
- https://docs.typingmind.com/feature-list (full inventory)
- https://docs.typingmind.com/ (docs index / llms.txt)
- https://www.reddit.com/r/openrouter/comments/1nevp2e/paid_99_for_typingmind_lifetime_license_now/ (backlash)
- https://www.typingmind.com/ (homepage)
