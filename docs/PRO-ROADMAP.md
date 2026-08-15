# Pro incentive roadmap — BYO AI (synthesis of 3 research digs)

**Date:** 2026-08-15
**Agent:** LITECHAT-DIG (synthesis)
**Sources:** `docs/PRO-SCAN-COMPETITIVE.md` (15 competitors) · `docs/PRO-TRENDS-NEXT-BIG.md` (16 trends) · `docs/PRO-LATENT-WANTS.md` (14 wants, 71 URLs)
**Status:** Research — needs `LITECHAT-PROOF` approve before any ticket goes Ready. Human decides direction (H-008).

---

## The short version

All three digs converge on one bundle: **Voice + Memory + Ownership, sold once at $4.99.**

- **Trends** says voice is the next big feature (GPT-Live, Gemini Live; 150M/week ChatGPT voice users) and memory is a 2026 baseline expectation.
- **Latent-wants** says people pay for *protection of their work* (backup, search, export) and for *"pay once, own it forever"* (WinAmp made $100k/month in paper checks from $10 registrations).
- **Competitive** says small one-time unlocks convert: sync, export, web search, TTS, usage stats, count-limits — and that gating *reliability* (core chat, failover) reads as ransom and kills the honest brand.

The one-liner the app can truthfully own by 2027 (from trends §8):
**"Your key. Your voice. It remembers. No monthly bill."**

---

## Tier 1 — NOW (v1.1, all ~0 KB APK, all API-side, all extend existing code)

| # | Feature | Evidence | Extends | Pro gate |
|---|---------|----------|---------|----------|
| 1 | **Voice mode**: mic → Whisper STT (incl. Groq free tier) → chat → TTS read-aloud | Trends #1; Scan #3 (OpenCat & Chattica gate TTS) | Existing voice stub C-021 | Pro = full voice; free = 1 voice exchange/day |
| 2 | **Memory+**: /recall command, rolling summaries, memory visible/editable | Trends #2; Latent P-010 | C-020 MemoryManager (done, Pro) | Pro (already gated) |
| 3 | **Full-text search across chats** | Latent P-003 (★ the highest-value missing Pro feature; Room FTS4/5, ~0 KB) | Room DB | Pro |
| 4 | **Encrypted backup upgrade**: AES + scheduled local backups + restore-to-new-phone | Scan #4; Latent #1/#4 (sunk cost; "the file you own") | C-014 (done, Pro) | Pro |
| 5 | **Template deep**: import/export + curated packs | Scan #1 (count-limits convert); Latent "finish the game" | C-012 (done, Pro) | Pro (already) |
| 6 | **Quiet + registration**: lifetime "Registered — BYO AI · date · no renewal, ever" screen; paid users never see a sale again | Latent P-001/P-006 (WinAmp; anti-enshittification) | Billing | Pro |
| 7 | **Web search**: /browse + DDG search-then-answer | Scan #2 (web search paid in Chatbox & UnboundChat) | C-013 /browse (done, Pro) | Pro |

**Free keeps:** core chat, failover, multi-key, /imagine, /video, voice *input*, forks, compat matrix, LAN/Ollama, i18n, basic export, template share-copy.

## Tier 2 — FUTURE (v1.2+, when Tier 1 lands)

| # | Feature | Evidence | Notes |
|---|---------|----------|-------|
| 8 | **BYO-Sync**: auto-backup to user's own WebDAV / Google Drive (SAF) | Scan #5 (OpenCat gates iCloud sync); Trends #5 (HotSync pattern) | "Your storage, your key" — never a vendor cloud |
| 9 | **Tasks mode**: chat→action (reminders, follow-ups) with visible token-budget guardrail | Trends #4 (Gartner: 40% of enterprise apps w/ agents by 2027) | Guardrail non-negotiable on 4GB |
| 10 | **Overlay v2**: persistent bubble + screenshot→vision OCR ask | Scan #6 ("AI anywhere" = OpenCat's Pro hook) | Extends C-015 |
| 11 | **Usage dashboard**: cost history, per-model totals, stats export | Scan #7 (UnboundChat gates usage stats) | Keep live per-message cost free |
| 12 | **Custom look**: accent colors, OLED, icon pack | Scan #8; Latent "make it yours" (IKEA effect) | Visible everyday premium |
| 13 | **Profiles**: separate work/personal setups | Latent P-007 | Gate scale, not building blocks |
| 14 | **Slash-command library**: discoverable /browse /imagine /recall | Latent P-005 | Utility must be findable |
| 15 | **Advanced provider config**: custom headers, exotic endpoints | Latent P-009 (XDA/4PDA crowd pays for knobs) | Power-user, quiet |

## Tier 3 — NEVER GATE (all three digs agree)

Core chat · provider failover · multi-key · compat matrix & honesty ("your phone can't") · key security (encrypted, on-device) · /imagine & /video (user already pays the provider) · i18n/languages · LAN/Ollama endpoints · basic chat export · community (XDA/4PDA/GitHub, template sharing) · security updates · the no-subscription promise itself.

**Why:** gating any of these reads as ransom and breaks the trust that makes $4.99 feel fair. The compat matrix IS the brand. TypingMind's $99 "lifetime" backlash (excluding cloud sync) is the cautionary tale: a lifetime buy must include everything visible.

## Pricing guardrails (from the digs)

- One-time band for "remove ads" IAP: **$2.99–$4.99** — our $4.99 sits at the top, fine.
- **Never** ship a subscription; the anti-rent position is the moat.
- RevenueCat data: ~30% of subscriptions churn in month 1 — one-time removes that entire problem.
- Count-limits and unlocks convert; paywalling the *app's reliability* does not.

## Next step

Human answers **H-008** (direction: full bundle / voice-only first / pick from Tier 1). Then PROOF grades this pack, then WIRE gets Ready tickets.
