# R-010 — Final gap analysis: everything missed

Date: 2026-08-09
Based on: cross-referencing all 15 implemented tickets vs competitor patterns + stubs

---

## GAP 1: Stub code needs wiring (4 partial implementations)

Several tickets have code in ViewModel but no UI connection:

| Ticket | What's coded | What's missing |
|--------|-------------|----------------|
| **C-021** | Mic button in composer | No actual SpeechRecognizer intent, no permission request, no activity result |
| **C-022** | Export/Import buttons in Settings | No file picker (ActivityResultContracts), no JSON parse on import |
| **C-016** | attachImage() in ViewModel | No gallery/file picker button in chat composer |
| **C-015** | OverlayService + manifest | No UI toggle to start/stop overlay, no permission request flow |

**Action:** → Create C-025: Wire stubs — complete C-021/C-022/C-016/C-015 UI connections

---

## GAP 2: verify_static.py not updated for batch 1-3 tickets

The verify script at 53/53 tests C-001 through C-010 + Kai honesty rule, but has no guards for:
- C-013 (Jsoup dep, /browse, fetchPage)
- C-014 (exportChats, importChats)
- C-015 (OverlayService, SYSTEM_ALERT_WINDOW)
- C-016 (attachImage, Base64)
- C-017 (ProviderEntry, getProviderList, failover)
- C-018 (ConvEntity.model, convModel switching)
- C-019 (Test button, Connected check)
- C-020 (MemoryManager, hit count)
- C-021 (voice input, listening)
- C-022 (Export/Import buttons, JSON)
- C-023 (NamedKeyStore)
- C-024 (MessageEntity.parentId)

**Action:** → Create C-026: Add verify_static guards for C-013 through C-024

---

## GAP 3: Competitor patterns we noted but never ticketed

### From Agora subagent:
- **Scheduled tasks** (WorkManager, cron-based, headless generation) — no ticket
- **DDG scraper** (free web search, no API key needed) — alternative to Jsoup /browse
- **Per-conversation draft** (auto-save input text before leaving chat) — no ticket

### From EveryTalk subagent:
- **Streaming pause/freeze** (flatMapLatest + emptyFlow() to suspend UI updates) — not in C-009 scope
- **IncrementalStreamBlockParser** (O(1) tail-only re-parsing, 60ms debounce) — not ticketed
- **Markdown Extension Preprocessor** (details, footnotes, emoji shortcodes) — not ticketed

### From Kai subagent:
- **Heartbeat prompt assembly** (scheduled LLM self-checks, "HEARTBEAT_OK" sentinel) — no ticket
- **3-trigger task model** (TIME/CRON/HEARTBEAT) — no ticket

### From ChatCat:
- **i18n** (6 languages: EN, ZH, ES, JA, DE, FR) — no ticket
- **Accent palettes + dynamic color** — no ticket

### From AetherisAI:
- **Live model discovery caching** (Room cache + pin/hide/refresh) — no ticket
- **AES-256 backup encryption** — C-014 is plain file copy, no encryption

**Action:** → Promote the high-ROI ones:
- C-027: Scheduled tasks (WorkManager — ~30KB, Agora steal)
- C-028: Incremental stream block parser (EveryTalk steal, ~100 lines)
- C-029: i18n with 6 languages (ChatCat steal, ~50KB)
- C-030: Live model discovery caching (AetherisAI steal)
- C-031: Streaming pause/freeze (EveryTalk steal, ~20 lines)
- C-032: Markdown extension preprocessor (EveryTalk steal, ~30 lines)
- C-033: DDG scraper (Agora steal — free, no API key)
- C-034: Per-conversation draft persistence (Agora steal)
- C-035: AES-256 backup encryption (AetherisAI steal — enhance C-014)

---

## GAP 4: Distribution not fully executed

D-004 created F-Droid metadata + XDA template, but missing:
- **Play Store listing** (short + full description, screenshots)
- **awesome-byok-apps PR** (actual submission)
- **r/androidafterlife crosspost** (thread template)
- **README update** reflecting all new features

**Action:** → Create D-005: Complete distribution — Play listing, awesome-byok-apps PR, README update

---

## GAP 5: No tests for new features

All 15 new tickets (C-008 through C-024) have zero unit tests. The verify_static.py catches structural issues but not behavior.

**Action:** → Create C-036: Add unit tests for new features (ContextTrimmer, MemoryManager, NamedKeyStore)

---

## GAP 6: README is out of date

Last README update was before C-008. The README doesn't mention:
- Markdown rendering
- /imagine, /browse, /ocr commands
- Prompt templates
- Token compression
- Height placeholders
- Voice input
- Backup/restore
- Multi-key support
- Conversation forks
- Floating overlay
- Connection testing
- Provider failover
- Settings export/import
- Persistent memory

**Action:** → Update README (included in D-005)

---

## Summary: what to build next

| Priority | Tickets | Effort | Why |
|----------|---------|--------|-----|
| 🔴 P0 | C-025 (wire stubs) | ~2h | Makes 4 features actually usable |
| 🔴 P0 | C-026 (verify guards) | ~30min | Protects against regressions |
| 🟡 P1 | D-005 (distribution) | ~1h | Unlocks Play Store + community growth |
| 🟢 P2 | C-027 through C-035 | ~2 days | Competitive parity with Agora/EveryTalk/Kai |
| 🟢 P2 | C-036 (tests) | ~4h | Quality foundation |

**Currently 0 Ready tickets.** All 24 coding tickets are Done. 1 docs ticket is Done (D-004).