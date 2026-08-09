# Competitive deep-dive — steal list for LiteChat

Date: 2026-08-09
Status: In progress (3 subagents analyzing Agora, EveryTalk, Kai)

## TypingMind ($1M+ ARR) — what to steal

TypingMind is the BYOK market leader (web, not Android). Key patterns:

| Feature | What it does | LiteChat equivalent | Steal? |
|---------|-------------|-------------------|--------|
| **One-time lifetime license** | $59-199 one-time, 3 tiers | LiteChat already has $4.99 Pro | ✅ Already matched |
| **AI Agents** | Pre-configured personas with specific model, knowledge files, few-shot examples | LiteChat has prompt templates (C-012) — simpler | 🔶 Upgrade C-012 to include model binding + knowledge |
| **Plugin system** | Web Search, DALL-E 3, Artifacts (live HTML/CSS) | LiteChat has /imagine, could add /browse | 🔶 Add /browse (C-013 Ready) |
| **Multi-model chat** | Compare responses from multiple models | LiteChat is single-model | 🔶 Future: model comparison view |
| **Prompt Library** | Save/reuse/share complex prompts with variables | C-012 covers this but simpler | 🔶 Add prompt import/export, more built-ins |
| **Cloud Sync** | Reliable cloud sync across devices | C-014 (manual SAF) — simpler | 🔶 Implement C-014, add Drive API later |
| **MCP support** | Connect to Zapier, DBs, etc. | ChatCat has MCP — LiteChat could add | 🔶 Future: MCP in thin client |

**Key lesson:** TypingMind's pricing tiers (Standard/Extended/Premium for one-time) validate LiteChat's $4.99 impulse-buy approach for mobile. TypingMind proves BYOK + one-time = viable business.

---

## AetherisAI (★4) — what to steal

| Feature | LiteChat readiness | Steal priority |
|---------|-------------------|---------------|
| **Live model discovery** (GET /v1/models → Room cache → pin/hide/refresh) | C-005 already does /models fetch, but doesn't cache per-provider | ⭐⭐⭐ |
| **Dual SSE framing** (OpenAI [DONE] + Anthropic typed events) | LiteChat only handles OpenAI-compatible SSE | ⭐⭐ |
| **AES-256 backup system** (encrypted file I/O) | C-014 covers SAF backup, but could add encryption | ⭐⭐ |
| **Per-conversation model binding** | LiteChat uses global model setting — per-conversation would be useful | ⭐⭐⭐⭐ |

---

## ChatCat (★1) — what to steal

| Feature | LiteChat readiness | Steal priority |
|---------|-------------------|---------------|
| **Provider connection testing** ("Test Connection" button before saving) | LiteChat doesn't validate provider config — would reduce support | ⭐⭐⭐⭐ |
| **iOS-style grouped settings** | LiteChat has simpler LazyColumn settings | ⭐⭐ |
| **6-language i18n** | LiteChat is English-only | ⭐⭐ |
| **MCP in thin client** | Proves MCP doesn't need fat runtime | ⭐⭐⭐ |
| **Accent palettes + dynamic color** | LiteChat is single-theme dark | ⭐⭐ |

---

## EveryTalk (★176) — SUBAGENT DEEP-DIVE COMPLETE

**532 Kotlin files. 12 stealable patterns across 3 tiers.**

### Tier 1 (High Value)

| # | Feature | LiteChat status |
|---|---------|----------------|
| 1 | **Citation cards** — PageSourcesButton pill with stacked domain favicons + WebMarkdownSourcesExtractor | → C-013 scope: add citation cards to /browse |
| 2 | **Incremental Streaming Block Parser** — O(1) tail-only re-parsing, 60ms debounce, 8ms first-flush | → Future: replace current SSE parser |
| 3 | **Anthropic Direct Client** — Full SSE parser for Messages API, 50-loop tool calling, native context compaction | → Future: Anthropic support |
| 4 | **Tool Loop Context Guard** — Cross-provider tool output compression, 64K hard cap, oldest-first truncation | → Already C-010 (token compression) |
| 5 | **Image Gen Model Family Detection** — detectFamily() with per-family aspect ratios, quality tiers | → Already C-011 (/imagine), could enhance |

### Tier 2 (Medium Value)

| 6 | **Streaming Pause/Freeze** — flatMapLatest + emptyFlow() to suspend UI | → Add to C-009 scope |
| 7 | **Markdown Extension Preprocessor** — <details>, footnotes, emoji shortcodes | → Future markdown enhancement |

### Tier 3 (Nice-to-Have)

Code block card with inline web preview, scroll fade edge gradients, prompt cache policy.

---

## Kai 9000 (★1200) — SUBAGENT DEEP-DIVE COMPLETE

**1847 files. Most surprising find: the "honesty rule."**

| # | Feature | Thin-client? | LiteChat status |
|---|---------|-------------|-----------------|
| 1 | **Provider failover chain** | ✅ | → Already C-017 |
| 2 | **Memory system** — KV JSON, 5-hit promotion, append to soul | ✅ | → Already C-020 |
| 3 | **Heartbeat prompt assembly** — pure-function builder, "HEARTBEAT_OK" sentinel | ⚠️ Pattern only | → Note for future |
| 4 | **3-trigger task model** — TIME/CRON/HEARTBEAT | ⚠️ Pattern only | → Note for future |
| 5 | **"Honesty rule"** — single sentence in system prompt that measurably fixes model fabrication: *"Do not fabricate tool outputs, file contents, citations, or completed work."* | ✅ Zero cost | → **ADD TO CHATVIEWMODEL NOW** |

**NOT portable:** Linux sandbox, kai-ui, Android daemon, MCP servers — all bundled-runtime.

---

## Agora (newo-ether) — SUBAGENT DEEP-DIVE COMPLETE

**274 Kotlin files, ~75,500 LOC, 51MB APK, 100% free (no monetization)**

| # | Feature | APK cost | LiteChat status |
|---|---------|----------|-----------------|
| 1 | **Multi-key per provider** — named keys, radio-button selection, masked previews | ~15KB | → NEW TICKET C-023 |
| 2 | **Arbitrary base URLs** — per-provider URL with debounced save, protocol-aware path resolution | ~8KB | LiteChat already supports custom baseUrl |
| 3 | **File-based memory system** — active_memory.md + memory_db/, CRUD tools, no vector DB | ~12KB | → Already C-020 (persistent memory) |
| 4 | **Model aliasing** — custom display names, survives provider renames | ~3KB | → Already C-005 (/models) |
| 5 | **Per-conversation settings overrides** — merge pattern for context window, temp, tool toggles | ~6KB | → Already C-018 (per-conversation binding) |
| 6 | **Scheduled tasks** — one-shot + cron, WorkManager, headless generation | ~30KB | → NEW: Scheduled prompts |
| 7 | **Conversation loops** — periodic auto-injection, revision guards, cycle limits | ~20KB | → Future (agent feature) |
| 8 | **Conversation forks** — message tree, branch selection, attachment-safe cloning | ~25KB | → NEW TICKET C-024 |
| 9 | **Web search** — DDG scraper (free, no API key) | ~15KB | → Already C-013 (/browse) |
| 10 | **MCP integration** — SSE transport, tool discovery, single-server | ~35KB | → Future |
| — | **Unread indicators, draft persistence, compare-and-set titles, audit trail** | 0 KB | Patterns to adopt in existing code |

---

## Priority steal ranking (so far)

1. ⭐⭐⭐⭐⭐ **Provider failover chain** (Kai) — try next provider if primary fails
2. ⭐⭐⭐⭐⭐ **Per-conversation model binding** (AetherisAI) — switch models per chat
3. ⭐⭐⭐⭐ **Provider connection testing** (ChatCat) — "Test" button in Settings
4. ⭐⭐⭐⭐ **Persistent memory with hit-count** (Kai) — promote facts to system prompt
5. ⭐⭐⭐ **Web search citation cards** (EveryTalk) — for C-013 /browse
6. ⭐⭐⭐ **Voice input** (EveryTalk) — Android SpeechRecognizer, zero APK
7. ⭐⭐⭐ **Live model discovery caching** (AetherisAI) — enhance C-005
8. ⭐⭐ **Dual SSE framing** (AetherisAI) — Anthropic support

*Subagent deep-dives for Agora, EveryTalk, and Kai will refine this list.*