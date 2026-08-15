# Launch pack — BYO AI (D-005)

**Date:** 2026-08-15
**Positioning locked:** H-001 name BYO AI · H-003 $4.99/SKU `BYO_pro` · H-004 promise "Built for a $30 phone" (Walmart Moto G 2025, $29.88, 4GB) · H-005 FOSS first · H-006 quiet underdog theme
**Theme law:** `docs/THEME-SHOW-DONT-TELL.md` — everyday words everywhere a user reads.
**Cost:** everything here is **$0** except the Play developer account (**$25**, flagged; not needed until step 7).

---

## Channel order (FOSS first, per H-005)

| # | Channel | Ready? | What's needed | Who |
|---|---------|--------|---------------|-----|
| 1 | **GitHub Releases + Obtanium** | metadata done | Build signed `fossRelease` APK, create v1.0.0 release on GitHub, attach APK | human (build on this box or CI artifact) |
| 2 | **F-Droid** | metadata refreshed (`fastlane/metadata/android/en-US/fdroid.yml`) | Submit build request via F-Droid repo MR (metadata + build recipe), wait for inclusion | human (F-Droid account) |
| 3 | **XDA thread** | template refreshed (`docs/DISTRIBUTION-XDA-TEMPLATE.md`) | Post thread, attach APK + source + screenshots | human |
| 4 | **4PDA thread** | covered in `docs/DISTRIBUTION-FOSS.md` | Post thread (RU forum, same template) | human |
| 5 | **r/androidafterlife post** | draft below | Post with real screenshots; answer comments | human |
| 6 | **awesome-byok-apps PR** | text below | Fork `yatsyk/awesome-byok-apps`, add entry, open PR | human |
| 7 | **Play Store** | listing copy + screenshot plan done (`docs/PLAY-LISTING-DRAFT.md`) | **Pay $25**, create app, search exact title "BYO AI" in Play Console, upload AAB, Data Safety form, privacy URL, create `BYO_pro` managed product | human |

---

## r/androidafterlife post (draft)

Sub is about running modern software on old Android — this fits exactly. Post as the dev, be honest about it, answer every comment.

**Title:** I made an AI chat app that runs on a $30 phone

**Body:**

> I've been keeping an old phone alive for years, and I got tired of every AI chat app being a 50 MB monster that needs a flagship. So I built one that doesn't.
>
> It's called BYO AI. It uses your own API key (OpenAI, OpenRouter, Groq, Ollama — anything OpenAI-compatible), so there's no account, no subscription, and nothing is sent anywhere except straight from your phone to the API you picked.
>
> The phone in mind: the Moto G 2025 that Walmart sells for $29.88. 4GB of RAM. It runs fine there. If your phone is low on memory, the app tells you plainly instead of pretending — it shows an honest compatibility table.
>
> - ~2 MB APK, native Kotlin + Compose
> - Free with one small banner, or $4.99 once to remove it (no monthly bill, ever)
> - Open source, MIT, FOSS flavor with no Google services
> - Android 8.0+
>
> Downloads: [GitHub Releases](https://github.com/flamingspade1995-coder/litechat-android/releases) · [F-Droid](https://f-droid.org/) (submitted)
>
> I tested it on [insert real phone + RAM number here]. Happy to answer questions.

**Before posting:** fill the real-phone line with the actual device you ran it on (honesty law — never fake a test).

---

## awesome-byok-apps PR text

Fork [yatsyk/awesome-byok-apps](https://github.com/yatsyk/awesome-byok-apps), add to the Android list, open PR. Suggested entry:

```markdown
- [BYO AI](https://github.com/flamingspade1995-coder/litechat-android) — Android. Thin BYOK chat for OpenAI-compatible APIs. 4GB-friendly (~2MB APK), honest low-memory check, ads + one-time $4.99 Pro, FOSS flavor without Google services.
```

---

## Launch checklist (for the human, in order)

- [ ] Build signed FOSS APK (CI artifact or local) and publish v1.0.0 GitHub release
- [ ] Submit F-Droid build request (metadata already in repo)
- [ ] Post XDA thread (template refreshed)
- [ ] Post 4PDA thread
- [ ] Post r/androidafterlife story (draft above — fill real phone line first)
- [ ] Open awesome-byok-apps PR
- [ ] Take the 4 screenshots per plan, then pay the **$25** Play fee and submit the listing + AAB
- [ ] In Play Console: create managed product **`BYO_pro`** at **$4.99** (must match `PLAY_PRO_SKU` in code)
- [ ] Replace AdMob sample IDs with real ones before the Play build (flagged: AdMob is free)

## Don't

- Post anything with fight words, rival names, or "runs a real model on 4GB"
- Ship the play flavor to F-Droid (foss flavor only — AntiFeatures marked)
- Publish screenshots that aren't real
