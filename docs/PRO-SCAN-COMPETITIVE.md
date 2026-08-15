# PRO-SCAN — Competitive Pro-Monetization Scan for BYO AI

**Date:** 2026-08-15
**Author:** LITECHAT-DIG subagent (competitive Pro-monetization scanner)
**Scope:** What other BYOK/AI-chat apps gate behind Pro, which paywalls actually convert, and a ranked Pro-incentive shortlist for BYO AI.
**Status:** Research (backstage). Per `docs/THEME-SHOW-DONT-TELL.md`: findings here are **backstage**; anything that would surface in a user-facing listing is flagged below and must be re-worded in everyday language.
**Constraint honored:** $0 tools only; no gradle/build/terminal-heavy work; every load-bearing fact has a URL.

---

## 1. Executive summary (the short version)

- **The BYOK app economy runs on three models, in order of prevalence:** (1) free open source / F-Droid with zero monetization (Agora, Kai 9000, Maskan, maid, EveryTalk, OpenMinis, Enchanted, Aiyo); (2) free app + **one-time Pro unlock** of convenience/utility features (OpenCat $9.99, Pal Chat $6.99, BoltAI $79–99, MindMac $29, TypingMind $39–99); (3) free app + **subscription for bundled model access** (Chatbox $3.50–$39.99/mo) or freemium monthly + lifetime hybrid (Chattica $2.99/mo or $59.99 once; UnboundChat moving to paid plans April 2026).
- **What gets gated, repeatedly, by the apps that actually charge:** sync/backup, export, web search, voice/TTS, image-gen access, usage statistics, app lock/privacy toggles, count limits (characters, personas, templates), and "use AI anywhere" conveniences (keyboard extension, overlay).
- **What almost nobody gates:** core chat, multi-key/failover reliability, local model support, i18n, and anything that smells like a trust feature. The FOSS tier is table stakes.
- **Our $4.99 one-time Pro sits in the validated band** — the standard "remove ads" IAP is $2.99–$4.99 ([Kanopy](https://kanopylabs.com/blog/mobile-app-monetization-strategies-guide)), and mobile BYOK one-time unlocks cluster at $6.99–$9.99 (OpenCat, Pal Chat). $4.99 on Android (a cheaper market than iOS) is competitive-to-cheap.
- **The single biggest cautionary tale:** TypingMind sells a "$99 lifetime" license but excludes cloud sync/memory (separate recurring subscription) → buyers call it "daylight robbery" on Reddit. **Never sell a "lifetime" that excludes the features users actually want, and never move a gate after purchase.**

---

## 2. Competitor table — what each gates behind Pro/pay

| App | Platform | Free tier | Gated behind Pro / pay | Price model | What we can learn |
|---|---|---|---|---|---|
| **Agora** (newo-ether) | Android | Everything: multi-key, forks, memory, DDG search, on-device GGUF, remote control | Nothing (MIT, F-Droid + Play) | Free / FOSS | Feature-complete FOSS is our F-Droid floor; Play build must justify itself. 51MB APK vs our ~2MB is our diff. [F-Droid](https://f-droid.org/en/packages/com.newoether.agora/) · [GitHub](https://github.com/newo-ether/Agora) |
| **Kai 9000** | Android/iOS/desktop (KMP) | Everything incl. heartbeat, memory, tools, failover | Nothing (optional sponsorship on Play) | Free / FOSS | Memory & failover are given away free by the most advanced FOSS client → gating our memory (currently Pro) is defensible but not differentiating. [GitHub](https://github.com/SimonSchubert/Kai); monetization per project ref `references/thin-client-patterns-research.md` §12 |
| **Maskan** | Android | Everything: 11 providers, vision, voice, presets, folders | Nothing (GPL-3.0) | Free / FOSS | Voice + vision free here → weak gates. [F-Droid](https://f-droid.org/en/packages/app.maskan.chat/) |
| **maid** | Android | Everything: local llama.cpp + remote providers | Nothing | Free / FOSS | "No subscription, no cloud" is literally the pitch. [GitHub](https://github.com/Mobile-Artificial-Intelligence/maid) · [site](https://mobile-artificial-intelligence.com/maid) |
| **EveryTalk** | Android | Everything: multimodal, web search, voice, image gen | Nothing (MIT, 178★) | Free / FOSS | A very complete Chinese-ecosystem client monetizes nothing; they run on adoption. [GitHub](https://github.com/roseforljh/EveryTalk) |
| **OpenMinis** | iOS/Android/macOS | Everything: agent, Linux shell, MCP, scheduled tasks, overlay | Nothing (open source "coming soon") | Free | Overlay + scheduled tasks + skills shipped free by an agent app → our overlay Pro-gate is a convenience gate, not an agent-race gate. [Site](https://openminis.app/) |
| **Enchanted** | iOS/macOS | Everything (Ollama/local focus) | Nothing | Free / FOSS | Local-model crowd pays nothing, ever. [GitHub](https://github.com/gluonfield/enchanted) · [App Store](https://apps.apple.com/us/app/enchanted-developers-only/id6474268307) |
| **Chatbox** (chatboxai.app) | Desktop/iOS/Android/Web | Full BYOK app (chat, providers, local storage) | Web search, image chat, document chat, MCP, knowledge base, image gen quotas — only in **Chatbox AI Service** subscription | Hybrid: free BYOK app + subscription $3.50–$39.99/mo for bundled model access | The industry's best example of **gating features behind the paid service, not the BYOK app**. Web search & image gen are subscription features. [Plans](https://chatboxai.app/en/guide/chatbox-ai/plans) · [BYOK guide](https://chatboxai.app/en/guide/byok) · [App Store](https://apps.apple.com/us/app/chatbox-ai-chatbot-assistant/id6447763703) |
| **TypingMind** | Web/desktop/PWA (mobile-friendly; no native Android app) | Free tier: basic chat, ads/popups | One-time license ladder: Standard $39 (ads off, AI agents, voice input, share chats) → Extended $79 (image gen & editing, web search, TTS, vision, upload documents) → Premium $99 (multi-model chats, unlimited plugins, projects & folders, artifacts, free updates); **cloud sync/memory/knowledge-base = separate recurring subscription NOT in any lifetime tier**; Teams/bulk $395+ | One-time $39/$79/$99 + optional cloud sub | The definitive one-time-BYOK case AND the definitive anti-pattern: sync/memory excluded from "lifetime" → public "daylight robbery" backlash. Their own testimonials praise **folders + search + agents**. Full 40-feature dig + steal list: `docs/TYPINGMIND-DIG.md`. [Pricing](https://www.typingmind.com/buy) · [feature list](https://docs.typingmind.com/feature-list) · [backlash thread](https://www.reddit.com/r/openrouter/comments/1nevp2e/paid_99_for_typingmind_lifetime_license_now/) |
| **OpenCat** | iOS/macOS | Full BYOK chat, prompt library, markdown, multi-model | Pro $9.99 once: keyboard extension, **iCloud sync**, Siri, TTS, team. Separate Cloud AI sub ($2.99–$19.99/mo) for bundled models | Free + one-time Pro $9.99 + optional subscription | **One-time Pro gating iCloud sync has worked for years** (Forbes/TechCrunch featured). Sync is a first-class Pro gate on iOS. [Site](https://opencat.app/) · [App Store IAPs](https://apps.apple.com/us/app/opencat-ai-chat-agent-mcp/id6445999201) |
| **Pal Chat** | iOS/macOS | Full BYOK chat, offline models, Siri shortcuts, custom prompts | Pro $6.99 (lifetime unlock per user reports); advanced voice mode via subscription | Free + one-time Pro + optional sub | Minimal one-time Pro ($6.99) + free-model promos as growth. [App Store](https://apps.apple.com/us/app/pal-chat-ai-chat-client/id6447545085) · [MPU Talk](https://talk.macpowerusers.com/t/ios-app-to-chat-with-ai-via-api/37093) · [r/iosapps](https://www.reddit.com/r/iosapps/comments/1iuuk44/unlimited_free_openai_access_on_pal_chat_the_best/) |
| **UnboundChat** | Android/Windows | Core chat, 100+ models, voice, vision, image gen, 31 languages | Pro (all currently free via promo until 2026-03-31, paid plans from 2026-04-01): **encrypted cloud sync & Drive backups, zero-data-retention mode, app lock, analytics-off, web search & usage stats, local backups, folders, custom prompts, export** | Freemium → subscription (future) | The clearest Android blueprint of what to gate: **privacy toggles, sync, web search, stats, export**. Note they put voice/vision/image gen in the free tier. [Play](https://play.google.com/store/apps/details?id=com.marko.unboundchat) · [Site](https://unboundchat.net/) |
| **ChatticaAI** | iOS/Android | 2 characters, 1 persona, full chat, lorebook | Unlimited characters/personas, Adventure Mode, group chats, image API access, **TTS & voice input, export** | Freemium: $2.99/mo **or** $59.99 one-time lifetime | Count-limits + export + voice + image-access as gates; **offers lifetime alongside subscription** — the hybrid RevenueCat says 35% of apps now use. [Site](https://chattica.ai/) · [r/ChatbotRefugees](https://www.reddit.com/r/ChatbotRefugees/comments/1qmobaa/chatticaai_is_a_byok_ios_and_android_customizable/) |
| **BoltAI** | macOS | Trial (3 days) | The app itself: $79 Essential / $99 Pro, one-time, 30-day money-back | One-time (Mac power-user pricing) | One-time works even at $99 for desktop power users; $4.99 mobile is a rounding error → upsell path. [Pricing](https://boltai.com/pricing) · [r/macapps "worth the price"](https://www.reddit.com/r/macapps/comments/1d3628l/app_review_boltai_worth_the_price_aillm/) |
| **MindMac** | macOS | Trial | The app: Basic $29 one-time (+1yr updates) | One-time | Even cheaper desktop one-time ($29) exists and sells (r/ChatGPT buyers). [Site](https://mindmac.app/) · [r/ChatGPT](https://www.reddit.com/r/ChatGPT/comments/1juu3vx/anyone_use_mindmac/) |
| **ChatCat** (per project notes) | Android | — | Public pricing info scarce; project notes credit it with i18n (6 languages) + accent palettes + provider connection test | Unverified / likely free | Our C-019 test button came from here. Accent palettes = a cheap, visible premium surface. (Flag: public data thin; treat row as research-note quality.) |

*Cluster of other free players observed in BYOK threads: Aiyo, Cherry Studio, Witsy, Open WebUI — the r/LocalLLaMA crowd recommends exclusively free tools ([thread](https://www.reddit.com/r/LocalLLaMA/comments/1k8qwzo/best_apps_for_byok_ai/)).*

---

## 3. Which paywalls actually convert — evidence

### 3.1 Small one-time unlocks on mobile BYOK apps demonstrably work
- **OpenCat** has sold a $9.99 one-time Pro (keyboard extension, iCloud sync, Siri, TTS) for years and was press-featured (Forbes/TechCrunch/Wired badges on [opencat.app](https://opencat.app/)). iCloud sync behind a one-time paywall is the longest-running proof point that **sync converts**.
- **Pal Chat** ($6.99 Pro) and **Chattica** ($59.99 lifetime option) both monetize BYOK iOS users with lifetime unlocks, not subscriptions — [App Store](https://apps.apple.com/us/app/pal-chat-ai-chat-client/id6447545085), [chattica.ai](https://chattica.ai/).
- **BoltAI** sells a $99 one-time Mac license with a 30-day guarantee and gets "worth the price" reviews ([r/macapps](https://www.reddit.com/r/macapps/comments/1d3628l/app_review_boltai_worth_the_price_aillm/)). **MindMac** sells $29 one-time ([mindmac.app](https://mindmac.app/)) and r/ChatGPT users buy it ([thread](https://www.reddit.com/r/ChatGPT/comments/1juu3vx/anyone_use_mindmac/)).
- **Takeaway:** the one-time BYOK price ladder runs $6.99 → $9.99 → $29 → $99. Our $4.99 is the bottom of the ladder — the impulse tier. It will convert the *most* price-sensitive users; it will never be a "fat" revenue stream, which matches our R-008 revenue bands (indie success $4k–$21k).

### 3.2 "Remove ads" is a proven, standard IAP — and our $4.99 is in-band
- Monetization guide data: utility apps monetize light users at $0.03–$0.08/DAU from ads, and "light spenders buy a 'remove ads' IAP for **$2.99 to $4.99**" ([Kanopy](https://kanopylabs.com/blog/mobile-app-monetization-strategies-guide)). Our $4.99 one-time remove-ads Pro is at the top of the standard band — correct.
- Non-consumable "remove ads / pro tier" unlocks are a first-class strategy in every 2026 monetization roundup ([CatDoes](https://catdoes.com/blog/mobile-app-monetization-strategies)).

### 3.3 Subscription churn is brutal; hybrid (lifetime + sub) is the trend
- RevenueCat State of Subscription Apps 2025 (75,000 apps, $10B+ tracked): **~30% of annual subscriptions are cancelled in the first month**; cheap annual plans retain 36% of users after a year vs 6.7% for high-priced monthly; **35% of apps now mix subscriptions with lifetime purchases/consumables** ([RevenueCat](https://www.revenuecat.com/state-of-subscription-apps-2025/)).
- AI apps monetize well **if differentiated**: median AI-app revenue per install is $0.63 after 60 days (2× the $0.31 overall median) ([RevenueCat](https://www.revenuecat.com/state-of-subscription-apps-2025/)).
- **Takeaway:** a pure monthly subscription is a churn trap for a $0-server-cost BYOK client; a one-time $4.99 with zero marginal cost and no churn is structurally the right shape. If a subscription is ever added (e.g., future cloud features), it must be **additive** (Chatbox/Chattica/OpenCat pattern), never a replacement for the lifetime option.

### 3.4 Users riot when a "lifetime" paywall excludes what they want
- TypingMind $99 lifetime buyer: *"the 'lifetime' license is just for the shell — if I want memory/sync across devices I have to keep paying… another redditor called it daylight robbery"* ([r/openrouter](https://www.reddit.com/r/openrouter/comments/1nevp2e/paid_99_for_typingmind_lifetime_license_now/)). Even defenders admit *"it's not obvious unless you dig around the FAQ"* (same thread).
- Same pattern: r/TypingMind threads about hidden storage costs ([1](https://www.reddit.com/r/TypingMind/comments/1in489p/has_typingmind_hidden_costs_how_has_you/), [2](https://www.reddit.com/r/TypingMind/comments/1j4v8kr/why_just_why_is_there_no_storage_before_you_buy/)).
- **Takeaway (hard rule):** the Pro screen must list exactly what $4.99 buys and never exclude a feature users already see working in-app. Never move an existing free feature behind Pro post-launch — that is the "daylight robbery" move.

### 3.5 Ads in chat apps are hated — the banner itself is a conversion driver
- CharacterAI's ad rollout (ads *inside* chat) produced megathreads of fury: *"NEVER include ads that pop up while we are actively chatting"* ([r/CharacterAI feedback megathread](https://www.reddit.com/r/CharacterAI/comments/1m3gspj/feedback_megathread_ads/)), and users rage about ads mid-chat ([thread](https://www.reddit.com/r/CharacterAI/comments/1rhyoow/anyone_else_getting_ads_while_youre_chatting_with/)).
- **Takeaway:** our single **banner** (never in the message flow) is the right ad shape for a chat app; and the worse ads feel in the category, the more the $4.99 "no banner, ever" promise converts. This is our strongest Pro anchor and it costs us nothing to keep banner-only.

### 3.6 The FOSS crowd pays nothing — monetize the Play build, not F-Droid
- Every serious FOSS BYOK client (Agora, Kai, Maskan, maid, EveryTalk, OpenMinis, Enchanted, Aiyo) is free; r/LocalLLaMA recommendations are all free tools ([thread](https://www.reddit.com/r/LocalLLaMA/comments/1k8qwzo/best_apps_for_byok_ai/)).
- **Takeaway:** our `foss` flavor (F-Droid) is reputation/table-stakes; the `play` build is where the $4.99 lives (already our C-002 flavor split). Never ask the FOSS crowd for money; never gate reliability in the Play build either — they compare.

---

## 4. What our $4.99 one-time Pro can and cannot reasonably gate

Current gate map (per `docs/BACKLOG.md`): **Pro = ads removed + unlimited templates (C-012) + /browse web fetch (C-013) + backup/restore (C-014) + overlay (C-015) + image attach/vision (C-016) + memory (C-020).** Free = 1 banner + 1 template + core chat + /imagine + /video + voice input + multi-key + failover + forks + model binding + context compression + settings export.

### CAN gate (competitor-validated, zero marginal cost, no trust damage)
| Feature | Competitor precedent | Verdict |
|---|---|---|
| Web search / browse (already Pro) | Chatbox gates web search in paid plans; UnboundChat gates web search | ✅ keep Pro; consider adding DDG-search-then-browse |
| Backup/restore & export (already Pro) | Chattica gates export; UnboundChat gates backups & export | ✅ keep Pro; upgrade to AES-encrypted export + scheduled backups |
| Overlay (already Pro) | OpenCat gates keyboard extension (same "AI anywhere" class); OpenMinis ships overlay free but it's their flagship | ✅ keep Pro |
| Unlimited templates (already Pro) | Count-limits convert (Chattica's 2-character free cap) | ✅ keep; add template import/export & packs |
| Memory (already Pro) | TypingMind's memory is its #1 paid-feature pain point | ✅ keep Pro — but see 3.4: never advertise "all included" then charge more |
| **TTS / read-aloud (new)** | OpenCat Pro-gates TTS; Chattica Pro-gates TTS & voice | ✅ strong new gate, Android TTS is free to us |
| **Usage dashboard / cost history (new)** | UnboundChat gates "usage statistics" | ✅ strong new gate; keep the single per-message cost line free (trust), gate history/aggregates |
| **Custom themes / accent colors / icon pack (new)** | ChatCat accent palettes; Chattica ships themes as surface area | ✅ cheap, visible, everyday ("Colors"), zero APK |
| **BYO-Sync via user's own WebDAV/Drive (new)** | OpenCat gates iCloud sync; UnboundChat gates sync; TypingMind = the cautionary tale | ✅ the single most-wanted gate; zero server cost (user's storage) — see 3.4 for wording discipline |

### CANNOT reasonably gate (trust/reliability/free-ecosystem features)
- **Core chat, multi-key, provider failover** — Agora/Kai give these away; gating reliability reads as ransom and breaks the "honest app" brand.
- **/imagine, /video, voice input** — the user pays the provider for these; gating them protects no revenue, and Chatbox/Maskan/UnboundChat keep them free. (Chattica gates them, but only in a character-app niche.)
- **Context compression, per-conversation model binding, forks** — power features the FOSS tier owns.
- **The compat matrix, per-message cost line, no-account policy** — these *are* the brand (differentiation doc); they must stay free.
- **i18n** — never gate languages (UnboundChat ships 31 languages free).
- **LAN/Ollama/local endpoints** — the "works offline, works on a $30 phone" story; free forever.

---

## 5. Ranked shortlist — 10 Pro-incentive ideas (effort × impact)

Ranking key: **Effort** = dev time + APK cost for our thin client. **Impact** = expected effect on $4.99 conversion (competitor evidence-based). Theme-law flag: **UF** = would appear in user-facing copy/screens → must use everyday words.

| # | Idea | Effort | Impact | Why (evidence) | UF wording note |
|---|---|---|---|---|---|
| 1 | **Unlimited templates + template import/export + curated packs** (extend existing C-012) | Low (0KB) | High | Count-limits convert (Chattica); zero-cost; already Pro, just deepen | "Make your own prompts. Unlimited." |
| 2 | **Web search: /browse + DDG search-then-answer** (extend existing C-013) | Low–Med (0KB, OkHttp + Jsoup already in) | High | Web search is a paid feature in Chatbox & UnboundChat | "Search the web in chat." |
| 3 | **Read-aloud (TTS) with voice choice + stop** | Low (Android TTS; 0KB) | Med–High | OpenCat & Chattica both gate TTS | "Listen to replies." |
| 4 | **Encrypted backup (AES) + scheduled local backups + restore to new phone** (upgrade C-014) | Med (0KB, stdlib crypto) | High | UnboundChat & Chattica gate backups/export; TypingMind proves demand | "Back up your chats, your way." |
| 5 | **BYO-Sync: auto-backup to the user's own WebDAV / Google Drive (SAF)** | Med–High (0KB server cost) | High | OpenCat gates iCloud sync ($9.99 Pro, long-lived); UnboundChat gates sync | "Sync between your devices." — must clearly be "your storage, your key" |
| 6 | **Overlay quick-ask v2: persistent bubble + screenshot→vision OCR ask** (upgrade C-015/C-016) | Med | Med–High | Overlay already Pro; OCR was R-008's C-016 idea; "AI anywhere" is OpenCat's Pro hook | "Ask from any app." |
| 7 | **Usage dashboard: cost history, per-model totals, export of stats** | Med | Med | UnboundChat gates usage stats; deepens "quiet receipt" theme (keep live per-message cost free) | "See what each chat cost." |
| 8 | **Custom look: accent colors + dark/light/OLED + icon pack** | Low–Med (0KB) | Med | ChatCat accent palettes; Chattica themes; visible everyday premium | "Make it yours." / "Colors." |
| 9 | **Advanced prompts: multi-template chains, per-template model binding, variable schemas** | Med | Med | Prompt library is the Pro hook of Chatbox/OpenCat/Pal Chat | "Prompts that do more." |
| 10 | **Fork explorer: branch map, merge, export a branch as a document** (extend C-024) | Med–High | Low–Med | Power-user niche (Agora forks are free); low mass-market pull — keep low | — (probably never user-facing) |

**Not ranked but worth remembering:** home-screen chat widget, "no-banner on launch screen" already included, community prompt packs via GitHub JSON (differentiation doc #6 — keep **free**, it builds community, not revenue).

---

## 6. Do-not list (from competitor failures)

1. **Never move a gate after purchase** and never sell "lifetime" with excluded core wants (TypingMind "daylight robbery" — §3.4).
2. **Never put ads inside the chat flow** — banner only (CharacterAI backlash — §3.5). Our current design is already correct; protect it as an invariant.
3. **Never make the paid tier a pure subscription** without a lifetime option — churn data is brutal (§3.3); hybrid is the 2025+ pattern.
4. **Never gate reliability/trust features** (failover, multi-key, context compression, cost line, compat matrix) — §4 CANNOT list.
5. **Don't copy the name-confusion trap:** the Play category is full of copycat "ChatBox"-style listings (e.g., [this one](https://play.google.com/store/apps/details?id=chatgpt.ai.chatbot.open.chat.gpt.bot.writer.assistant) with angry subscription reviews that are *not* the real Chatbox). Our unique name (BYO AI) is an asset — keep it.
6. **Don't gate i18n, LAN/Ollama, or local endpoints** — §4 CANNOT list.

---

## 7. Key sources (load-bearing URLs)

- Pricing/features: [Chatbox plans](https://chatboxai.app/en/guide/chatbox-ai/plans) · [TypingMind buy](https://www.typingmind.com/buy) · [OpenCat](https://opencat.app/) · [Pal Chat App Store](https://apps.apple.com/us/app/pal-chat-ai-chat-client/id6447545085) · [UnboundChat Play](https://play.google.com/store/apps/details?id=com.marko.unboundchat) · [Chattica](https://chattica.ai/) · [BoltAI pricing](https://boltai.com/pricing) · [MindMac](https://mindmac.app/) · [Agora F-Droid](https://f-droid.org/en/packages/com.newoether.agora/) · [Maskan F-Droid](https://f-droid.org/en/packages/app.maskan.chat/) · [maid](https://mobile-artificial-intelligence.com/maid) · [EveryTalk](https://github.com/roseforljh/EveryTalk) · [OpenMinis](https://openminis.app/) · [Enchanted](https://github.com/gluonfield/enchanted) · [Aiyo F-Droid](https://f-droid.org/en/packages/com.beradeep.aiyo/)
- Conversion/churn data: [RevenueCat State of Subscription Apps 2025](https://www.revenuecat.com/state-of-subscription-apps-2025/) · [Kanopy monetization guide](https://kanopylabs.com/blog/mobile-app-monetization-strategies-guide) · [CatDoes monetization strategies](https://catdoes.com/blog/mobile-app-monetization-strategies)
- User sentiment: [TypingMind $99 backlash](https://www.reddit.com/r/openrouter/comments/1nevp2e/paid_99_for_typingmind_lifetime_license_now/) · [r/TypingMind hidden costs](https://www.reddit.com/r/TypingMind/comments/1in489p/has_typingmind_hidden_costs_how_has_you/) · [r/TypingMind storage](https://www.reddit.com/r/TypingMind/comments/1j4v8kr/why_just_why_is_there_no_storage_before_you_buy/) · [CharacterAI ad megathread](https://www.reddit.com/r/CharacterAI/comments/1m3gspj/feedback_megathread_ads/) · [CharacterAI mid-chat ads](https://www.reddit.com/r/CharacterAI/comments/1rhyoow/anyone_else_getting_ads_while_youre_chatting_with/) · [BoltAI "worth it" review](https://www.reddit.com/r/macapps/comments/1d3628l/app_review_boltai_worth_the_price_aillm/) · [Pal Chat r/iosapps](https://www.reddit.com/r/iosapps/comments/1iuuk44/unlimited_free_openai_access_on_pal_chat_the_best/) · [r/LocalLLaMA BYOK picks](https://www.reddit.com/r/LocalLLaMA/comments/1k8qwzo/best_apps_for_byok_ai/) · [Chattica r/ChatbotRefugees](https://www.reddit.com/r/ChatbotRefugees/comments/1qmobaa/chatticaai_is_a_byok_ios_and_android_customizable/)
- Market map: [BYOKList](https://byoklist.com/) (BYOK tool marketplace incl. $49 one-time players)
- Internal cross-refs: `docs/PREMIUM-STRATEGY.md` (R-008) · `docs/COMPETITIVE-DIFFERENTIATION.md` · `docs/GAP-ANALYSIS-R010.md` · `docs/BACKLOG.md` · `docs/THEME-SHOW-DONT-TELL.md` · skill ref `references/thin-client-patterns-research.md` §12 (monetization models)
