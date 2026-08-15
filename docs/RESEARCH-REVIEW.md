# LiteChat — research review (PROOF)

**Codeword:** `LITECHAT-PROOF`  
If the human says that word, you are the **research reviewer**. You grade DIG’s work. You do not rewrite it. You do not touch the app.

**Read first:** this file + `docs/TEAM.md` + `docs/BACKLOG.md` + `docs/THEME-SHOW-DONT-TELL.md`.

---

## Trigger

Human: `LITECHAT-PROOF` + a doc name, an `R-` ticket, or “latest DIG drop.”  
Default if unnamed: most recent `Research` ticket and the docs it points at.

**Every** research ticket must get an **Approve** here before anyone flips it to **Ready**. Human may override in BACKLOG in one written line.

---

## You own

- Read-only pass over the named `docs/*.md` + the ticket text
- The checklist below, in order
- This file: append a dated verdict (never delete old verdicts)

## You must not

- Edit `app/**`, gradle, CI
- Rewrite DIG docs (`SALES-*`, `THEME-*`, `APP-NAMING-*`, etc.)
- Mark BACKLOG **Ready** / **Done** (DIG or human does that **after** Approve)
- Invent product policy (price, name, legal)
- Start a new research epic or clone repos
- Run Gradle
- Comment on Kotlin style (that is `LITECHAT-REVIEW`)

---

## Checklist (stop for a hard fail if you want speed; full pass on a big drop)

1. **Scope** — Named files only. DIG did not sneak into app code.
2. **Grounding** — Load-bearing facts have a URL DIG actually opened. Guesses are marked. Sources block matches cites if they used one.
3. **Product laws** — Tier A, 4GB honesty, no 7B, no WebView, no fake RAM. **Cost** labeled before anything that spends money.
4. **Theme** — Anything a *user* would read must pass `docs/THEME-SHOW-DONT-TELL.md`. Fight talk only in backstage docs that say so at the top.
5. **Ticket quality** — A ticket aiming at Ready needs: goal, checkbox AC, files likely touched, out of scope, research link.
6. **Usefulness** — Changes a WIRE build or a human H-00N. Pure essay with no ticket and no question → Issues.

---

## Verdict format (append below, newest first)

```
## Research review — YYYY-MM-DD — <doc or R-id>
**Verdict:** Approve / Issues
### Issues
1. **<file.md>** — what’s wrong in plain words
   - Why it matters: bad fact / unsafe Ready / theme leak / missing cost
   - Suggested fix: what DIG should change
```

No style nits. No “I would have written it prettier.”

---

## Prompt paste (human → new session)

```
LITECHAT-PROOF
Repo: /opt/data/workspace/byok-chat-android
Read docs/RESEARCH-REVIEW.md + docs/TEAM.md + docs/BACKLOG.md + docs/THEME-SHOW-DONT-TELL.md.
You are READ-ONLY except this file (docs/RESEARCH-REVIEW.md).
Grade ticket <R-ID> (or the latest DIG drop).
Do not edit DIG docs. Do not edit app/**. Do not run gradle.
Write Approve / Issues at the top of the log below.
```

---

## Flow

```
DIG writes docs + ticket stays Research
        │
        ▼
PROOF grades → this file
        │
        ├── Approve  → DIG or human may set Ready
        └── Issues   → DIG fixes → PROOF again
                │
                ▼
         WIRE takes Ready only
                │
                ▼
         REVIEW grades code (docs/REVIEW.md)
```

---

# Log (newest first)

## Research review — 2026-08-15 — Re-check: PROOF issues 1–3 (commit f1d97de)

**Scope:** the 3 fixes from `f1d97de` only: F-Droid metadata (`fastlane/metadata/android/en-US/fdroid.yml` + description files + `docs/LAUNCH-PACK.md` row 2), C-032 status + `docs/PLAY-POLICY-AUDIT.md` blocker #1, `docs/PRO-ROADMAP.md` /imagine boundary line.  \
**Role:** `LITECHAT-PROOF` re-check. Read-only except this file. No gradle. No `app/**`. Re-verified every fix against real bytes: `git status` clean, `yaml.safe_load` on fdroid.yml, `wc -c` on both description files, live `curl` on the privacy URL.

**Verdict: Approve** — all 3 issues closed.

### Issue-by-issue

1. **F-Droid metadata — ✅ closed.** `fdroid.yml` has no `subdir: app` and no literal `commit: main` (placeholder `<FULL_SHA_OF_v1.0.0_TAG>` + inline comment; YAML parses). NonFreeNet reason is now the real rationale ("connects to user-chosen proprietary AI APIs by default"). Description rewritten for the foss build — no ads/Pro/billing talk; ends "no ads, no Google Play services, and no billing. Free forever." `short_description.txt` exists at 61 chars (≤80); `full_description.txt` at 925 bytes (≤4000). `LAUNCH-PACK.md` row 2 reflects the fix and adds the tag-at-release step (Tag `v1.0.0`, submit MR as `metadata/com.byoai.chat.foss.yml`).
2. **C-032 + privacy blocker — ✅ closed.** BACKLOG C-032 notes PROOF approved Research B post-hoc (re-affirmed after the pre-PROOF Ready flag). PLAY-POLICY-AUDIT blocker #1 is marked **RESOLVED 2026-08-15** with the Pages-enable fix, and I independently verified `https://flamingspade1995-coder.github.io/litechat-android/privacy.html` returns **HTTP 200** (serves the BYO AI Privacy Policy) — the doc's claim is true, not just asserted.
3. **PRO-ROADMAP boundary — ✅ closed.** Explicit line: `/imagine` and `/video` generation stay **free forever** (user already pays the provider); the paid layer is what sits on top — image editing (separate API call), folders, search, organization. Names PRO-SCAN's "never gate /imagine" and states the reconciliation, so a future WIRE cannot read a contradiction.

### Nits (not Issues)

- `fdroid.yml` is now explicitly documented as the fdroiddata-submission reference (recipe lives in fdroiddata as `com.byoai.chat.foss.yml`) — correct per the audit; the file still contains `commit:` as a placeholder, which is the intended pre-submission state, not a live recipe.
- Re-verify the placeholder swap + HTTP 200 at actual submission time (URL depends on Pages staying enabled).

### Next

Issues 1–3 are closed. C-032 Ready stands; Research A/B/C + PRO pile are safe to build from. No further PROOF action needed on this drop.

---

## Research review — 2026-08-15 — Full 2026-08-15 pile (R-016 pack, TypingMind dig, Research A/B/C, H-001/H-008/H-009/H-010)

**Scope:** `docs/PRO-SCAN-COMPETITIVE.md`, `docs/PRO-TRENDS-NEXT-BIG.md`, `docs/PRO-LATENT-WANTS.md`, `docs/PRO-ROADMAP.md`, `docs/TYPINGMIND-DIG.md`, `docs/FDROID-BUILD-AUDIT.md` (Research A), `docs/PLAY-POLICY-AUDIT.md` (Research B), `docs/GLOBAL-MARKETS-RESEARCH.md` (Research C), `docs/QUESTIONS-FOR-HUMAN.md` H-001/H-008/H-009/H-010, BACKLOG C-032/R-016.
**Role:** `LITECHAT-PROOF`. Read-only except this file. Did not run gradle. Did not touch `app/**` or BACKLOG statuses. Spot-checked two load-bearing URLs (TypingMind backlash thread — real, title matches; F-Droid Build Metadata Reference — confirms "full commit hash" rule at line 385).

**Verdict: Issues** (small, fixable; the pile itself is the strongest DIG drop yet)

### Per-doc verdict

| Doc | Verdict | One line |
|---|---|---|
| PRO-SCAN-COMPETITIVE | ✅ Approve | Grounded competitor table; validated gate map; TypingMind cautionary tale verified live |
| PRO-TRENDS-NEXT-BIG | ✅ Approve | Voice+Memory bundle evidence is real; explicit user-facing/backstage split; no-7B/WebView laws held |
| PRO-LATENT-WANTS | ✅ Approve | 14 wants × URLs; never-gate list is correct and pro-theme |
| PRO-ROADMAP | ✅ Approve (1 fix) | Clean synthesis; needs one explicit line on the image-edit gate (issue 3) |
| TYPINGMIND-DIG | ✅ Approve | Verified source; steal list matches roadmap; feeds H-008 |
| FDROID-BUILD-AUDIT (A) | ✅ Approve (research) | Audit is accurate — but its fixes are **untracked** (issue 1) |
| PLAY-POLICY-AUDIT (B) | ✅ Approve (research) | Correct CONDITIONAL PASS; C-032 spawned — but was set Ready too early (issue 2) |
| GLOBAL-MARKETS-RESEARCH (C) | ✅ Approve | 47 sourced URLs, `[unverified]` marked, answers H-009, spawns H-010 |
| QUESTIONS-FOR-HUMAN H-001/H-008/H-009/H-010 | ✅ Approve | H-001 decision + deferral note recorded; H-008 hold recorded; H-009 locked; H-010 ready |

### Checklist

1. **Scope** — `git log` since the approved C-031 shows only docs/BACKLOG commits; tree clean. No DIG sneak into `app/**`. H-001 deferral and H-008 hold both recorded in QUESTIONS-FOR-HUMAN. Correct.
2. **Grounding** — Load-bearing facts carry URLs; sources blocks match cites; `[unverified]` used where honest (ChatCat row, Pakistan, CNN Brasil). Both claims I spot-checked were true.
3. **Product laws** — Held across the pile: Tier A thin client, 4GB honesty, no 7B (explicitly rejected), no WebView (banned), no fake RAM ("runs a real model on 4GB" stays banned), costs labeled ($0 tools, $4.99, voice per-minute, Groq free tier, $25 Play).
4. **Theme** — All three PRO digs stamped BACKSTAGE. PLAY-LISTING-DRAFT, XDA template, reddit draft, awesome-byok-apps entry: everyday words, no banned fight terms, no named rivals. The 2027 one-liner is calm and true.
5. **Ticket quality** — C-032 has goal/AC/files/out-of-scope — full ticket. P-00x are research-grade proposals (named + mini-AC), correctly not Ready; they must be upgraded to full tickets before any goes Ready after H-008.
6. **Usefulness** — R-016 answers H-008 (recommendation A, fallback C); Research C answers H-009's "name the markets" and spawns H-010; Research B spawned C-032. **Research A changes nothing yet — that is issue 1.**

### Issues

1. **`docs/FDROID-BUILD-AUDIT.md` fixes are untracked; `fastlane/.../fdroid.yml` is still the exact stale recipe the audit flagged.**
   - Why it matters: fdroid.yml still has `commit: main` + `subdir: app` (audit's two ❌ FAILs — build break + review blocker), description lines 27–28 still advertise "One-time $4.99 Pro removes ads" + "FOSS flavor available" (false for the foss build the audit reviewed — flagged as 1.19 review friction), wrong NonFreeNet reason (1.21), and no `short_description.txt`/`full_description.txt` (1.17). **`docs/LAUNCH-PACK.md` row 2 claims "metadata refreshed" — that is false.** No ticket exists to apply the audit's fixes, and D-005's AC does not cover them. If WIRE takes D-005 as-is, the broken recipe ships to F-Droid.
   - Suggested fix: DIG writes one fix ticket (or amends D-005's AC): remove `subdir: app`, pin full-SHA commit, tag `v1.0.0`, rewrite description for the foss build (no ads/Pro copy), add short/full description `.txt`, correct NonFreeNet reason. Do not submit to F-Droid before it lands.

2. **C-032 was flipped to Ready (commit 322479c) before this PROOF pass.**
   - Why it matters: the flow says research-derived tickets wait for Approve; this is the same class of violation as the old C-031-ready-without-approval (which was Blocked). Today's Approve legitimizes C-032, but the ordering was wrong. Related: Research B's blocker #1 (privacy URL 404) has no ticket either — only a launch-checklist mention.
   - Suggested fix: keep C-032 Ready now that this pile passes; add "privacy URL returns 200 before $25" to D-005's AC or a small ticket; from now on, tickets derived from an ungraded pile stay Research/Blocked.

3. **PRO-ROADMAP Tier 1 row 12 gates "image editing" while PRO-SCAN §4 says never gate /imagine & /video.**
   - Why it matters: reconcilable — `/imagine`/`/video` stay free ("Free keeps" list) and the edit variant is a new capability, not a moved gate — but the docs never say that, so a future WIRE could read a contradiction.
   - Suggested fix: one line in PRO-ROADMAP: "generation stays free; only the new gpt-image-1 edit variant is Pro (TypingMind precedent)".

### Nits (not Issues)

- P-00x tickets are proposal-grade — fine at Research, must gain files-touched + out-of-scope before Ready (post H-008).
- XDA template's "Why this over the official ChatGPT app?" is factual, but keep it that way — never let it drift into named-enemy copy.

### Ready for human decisions

**H-008 (Pro bundle direction)** and **H-010 (per-country pricing)** are ready: research recommends A (full Tier 1 bundle, fallback C) and B (₹199/R$14.90-class overrides, US stays $4.99). H-001 (BYO AI) and H-009 (languages free, always) are decided and recorded. After DIG fixes issues 1–3, this pile is fully safe to build from.

---

## Research review — 2026-08-15 — C-031 naming pack (BYO AI + 4GB line)

**Scope:** H-001 lock, `docs/APP-NAMING-RESEARCH.md` §9, BACKLOG C-031.  
**Role:** `LITECHAT-PROOF`. Did not edit DIG docs. Did not edit `app/**`. Did not mark Ready.

**Verdict:** Approve

### Checklist

1. **Scope** — Docs + ticket only. No app code. Correct.
2. **Grounding** — 26-name table already existed. Human locked the pick. “Works on 4GB phones” is a product-law claim (thin client), not a new historic fact. Play Console exact-name search is still **submit-time** (DIG already said so). Fine.
3. **Product laws** — Held. Ticket bans “runs a real model on 4GB.” $0 now. Play later ~$25 flagged.
4. **Theme** — Name is everyday picnic talk. 4GB is in the 80-letter line, not the title. No fight words. Name test still: “I put my key in and chat.”
5. **Ticket quality** — Goal, locked copy, checkbox AC, files, out of scope, cost. Ready-shaped **after** this Approve.
6. **Usefulness** — WIRE can rename without guessing.

### What is solid

- **BYO AI** + *Chat with your own key. Works on 4GB phones. No monthly bill.* (61/80)
- `applicationId` → `com.byoai.chat` **before** first Play upload. Package stays LiteChat.
- 4GB is a fact people can read. It is not a SoftRAM boast.

### Nits (not Issues)

- AC says update `docs/PLAY-LISTING-DRAFT.md`. That file is **missing**. WIRE should **create** it with the locked name + short line. Do not invent extra boast copy.
- Collision check was web search, not Play Console. Recheck the exact title when someone opens Play Console.

### Not approved (and not asked)

- R-015 overnight roadmap is still **Research** with no PROOF pass. Separate job.
- Icon, domain, Play upload, paid ads.

### Next

DIG or human may set **C-031 → Ready**.  
Then WIRE may take it. I did not flip the status.

---

## Research review — 2026-08-15 — R-014 grey sales / good twins

**Scope:** `docs/GREY-SALES-GOOD-TWINS.md` + BACKLOG R-014.  
**Role:** `LITECHAT-PROOF`. Did not edit DIG docs. Did not edit `app/**`.

**Verdict:** Approve

### Checklist

1. **Scope** — Docs only. No app code. DIG refused to write a working boiler-room script. Correct.
2. **Grounding** — Load-bearing history has opened URLs (Adams, Smithsonian, Brignull/deceptive.design, FTC Click-to-Cancel, boiler-room pages). Sources block is present. Small nits below; not enough to fail.
3. **Product laws** — Held. No 7B / SoftRAM. $4.99 once. Cost of the dig is $0. Play cut mentioned before Pay.
4. **Theme** — File is stamped BACKSTAGE. User-facing twins are everyday (“low on memory,” “Not now,” “Paste your key”). Fight-poster lines banned. Matches `THEME-SHOW-DONT-TELL.md`.
5. **Ticket quality** — R-014 is **Research**, no Ready child. Right. DIG even delayed a copy-check ticket until after this grade.
6. **Usefulness** — This is a **do-not-trap** law for later Pro / banner / memory copy. It does not invent a WIRE feature. That is the right outcome.

### What is solid

- Four-part “good way” test (real fact, easy no, we’d be proud, legal/theme).
- Honest twins map 1:1 onto bones we already want (demo on their phone, no account, one banner, pay once).
- Best twin is already in the product plan: **no subscription maze**.
- Banned list is clear enough that WIRE cannot “test” a fake clock later and call it research.

### Nits (not Issues)

- Kirby “$2,000” is not on the cited Kirby page [9]. Drop the dollar or add the page that has it.
- Classic-close table is classroom folklore, not cited. Fine as backstage manners. Do not treat those names as Play copy.

### Not approved (and not asked)

- A **copy-check** coding ticket (Pro screen / banner / memory note). Does not exist yet. Write it only if you want WIRE to audit existing strings against this file — then call PROOF on *that* ticket.
- C-031 rebrand — still Blocked.

### Next

DIG or human may set R-014 to **Done — research only**.  
Do **not** set anything Ready from this file.

---

## Research review — 2026-08-15 — R-011/R-012/R-013 DIG fix (second pass)

**Scope:** same pile after DIG repair: sales, shine, King’s Road, theme law, H-005, C-031, R-011/R-012/R-013.  
**Role:** `LITECHAT-PROOF`. Did not edit DIG docs. Did not edit `app/**`.

**Verdict:** Approve

### Checklist

1. **Scope** — Docs only. No app code.
2. **Grounding** — Unchanged from first pass (still good enough). New text is policy stamps, not new history claims.
3. **Product laws** — Still held. Cost flags still there.
4. **Theme** — Sales + shine now say BACKSTAGE / DO NOT USE IN LISTING at the top. Banned “They laughed…” is labeled banned. Everyday lines match `THEME-SHOW-DONT-TELL.md`. King’s Road swipe list is gone (only a “do not use” leftover). H-005 rec is **N / Y / your call**, not Y Y Y.
5. **Ticket quality** — No new Ready ticket (correct). C-031 is **Blocked**. R-011/R-012 are Done-research, no Ready child.
6. **Usefulness** — This drop made the pile safe for later naming/Play work. It did not invent WIRE work. That is the right outcome.

### First-pass issues — closed

1. C-031 Ready with no name → **Blocked**. Closed.
2. Paste-ready fight copy → stamped / banned / replaced. Closed.
3. H-005 vs H-006 fight → H-005 rewritten. Closed.
4. Essay with unsafe Ready child → no Ready child. Closed.
5. King’s Road swipe file → deleted. Closed.

### Not approved (and not asked)

- C-031 rebrand — still Blocked until H-001 + a **naming-pack** Approve.
- A Play listing rewrite ticket — does not exist yet. Do not start one without H-001.
- Older Ready items elsewhere in BACKLOG (premium, play-listing draft from earlier tickets) — **out of this pile**. Not graded here.

### Next

DIG or human may strike “Waiting second PROOF pass” from R-011.  
Do **not** set C-031 Ready.  
Human can answer H-001 / leftover H-004 / H-005 when they want a name.

---

## Research review — 2026-08-15 — R-011 + R-012 + R-013 (sales / shine / theme pile)

**Scope:** `docs/SALES-POSITIONING-HISTORIC.md`, `docs/SHINE-UNCONVENTIONAL.md`, `docs/KINGS-ROAD-THEME.md`, `docs/THEME-SHOW-DONT-TELL.md`, BACKLOG R-011/R-012/R-013, H-001/H-004/H-005/H-006, C-031.  
**Role:** first `LITECHAT-PROOF` pass. No DIG rewrites in this step.

**Verdict:** Issues

### What is solid

- Grounding on the historic files is good enough: ledgers, Sources blocks, `[unverified]` used once on purpose.
- Cost flags exist (Play ~$25, Billing cut, “don’t buy the Schwartz book”).
- Product laws held: no 7B, no SoftRAM, no WebView, Tier A.
- H-006 quieter is written down. `THEME-SHOW-DONT-TELL.md` is the right user-facing law. `KINGS-ROAD-THEME.md` now says BACKSTAGE ONLY at the top.
- DIG did **not** invent a name list while H-004 was empty. Correct.

### Issues

1. **`docs/BACKLOG.md` C-031** — Status is **Ready** while H-001 is still empty and H-004 is still empty.
   - Why it matters: WIRE is allowed to take Ready tickets. A rebrand with no chosen name is not ready. This ticket also never passed PROOF (PROOF did not exist yet).
   - Suggested fix: DIG sets C-031 to **Blocked** (or **Research**) until H-001 is filled **and** this file has Approve on the naming pack.

2. **`docs/SALES-POSITIONING-HISTORIC.md` + `docs/SHINE-UNCONVENTIONAL.md`** — Still offer user-facing lines that break the quieter lock: “They laughed…”, named-night / bout language, polarize-as-hero copy.
   - Why it matters: H-006 / `THEME-SHOW-DONT-TELL.md` bans fight talk in anything a user reads. A later DIG or WIRE could paste those lines into the Play draft.
   - Suggested fix: DIG stamps those sections **DO NOT USE IN LISTING / UI** (or moves them under a Backstage heading). Keep the *ideas*. Kill the paste-ready fight sentences.

3. **`docs/QUESTIONS-FOR-HUMAN.md` H-005** — Still recommends **Y Y Y** (public dare, polarize vs ChatGPT/Agora, underground week) after H-006 said everyday + don’t tell.
   - Why it matters: two human questions now point opposite ways. Polarize-by-name on the store is the thing the theme law just banned.
   - Suggested fix: DIG rewrites the H-005 recommendation to match quieter: numbers-in-a-screenshot OK; named-enemy listing copy not OK; underground week is a distribution choice, not a fight poster.

4. **R-011 / R-012** — Still “waiting on H-004.” No Ready coding ticket came out of them except the unsafe C-031 gate. Fine as research, but the pile has no single “what WIRE does next” that PROOF can Approve.
   - Why it matters: usefulness rule. History without a safe ticket is a stack of paper.
   - Suggested fix: After issues 1–3, DIG either (a) leaves R-011/R-012 as Done-research with **no** Ready child, or (b) writes one small Ready ticket that only updates Play-draft *after* H-001 — and only with everyday words.

5. **`docs/KINGS-ROAD-THEME.md`** — Still contains a “Words we can steal” list (“The small one. Still up.”) below the new backstage banner.
   - Why it matters: the banner says don’t paste fight words; the body still looks like a swipe file.
   - Suggested fix: DIG wraps that list in an explicit “agents only / never listing” box, or deletes the swipe lines now that `THEME-SHOW-DONT-TELL.md` exists.

### Not issues

- H-004 still open: correct pause, not a PROOF fail.
- Theme law itself: Approve that file on its own. Do not rewrite it.
- No app/** edits in this DIG drop.

### Next

DIG fixes 1–5. Human (or DIG) calls `LITECHAT-PROOF` again on the same pile. **Do not** set C-031 Ready until that second pass says Approve.
