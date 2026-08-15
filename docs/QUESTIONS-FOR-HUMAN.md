# Questions for human (product owner)

Agents must not guess these. Answer briefly; research/coding will update BACKLOG.

<!-- Template

### H-00N — short title
- **From:** research | coding
- **Decision needed:**
- **Options:**
- **Recommendation:**
- **Decision:** _(human fills)_

-->

### H-001 — applicationId / store name
- **From:** research
- **Decision needed:** Final Play name + `applicationId` (currently `com.litechat.android`).
- **Options:** **BYO AI** (recommended — "bring your own", free, clear) · **BYOK Chat** (category-literal, dev-recognizable) · **2G AI** (retro weak-phone charm) · keep "LiteChat" (❌ collides with a live offline-AI-chat app `com.sbro.litechat` — not recommended).
- **Recommendation:** **BYO AI** — short description: *“Chat with your own key. Works on 4GB phones. No monthly bill.”* (61/80). 4GB lives in that line + a screenshot, **not** the name. Full table: `docs/APP-NAMING-RESEARCH.md`.
- **Hold:** Play Console must search the exact name at submit time. DIG will not flip C-031 to Ready until PROOF grades this naming pack.
- **Decision:** **BYO AI** (human, 2026-08-15). Short description: *“Chat with your own key. Works on 4GB phones. No monthly bill.”* applicationId when WIRE runs C-031: `com.byoai.chat`. Namespace/package stay `com.litechat.android`.

### H-002 — FOSS applicationId
- **From:** coding (when C-002 starts)  
- **Decision needed:** Same id as play or `.foss` suffix / separate id?  
- **Recommendation:** `applicationIdSuffix = ".foss"` for side-by-side install  
- **Decision:**  

### H-003 — Pro price / SKU
- **From:** research
- **Decision needed:** Confirm SKU `litechat_pro` and price band
- **Decision:** **$4.99 one-time, SKU `BYO_pro`** (human 2026-08-15). `PLAY_PRO_SKU` buildConfigField updated to `BYO_pro`; README updated. Play Console must create the managed product with this exact id.

### H-004 — Who we sell to first (historic positioning map)
- **From:** research (R-011 `docs/SALES-POSITIONING-HISTORIC.md`)
- **Decision needed:** Four answers. DIG will not invent name/hook lists until these are filled. They change the historic stack (Hopkins vs Listerine vs shareware vs Trout).
- **Questions:**
  1. **First human.** Who is the *first* person you want to tap Install? Pick the one you would tattoo on the icon, not the "everyone" answer. (A) already has an API key and hates fat apps (B) hates the $20/month ChatGPT bill and has no key yet (C) owns a cheap/old 4GB phone that melts (D) wants privacy/no-account more than anything else
  2. **One promise.** If the app could be famous for only one sentence, which? (A) "We tell you when your phone is too weak" (B) "Your key, your brain, we never see it" (C) "Built for a $150 phone" (D) "Pay once, no rent"
  3. **Disease or desire.** Listerine named a hidden pain. Hopkins rode a want people already had. Which? (A) name the pain ("your phone is lying about RAM" / "you are renting a chatbot") (B) ride the want ("bring your own AI to the party") (C) both — pain in the subtitle, want in the name
  4. **Front door.** How should a stranger first meet us? (A) shareware: free real chat + one banner, $4.99 kills it (current plan) (B) Trojan Horse: a tiny useful free tool (cost display? compat check?) that later becomes the chat app (C) Caples story: launch as a Reddit/X story about a cheap phone, app is the punchline
- **Recommendation:** 1C (or anyone on a normal phone), 2 mixed as *user words* (“works / yours / no monthly bill”). Theme lock: show don’t tell — `docs/THEME-SHOW-DONT-TELL.md`.
- **Decision:** (human 2026-08-15)
  1. **First human: C** (cheap/old 4GB phone owner) — follows from the promise.
  2. **One promise: C, upgraded — “Built for a $30 phone.”** Verified 2026-08-15: Walmart sells the **Straight Talk Moto G 2025 (5G, 64GB) at $29.88 with 4GB RAM** (locked to Straight Talk; plans from $35/mo) — https://www.walmart.com/ip/Straight-Talk-Motorola-Moto-g-2025-5G-64GB-Blue-Prepaid-Smartphone-Locked-to-Straight-Talk/14552506783 . Honest, checkable, nobody else can say it.
  3. **Disease or desire: C** (both — want in the name, pain in the subtitle).
  4. **Front door: A** (shareware — free chat + one banner, $4.99 removes it).

### H-005 — Which shine stunts we will actually run
- **From:** research (R-012 `docs/SHINE-UNCONVENTIONAL.md`) + H-006 quieter lock
- **Decision needed:** Logistics only. Not a fight poster.
- **Questions:**
  1. Off-store honesty test later (same cheap phone, filmed, we can lose) — **not** on the Play listing? **Y/N**
  2. Store page names **no** rival (ChatGPT / Agora stay off the listing)? **Y/N** — theme law already says yes
  3. Ship FOSS / 4PDA / XDA **before** Play (distribution, not a stunt)? **Y/N**
- **Recommendation:** **N / Y / your call.** After H-006: no store dare, no named enemy on Play. Screenshot of a real size number or a calm “low memory” note is enough. Underground week is optional logistics (`docs/DISTRIBUTION-FOSS.md` already exists).
- **Decision:** (human 2026-08-15, adopted research recommendations)
  1. **Honesty test: N** — not now; save as a post-launch story.
  2. **No rival names on listing: Y** — already theme law.
  3. **Ship FOSS / 4PDA / XDA before Play: Y** — the “secret Mississippi field” move; costs nothing.

### H-006 — Lock King's Road as the underdog theme
- **From:** research (R-013 `docs/KINGS-ROAD-THEME.md` + `docs/THEME-SHOW-DONT-TELL.md`)
- **Decision needed:** Is the app’s face “the unmasked underdog in the ring,” with old fat apps as the champs?
- **Options:**
  - **Yes, lock it.** Next DIG pass = underdog name concepts + Play listing as a bout card. No wrestler mascot unless you ask.
  - **Yes, but quieter.** Same grammar (kick out, stay thin, publish losses). No fight words on the listing.
  - **No.** Keep generic thin-BYOK. Drop the ring metaphor.
- **Recommendation:** **Yes, but quieter.**
- **Decision:** **Yes, but quieter** (human, 2026-08-15). Vibe + architecture show a small app against the fat/expensive norm. For regular people. Do **not tell** them it is a fight. Everyday chat app on the face. Source of truth: `docs/THEME-SHOW-DONT-TELL.md`. Backstage grammar only: `docs/KINGS-ROAD-THEME.md`.

### H-007 — Fourth agent codeword
- **From:** plan `/opt/data/.hermes/plans/2026-08-15_051453-research-review-agent.md`
- **Decision needed:** Name + how often + first job
- **Decision:** **PROOF**, **every** research ticket, **grade the pile we just wrote** (human 2026-08-15). First verdict: Issues — see `docs/RESEARCH-REVIEW.md`.

### H-008 — Pro bundle direction (from R-016)
- **From:** research (R-016, synthesis `docs/PRO-ROADMAP.md` + TypingMind digs `docs/TYPINGMIND-DIG.md`)
- **Decision needed:** Which Pro direction to build. All digs converge on **Voice + Memory + Ownership at $4.99 once** ("Your key. Your voice. It remembers. No monthly bill."), but scope is the human's call.
- **Options:**
  - **A) Full Tier 1 bundle** — voice mode, memory+/recall, full-text search, encrypted backup upgrade, template + persona packs, quiet+registration, web search, **chat folders**, image editing. Biggest Pro value, most work (~1-2 weeks of coding).
  - **B) Voice first** — just voice mode + read-aloud (the next big feature), everything else later. Smallest meaningful step.
  - **C) Ownership first** — search + backup upgrade + quiet/registration + folders (no voice). Best "people pay to protect their work" play, zero new APIs.
  - **D) Pick from the Tier 1 list** — human names the 2-3 features.
- **Free regardless of choice:** language output control, model knobs + prompt caching, pin chats, save draft (TypingMind steals; honesty brand).
- **Recommendation:** **A** (the bundle is the moat; ~0 KB cost each), fallback **C** if voice feels risky.
- **Decision:** _(human fills)_

### H-009 — Language support scope (i18n)
- **From:** research (R-016 addendum; human asked "can we add all the languages")
- **Decision needed:** How much of the app should speak other languages, and how far.
- **Facts:** app has only **2** strings in resources; **~100 user-facing strings are hardcoded in Kotlin** (Screens.kt etc.). So UI translation needs one string-extraction refactor first (small ticket, ~0 KB APK). Android supports any number of locales — adding a language = one `values-XX/strings.xml` file. F-Droid/XDA/4PDA communities translate FOSS apps for free.
- **Options:**
  - **A) Language output only** — model replies in a chosen language (system-prompt setting). Free, trivial, do now. UI stays English.
  - **B) UI in top markets** — extract strings, then ship the 5-10 languages Research C (global markets) names (Spanish, Portuguese, Indonesian, Hindi, French, Arabic, Russian…). Community-translated via FOSS channels.
  - **C) All languages, machine-translated** — extract strings + ~100 machine-translated locales. Technically possible, but broken translations hurt the honest brand and Play reviews.
  - **D) B now, C later** — extract once, ship top markets first, let community drive the long tail.
- **Recommendation:** **D** (A is included free anyway). Research C runs before this is built.
- **Decision:** **Languages are free, always** (human 2026-08-15). UI i18n AND language output control are free — never gated. Voice mode is Pro; language is NOT. Long tail community-driven (option D); Research C names the first markets.
