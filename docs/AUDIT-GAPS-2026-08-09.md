# Cross-reference audit — gaps, changes, opportunities

Date: 2026-08-09
Based on: 25 research docs, 20+ competitors, 16 tickets (10 Done, 6 Ready)

---

## Gap 1: Provider failover chain — NO TICKET EXISTS

**Competitor:** Kai 9000 — "ordered priority list, auto-failover on outages"
**What it does:** If primary API provider returns 5xx/timeout, auto-try next provider in user's list.
**LiteChat status:** C-004 handles stream-broken per-baseUrl, but there's no multi-provider failover. If OpenAI is down, user gets error toast.
**APK cost:** 0 KB
**Dev effort:** ~50 lines — try/catch around API call, iterate provider list.
**Action:** → Create C-017: Provider failover chain

---

## Gap 2: Per-conversation model binding — NO TICKET EXISTS

**Competitor:** AetherisAI — "per-conversation model+system prompt binding"
**What it does:** Each conversation remembers which model it was created with. When you return to an old chat, it uses the original model, not the global default.
**LiteChat status:** Global model setting in Settings. Switching models mid-session is annoying.
**APK cost:** 0 KB — add `model` field to ConversationEntity
**Dev effort:** ~30 lines + Room migration
**Action:** → Create C-018: Per-conversation model binding

---

## Gap 3: Provider connection testing — NO TICKET EXISTS

**Competitor:** ChatCat — "full-screen provider editor with Test Connection button"
**What it does:** Before saving a new provider config, user can tap "Test" to verify the endpoint works. Reduces support burden.
**LiteChat status:** Users paste baseUrl + key → hope it works. No validation until first send.
**APK cost:** 0 KB — just a GET /v1/models call on save
**Dev effort:** ~20 lines
**Action:** → Create C-019: Provider connection test button

---

## Gap 4: Persistent memory / learning — NO TICKET EXISTS

**Competitor:** Kai 9000 — "facts promoted to system prompt after hitCount≥5"
**What it does:** The app notices when the user repeatedly says things like "I prefer short answers" or "My name is Alex." After N repetitions, it promotes these facts to the system prompt automatically.
**LiteChat status:** No memory system. Each conversation is isolated.
**APK cost:** 0 KB — store in Room, inject into system prompt
**Dev effort:** ~80 lines
**Thin-client compatible:** Yes — pure data, no agent runtime needed.
**Action:** → Create C-020: Persistent user memory (Pro-gated)

---

## Gap 5: Voice input — NO TICKET EXISTS

**Competitor:** EveryTalk, Ke-Chat — voice input for messages
**LiteChat status:** Text-only input. No voice.
**APK cost:** 0 KB — Android's built-in SpeechRecognizer (no ML Kit needed)
**Dev effort:** ~30 lines
**Action:** → Create C-021: Voice input via Android SpeechRecognizer

---

## Gap 6: No distribution/marketing tickets exist

**Competitors found on:** 4PDA, XDA, F-Droid, r/androidafterlife, awesome-byok-apps, X/Twitter
**LiteChat status:** Code is done but no distribution plan has been ticketed.
**Action:** → Create D-004: Distribution pack — F-Droid metadata, XDA post template, awesome-byok-apps PR, r/androidafterlife post

---

## Gap 7: C-016 scope should widen

**Current:** Image attachment + vision model (OCR)
**Should be:** Image attachment + vision model + file attachment (PDF, text files)
**Reason:** ChatCat and EveryTalk both support PDF/file upload. LiteChat's image attachment code is 90% of the way to general file attachment. The vision model can also read screenshots of documents.
**Action:** Expand C-016: "Image + file attachment with vision model support"

---

## Gap 8: C-013 (/browse) should add citation cards

**Current:** /browse fetches page → injects text → LLM responds
**Should be:** /browse fetches page → shows citation card with URL/title → LLM responds
**Reason:** EveryTalk and TypingMind both show citation cards for web-sourced content. Without cards, users don't know where the info came from.
**Action:** Add citation card rendering to C-013 scope

---

## Gap 9: No settings import/export exists

**Competitor:** Agora — key aliases/rotation. ChatPPP — preset binding.
**LiteChat status:** Users manually enter API key, baseUrl, model. No way to export/import config.
**This is a friction point** for users who use multiple providers or switch devices.
**Action:** → Create C-022: Settings export/import (JSON)

---

## Things LiteChat is doing RIGHT that competitors don't

| Feature | LiteChat | Competition |
|---------|----------|-------------|
| Compat matrix (honest RAM bands) | ✅ Unique | No one else does this |
| 4GB-first positioning | ✅ Core marketing | Everyone targets flagships |
| $4.99 one-time impulse Pro | ✅ | TypingMind is $59-199, others are free or subscription |
| Slash-command UX (/imagine, /browse) | ✅ Novel | No BYOK Android client uses commands |
| Streaming fallback (numAi-plus) | ✅ C-004 | Most just fail silently |
| Dual flavor (play + foss) | ✅ C-002 | Almost no one else splits |
| Lazy AdMob (no cold-start) | ✅ C-001 | Competitors initialize at app start |

---

## Revised priority: what to build next

| # | Ticket | What | APK | Effort |
|---|--------|------|-----|--------|
| 1 | **C-017** | Provider failover chain (Kai steal) | 0 KB | 50 lines |
| 2 | **C-018** | Per-conversation model binding (AetherisAI steal) | 0 KB | 30 lines |
| 3 | **C-019** | Provider connection test (ChatCat steal) | 0 KB | 20 lines |
| 4 | **C-020** | Persistent user memory (Kai steal, Pro) | 0 KB | 80 lines |
| 5 | **C-021** | Voice input (EveryTalk steal) | 0 KB | 30 lines |
| 6 | **C-013** | /browse web scraping (with citation cards) | ~30KB | ~60 lines |
| 7 | **C-016** | Image + file attachment + vision | 0 KB | ~80 lines |
| 8 | **C-022** | Settings export/import | 0 KB | 30 lines |
| 9 | **D-004** | Distribution pack | 0 KB | docs only |

All 9 are zero-APK except C-013 (~30KB for Jsoup).

---

*Update pending: Agora/EveryTalk/Kai subagent deep-dives may surface additional gaps.*