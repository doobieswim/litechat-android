# R-008 — Premium tier strategy & feature roadmap

Date: 2026-08-09
Status: Research → Done
Source: User strategy input

## Pricing validation

The existing $4.99 one-time Pro (C-003, C-007) is validated by user strategy as the correct price point. Key reasoning:

- **Impulse-buy threshold:** $4.99 is below the psychological $5 barrier
- **Zero server costs:** Client-side BYOK means 100% margin after Play's 15% cut ($4.24 net per sale)
- **No subscription fatigue:** Users are fleeing $20/month AI subscriptions; one-time $4.99 is the antidote

### Revenue bands

| Tier | Purchases | Net Revenue | How |
|------|-----------|-------------|-----|
| Indie success | 1k-5k | $4k-$21k | Reddit, Product Hunt, Play Store SEO |
| Mid-market | 10k-30k | $42k-$127k | Best-of lists, YouTube reviews |
| Market leader | 50k+ | $212k+ | Top Play Store productivity charts |

Google takes 15% on first $1M/year → $4.24 net per $4.99 sale.

---

## Premium feature evaluation

Four Pro-gated features proposed. Each evaluated against LiteChat's thin-client constraints (Tier A only, no server costs, minimal APK impact, 4GB-first).

### 1. In-App Web Scraping — ✅ HIGH FIT

| Criteria | Assessment |
|----------|-----------|
| Server cost | Zero — phone fetches, LLM processes |
| APK impact | Zero — OkHttp already in deps, platform WebView optional |
| Thin-client alignment | Perfect — Opera Mini pattern: thin client scrapes, remote brain reads |
| Implementation | OkHttp GET → Jsoup or regex text extraction → inject into conversation context |
| Pro-gate | Clearly premium: "Browse the web" is a premium feature in every AI chat app |

**Architecture:**
```
User: /browse https://example.com
  → OkHttp GET → extract <body> text → truncate to ~8K chars
  → Prepend to next message: "Here is the content of https://example.com:\n\n{text}"
  → Send to LLM
```

**Additional Pro value:** Could also support `/browse` followed by a question: `/browse https://... what is the main argument?` — fetches, injects, asks.

**Risk:** Some sites block non-browser User-Agents. Mitigation: set a browser-like UA, handle 403 gracefully.

### 2. BYO-Cloud Sync — ✅ GOOD FIT

| Criteria | Assessment |
|----------|-----------|
| Server cost | Zero — user's own Google Drive/Dropbox |
| APK impact | Google Drive API ~200KB, or just export/import via SAF |
| Thin-client alignment | Palm HotSync pattern: user owns the sync, not the vendor |
| Implementation | Two approaches: (A) Google Drive API for auto-backup, (B) Storage Access Framework for manual export/import |

**Architecture (Option B — simpler, no Drive API dep):**
```
Settings → "Back up chats"
  → Export: encrypt Room DB → write JSON to user-chosen folder via SAF
  → Import: user picks file → decrypt → merge into Room DB
```

**Option A (full auto-sync):** Google Drive REST API, periodic backup of encrypted chat DB. Needs OAuth, more complex. Pro-only.

**Recommendation:** Start with Option B (manual export/import via SAF). Add Google Drive auto-backup as a stretch goal. Both Pro-gated.

### 3. Floating Chat Overlay + OCR — ⚠️ MODERATE FIT

| Criteria | Assessment |
|----------|-----------|
| Server cost | Zero (if cloud OCR via user's key) or zero (if on-device ML Kit) |
| APK impact | ML Kit Text Recognition: ~8MB (too heavy) / Cloud OCR: zero (reuses existing API) |
| Thin-client alignment | Mixed: overlay is native Android (good), on-device OCR is anti-thin |
| Implementation complexity | High — SYSTEM_ALERT_WINDOW permission, overlay service, OCR pipeline |

**Split recommendation:**

**3a. Floating Chat Overlay (Pro):** SYSTEM_ALERT_WINDOW, small chat bubble that opens a minimal Compose chat overlay. User can query AI without leaving current app. This is genuinely premium and has zero APK cost.

**3b. OCR via cloud (Pro):** User takes screenshot → app sends to GPT-4V/Claude Vision via existing API key → extracts text. No on-device ML needed. Pure thin-client.

**3c. OCR on-device (NOT recommended):** ML Kit adds 8MB. Anti-thin. Skip.

**Recommendation:** Split into two separate tickets. Floating overlay first (lower risk, higher wow-factor). Cloud OCR second. Both Pro-gated.

### 4. Prompt Variables — ✅ EXCELLENT FIT

| Criteria | Assessment |
|----------|-----------|
| Server cost | Zero |
| APK impact | Zero — pure UI + data model |
| Thin-client alignment | Perfect — adds professional utility without bloat |
| Implementation | Template engine in ViewModel, template CRUD in Settings |

**Architecture:**
```
Template: "Review this text using a [Tone] voice for an audience of [Target Audience]"
  → User fills: Tone = "professional", Target Audience = "engineers"
  → Rendered: "Review this text using a professional voice for an audience of engineers"
  → Sent as system prompt
```

**Data model:**
```kotlin
data class PromptTemplate(
    val id: String,
    val name: String,
    val template: String,  // "Review this using a [Tone] voice..."
    val variables: Map<String, String>,  // {"Tone": "professional", "Target Audience": "engineers"}
)
```

Store in DataStore JSON. Ship 3-5 built-in templates. Users create custom ones. Pro-gated (free tier gets 1 template).

---

## Priority ranking

| # | Feature | APK cost | Dev effort | Wow factor | Pro value |
|---|---------|----------|------------|------------|-----------|
| 1 | Prompt Variables | 0 KB | Low | Medium | High |
| 2 | Web Scraping (/browse) | ~30KB (Jsoup) | Low | High | High |
| 3 | Cloud Sync (SAF export) | 0 KB | Low-Medium | Medium | Medium |
| 4 | Floating Overlay | 0 KB | Medium-High | High | High |
| 5 | Cloud OCR (GPT-4V) | 0 KB | Low | Medium | Medium |

---

## New backlog tickets

### C-012 — Prompt template variables (Pro-gated)

**Status:** Ready
**Goal:** Users create prompt templates with dynamic `[Variable]` fields. Pro users get unlimited templates; free tier gets 1 built-in.

### C-013 — In-app web scraping (/browse command, Pro-gated)

**Status:** Ready
**Goal:** `/browse <url>` fetches web page, extracts text, injects into conversation context. Pro-only.

### C-014 — Manual chat backup/restore via SAF (Pro-gated)

**Status:** Ready
**Goal:** Export encrypted chat database to user-chosen folder, import from file. Storage Access Framework. Pro-only.

### C-015 — Floating chat overlay (Pro-gated)

**Status:** Idea
**Goal:** Floating chat bubble accessible from any app. SYSTEM_ALERT_WINDOW. Minimal Compose overlay.

### C-016 — Cloud OCR via vision model (Pro-gated)

**Status:** Idea
**Goal:** Take screenshot → send to GPT-4V/Claude Vision via existing API key → extract text → inject into chat.

---

## Revenue-maximizing launch sequence

```
v1.0: Core chat + $4.99 Pro (ads removal)          → C-001 through C-007  ✅ DONE
v1.1: Image generation (/imagine)                    → C-011               ✅ DONE
v1.2: Prompt variables + Web scraping                → C-012 + C-013       ← NEXT
v1.3: Cloud sync + Floating overlay                  → C-014 + C-015
v1.4: Markdown rendering + Height placeholders       → C-008 + C-009
```

Each release ships one free feature (retention) + one Pro feature (conversion). The pattern keeps the free tier useful while steadily building Pro value, converting the "I'll just use the free version" users over time.

---

**R-008 complete.** C-012, C-013, C-014 are Ready. C-015, C-016 are Ideas. Recommend coding C-012 first (zero APK cost, highest Pro value per dev hour).