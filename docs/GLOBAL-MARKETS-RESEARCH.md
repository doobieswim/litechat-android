# Global markets research — the $30-phone audience for BYO AI (Research C)

**Date:** 2026-08-15
**Agent:** LITECHAT-DIG (global-markets researcher)
**Status:** Research (not Ready — PROOF must grade; feeds H-009 language scope, H-003 pricing review, Play listing work)
**Task source:** H-009 "Languages are free, always" → name the first markets/languages. Also H-001 note (Research C global markets), H-008 hold.
**Product frame:** BYO AI — thin BYOK AI chat for 4GB phones, free + one banner, $4.99 one-time Pro, FOSS-first launch (GitHub/F-Droid/XDA/4PDA before Play), English-only UI today, no accounts.
**Reference phone:** Moto G 2025 (Straight Talk, Walmart, $29.88, 4GB RAM) [46]. Outside the US promo aisle, the "$30 phone" is a Tecno/Infinix/itel/Redmi-class device — see §0.
**Cost:** $0. All claims carry a URL. Sources block at the bottom, mechanically rendered from the retrieval ledger.

---

## TL;DR

1. **The sub-$100 smartphone is a massive, real market:** IDC counted **170M+ sub-$100 devices shipped in 2025** [1]; Africa's 2025 shipments were **81% below $200** [3]; Brazil's sub-$200 band doubled in 2024 to **41% of shipments** [4]. The 2026 memory-crisis price hikes are squeezing this exact segment [1][2] — which is precisely the audience that needs a 2 MB chat app instead of a 50 MB monster.
2. **Ranked launch priority:** **India → Brazil → Indonesia → Mexico/LATAM (Spanish) → Nigeria → Philippines → Vietnam → Egypt/other Africa (French/Arabic/Swahili)**. Russia/CIS stays a FOSS-channel play (4PDA), not a Play monetization play.
3. **Top languages to ship (UI + listing):** English (already shipped), **Portuguese (Brazil), Spanish (LATAM), Hindi (India), Indonesian, French (francophone Africa), Arabic (Egypt/MENA), Russian (CIS/FOSS)** — then Vietnamese, Bengali, Tagalog, Urdu, Swahili via community translation.
4. **BYOK is viable in every one of these markets — via free no-credit-card API keys, not paid keys.** Google AI Studio (Gemini) gives free keys with no card [21]; Groq free tier needs no card [24][25]; OpenRouter has free models with no payment method [22][23]. A Google account is the single universal on-ramp.
5. **$4.99 flat is too expensive for most of this audience.** Play supports per-country IAP pricing [33][34]; PPP-adjusted overrides (₹199 India, R$14.90 Brazil, Rp49k Indonesia…) are the honest play. US price stays $4.99 (H-003 intact).
6. **Localize the listing before the app.** Play listing localization drives downloads in these markets and costs ~$0 (copy only). App UI localization waits for the one-time string-extraction refactor (H-009 option D).

---

## 0. The "$30 phone" — global reality check

The locked reference is a US promo phone: **Straight Talk Moto G 2025 (5G, 64GB) at $29.88, 4GB RAM** [46]. That is a loss-leader prepaid deal; the same $30-35 price point in the rest of the world buys a different class of phone, but the *engineering envelope is the same*: 4GB (or less) RAM, small eMMC storage, entry SoC, no flagship GPU. Evidence that this tier is the volume engine of the global market:

- **IDC: sub-$100 segment = 170M+ devices in 2025**, and the segment is "economically unviable" as memory costs rise — OEMs are pushing prices up, shrinking the tier [1].
- **2026 outlook: global shipments -13.9% to 1.09B; the sub-$200 segment shrinks most**; MEA -23%, Central/Eastern Europe -19%, Asia-Pacific ex-China-Japan -14% because those regions are most concentrated in sub-$200 devices [1][2].
- **Africa (Omdia): 84.4M smartphones shipped in 2025, +13% YoY; 81% of shipments priced below $200**; smartphones are only ~55% of total handsets shipped — a giant feature-phone→smartphone upgrade wave still running [3]. Transsion (Tecno/Infinix/itel) holds 44% share — these are exactly the $30-100 devices [3].
- **Brazil (Omdia/Canalys): 40M+ units in 2024; sub-$200 (~R$1,110) shipments doubled vs 2023 = 41% of shipments**; 175M smartphone installed base [4].
- **India (IDC): 152M units in 2025 (flat), ASP $282** [11]; the entry-level **sub-$100 segment grew 22.9% YoY in 1H25** (Xiaomi-led) [9] — the only segment still growing fast.
- **Indonesia (Counterpoint):** entry-level **<$150 fell 19% YoY in Q1 2026** as memory-cost price hikes (7-45%) hit the bottom [6]; Xiaomi's Redmi A-series still leads the entry segment — the installed base of cheap Androids is enormous even as new sales slow [6].
- **Vietnam:** sub-$200 = ~50% of shipments in Q1 2025 [8]. **SEA overall:** devices ≤$200 declined 5% in 2025 while value rose [12].
- **Egypt:** US$100-199 = 60% of shipments, +19% YoY in 4Q25 [3]. **Nigeria:** +25% in 4Q25, sub-$200 dominant [3]. **Philippines:** 18M units 2024 (+6.1%), growth "as vendors introduced affordable entry-level smartphones" [10].
- Omdia's 2024 data already showed the trend: sub-$150 shipments grew 33% YoY (90M → 120M units in 1Q24) [5].

**Reading:** the $30-phone audience is the emerging-market entry tier. It is huge (hundreds of millions of devices/year), mostly Android, and currently squeezed by inflation in exactly the way that makes a 2 MB, 4GB-friendly, no-subscription chat app relevant. The app's "built for a $30 phone" promise is not a US-only story — it is the story of the volume market everywhere.

---

## 1. Ranked market table

Priority score (0-100) = weighted: sub-$100 volume evidence (30) + Play availability & payment (20) + AI-chat demand evidence (20) + BYOK viability via free-tier keys (15) + language coverage/cost to reach (10) + FOSS-channel fit (5).

| Rank | Market | Sub-$100 phone evidence | Android / Play | Language | BYOK viability | Score | Notes |
|---|---|---|---|---|---|---|---|
| 1 | **India** | 152M units 2025 [11]; sub-$100 +22.9% 1H25 [9]; entry band still the volume tier [7] | 92.4% Android mobile (StatCounter) [13]; 95.7% per aggregated active-device data [15]; Play + UPI [30] | English + Hindi + regional | **High** — free Gemini key [21], Groq/OpenRouter free [24][22]; UPI for Pro [30] | **95** | ChatGPT = #2 most-downloaded app 2025 (>100M downloads, India leads installs at 13.7%) [38][39]; #2 ChatGPT web-traffic country [36] |
| 2 | **Brazil** | 40M+ units 2024; sub-$200 = 41% of shipments (doubled YoY) [4] | 84.1% Android [15]; Play + Pix [30] | Portuguese | **High** — free Gemini/Groq/OpenRouter; Pix [32] | **90** | ChatGPT #3 web-traffic country; Gemini #4 [36]; 148M WhatsApp users (WhatsApp has 3B+ MAU globally) [43][44] |
| 3 | **Indonesia** | Entry tier is the volume band (Xiaomi leads it with Redmi A-series); <$150 fell 19% YoY in Q1 2026 under price hikes [6] | 89.6% Android [15] (77.2% per StatCounter web mix [14]); Play + e-wallets | Indonesian | **High** — free keys work; e-wallet/Play billing | **84** | ChatGPT #4 most-downloaded app 2025 (47M) [38]; Gemini top-5 country [36]; ChatGPT top-5 market [47]; 112M WhatsApp users [43] |
| 4 | **Mexico + LATAM (Spanish)** | LATAM entry tier large; Mexico 76.4% Android [15] | Play normal; cards + OXXO-type rails | Spanish | **High-Med** | **78** | Spanish = #3 internet language (364M) [17]; Android >85% in Brazil/India/Indonesia/Vietnam [16]; "chat IA"/"chat gpt" demand; LATAM 86.5% Android [15] |
| 5 | **Nigeria** | Africa 84.4M 2025 (+13%); sub-$200 dominant; Transsion 44% [3] | 91.3% Android [15]; Play cards/gift cards [31] | English (+ Hausa/Yoruba later) | **Med-High** — free keys yes; paid keys/cards scarce | **74** | English works today; 40M WhatsApp users [43]; fast-growing AI demand (low-income adoption 4x [40]) |
| 6 | **Philippines** | 18M units 2024, entry-led growth [10] | ~90% Android class [15]; Play + GCash | English + Tagalog | **High** — English UI works; strong AI demand | **73** | English-speaking mass market; strong SEA ChatGPT demand [38] |
| 7 | **Vietnam** | sub-$200 ≈ 50% of shipments [8] | 84% Android [15] | Vietnamese | **Med-High** — cards common, free keys work | **70** | Vietnamese = 1.0% of web content [17]; SEA ChatGPT download wave includes Vietnam [38] |
| 8 | **Egypt + MENA (Arabic)** | $100-199 = 60% of shipments, +19% [3] | 90.4% Android [15] | Arabic | **Med** — free keys yes; paid keys hard | **67** | Arabic = 237M internet users (#4) [17]; Egypt 22% growth 4Q25 [3] |
| 9 | **Pakistan** | 30M+ handsets locally manufactured 2025, shipments +13% [LinkedIn/IDC-analyst, unverified]; PPP lowest of list [35] | 93.8% Android [15] | Urdu + English | **Low-Med** — free keys yes; monetization minimal | **60** | Low PPP ($6.6k) [35]; Play billing thin; FOSS/dev community exists |
| 10 | **Bangladesh** | Cheap-phone mass market; 96.1% Android [15] | 96.1% Android [15] | Bengali | **Low-Med** | **58** | Bengali 272M speakers [18]; monetization minimal |
| 11 | **Kenya + East Africa (Swahili)** | Kenya +3% 4Q25; sub-$100 class significant (SA sub-$100 = 22%) [3] | ~90% Android class [15] | Swahili + English | **Low-Med** — free keys yes; M-Pesa not on Play | **55** | Swahili ~90M+ speakers; strong feature→smartphone transition [3] |
| 12 | **Russia/CIS** | CEE entry tier large; -19% 2026 forecast [2] | ~86% Android class [15] | Russian | **Special** — Play billing **unavailable to RU users**; FOSS channels strong | **50 (channel score)** | 4PDA = 11M unique visitors/mo [28]; Russian 116M internet users [17]; monetize via FOSS goodwill + future RU stores, not Play Pro |

**Why China is absent:** Google Play has been banned in China since 2011; Android there is 300+ fragmented stores (Huawei AppGallery, Tencent, Xiaomi…) [42]. Chinese users are the world's #2 internet-language block [17] but not reachable through Play — BYO AI's chosen distribution. Out of scope for v1; revisit only via F-Droid/APKPure sideload channels if the community asks.

**Why the US/UK remain relevant:** the $29.88 Walmart phone is a US SKU and the FOSS channels (XDA, r/androidafterlife) are English-first [29]; the US market is where early BYOK users and review sites live. Keep US/CA/UK/AU as default high-price markets, but volume is emerging markets.

---

## 2. Top 8 languages, ranked

Rule from H-009: **languages are free, always** — nothing here gates a language behind Pro. This ranking is about *order of work* (one `values-XX/strings.xml` per language after the string-extraction refactor; community translation via F-Droid/XDA/4PDA costs ~$0 [27][28][29]).

| Rank | Language | Speakers / internet reach | Why for $30-phone audience | First markets |
|---|---|---|---|---|
| 1 | **English** (shipped) | 1.5B total [18]; 1.19B internet users (#1) [17] | Default; covers India urban, Nigeria, Philippines, Pakistan elite, Africa business class; all FOSS channels | Global |
| 2 | **Spanish** | 560M total [18]; 364M internet users (#3) [17] | Entire LATAM + US Hispanic; LATAM is 86.5% Android [15]; "chat IA" demand | Mexico, Colombia, Argentina, Peru |
| 3 | **Portuguese** | 264-279M total [18]; 172M internet users [17] | Brazil = #2 priority market, 41% of shipments sub-$200 [4] | Brazil |
| 4 | **Hindi** | 609M total [18] | India mass market: 98% of Indian internet users consume Indic-language content; 57% of urban users prefer regional content (IAMAI-KANTAR 2024) [20]; ~90% of Indians don't speak English [19] | India (+ Tamil/Telugu/Bengali/Marathi next) |
| 5 | **Indonesian** | 200M total [18]; 198M internet users (#5) [17] | Indonesia = #3 priority market; ChatGPT 47M downloads there in 2025 [38] | Indonesia |
| 6 | **French** | ~300M total [18]; 145M internet users [17] | Lingua franca of francophone Africa (Senegal, Côte d'Ivoire, Cameroon, DRC) — the other half of Africa's 84M-unit market [3]; also FR/CA | Francophone Africa |
| 7 | **Arabic** | 237M internet users (#4) [17] | Egypt's $100-199 tier is 60% of shipments [3]; MENA Android 75.9-90.4% [15] | Egypt, Algeria, Morocco, Iraq |
| 8 | **Russian** | 116M internet users [17]; 260M speakers [18] | 4PDA is the #1 RU Android community (11M unique/mo) [28]; FOSS translators; Play billing blocked for RU users → goodwill + channel play | Russia/CIS via 4PDA/F-Droid |

**Next wave (community-driven, option D):** Vietnamese (86M+ [18]; 1.0% web [17]), Bengali (272M [18]), Tagalog (~90M; Philippines English-first anyway), Urdu (Pakistan), Swahili (East Africa), plus Hindi's regional siblings (Tamil, Telugu, Bengali, Marathi, Gujarati, Kannada, Malayalam, Punjabi — Google itself is shipping Gemini Live to these [20]).

**Rationale note:** Chinese drops out despite being #2 internet language because of the Play ban (§1). German/Japanese/Korean drop out because their users buy phones far above $100 and are already served by every AI app.

---

## 3. What users search for on Play (local-language terms)

Evidence base: ChatGPT was the **most-downloaded non-gaming app globally in 2025 (1.08B downloads; 770M on Play)**, Gemini #2 at 354M, DeepSeek 118M, Cici (ByteDance, SEA/LATAM-focused) 150M [38][39]; ChatGPT held ~78% of measured AI-chatbot traffic share in mid-2026 [37]; ChatGPT was **#2 most-downloaded app in India 2025** (>100M) and **#4 in Indonesia (47M)** [38]. So in every one of these markets, users are already searching for AI chat apps in volume — mostly by typing the brand words below.

| Market | Primary search terms (Play) | Secondary / local | Evidence |
|---|---|---|---|
| India | `chat gpt`, `chatgpt`, `ai chat`, `chatbot` | `एआई चैट`, `चैट जीपीटी` (Hindi) | ChatGPT #2 most-downloaded app India 2025 (>100M downloads) [38]; OpenAI is giving Indians a year of free ChatGPT "Go" — mass-market AI push into India [41] |
| Brazil | `chat gpt`, `chat ia`, `inteligência artificial`, `chatbot` | `ia chat` | ChatGPT #3 ChatGPT web-traffic country, Gemini #4 [36]; "chat gpt" is mainstream Brazilian framing (CNN Brasil reported ChatGPT the most-downloaded app in Q1 2025) [unverified] |
| Indonesia | `ai chat`, `chatbot ai`, `chat gpt`, `chatbot gratis` | — | ChatGPT #4 downloads 2025 (47M) [38]; Gemini top-5 country [36] |
| Mexico/LATAM | `chat ia`, `chat gpt`, `inteligencia artificial`, `chatbot` | — | Spanish #3 internet language [17]; "chat IA" is the standard Spanish term |
| Nigeria | `chat gpt`, `ai chat`, `chatbot` (English) | — | English official language; AI demand rising (low-income adoption 4x [40]) |
| Philippines | `chat gpt`, `ai chat`, `chatbot` (English/Taglish) | — | English-speaking; ChatGPT demand in SEA [38] |
| Vietnam | `ai chat`, `chatbot`, `chat gpt` | `trò chuyện AI` | Vietnamese web 1.0% [17]; SEA ChatGPT wave [38] |
| Egypt/MENA | `شات جي بي تي`, `شات`, `ذكاء اصطناعي` | `chat gpt` (Latin) | Arabic 237M internet users [17]; Egypt entry tier 60% $100-199 [3] |
| Russia | `чат gpt`, `нейросеть`, `чат бот`, `chat gpt` | — | Russian 116M internet users [17]; 4PDA [28] |
| France/West Africa | `chat gpt`, `ia`, `chatbot` | `chatgpt` | French 145M internet users [17] |

**ASO implication:** Google Play indexes the listing's localized title/short description per locale, so shipping `pt-BR`, `es-419`, `hi`, `id`, `fr`, `ar`, `ru` listing translations lets BYO AI rank on `chat ia` (BR/MX), `ai chat` (ID), `एआई चैट` (IN) etc. The generic "chat" / "AI chat" keyword space is contested by giants; the differentiating long-tail is **"your own key / sem assinatura / tanpa langganan / बिना सदस्यता"** — no-subscription phrasing, which is the product's actual promise. This is cheap: listing copy only. (Theme law: everyday words, no fight language, applies in every language.)

---

## 4. BYOK viability — free-tier, no-credit-card API paths

**Key finding: BYOK works in every target market via free keys, because the biggest providers don't require a card for free tiers.**

| Provider | Free tier | Card needed? | Rate limits | Works where | Evidence |
|---|---|---|---|---|---|
| **Google AI Studio (Gemini API)** | Free tier with API key | **No** | Rate-limited free tier | Everywhere with a Google account — universal on-ramp for India/Brazil/Indonesia/Nigeria/Egypt | [21] |
| **Groq** | Free developer tier | **No** | ~30 req/min, ~14.4k req/day | Global | [24][25] |
| **OpenRouter** | 25+ `:free` models | **No** (free models need no credits) | ~1,000 free req/day | Global | [22][23] |
| **Mistral** | "Experiment" plan | **No** | Rate-limited | Global | [26] |
| **GitHub Models** | Free via GitHub account | **No** | Rate-limited | Global (devs) | [26] |
| **Cerebras / NVIDIA NIM / SiliconFlow** | Free tiers | **No** | Rate-limited | Global / CN-focused | [26] |

Paid keys remain hard in most target markets:
- **OpenAI/Anthropic API**: international credit/debit card required — scarce or blocked for many users in Nigeria, Kenya, Egypt, Pakistan; workable in India (international-enabled cards common), Brazil, Indonesia, Philippines.
- **DeepSeek**: prepaid top-up (¥2+), paid via **Alipay/WeChat**; international cards frequently fail — impractical for most of these markets [45].
- **OpenRouter paid**: credit card or crypto [22].

**Consequence for BYO AI:** the onboarding flow should *lead with the free-key path*, not OpenAI's paid key: preset buttons for **Google AI Studio (Gemini), Groq, OpenRouter free** — each with a 1-tap "get a free key here" link and no-account-required messaging. Users who already pay OpenAI/Groq/OpenRouter (the "maybe already pays" persona from the theme doc) can paste their paid key too. This makes BYOK genuinely viable on a $30 phone in Jakarta or Lagos, where a $20/mo ChatGPT plan is 40-60% of a weekly wage but a Google account is free. The app already has presets for OpenAI/OpenRouter/Groq/Ollama — the change is *default order + free-key guidance*.

**Pro purchase viability (Play billing):** Play accepts UPI in India and Pix in Brazil (plus cards/gift cards/carrier billing broadly; Kenya lists Visa/Mastercard/JCB) [30][31]; Pix alone has 174M users (~82% of Brazil) [32]. So the $4.99 Pro *can be bought* in target markets — the question is whether it *should cost* $4.99 (see §7).

---

## 5. FOSS-channel reach (F-Droid / 4PDA / XDA)

| Channel | Reach | Relevance to target markets |
|---|---|---|
| **F-Droid** | 4,061 apps on main repo; **18M+ cumulative app downloads/updates in 2025** [27] | Small but global and free; the FOSS-first launch channel (H-005). Metrics dashboard exists for per-app downloads. |
| **4PDA** | **11M+ unique visitors/month, 156M+ page views/month** [28] | The #1 Russian/CIS Android community — the largest single FOSS-adjacent distribution forum in a non-English language. RU translation + thread = Russian-speaking users without Play dependency. |
| **XDA** | ~13.7M visits/month (2026) [29] | English-speaking enthusiast/dev base — the "4GB nerds" who notice the bones; launch thread + reviews feed back into Play ASO. |
| **r/androidafterlife** | Subreddit (no hard public stat) | Small but perfectly-targeted: people running modern software on old phones — the exact $30-phone persona. |

**Reading:** FOSS channels are reach, not volume — they seed credibility and translations. Volume comes from Play (that's where 18M-unit markets like the Philippines *look for apps*). 4PDA gives Russian for free; F-Droid's contributor culture gives the long tail (VN, ID, AR, FR) for free, matching H-009 option D exactly.

---

## 6. Listing first or app first?

**Localize the Play listing first, the app UI second.** Reasons:

1. **Cost/risk asymmetry:** a Play listing can carry per-locale translations (title, short description, long description, screenshots text) in Play Console with zero engineering — no string extraction, no code, no APK. The app has ~100 user-facing strings hardcoded in Kotlin; UI localization needs a refactor ticket first (H-009 facts) [H-009].
2. **Downloads follow language:** in every target market users search in their own language (§3); a localized listing is what makes the app *findable* and *legible* before install. The English-only listing in Brazil competes against Portuguese listings.
3. **English is fine for the FOSS-first launch** (GitHub/F-Droid/XDA/4PDA/r/androidafterlife are English/Russian technical communities) — but the moment Play goes live, ship listing translations for **pt-BR, es-419, hi, id** at minimum (top-3 markets + LATAM), then fr, ar, ru.
4. **Sequence:** v1.0 (now) = English everywhere, FOSS channels. v1.1 = listing localization (pt-BR, es-419, hi, id) + string-extraction refactor ticket. v1.2 = app UI in PT-BR + ES-419 + HI + ID; community translations (RU via 4PDA, FR/AR/VN/BN/TL/UR/SW) land as they arrive. Language output control (model replies in chosen language) ships free and early — it's a system-prompt string (H-009 option A, included anyway).

---

## 7. Pricing recommendation — flat $4.99 vs per-country

**Recommendation: keep US $4.99 (H-003 intact), and add per-country price overrides at ~30-60% of US price, set once in Play Console.** Play supports arbitrary local-currency IAP prices per country, with auto-conversion from the base price and manual overrides [33][34]. Auto-conversion alone is not enough — for a $19.99 app, auto-conversion lands ~₹1,900 in India vs a PPP-appropriate ~₹650-700 [34]; the same logic applies to $4.99 (~₹450-470 auto vs ~₹199-299 appropriate).

PPP context (World Bank, GDP per capita PPP 2025 vs US $90,027) [35]:

| Market | GDP/capita PPP (US$) | Ratio vs US | Pure-PPP equivalent of $4.99 | Recommended local price (30-60% of US) |
|---|---|---|---|---|
| US | 90,027 | 1.00 | $4.99 | **$4.99** (unchanged) |
| India | 11,748 | 0.13 | $0.65 | **₹199** (~$2.33) |
| Pakistan | 6,573 | 0.07 | $0.36 | PKR 450 (~$1.60) |
| Kenya | 7,016 | 0.08 | $0.39 | KES 250 (~$1.95) |
| Nigeria | 9,532 | 0.11 | $0.53 | ₦3,500 (~$2.20) |
| Bangladesh | 10,154 | 0.11 | $0.56 | ৳199 (~$1.65) |
| Philippines | 12,577 | 0.14 | $0.70 | ₱199 (~$3.50) |
| Indonesia | 17,660 | 0.20 | $0.98 | Rp49.000 (~$3.10) |
| Vietnam | 18,088 | 0.20 | $1.00 | ₫49.000 (~$2.00) |
| Egypt | 20,204 | 0.22 | $1.12 | EGP 99 (~$2.00) |
| Brazil | 23,433 | 0.26 | $1.30 | R$14.90 (~$2.70) |
| Mexico | 25,868 | 0.29 | $1.43 | MX$69 (~$3.70) |
| Russia | — | — | — | **not sellable on Play** (RU billing blocked) — skip |

**Why not go lower (pure PPP)?** a one-time purchase at $0.50-1.00 reads as "free-ish" and undercuts the "pay once" signal; the practical indie floor in these markets is ~$1.5-2.5, and banner ads (not Pro) remain the volume monetization. Also: users who cannot or will not pay still get the full app with one banner — Pro is a gratitude purchase, and the theme is "one small ask."

**Pricing in-app reality check:** Pro revenue per 1,000 users will be ~10-30x lower per user in India vs US even after overrides; volume must come from India/Brazil/Indonesia installs, revenue from US/EU/LATAM. That is fine for a $0-CAC FOSS launch; do not distort the roadmap to chase emerging-market ARPU.

**Suggested new H-question:** H-010 — approve per-country Pro overrides (US stays $4.99; ~10-12 country overrides as above). No SKU change (`BYO_pro` intact), no code change (Play Console only), theme-compliant ("one small ask" stays small everywhere).

---

## 8. Launch sequence

| Phase | Markets/languages | Actions |
|---|---|---|
| **v1.0 (now, FOSS-first)** | English; US/EU + global English reach | GitHub release, F-Droid submission [27], XDA thread [29], 4PDA thread [28], r/androidafterlife post; onboarding preset order → Gemini free / Groq free / OpenRouter free first (no-card guidance) |
| **v1.1 (Play live)** | Play global; listing translations **pt-BR, es-419, hi, id** (+ fr, ar, ru if cheap) | Pay $25, submit, create `BYO_pro` ($4.99 US) + per-country overrides (§7, pending H-010); add the 3 no-card free-key presets to onboarding if not in v1.0 |
| **v1.2 (app i18n wave 1)** | UI: **Portuguese (BR), Spanish (ES-419), Hindi, Indonesian**; language-output control (free, always) | String-extraction refactor ticket first (H-009 option D); ship 4 `values-XX` files; community translators via F-Droid/4PDA/XDA threads |
| **Later (community-driven)** | Vietnamese, French, Arabic, Russian (4PDA), Bengali, Tagalog, Urdu, Swahili; then India regional languages (Tamil/Telugu/Bengali/Marathi…) | Accept community PRs; Play listing locales added as UI locales ship |

**Ordering logic:** India/Brazil/Indonesia first because they have (a) the volume (§1), (b) the demand evidence (§3), (c) working Play billing (UPI/Pix) [30], and (d) free-key BYOK paths (§4). Nigeria/Philippines/Vietnam next because English covers them cheaply. Egypt/MENA and francophone Africa after, because Arabic/French need translation work but the device base is real [3]. Russia stays channel-only (4PDA/F-Droid) due to Play billing limits.

---

## 9. Risks / open items

1. **2026 entry-tier contraction** — sub-$100 shipments are shrinking under memory-cost inflation [1][2]; the *audience* (installed base of cheap phones) is stable and huge, but new-device growth is negative. Positioning ("built for a $30 phone") stays honest; volume expectations should not assume 2025 growth continues.
2. **Play billing availability** — RU users cannot buy on Play; some markets rely on cards (Kenya, Nigeria) where card penetration is low [31]. Mitigation: free-tier BYOK + banner ads carry the product there.
3. **Free-tier API fragility** — Gemini/Groq/OpenRouter free tiers are rate-limited and can change; the app's existing stream→non-stream fallback and multi-provider presets already soften this. Do not promise "unlimited free" anywhere in copy.
4. **India's regional languages** — Hindi alone does not cover India (98% of users consume Indic-language content, not Hindi-only) [20]; ship Hindi first, then the big four (Tamil, Telugu, Bengali, Marathi) via community. Google itself is expanding Gemini Live across these [20] — demand is proven.
5. **Chinese market** — excluded by Play ban [42]; if a community channel (APKPure/F-Droid mirrors) ever emerges, Chinese localization would unlock the #2 internet-language block [17], but it is out of scope for v1.

---

## Sources

[1] https://www.idc.com/promo/smartphone-market-share
[2] https://www.idc.com/resource-center/blog/worldwide-smartphone-market-to-decline-13-9-in-2026-as-memory-crisis-and-us-iran-war-constrain-growth
[3] https://omdia.tech.informa.com/pr/2026/feb/african-smartphone-market-jumps-14percent-in-4a25-as-entry-tier-pressures-signal-2026-reset
[4] https://omdia.tech.informa.com/blogs/2025/april/brazil-smartphone-market-in-2025
[5] https://omdia.tech.informa.com/pr/2024/jun/omdia-reports-booming-demand-for-low-end-smartphones-priced-under-150-dollars
[6] https://counterpointresearch.com/en/insights/indonesia-smartphone-market-q1-2026
[7] https://counterpointresearch.com/en/insights/post-insight-research-notes-blogs-india-smartphone-shipments-decline-7-yoy-in-q1-2025-premiumization-drives-highestever-q1-average-selling-price
[8] https://counterpointresearch.com/en/insights/vietnam-smartphone-market-q1-2025
[9] https://india.entrepreneur.com/news-and-trends/smartphone-shipments-to-decline-in-2025-idc/495736
[10] https://my.idc.com/getdoc.jsp?containerId=prAP53189425
[11] https://www.biztechreports.com/news-archive/2026/3/10/india-smartphone-market-flat-at-152-million-units-in-2025-with-2026-volumes-expected-to-fall-idc-march-11-2026
[12] https://telconews.asia/story/southeast-asia-smartphone-value-rises-as-shipments-stall
[13] https://gs.statcounter.com/os-market-share/mobile/india
[14] https://gs.statcounter.com/os-market-share/mobile/indonesia
[15] https://www.digitalapplied.com/blog/mobile-os-market-share-2026-ios-vs-android
[16] https://www.demandsage.com/android-statistics
[17] https://en.wikipedia.org/wiki/Languages_used_on_the_Internet
[18] https://www.babbel.com/en/magazine/the-10-most-spoken-languages-in-the-world
[19] https://www.isocfoundation.org/2023/05/what-are-the-most-used-languages-on-the-internet
[20] https://www.markhub24.com/post/voice-search-in-india-winning-the-next-wave-of-regional-queries
[21] https://ai.google.dev/gemini-api/docs/pricing
[22] https://openrouter.ai/docs/faq
[23] https://openrouter.ai/openrouter/free
[24] https://pricepertoken.com/endpoints/groq/free
[25] https://www.grizzlypeaksoftware.com/articles/p/groq-api-free-tier-limits-in-2026-what-you-actually-get-uwysd6mb
[26] https://github.com/mnfst/awesome-free-llm-apis
[27] https://f-droid.org/en/2026/01/23/fdroid-in-2025-strengthening-our-foundations-in-a-changing-mobile-landscape.html
[28] https://4pda.to/advertisement-eng
[29] https://www.semrush.com/website/xda-developers.com/overview
[30] https://support.google.com/googleplay/answer/1626831?hl=en&co=GENIE.CountryCode%3DBR
[31] https://support.google.com/googleplay/answer/2651410?hl=en&co=GENIE.CountryCode%3DKE
[32] https://www.fxcintel.com/research/analysis/upi-pix-2025-growth
[33] https://codelabs.developers.google.com/codelabs/play-billing-unlock-new-markets-regional-pricing
[34] https://pricepush.app/blog/google-play-iap-pricing-by-country
[35] https://data.worldbank.org/indicator/NY.GDP.PCAP.PP.CD
[36] https://momenticmarketing.com/blog/top-ai-chatbots
[37] https://gs.statcounter.com/ai-chatbot-market-share
[38] https://www.businessofapps.com/data/most-popular-apps
[39] https://www.businessinsider.com/chatgpt-openai-app-downloads-users-ai-llm-grok-gemini-deepseek-2025-10
[40] https://openai.com/index/how-people-are-using-chatgpt
[41] https://www.bbc.com/news/articles/c14pr0enjr6o
[42] https://www.forbes.com/sites/eladnatanson/2019/09/03/the-other-android-app-stores-a-new-frontier-for-app-discovery
[43] https://www.bankmycell.com/blog/number-of-whatsapp-users
[44] https://backlinko.com/whatsapp-users
[45] https://api-docs.deepseek.com/faq
[46] https://www.walmart.com/ip/Straight-Talk-Motorola-Moto-g-2025-5G-64GB-Blue-Prepaid-Smartphone-Locked-to-Straight-Talk/14552506783
[47] https://explodingtopics.com/blog/chatgpt-users
