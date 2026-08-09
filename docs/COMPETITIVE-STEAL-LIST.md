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

## EveryTalk (★176) — already stole height placeholders (C-009)

**Additional patterns to steal:**

| Feature | LiteChat readiness | Steal priority |
|---------|-------------------|---------------|
| **Web search citation cards** (from R-009 research) | LiteChat has no web search yet, but C-013 (/browse) is Ready | ⭐⭐⭐ |
| **Tool call UI** (rendering model tool calls as interactive cards) | Future MCP/agent feature | ⭐⭐ |
| **Voice input** (Android SpeechRecognizer) | Zero-APK voice input for messages | ⭐⭐⭐ |
| **Anthropic Messages API direct** | Beyond OpenAI-compatible — full Anthropic SSE | ⭐⭐ |

---

## Kai 9000 (★1200) — thin-client compatible steals?

| Feature | Thin-client compatible? | Steal priority |
|---------|------------------------|----------------|
| **Heartbeat/periodic checks** | Yes — could be a cron-like "check for updates" in background | ⭐⭐ |
| **Persistent memory** (hit-count promotion to system prompt) | Yes — pure data model, stored in Room | ⭐⭐⭐⭐ |
| **Multi-provider failover chain** | Yes — if primary provider fails, try next | ⭐⭐⭐⭐⭐ |
| **Screen builder** (AI generates interactive UI) | No — requires agent runtime | ❌ |
| **Linux sandbox on Android** | No — anti-thin client | ❌ |

---

## Agora (newo-ether) — pending subagent deep-dive

**Known from F-Droid survey:**
- 51MB APK (LiteChat targets ~2MB — 25x smaller)
- Multi-provider with key aliases/rotation
- Room DB, memory features
- Agentic workflows

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