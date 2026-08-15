# PRO-LATENT-WANTS — what users secretly want, mapped to Pro

**Date:** 2026-08-15
**Agent:** LITECHAT-DIG (research subagent)
**Status:** Research only. **No Ready ticket.** PROOF must approve before any ticket.
**User-facing law:** `docs/THEME-SHOW-DONT-TELL.md` (**wins** — this file is BACKSTAGE; no fight words, no "power user," no pressure lines on Play/UI)
**Sister files:** `docs/PREMIUM-STRATEGY.md` (R-008) · `docs/SALES-POSITIONING-HISTORIC.md` (R-011) · `docs/GREY-SALES-GOOD-TWINS.md` (R-014) · `docs/BACKLOG.md`
**Cost:** $0. Free web pages only. No paid tools, no paid books.
**Method:** web/grep only. Every load-bearing fact below has a URL. Facts I could not verify are marked `[unverified]`.

---

## Say it like I'm five

Nobody ever paid $4.99 for "a banner removed." They pay because a hidden want got touched. This file collects the hidden wants — the ones users have no word for — proves each one made people pay real money before (WinAmp, mIRC, Doom, Palm, KeePass), then maps each to a concrete BYO AI Pro feature.

One sentence: **the free app proves the app is good; Pro sells the feeling that was already in the room.**

---

## Part 1 — The 14 latent wants

Format per want: **the want (in a user's real voice)** → evidence (era + URL) → money signal → Pro feature map (existing C-0xx or proposed `P-0xx`) → psychology lever.

---

### 1. "I never want to pay again after today" — pay-once, own it forever

**Evidence**
- **Winamp (1998):** Nullsoft switched Winamp from freeware to **$10 shareware** — with *no extra features for paying*. It still brought in **$100,000/month from $10 paper checks in the mail** — i.e. 10,000 people/month paid purely out of goodwill. `https://en.wikipedia.org/wiki/Winamp`
- **mIRC (1995–):** a *chat client* — closest living ancestor of a BYOK chat app — is shareware: "requires payment for registration after the 30-day evaluation period." Millions of IRC users paid $20 once. `https://en.wikipedia.org/wiki/mIRC`
- **Subscription fatigue (2023–2026):** Americans spend ~**$219/month** on subscriptions but *estimate* $86 (C+R Research, a 2.5× perception gap); 89% underestimate their spend. `https://resubs.app/resources/subscription-spending-statistics` · `https://www.readless.app/blog/subscription-fatigue-statistics-2026`
- **Enshittification:** Cory Doctorow's word for platforms that degrade users to extract rent (coined 2022, American Dialect Society Word of the Year 2023). "Pay once" is the structural opposite of enshittification. `https://en.wikipedia.org/wiki/Enshittification`
- **FTC Click-to-Cancel (2024):** regulators now treat subscription traps as a consumer-harm category. The whole regulatory wind is at our back. `https://www.ftc.gov/business-guidance/blog/2024/10/click-cancel-ftcs-amended-negative-option-rule-what-it-means-your-business`

**Money signal:** Winamp: $100k/month for *zero* paid features. The price of "never again" is not a price — it's relief.

**Pro map:** The $4.99 once **is** the feature. Add `P-001 — Lifetime registration screen`: after purchase, a calm "Registered — BYO AI · [date] · no renewal, ever." A receipt that is also an identity object (see #6). Anchoring: $4.99 once next to the $20/month anchor already in the user's head.

**Psychology:** pay-once = ownership identity ("I bought a tool") vs subscription = rent (chronic small loss). Relief of the recurring bill is a gain framed as the removal of a loss.

---

### 2. "Let me taste the whole game, then finish it" — the shareware episode

**Evidence**
- **Apogee (1987–):** "popularized a distribution model where each game consists of three episodes, with the first given away free as shareware and the other two available for purchase." `https://en.wikipedia.org/wiki/Apogee_Software`
- **Wolfenstein 3D via Apogee (1992):** "the first 10 levels … were free. It was the additional levels that were sold… This allowed people to copy the first part of the game… and see how well the game performed on their machines before they bought the whole game. The free teaser file spread like a virus." By end of 1993 id had sold **100,000+** units through shareware — destroying the old record by 5×. `https://www.gamedeveloper.com/business/monsters-from-the-id-the-making-of-i-doom-i-`
- **Doom (1993):** the shareware episode *was* the marketing; "registered" was a status word. `https://en.wikipedia.org/wiki/Shareware`

**Money signal:** the free episode was *the best marketing ever invented for software* — a real taste, on your own machine, no account, no clock. People paid for the rest of the game.

**Pro map:** Our free tier is episode 1: core chat + 1 template + one banner. Pro = the rest: unlimited templates (C-012), /browse (C-013), overlay (C-015), vision (C-016), memory (C-020), backup (C-014). **The free episode must stay excellent** — a rotten episode 1 killed shareware's reputation, and a nagging free tier kills ours (see SoftRAM, #7).

**Psychology:** completeness drive ("I've had 3 of the episodes — I want the 4th"); feature-flagging as "more game," not "less app."

---

### 3. "My stuff is MINE — give me the file" — the file you own

**Evidence**
- **Palm HotSync (1996–):** users synced their data to *their* PC — the vendor never held it. The whole Palm paid-app culture (Handango, founded 1999, sold PDA/smartphone apps; users routinely paid $10–30 for small utilities) ran on "the app is cheap, my data is priceless." `https://en.wikipedia.org/wiki/Handango` · `https://en.wikipedia.org/wiki/HotSync`
- **KeePass:** trust is *the file* — an encrypted .kdbx the user holds. People switched password managers over who owns the file. `https://en.wikipedia.org/wiki/KeePass`
- **Obsidian:** notes stored as local plain markdown files; the "local-first" movement's manifesto is literally titled **"You own your data, in spite of the cloud."** `https://en.wikipedia.org/wiki/Obsidian_(software)` · `https://www.inkandswitch.com/local-first/`

**Money signal:** users pay *more* for tools that give them the file (Obsidian's paid sync rode on local ownership; KeePass won against free competitors on the file).

**Pro map:** C-014 encrypted backup (done). Add **`P-002 — Export conversations as .md / .txt`** (Pro) and keep a **free plain export** (see Part 4 — never fully gate portability). The exported file is a *thing* they can see, hold, re-import. "The file you own" is the single most tangible Pro value on a $4.99 ticket.

**Psychology:** endowment effect (their chats feel like possessions → losing them feels like theft); tangibility — an abstract "cloud" is not owned, a file is.

---

### 4. "Don't let my work disappear" — sunk cost protection

**Evidence**
- **Sunk cost (Arkes & Blumer, 1985):** people keep investing to protect prior investment. `https://en.wikipedia.org/wiki/Sunk_cost`
- **Local-first essay:** "The more time you invest in using one of these apps, the more valuable the data in it becomes to us" — and the fear of lock-in is the pain. `https://www.inkandswitch.com/local-first/`
- **Palm pattern:** long-lived Memo/Datebook data made users buy *conduit* software to move it to a new device — they paid to move the data, not for the app.

**Money signal:** the value people protect is their own history; tools that protect it get paid, tools that threaten it get abandoned (every cloud-shutdown graveyard).

**Pro map:** everything that protects/extends their build: C-014 backup, C-020 memory, unlimited templates (C-012), and proposed **`P-003 — Full-text search across chats`** (Room FTS, ~0 KB) so a long history is *findable*, hence worth keeping. The deeper their history, the higher their switching cost — make that cost feel like safety, not a cage.

**Psychology:** sunk cost + loss aversion (losses hurt ~2× gains, Kahneman & Tversky 1979 `https://en.wikipedia.org/wiki/Prospect_theory`). The Pro pitch is "your work is safe here," not "pay or lose it" — never a ransom.

---

### 5. "Make this old phone feel new again" — weak-phone dignity

**Evidence**
- **Opera Mini (2005–):** a thin client that server-rendered pages so *feature phones* could browse; it became one of the most-used mobile apps in the world, preinstalled on cheap phones. The thin client *was* the product for the weak-phone world. `https://en.wikipedia.org/wiki/Opera_Mini`
- **r/androidafterlife:** "a community dedicated to repurposing forgotten Android, iPhone, and Windows phones" — tens of thousands of people who *love* squeezing life out of old devices. `https://www.reddit.com/r/androidafterlife/`
- **The dumbphone revival is real and large:** ~**210 million feature phones sold globally last year (~15% of all handsets)** (Counterpoint Research). `https://counterpointresearch.com/en/insights/Dumbphones%20are%20getting%20smarter%20and%20more%20premium.%20Are%20they%20the%20solution%20for%20disillusioned%20phone%20addicts`
- **J2ME paid-app culture:** feature phones ran tiny paid Java ME apps; the market normalized "small app, paid once, no bloat." `https://en.wikipedia.org/wiki/Java_ME`

**Money signal:** an entire industry (Opera Mini, feature-phone OEM bundles, J2ME stores) monetized "make the cheap phone capable."

**Pro map:** on a 4GB phone, **removing the ad banner is a performance upgrade** — less RAM, less battery, no ad-network wakeups. Sell it that way (calmly, truly): "Free has one banner; Pro removes it *and* the phone breathes." Add **`P-004 — Comfort mode`** (Pro): disable animations/motion, cap image decode — the RAM-matrix honesty made into a toggle. All existing RAM-tuned features (C-009/10/28/29/30) are the bones.

**Psychology:** identity ("my phone isn't junk"); performance-as-respect; loss aversion on battery life.

---

### 6. "I want to be a registered user, not a number" — registration identity

**Evidence**
- **Winamp again, the killer stat:** "$10 shareware. **Despite the fact that there would be no extra features by paying $10,** Winamp's popularity … brought Nullsoft **$100,000 a month** … from $10 paper checks in the mail from paying users." People paid for *registration itself*. `https://en.wikipedia.org/wiki/Winamp`
- **mIRC:** registered users got a name next to their handle — identity in the community. `https://en.wikipedia.org/wiki/mIRC`
- **Doom:** "registered version" was a badge of honor in the shareware scene; **Apogee's episode model** made "I bought the rest" a bragging right. `https://en.wikipedia.org/wiki/Apogee_Software`

**Money signal:** $100k/month for zero features. Registration is a *belonging* purchase, not a utility purchase.

**Pro map:** `P-001` (lifetime registration screen) doubles as this. Keep it *quiet* — theme law bans "power user"/badge talk to users. A simple "Registered — thank you" with a date. Internally, this is our prestige lever: the user who "bought once" is the user who *owns* the tool, and they tell one friend.

**Psychology:** prestige/status + reciprocity (shareware honor system: "I use it daily, so I pay"); belonging (Schwartz's stage-5 identity sell — "people like us use this").

---

### 7. "Show me the factory — and never lie to me" — honesty as trust engine

**Evidence**
- **SoftRAM (1995) — the anti-role-model:** sold ~**700,000 copies** of a fake "RAM doubler" whose compression was a stub; FTC settlement for "misrepresented and/or failed to substantiate the performance"; later voted **#3 Worst Tech Product of All Time** by PC World. The short-term money was huge; the trust death was permanent. `https://en.wikipedia.org/wiki/SoftRAM`
- **Shareware's honor system:** the Association of Shareware Professionals sold "try before you buy" as an *ethic* — the category grew only because most shareware was honestly good. `https://en.wikipedia.org/wiki/Shareware`

**Money signal:** SoftRAM proves the *wrong* honesty play makes millions fast and kills the product forever. The inverse — a tool that visibly tells the truth — is our rarest, cheapest moat (already in `SALES-POSITIONING-HISTORIC.md` as the Hopkins "factory tour").

**Pro map:** **Never gate the compat matrix or the honest "low memory" note.** The Pro screen can *show the factory*: "Free includes one small banner. Pay once to remove it. No monthly charge. That's the whole business model." Honesty is what makes $4.99 feel *fair* instead of *sneaky* — and fairness is what converts.

**Psychology:** the honesty premium; trust as a purchase enabler. A user who believes the app lies will never pay; a user who believes the app is honest pays *without knowing why* (this is #1 on the ranked list, see Part 2).

---

### 8. "Give me the tool that does the boring job in one tap" — utility

**Evidence**
- **Handango / PalmGear:** the best-sellers were *single-purpose utilities* — document readers, calculators, expense trackers — bought for $10–30 each. Users paid for "this one job, done well, on my device." `https://en.wikipedia.org/wiki/Handango`
- **J2ME:** the same on feature phones — one-trick apps people paid for via operator billing. `https://en.wikipedia.org/wiki/Java_ME`
- **Opera Mini:** one job (compress the web for weak phones), done brilliantly. `https://en.wikipedia.org/wiki/Opera_Mini`

**Money signal:** the paid-app store era was built on $10–30 one-trick utilities. People pay for *removal of friction*, not for feature lists.

**Pro map:** C-013 (/browse), C-016 (vision/OCR via their key), C-015 (overlay), C-011 (/imagine) = "the Swiss Army tool that fits a 4GB phone." Add **`P-005 — Slash-command library`** (Pro): discoverable list of commands (`/browse`, `/imagine`, `/recall`, `/summarize`) so the utility is findable, not hidden.

**Psychology:** convenience = paying to delete friction; "one tap" framing (Wheeler's sizzle — sell the feeling, not the steak).

---

### 9. "Let me find my own words later" — search your history

**Evidence**
- **Evernote's growth engine was search** ("Remember everything") — users hoard notes *because* they can find them; search is what makes hoarding feel safe.
- **Obsidian/Joplin/KeePass** all make retrieval the core promise — the file is only worth keeping if you can get a fact back out of it. `https://en.wikipedia.org/wiki/Obsidian_(software)` · `https://en.wikipedia.org/wiki/KeePass`

**Money signal:** "second brain" apps monetized this want harder than almost any other category (Obsidian Sync, Evernote subscriptions) — because search turns accumulated history into a *working asset*.

**Pro map:** **`P-003 — Full-text search across chats` (Pro, ~0 KB, Room FTS4/5).** This is the highest-value missing Pro feature we have. Bonus: conversation folders/tags (Pro). Careful: basic export stays free (#3, Part 4); *search and organization* is the Pro layer.

**Psychology:** data-hoarding = safety; search = the payoff; endowment ("my archive is valuable") feeds conversion at the moment of need ("where did I put that?").

---

### 10. "Leave me alone" — no nagging, attention respect

**Evidence**
- **Digital minimalism / dumbphone revival:** Light Phone's own testimonials: "It's really refreshing to not be sold something all of the time." `https://www.thelightphone.com/`
- **Enshittification backlash:** the #1 complaint pattern of 2023–2026 apps is *begging* — notifications, upsells, interstitials. `https://en.wikipedia.org/wiki/Enshittification`
- **Dark-pattern catalog (Brignull):** nagging is a named abuse type; the FTC's 2024 negative-option rule targets the machinery of nag-until-pay. `https://deceptive.design` · `https://www.ftc.gov/business-guidance/blog/2024/10/click-cancel-ftcs-amended-negative-option-rule-what-it-means-your-business`

**Money signal:** the entire dumbphone/Light-Phone economy exists because people *pay hardware money* to escape app begging. Silence is a premium good in 2026.

**Pro map:** Pro = ads gone (the biggest nag). Add **`P-006 — Quiet mode`** (Pro): zero notifications, zero promotional surfaces, ever. And a policy feature: **paid users never see a sale again** — one purchase, then the app goes silent. This is the anti-enshittification promise, and it costs 0 KB.

**Psychology:** attention = dignity; the "respect" purchase; relief from chronic small losses (each ad is a tiny loss aversion hit — removing them all at once is a big felt gain).

---

### 11. "I built this — protect my build" — IKEA effect on the BYOK setup

**Evidence**
- **IKEA effect (Norton, Mochon & Ariely, 2011):** people pay **63% more** for things they partly built themselves. `https://en.wikipedia.org/wiki/IKEA_effect`
- **mIRC scripting:** users wrote scripts, then paid to register — the *build* preceded the *buy*. `https://en.wikipedia.org/wiki/mIRC`
- **Winamp skins/plugins:** the community built on top, then paid. `https://en.wikipedia.org/wiki/Winamp`

**Money signal:** effort invested → willingness to pay rises. BYOK users have done the labor: pasted a key, tuned providers, made templates, forked conversations, taught the memory. That labor is the 63%.

**Pro map:** the free 1-template cap makes the build *finite*; Pro removes the ceiling (C-012). Add **`P-007 — Profiles`** (Pro): separate work/personal setups (keys, providers, templates, memories) — the power-user build made organized. Never gate the building blocks (keys, providers, forks stay free); gate the *scale and organization* of the build.

**Psychology:** IKEA effect + effort justification; the cap is a *loss of future build* (loss aversion on something they haven't made yet but want to).

---

### 12. "Be part of the scene" — community belonging

**Evidence**
- **Demoscene:** a 40-year culture where people craft inside hard constraints for *love* — the scene, not the money, is the point. Constraint-as-identity. `https://en.wikipedia.org/wiki/Demoscene`
- **XDA / 4PDA:** users test, translate, and evangelize apps they love; they reward honest devs with donations and threads. `https://xda-developers.com` · `https://4pda.to`
- **r/androidafterlife:** the exact community that revives old phones — our audience by definition. `https://www.reddit.com/r/androidafterlife/`
- **WinAmp's community** (skins, forums, the llama) — a free product with a *scene* around it. `https://en.wikipedia.org/wiki/Winamp`

**Money signal:** scenes convert to donations and purchases (XDA donation culture; shareware honor system). Belonging is why the Winamp $10 check arrived with a thank-you note.

**Pro map:** **Never gate community.** XDA/4PDA/GitHub presence, support threads, template *sharing* between users — all free. Add **`P-008 — template share-copy`** (FREE): export a template as text/JSON to paste in a forum; import from paste. The scene builds itself; Pro's only role here is the $4.99 as *patronage* ("support the dev once" — a thank-you, not a toll).

**Psychology:** reciprocity + belonging; Schwartz stage-5 identity ("people like us use this") — but whispered, never shouted (theme law).

---

### 13. "Don't rent my brain to the cloud" — sovereignty & control

**Evidence**
- **Local-first manifesto (Ink & Switch, 2019):** "There is no cloud, it's just someone else's computer"; the seven ideals are about *ownership and agency*. `https://www.inkandswitch.com/local-first/`
- **KeePass / Obsidian / Joplin:** the "own your data" category is one of the most loyal, most willing-to-pay user bases in software. `https://en.wikipedia.org/wiki/KeePass`
- **BYOK itself** is the sovereignty want — the app is a window; the brain is theirs.

**Money signal:** self-hosters and local-first users pay for *control features* (Obsidian Sync, KeePass mobile apps) that deepen ownership — not for content.

**Pro map:** key-on-device, no account, encrypted storage = **free, always, never gated** (security is not a upsell; see Part 4). Pro deepens *control*, not security: **`P-009 — advanced provider config`** (Pro): custom headers, weird OpenAI-compatible endpoints, per-template model presets; plus P-007 profiles. The XDA/4PDA crowd pays for knobs — quietly.

**Psychology:** autonomy; power-user identity (backstage — the word is banned on user surfaces); "the tool obeys me."

---

### 14. "I want the machine to remember me" — persistent context

**Evidence**
- **The whole note-taking / second-brain category** (Obsidian, Evernote, Roam) monetized "external memory." `https://en.wikipedia.org/wiki/Obsidian_(software)`
- **Palms:** users kept Memo pads for *years* — device memory was the feature. `https://en.wikipedia.org/wiki/HotSync`
- **Memory as premium** is proven by Obsidian Sync and Evernote subscriptions — but we can sell it *once*.

**Money signal:** "remember me" is one of the few wants users will subscribe for — which is exactly why our *one-time* version is a differentiator.

**Pro map:** C-020 memory (done, Pro). Add **`P-010 — auto chat summaries`** (Pro): each conversation gets a rolling summary line; **`/recall <fact>`** command to ask "what did I say about X" (uses their key, ~0 KB). Context-trimming (C-010, free) keeps the pipe open so memory stays useful on 4GB.

**Psychology:** the "second brain" want; sunk cost + endowment (memory is their build); pay-once beats the subscription incumbents on their home turf.

---

## Part 2 — Ranked: the "would pay without knowing why" Top 10

Ranking = conversion power × historical proof × zero-server cost × fit with $4.99-once + theme.

| # | Latent want | Pro feature(s) | Why they pay without knowing why | Proof weight |
|---|-------------|----------------|----------------------------------|--------------|
| 1 | **Don't let my work disappear** (sunk cost + loss aversion) | C-014 backup, C-020 memory, C-012 unlimited templates, **P-003 search** | The deepest users have the most to protect; they never say "I'm buying backup," they say "I should get Pro." | ★★★★★ (sunk cost; Palm conduits; local-first) |
| 2 | **Pay once, own it forever** | The $4.99 itself + **P-001 lifetime registration** | Buying "never again" is relief, not expense; WinAmp's $100k/month proves it. | ★★★★★ (WinAmp; $219/mo fatigue stats; enshittification) |
| 3 | **Finish the game** (episode model) | Unlimited templates + tool unlocks (C-012/13/15/16/20) | They've tasted episode 1 daily; completing the set feels inevitable. | ★★★★★ (Apogee; Wolfenstein 100k; Doom) |
| 4 | **The file you own** | C-014 encrypted backup + **P-002 .md/.txt export** | A file in their hand is worth more than any cloud promise. | ★★★★☆ (KeePass; Obsidian; HotSync; local-first) |
| 5 | **Old phone feels new** | Banner-off-as-performance + **P-004 comfort mode** | On 4GB, Pro is a *speed* purchase disguised as an ad removal. | ★★★★☆ (Opera Mini; r/androidafterlife; 210M feature phones) |
| 6 | **One-tap utility** | C-013 /browse, C-016 vision, C-015 overlay + **P-005 command library** | "This app does everything I open other apps for." | ★★★★☆ (Handango bestsellers; J2ME) |
| 7 | **I built this** (IKEA effect) | C-012 cap removal + **P-007 profiles** | Their labor is in the app; the cap stings like a lost build. | ★★★★☆ (IKEA 63%; mIRC scripts) |
| 8 | **Find my own words** | **P-003 full-text search** | "Where did I put that?" is a purchase moment disguised as a search. | ★★★★☆ (Evernote/Obsidian retrieval promise) |
| 9 | **Leave me alone** | Banner-off + **P-006 quiet mode** + never-upsell-paid-users policy | Silence is the 2026 premium good; Light Phone sells hardware on it. | ★★★☆☆ (dumbphone revival; enshittification) |
| 10 | **Registered identity** | **P-001** registration screen (quiet) | WinAmp: $100k/month for *zero* extra features. Belonging converts. | ★★★★☆ (WinAmp; mIRC; Doom "registered") |

**Ordering logic:** #1–#4 are *structural* (they ride on psychology everyone has). #5–#6 are *situational* (phone-specific, moment-specific). #7–#10 are *identity* (deeper, slower, but compound). Sell all ten with one $4.99 — the price is below the "should I think about this?" threshold (impulse-buy band, per R-008).

---

## Part 3 — Feature-flagging & monetization psychology (what gating does to value)

**Gating raises perceived value — if the free tier is excellent.**
- Scarcity/commitment research and a century of retail say a *flagged* feature reads as "the good stuff." The Apogee model is the proof: episode 1 free made episodes 2–3 *more* desirable. But it only works if episode 1 is genuinely great. A gated app whose free tier is a nagging husk reads as **ransomware** — and "ransomware" is the word that gets you an enshittification thread on r/Android.

**Gate extras, never essentials.**
- Essentials = chat, key security, honesty, portability (Part 4). Extras = organization, tools, memory, scale. Every Pro feature in Part 1 is an extra. If we ever find ourselves gating "the app works correctly," we've become SoftRAM's business model.

**The 1-template cap is a loss trigger, not an annoyance trigger.**
- Loss aversion (Kahneman & Tversky, 1979): losses hurt ~2× what gains please. The free cap should feel like a *gentle ceiling* ("you're at the free limit — add more with Pro"), never a *cliff* ("your template is locked"). The moment the cap deletes or greys out something they built, the endowment effect turns against us. Cap at *creation*, not at *use*: existing templates always keep working.

**The banner is a chronic small loss; removing it is a gain.**
- Every banner view is a tiny loss-aversion hit (attention, RAM, battery — all real on 4GB). Removal of all hits at once is a felt *gain*. Frame Pro as pain-removal + tool-addition, in that order (Wheeler's "which, not if," calmly: "Free with one small banner, or pay once — which one?").

**Price anchoring.**
- The $20/month anchor is already in the user's head; we don't have to paint it (theme law — no rival-bashing on store copy). $4.99 once, stated plainly, next to a lifetime of use, does the anchoring itself. Screenshots can show the price once, calmly.

**Endowment + IKEA = the BYOK superpower.**
- Every template, fork, provider, memory the user creates raises their willingness to pay (63% IKEA effect; endowment: it's *theirs*). The single best conversion play is therefore: **make the free tier maximally buildable, then cap the build gracefully.** More building → more value → more Pro. The app is the shelf; their labor is the IKEA furniture.

**Sunk cost must convert to safety, never to fear.**
- Long histories raise switching cost. If we weaponize that ("pay or lose it"), we're a trap (GREY-SALES banned list). If we *protect* it (backup, search, memory), sunk cost becomes loyalty. The line is thin and it is the whole moral of this file.

**"Pay once" is an identity, not a discount.**
- WinAmp: no features, $100k/month. The one-time purchase says "I am the kind of person who owns his tools." Keep it quiet, keep it true, never bolt a subscription onto it later — a "pay once" that turns into "pay forever" is the single most enshittifying betrayal available to us (FTC is watching; r/Android is watching).

**Collectibles — the quiet retention engine.**
- WinAmp skins, Doom episodes, demoscene releases: people collect *releases*. Ship **one new built-in template pack per update** (Pro gets it automatically; free users see it behind the cap). "Same app next year, plus a new episode" = retention + the collect-them-all want, at 0 KB and 0 server cost. Aligns with the theme's "same app next year" law.

---

## Part 4 — What we NEVER gate (community / honesty / trust)

1. **The compat matrix and the honest "low memory" note.** This is the trust engine that makes $4.99 feel fair. Gating honesty = SoftRAM. (SoftRAM: 700k copies, FTC, #3 worst tech product ever — `https://en.wikipedia.org/wiki/SoftRAM`)
2. **Key security & privacy facts.** Encrypted storage, no server, no account, privacy page — free, always. "Your key is safer with Pro" would be a lie and a scandal.
3. **Core chat itself.** Send, stream, fallback, per-conversation models, provider failover, connection test (C-017/18/19) — the turkey must never be rotten (R-011's loss-leader law). Degrading free chat to push Pro is the fastest way to a 1-star review wall.
4. **Basic data portability.** A free plain-text/JSON export of conversations. Pro owns the *encrypted* backup, restore, auto-backup, search (C-014, P-002/003) — but the user must always be able to *leave with their words*. GDPR-adjacent, trust-critical, and cheap. (This is the one place we consciously draw the line *below* the data; everything above it is Pro.)
5. **Community surfaces.** XDA thread, 4PDA thread, GitHub, r/androidafterlife presence, support, and user-to-user template sharing (P-008) — all free. The scene is marketing, loyalty, and QA in one.
6. **Security & bugfix updates.** "Pro: security patch" is an admission of a hostile product. Updates ship to everyone, forever.
7. **The no-subscription promise itself.** It's a policy, not a feature — but gating anything around it ("subscribe to see our no-subscription guarantee") would be the enshittification we exist to oppose. The promise is free, loud, and permanent.
8. **The honest factory tour.** The "free includes one small banner, pay once to remove it, that's the whole business model" sentence stays visible to everyone. Transparency is not a Pro feature.

Rule of thumb: **if a feature's absence would make the app feel like it's holding something hostage, it's free. If its presence makes the app feel more capable, it can be Pro.**

---

## Part 5 — Pro screen cheat-sheet (backstage; everyday words on screen)

| Lever | Honest twin (what actually shows) | Where |
|-------|-----------------------------------|-------|
| Loss aversion | "Free includes a small banner. Pay once to remove it." (pain first) | Pro screen, per THEME-SHOW-DONT-TELL |
| Anchoring | "$4.99 once. No monthly charge." (next to a lifetime of use) | Pro screen + Play listing |
| Episode model | "Free: chat + 1 template. Pro: every extra, forever." | Pro screen feature list |
| Endowment/IKEA | Cap message: "You're at the free limit — add more with Pro." (never locks old builds) | Template picker |
| The file you own | "Back up your chats to a file you keep." | Settings → Backup (C-014) |
| Registered identity | "Registered — BYO AI · [date] · no renewal, ever." (P-001, quiet) | Settings → About |
| Anti-enshittification | "One purchase. We never ask again." (P-006 quiet mode + policy) | Settings, once, calmly |
| Which, not if | "Free with a small banner, or pay once — which one?" (Wheeler) | Onboarding |
| Never | fake clocks, "2,481 people bought", "offer ends", confirmshame | — (GREY-SALES banned list) |

---

## Part 6 — Roadmap deltas (all 0-KB or near-0, all Pro except where marked FREE)

**Pro, proposed (highest value first):**
- `P-003` — Full-text search across chats (Room FTS; the #1 and #8 want; 0 KB)
- `P-001` — Lifetime registration screen ("Registered — no renewal, ever"; #2 + #10 want; 0 KB)
- `P-006` — Quiet mode + never-upsell-paid-users policy (#10 want; 0 KB)
- `P-002` — Export conversations as .md/.txt (#3 want; 0 KB)
- `P-004` — Comfort mode: disable motion, cap image decode (#5 want; 0 KB)
- `P-005` — Slash-command library (#8 want; ~5 KB)
- `P-007` — Profiles: work/personal setups (#11 + #13 want; ~10 KB)
- `P-009` — Advanced provider config: custom headers, per-template model presets (#13 want; ~5 KB)
- `P-010` — Auto chat summaries + `/recall` (#14 want; ~10 KB)
- `P-011` — Built-in template pack per release (collectibles; #3 want; 0 KB, retention)

**FREE, proposed (trust/community — never gate):**
- `P-008` — Template share-copy (export/import as text/JSON) — the scene builds itself
- Free plain-text export of conversations (portability floor)
- (Existing free: C-011 /imagine, C-017 failover, C-018 per-conv model, C-019 test connection, C-021 voice, C-022 settings export, C-023 multi-key, C-024 forks — keep free forever)

**Already built and correctly gated:** C-012 templates (cap at creation), C-013 /browse, C-014 backup, C-015 overlay, C-016 vision, C-020 memory. All are extras; none is an essential.

**Never add:** any subscription, ads on send, gating the matrix, gating key security, "Pro: security patch," or a clock anywhere.

---

## Part 7 — The one-paragraph summary for PROOF

Users don't want "features." Across 40 years of paid software, the wants that reliably convert are: *never pay again* (WinAmp: $100k/month for zero features), *finish the game I tasted* (Apogee/id: free episode → 100k+ paid), *hold my own file* (KeePass/Obsidian/local-first), *protect my work* (sunk cost — the deepest user is the best buyer), *make my old phone feel new* (Opera Mini, r/androidafterlife, 210M feature phones/year), *leave me alone* (dumbphone revival, enshittification backlash), *I built this — protect my build* (IKEA effect, 63%), *registered, not numbered* (mIRC, Doom), *one-tap utility* (Handango), *find my own words* (Evernote/Obsidian), *be part of the scene* (XDA/4PDA/demoscene), *own the machine* (local-first), *remember me* (second-brain). Every one maps to a 0-KB Pro feature on a $4.99-once ticket, and the honest twins are all already in our theme. The only unforgivable moves — SoftRAM lies, gating honesty, a subscription bolted onto "pay once," capping at the *use* of user-built things — are the ones GREY-SALES and this file ban.

---

## Sources (all free, all checked 2026-08-15)

- Apogee shareware episode model: https://en.wikipedia.org/wiki/Apogee_Software (→ 3D Realms)
- Wolfenstein 3D shareware, 100k units by end of 1993: https://www.gamedeveloper.com/business/monsters-from-the-id-the-making-of-i-doom-i-
- Winamp $10 shareware, $100k/month, no extra features: https://en.wikipedia.org/wiki/Winamp
- WinZip shareware/registration heritage: https://en.wikipedia.org/wiki/WinZip
- mIRC 30-day shareware registration (a chat client): https://en.wikipedia.org/wiki/mIRC
- Shareware / honor system: https://en.wikipedia.org/wiki/Shareware
- King (Candy Crush, "one of the most financially successful games utilising the freemium model"): https://en.wikipedia.org/wiki/King_(company)
- Freemium model: https://en.wikipedia.org/wiki/Freemium
- Handango (PDA/smartphone paid-app store, 1999): https://en.wikipedia.org/wiki/Handango
- Palm HotSync (user-owned sync): https://en.wikipedia.org/wiki/HotSync
- J2ME (feature-phone paid apps): https://en.wikipedia.org/wiki/Java_ME
- Opera Mini (server-rendered thin client for weak phones): https://en.wikipedia.org/wiki/Opera_Mini
- SoftRAM scam (FTC 1996, ~700k copies, PC World #3 worst): https://en.wikipedia.org/wiki/SoftRAM
- Enshittification (Doctorow 2022; ADS Word of the Year 2023): https://en.wikipedia.org/wiki/Enshittification
- Subscription fatigue stats ($219/mo vs $86 estimate; 89% underestimate): https://resubs.app/resources/subscription-spending-statistics · https://www.readless.app/blog/subscription-fatigue-statistics-2026
- FTC Click-to-Cancel (2024): https://www.ftc.gov/business-guidance/blog/2024/10/click-cancel-ftcs-amended-negative-option-rule-what-it-means-your-business
- IKEA effect (63%, Norton/Mochon/Ariely 2011): https://en.wikipedia.org/wiki/IKEA_effect
- Endowment effect (Thaler 1980): https://en.wikipedia.org/wiki/Endowment_effect
- Sunk cost (Arkes & Blumer 1985): https://en.wikipedia.org/wiki/Sunk_cost
- Loss aversion / prospect theory (Kahneman & Tversky 1979): https://en.wikipedia.org/wiki/Prospect_theory
- Local-first manifesto ("You own your data, in spite of the cloud"): https://www.inkandswitch.com/local-first/
- Obsidian (local knowledge base): https://en.wikipedia.org/wiki/Obsidian_(software) · https://obsidian.md
- KeePass (the file you own): https://en.wikipedia.org/wiki/KeePass
- Demoscene: https://en.wikipedia.org/wiki/Demoscene
- r/androidafterlife: https://www.reddit.com/r/androidafterlife/
- XDA / 4PDA: https://xda-developers.com · https://4pda.to
- Dumbphone/feature-phone market (Counterpoint: ~210M feature phones, ~15% of global sales): https://counterpointresearch.com/en/insights/Dumbphones%20are%20getting%20smarter%20and%20more%20premium.%20Are%20they%20the%20solution%20for%20disillusioned%20phone%20addicts
- Light Phone (attention-respect testimonials): https://www.thelightphone.com/
- Dark patterns: https://deceptive.design

*Pre-existing repo docs consumed (not re-sourced):* `docs/PREMIUM-STRATEGY.md` (R-008), `docs/SALES-POSITIONING-HISTORIC.md` (R-011), `docs/GREY-SALES-GOOD-TWINS.md` (R-014), `docs/THEME-SHOW-DONT-TELL.md` (H-006), `docs/BACKLOG.md`.
