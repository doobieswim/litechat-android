# PLAY-POLICY-AUDIT.md — BYO AI (com.byoai.chat)

**Audit date:** 2026-08-15 · **Auditor:** Play policy subagent (LITECHAT-DIG scope)
**App under audit:** BYO AI — thin BYOK chat client. Kotlin/Compose, minSdk 26, targetSdk/compileSdk 36 (API 36 / Android 16), `applicationId com.byoai.chat` (play flavor) + `com.byoai.chat.foss` (FOSS). AdMob banner (sample IDs), one-time IAP `BYO_pro` ($4.99) via Play Billing, no accounts, no server, API key in EncryptedSharedPreferences, `/imagine` + `/video` via the user's own OpenAI-compatible key. Privacy policy (currently broken URL) at `https://flamingspade1995-coder.github.io/litechat-android/privacy.html`.
**Repo facts verified from:** `app/build.gradle.kts`, `app/src/play/AndroidManifest.xml`, `app/src/main/AndroidManifest.xml`, `app/src/play/java/com/litechat/android/data/billing/BillingRepository.kt`, `app/src/main/java/com/litechat/android/ui/ChatViewModel.kt`, `app/src/main/java/com/litechat/android/data/prefs/SecureStore.kt`, `privacy.html`, `docs/PLAY-LISTING-DRAFT.md`, `docs/LAUNCH-PACK.md`, `docs/QUESTIONS-FOR-HUMAN.md` (H-001/H-003 locked).
**Method:** web research only (no gradle/builds). Every policy claim below carries a live URL (all verified reachable 2026-08-15). Policy pages renumber over time (Google introduced a standardized numbering system July 2026); if a link 404s at submission time, search the exact policy name in the [Policy Center](https://play.google/about/developer-content-policy/).

---

## 0. Headline verdict

**CONDITIONAL PASS — 2 blockers + 4 required fixes before the $25 is spent.** Nothing about BYO AI's design is fundamentally unpublishable (no accounts, BYOK, minimal permissions, no UGC hosting, Play Billing wired correctly). But as of today the submission would bounce on two hard blockers:

1. **Privacy policy URL returns HTTP 404** (verified live) — Play requires a valid privacy policy URL to complete the Data Safety form and pass review. **BLOCKER — ✅ RESOLVED 2026-08-15:** GitHub Pages enabled on the repo; `https://flamingspade1995-coder.github.io/litechat-android/privacy.html` verified **HTTP 200** after deployment.
2. **No in-app AI-content reporting/flagging feature exists** — the AI-Generated Content policy requires it for any app that generates AI content (chat, images, video). **BLOCKER.**

Plus required fixes: real AdMob IDs, EEA/UK consent (UMP), Terms-of-use/acceptable-use acceptance, honest Data Safety + content-rating answers.

---

## 1. Verdict table per policy area

| # | Policy area | Verdict | What the policy says (evidence) | What to do |
|---|-------------|---------|--------------------------------|------------|
| A | **AI-generated content (in-app)** | **FAIL → fix required** | Apps that generate content using AI (chatbots; AI images/video from prompts) must (1) prevent generation of Restricted Content (child exploitation, deceptive content), and (2) **contain in-app user reporting or flagging features** so users can report offensive content without exiting the app. [AI-Generated Content policy](https://support.google.com/googleplay/android-developer/answer/13985936) · [Best practices / safeguards](https://support.google.com/googleplay/android-developer/answer/16353813) | Add long-press → "Report" on every message (text, /imagine, /video results). Report flow stays in-app (reason picker → sends to dev email or GitHub issue URL). Add an acceptable-use screen (see §3.4). App cannot "see" the content (it goes to the user's own API), so reporting routes to the developer mailbox; that still satisfies the policy's letter. |
| B | **AI-generated content (store assets)** | **PASS (with rule)** | New Play Console self-declaration: any AI-generated/edited **store-listing asset** (screenshots, promo images, YouTube trailers) must be AI-labeled via the checkbox in the content creation flow; declared assets get an AI label on the store. [Declaring AI-generated content in Play Console](https://support.google.com/googleplay/android-developer/answer/17262077) | Screenshot plan already mandates **real UI captures** (PLAY-LISTING-DRAFT.md) → no AI label needed. If any asset is ever AI-generated, tick the box. Never fake screenshots. |
| C | **Data Safety form** | **RISK → PASS with exact answers** | All apps must complete the form; third-party SDK (AdMob) data must be declared; data transmitted off-device = "collected"; user-initiated transfers (prompts sent to the API the user chose) are not "shared". [Data Safety requirements](https://support.google.com/googleplay/android-developer/answer/10787469) | Fill exactly as §2. The two traps: claiming "No data collected" while AdMob runs (enforcement action), or claiming prompts are ephemeral (providers retain them — OpenAI 30 days etc.). |
| D | **Target API level** | **PASS** | From **Aug 31, 2026**: new apps and updates must target **Android 16 (API 36)+** (extension to Nov 1, 2026 available); existing apps must target 35+ to reach new users. [Target API level requirement](https://developer.android.com/google/play/requirements/target-sdk) · [Deadline reminder, July 2026](https://support.google.com/googleplay/android-developer/answer/17134731) | Already at targetSdk/compileSdk 36 → PASS. Keep it there; API 37 (Android 17) deadline will arrive Aug 2027. |
| E | **Ads (policy + AdMob)** | **RISK → fix required** | Ads are part of your app: must follow Restricted Content; must fit your content rating; deceptive/disruptive ads banned; non-fullscreen banners are explicitly fine. Advertising ID use must be disclosed. [Ads policy](https://support.google.com/googleplay/android-developer/answer/9857753) · [User Data policy](https://support.google.com/googleplay/android-developer/answer/10144311) | (1) Replace sample IDs (`ca-app-pub-3940256099942544/6300978111` banner in `build.gradle.kts` + `strings.xml`, app ID `ca-app-pub-3940256099942544~3347511713` in play manifest) with real ones from an AdMob account. (2) EEA/UK: personalized ads need consent via a certified CMP — integrate Google **UMP** or serve non-personalized ads only. (3) Complete the **Ads declaration** in Play Console app content. (4) Never show ads via the overlay service (SYSTEM_ALERT_WINDOW) — overlay must stay ad-free (it is). |
| F | **Billing (one-time IAP)** | **PASS** | Digital goods must use Google Play's billing system; pricing shown in-app must match Play's billing UI. [Payments policy](https://support.google.com/googleplay/android-developer/answer/9858738) · [Understanding the Payments policy](https://support.google.com/googleplay/android-developer/answer/10281818) · [Create an in-app product](https://support.google.com/googleplay/android-developer/answer/1153481) · [One-time products overview](https://support.google.com/googleplay/android-developer/answer/16430488) | Implementation is compliant: `BillingRepository.kt` uses `ProductType.INAPP`, `launchBillingFlow`, `acknowledgePurchase` (acknowledgment within 3 days is covered), and restore (`queryOwned`), SKU from `BuildConfig.PLAY_PRO_SKU` = `BYO_pro`. Create the managed product in Play Console with **exact id `BYO_pro`** at $4.99 and keep the in-app price display identical. Product IDs may not start with `android.test`; `BYO_pro` is valid (≤40 chars, lowercase start). |
| G | **Account deletion** | **PASS (exempt)** | Only apps that **allow account creation** must offer in-app + web account deletion. [Account deletion requirements](https://support.google.com/googleplay/android-developer/answer/13327111) | No accounts in BYO AI → requirement does not apply. Still answer the Data Safety deletion questions honestly (on-device data: user deletes via in-app history controls / clearing app data / uninstall; prompts sent to user-chosen APIs are subject to that provider's retention, outside dev control). |
| H | **Privacy policy** | **FAIL → fix required** | A valid privacy policy is required in the store listing, inside the app, and to complete the Data Safety form — even for "no data collected" apps. [User Data policy](https://support.google.com/googleplay/android-developer/answer/10144311) · [Data Safety prerequisites](https://support.google.com/googleplay/android-developer/answer/10787469) | **The hosted URL currently returns HTTP 404** (verified: `https://flamingspade1995-coder.github.io/litechat-android/privacy.html` → "Site not found · GitHub Pages"). Publish `privacy.html` (exists at repo root + `app/src/main/assets/`) on a live HTTPS host (enable GitHub Pages on the repo, or any static host) and re-verify 200 before paying. Add a sentence that prompts go to the user-configured third-party API and are governed by that provider's retention/privacy terms. |
| I | **Restricted content / AI media** | **RISK → mitigations** | No sexual content/profanity; no content facilitating child exploitation; no deceptive behavior — and AI-generated content must not be *generated* for these either. [Inappropriate Content](https://support.google.com/googleplay/android-developer/answer/9878810) · [Restricted Content topic](https://support.google.com/googleplay/android-developer/topic/9877466) · [AI-Generated Content policy](https://support.google.com/googleplay/android-developer/answer/13985936) · [Child exploitation](https://support.google.com/googleplay/android-developer/answer/9878809) · [Deceptive behavior](https://support.google.com/googleplay/android-developer/answer/9888077) | The app doesn't host or promote content, and generation happens at user-chosen providers (which filter). Add: acceptable-use terms (CSAM/sexual/violence/deception prohibited), the in-app report flow (A), and a note that model providers' safety filters apply. Keep the app itself free of sexual/profanity content. |
| J | **UGC policy** | **PASS (not UGC) + hygiene** | UGC = content visible to at least a subset of users; BYO AI content is private to the user → not UGC. UGC apps need terms, reporting, blocking. [UGC policy](https://support.google.com/googleplay/android-developer/answer/9876937) | Not in scope, but adding a one-time Terms/acceptable-use acceptance is cheap and covers the AI-content best-practice asks. |
| K | **Impersonation / "not affiliated"** | **PASS** | Apps must not mislead users about a connection to another company/app. [Impersonation policy](https://support.google.com/googleplay/android-developer/answer/9888374) | Listing already states "Not affiliated with OpenAI, Google, or any AI company" and the in-app disclaimer (privacy-and-disclaimer pattern) covers it. Keep the OpenAI brand off the icon/name; don't imply official status. |
| L | **Content rating (IARC)** | **RISK → answer honestly** | Every app needs an IARC content rating from the Play Console questionnaire; misrepresentation → removal. Ads must not exceed the rating. [Content Ratings policy](https://support.google.com/googleplay/android-developer/answer/9898843) | Complete the questionnaire truthfully for a chat + AI image/video-generation app with no built-in filters. Expect **Teen or Mature** depending on answers (chat UGC + generative media). Re-run the questionnaire if features change. |
| M | **Families / target audience** | **PASS** | Families policy applies only if targeting children. [Families](https://support.google.com/googleplay/android-developer/topic/9877766) · [Families policy](https://support.google.com/googleplay/android-developer/answer/9893335) | Answer "not designed for children" in Target audience and content. BYOK (paid API key) + no kids marketing = fine. Do not claim the Families badge. |
| N | **2026–2027 changes** | **PASS / monitor** | (1) Target API 36 from Aug 31, 2026 (done). (2) July 15, 2026: Age-Restricted Content extended to anonymous/random chat apps (BYO AI is a private local client — not anonymous/random chat; verify the app has no stranger-matching features — it doesn't). (3) User Data requirements explicitly extended to third-party AI integrations — you stay responsible for limited use/disclosure/consent of the AI APIs you point at. (4) Apps must be registered for Android developer verification (automatic for new apps). (5) New Play Policy Insights open-source skill for IDE/CLI checks. [July 2026 policy announcement](https://support.google.com/googleplay/android-developer/answer/17134731) · [April 2026 policy announcement](https://support.google.com/googleplay/android-developer/answer/16926792) · [Policy deadlines table](https://support.google.com/googleplay/android-developer/table/12921780) | Re-check `developer.android.com/distribute/play-policies` each quarter before updates. |
| O | **Permissions / FGS / overlay** | **RISK (minor)** | FGS + `SYSTEM_ALERT_WINDOW`: Android 15+ restricts starting foreground services from background while holding overlay permission; specialUse FGS must be declared/justified. Ads must not run via overlays. [Android 15 behavior changes](https://developer.android.com/about/versions/15/behavior-changes-15) · [Ads policy](https://support.google.com/googleplay/android-developer/answer/9857753) | Overlay chat service is started from foreground UI — OK. Declare the specialUse FGS purpose in Play Console; keep overlay ad-free; test overlay on an Android 15/16 device before release. |

---

## 2. Exact Data Safety form answers (checklist for Play Console → App content → Data safety)

Privacy policy must be live FIRST (fix §3.1). Then:

**2.1 "Does your app collect or share any of the required user data types?" → YES**

**2.2 Data types to select:**

| Data type | Collected? | Shared? | Ephemeral? | Required or optional | Purpose |
|---|---|---|---|---|---|
| **Messages → Other in-app messages** (chat prompts + AI replies, incl. `/imagine`/`/video` prompts) | **Yes** — transmitted off-device to the API server the user configured | **No** — user-initiated action with reasonable expectation (the API the user chose), plus in-app + privacy-policy disclosure | **No** — providers may retain (e.g., OpenAI default retention); do NOT claim ephemeral | **Users can choose** (they decide what to send) | **App functionality** |
| **Device or other IDs** (Android advertising ID, via AdMob SDK) | **Yes** | **Yes** (AdMob ad networks) | No | **Users can choose** (advertising ID can be reset/opted out via Google settings) | **Advertising or marketing** |
| **Approximate location** (only if AdMob serves location-based/personalized ads; IP-derived) | **Yes** (recommend declaring) | **Yes** | — | Users can choose | **Advertising or marketing** |

**Do NOT select:** Name, Email, User IDs, Photos/Videos, Files, App activity (no analytics), Crash logs, Diagnostics, Contacts, Location (precise), Payments/Purchase history (Play Billing data is handled by Google, and you store nothing). API key: stored on-device only (EncryptedSharedPreferences via Keystore — `SecureStore.kt`) → on-device processing is **not** in scope; it is sent only to the user's chosen API as part of the same user-initiated request covered above.

**2.3 Data handling questions:**
- **Encrypted in transit:** **Yes** (all traffic HTTPS/TLS).
- **Data deletion mechanism:** **Yes** — user can delete chat history in-app / clear app data / uninstall; note in the privacy policy that content sent to the user-chosen API is governed by that provider's retention and is outside the developer's control.
- **Account deletion section:** **No account creation** → account-deletion requirement N/A (still answer the form's questions as "no accounts").

**2.4 Security practices:** Encryption in transit: yes. (Independent security review / MASA: optional, skip for a $0 launch.)

**2.5 Target audience and content:** Not designed for children → Families policy N/A, no Families badge.

---

## 3. Pre-submission fixes (in order of criticality)

1. **[BLOCKER] Publish a live privacy policy URL.** Enable GitHub Pages on `flamingspade1995-coder/litechat-android` (or host `privacy.html` anywhere HTTPS). Verify `curl -I` returns **200** on the exact URL you enter in Play Console. Also ensure the in-app Settings link uses the same live URL.
2. **[BLOCKER] In-app AI-content reporting/flagging.** Long-press on any message (text / generated image / video) → "Report" → in-app reason picker → submits to the developer (mailto: to the account email or a GitHub issue URL). Policy: [AI-Generated Content](https://support.google.com/googleplay/android-developer/answer/13985936). Do not route the user out of the app to report.
3. **Replace AdMob sample IDs.** New AdMob app → real app ID (`ca-app-pub-XXXXXXXX~YYYYYYYY`) into `app/src/play/AndroidManifest.xml`; real banner unit ID into `defaultConfig.buildConfigField ADMOB_BANNER_ID` + `strings.xml banner_ad_unit_id`. Keep all three in sync; test with real IDs on a device. (Sample IDs currently: `ca-app-pub-3940256099942544/6300978111`, `~3347511713`.)
4. **EEA/UK consent (UMP) or non-personalized ads.** Integrate Google's UMP SDK consent flow (or serve non-personalized ads only) before launch — required since Jan 2024 for personalized ads to EEA/UK users and part of GDPR/DMA hygiene. [UMP setup](https://developers.google.com/admob/android/privacy) · [Google consent requirements](https://support.google.com/admob/answer/13554116).
5. **Terms-of-use / acceptable-use acceptance** on first launch: prohibited content (child exploitation, sexual, violent, deceptive, illegal), BYOK disclosure ("content goes to the API you configured; provider safety filters apply; not affiliated with OpenAI"), privacy link. Covers UGC-policy hygiene + AI-content best practices ([16353813](https://support.google.com/googleplay/android-developer/answer/16353813)).
6. **Privacy policy text refresh:** add "prompts are transmitted to the third-party API you configure and are subject to that provider's privacy/retention terms"; keep AdMob disclosure (already present in `privacy.html`); keep "we collect nothing ourselves".
7. **Data Safety form:** fill exactly per §2.
8. **Content rating questionnaire:** complete honestly; expect Teen/Mature; re-run if features change.
9. **FGS declaration:** declare the `specialUse` foreground service (chat overlay) with justification in Play Console; verify overlay + FGS on Android 15/16 (targetSdk 36) — FGS start restrictions apply while holding SYSTEM_ALERT_WINDOW ([Android 15 behavior changes](https://developer.android.com/about/versions/15/behavior-changes-15)).
10. **Release hygiene:** confirm no debug-only Pro unlock in release builds (repo gates it to debug builds, C-003); versionCode 1; AAB from `assemblePlayRelease` (applicationId `com.byoai.chat`); Play App Signing enrolled.

---

## 4. Step-by-step submission checklist (in order)

1. **Before paying:** complete §3 items 1–6 (both blockers + AdMob + consent + terms + privacy text).
2. **Create developer account** — [play.google.com/console](https://play.google.com/console), **$25 one-time** fee; verify identity + phone; set up a **payments profile** (merchant/tax/bank info — required to sell `BYO_pro` and receive payouts; W-8/W-9 as applicable). [Manage your developer account](https://support.google.com/googleplay/android-developer/topic/16285)
3. **Create app** — name **BYO AI**, app type "App", category (Productivity or Tools). Search the exact title "BYO AI" in Play Console first (web search ≠ Play search).
4. **App content page** (in order): **Privacy policy URL** (live 200) → **Ads declaration: Yes** → **Data safety form** (§2) → **Target audience and content** (not children) → **News apps: No** → **Account deletion** (no accounts) → **Content rating questionnaire** (IARC).
5. **Monetize with Play** → In-app products → create managed product **`BYO_pro`**, one-time, **$4.99** — exact id must match `BuildConfig.PLAY_PRO_SKU` ([Create an in-app product](https://support.google.com/googleplay/android-developer/answer/1153481)).
6. **Main store listing** — title BYO AI (≤30), short desc (≤80), long desc (per PLAY-LISTING-DRAFT.md incl. "Not affiliated…"), 4 real screenshots (real UI only; no AI-label needed), icon + feature graphic, developer contact email, privacy URL.
7. **Upload release** — signed **AAB** (play flavor, release build, targetSdk 36), versionCode 1, enroll **Play App Signing**; rollout to production (or staged: closed test → production; recommend a short closed/internal test first with real AdMob IDs and billing product).
8. **Review** — submit; monitor Policy status / emails; typical first review 1–7 days; keep extension window (Nov 1, 2026) in mind for any target-SDK surprises.
9. **Post-launch** — keep quarterly policy checks ([play-policies](https://developer.android.com/distribute/play-policies)); re-run the rating questionnaire if features change; keep AdMob + billing + privacy text in sync with the app.

---

## 5. What would get us REJECTED (top rejection drivers)

1. **Dead privacy policy URL** — current state (404). Immediate rejection/issue on App content.
2. **No in-app report/flag for AI content** — AI-Generated Content policy violation (chat + image + video generation all in scope).
3. **Data Safety form mismatch** — e.g., "No data collected" while AdMob collects the advertising ID, or prompts declared ephemeral when providers retain them. Enforcement action, not just rejection.
4. **Sample AdMob IDs in the production build** — ads never fill; reviewers see a broken ad experience; the sample app ID can cause manifest/serving issues.
5. **Personalized ads without EEA/UK consent** — GDPR/DMA exposure; Google requires a certified CMP.
6. **Misleading listing / affiliation** — implying OpenAI or Google endorsement (impersonation policy). The disclaimer prevents this — keep it.
7. **Missing/incorrect IARC content rating** — unrated apps are not allowed on Play (July 2026 clarification).
8. **In-app price ≠ Play price** for `BYO_pro` — Payments policy violation.
9. **Debug-only unlocks / test artifacts in release** — "misleading behavior"/functionality issues if Pro is granted for free in release builds.

---

## 6. Evidence URL index (all verified live 2026-08-15)

**Policies:**
- AI-Generated Content: https://support.google.com/googleplay/android-developer/answer/13985936
- AI safeguards best practices: https://support.google.com/googleplay/android-developer/answer/16353813
- Declaring AI-generated content (store assets): https://support.google.com/googleplay/android-developer/answer/17262077
- Data Safety form: https://support.google.com/googleplay/android-developer/answer/10787469
- User Data policy: https://support.google.com/googleplay/android-developer/answer/10144311
- Account deletion: https://support.google.com/googleplay/android-developer/answer/13327111
- Ads policy: https://support.google.com/googleplay/android-developer/answer/9857753
- Payments policy: https://support.google.com/googleplay/android-developer/answer/9858738 · Understanding: https://support.google.com/googleplay/android-developer/answer/10281818
- In-app products: https://support.google.com/googleplay/android-developer/answer/1153481 · One-time products: https://support.google.com/googleplay/android-developer/answer/16430488
- UGC policy: https://support.google.com/googleplay/android-developer/answer/9876937
- Content Ratings: https://support.google.com/googleplay/android-developer/answer/9898843
- Inappropriate Content (sexual/profanity): https://support.google.com/googleplay/android-developer/answer/9878810
- Child exploitation: https://support.google.com/googleplay/android-developer/answer/9878809 · Deceptive behavior: https://support.google.com/googleplay/android-developer/answer/9888077
- Impersonation: https://support.google.com/googleplay/android-developer/answer/9888374
- Restricted Content topic: https://support.google.com/googleplay/android-developer/topic/9877466
- Families policy: https://support.google.com/googleplay/android-developer/answer/9893335

**Requirements & deadlines:**
- Target API level: https://developer.android.com/google/play/requirements/target-sdk
- July 2026 policy announcement: https://support.google.com/googleplay/android-developer/answer/17134731
- April 2026 policy announcement: https://support.google.com/googleplay/android-developer/answer/16926792
- Policy deadlines table: https://support.google.com/googleplay/android-developer/table/12921780
- Google Play Policies hub (updates): https://developer.android.com/distribute/play-policies
- Android 15 behavior changes (FGS/overlay): https://developer.android.com/about/versions/15/behavior-changes-15

**Ads/consent:**
- AdMob UMP SDK: https://developers.google.com/admob/android/privacy
- Google consent requirements (EEA/UK): https://support.google.com/admob/answer/13554116

**Repo evidence:** `app/build.gradle.kts` (sample AdMob IDs, SKU `BYO_pro`, minSdk 26/targetSdk 36, flavors) · `app/src/play/AndroidManifest.xml` (sample app ID) · `app/src/main/AndroidManifest.xml` (permissions) · `BillingRepository.kt` (INAPP, acknowledge, restore) · `ChatViewModel.kt` (/imagine, /video) · `SecureStore.kt` + `NamedKeyStore.kt` (EncryptedSharedPreferences) · `privacy.html` (policy text; hosted URL 404) · `PLAY-LISTING-DRAFT.md` + `LAUNCH-PACK.md` (listing copy, $25 flag, real-AdMob-IDs flag).

*Next step for the human: fix §3 items 1–2 (privacy URL + in-app reporting), then §3.3–3.5. After that, paying the $25 is safe.*
