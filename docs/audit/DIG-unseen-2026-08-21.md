# DIG unseen audit — 2026-08-21

**Role:** `LITECHAT-DIG` (independent research lens)  
**Repo:** `/opt/data/workspace/byok-chat-android`  
**Method:** grep/read only. No Gradle. No `app/**` edits. Tickets stay **Research**.  
**Web budget:** 3 searches (OpenAI deprecations, Gemini models page, OpenRouter compare).  
**Theme law:** `docs/THEME-SHOW-DONT-TELL.md` (user-facing). This file is research; no fight words for listing.

Scope asked: picker ids vs docs, paid flags, Sora sunset copy, README vs catalog, theme-law user-facing hype, BACKLOG claims that are dead code.

---

## Verdict in one page

The **in-app picker** (`ProviderCatalog.kt`) is newer than the **user-facing docs**. Chat model ids for Gemini look current. Paid Grok/OpenAI/DeepSeek/Mistral warnings exist. Sora sunset is coded, not copy. The honesty holes are almost all **docs + cost labels**, not missing Kotlin doors.

| Area | Grade | One line |
|------|-------|----------|
| Picker chat ids | **Mostly current** | Gemini 3.6/3.7/3.1-pro-preview match Google’s models page. OpenAI `gpt-5.6-*` not re-verified on an official model URL this pass. |
| Docs vs catalog | **Fail** | README / F-Droid / XDA still list the **old 5 presets** (OpenAI, OpenRouter, Groq, Ollama, LM Studio). Live catalog is 10 names, Gemini first. |
| Paid flags | **Partial** | Host `paid=true` is right for xAI/OpenAI/DeepSeek/Mistral. OpenRouter + HF stay `paid=false` while pictures / some slugs can bill. Default DataStore model is **paid** `gpt-5.6-luna`. |
| Sora sunset | **Code yes, copy no** | `SORA_SUNSET_MS` = 2026-09-24 00:00 UTC. Official deprecation table has **no replacement**. User never sees a calendar line. `VIDEO-MONETIZATION.md` still hopes OpenAI will announce one. |
| Theme law (UI / Play draft) | **Pass** | `strings.xml`, Play listing draft, fastlane title/short line are everyday. XDA “why this over ChatGPT” is the only user-facing near-miss. |
| BACKLOG Done vs code | **Mostly wired now | 2026-08-15 dead-code set (overlay/memory/keys/forks) is **fixed**. Leftovers: unused `PRESETS`, never-built Pollinations, stale ARCHITECTURE + video-Pro sales doc. |

**New tickets (Research only):** R-021, R-022, R-023, R-024. Do not Ready.

---

## 1. Picker ids vs docs

### Live catalog (`ProviderCatalog.PROVIDERS`)

| Host | Chat picker ids | Image / video (not in dropdown) |
|------|-----------------|----------------------------------|
| Google Gemini | `gemini-3.6-flash`, `gemini-3.7-flash`, `gemini-3.1-pro-preview` | `gemini-3.1-flash-image` (+ fallbacks incl. dead-looking `gemini-2.5-flash-image`); video `veo-3.1-generate-preview` |
| Groq | `openai/gpt-oss-20b`, `openai/gpt-oss-120b`, `groq/compound` | refuse |
| OpenRouter | `openrouter/free`, `google/gemma-4-26b-a4b-it:free`, `openai/gpt-oss-20b:free` | pictures `openai/gpt-image-2`; **video refuse** |
| Hugging Face | `openai/gpt-oss-20b`, `Qwen/Qwen3-8B`, `openai/gpt-oss-120b` | refuse |
| Grok (xAI) | `grok-4.6`, `grok-4.5`, `grok-3-mini` | `grok-imagine-image-2.0` / `grok-imagine-video-1.5` |
| OpenAI | `gpt-5.6-luna`, `gpt-5.6-terra`, `gpt-5.6-sol` | `gpt-image-2` / `sora-2` until sunset |
| DeepSeek | `deepseek-v4-flash`, `deepseek-v4-pro` | refuse |
| Mistral | `*-latest` aliases | refuse |
| Ollama | `llama3.2`, `qwen3`, `gemma3` | refuse |
| Custom | type-it | treated like OpenAI for pictures/video |

### Grounding (3 searches, 2026-08-21)

1. **Sora** — [OpenAI deprecations](https://developers.openai.com/api/docs/deprecations): Videos API + `sora-2` / `sora-2-pro` **shutdown 2026-09-24**. Replacement column is `---`. Matches DIG-DOORS. Code constant `1_790_208_000_000` is that midnight UTC.
2. **Gemini chat** — [Gemini models](https://ai.google.dev/gemini-api/docs/models): `gemini-3.7-flash`, `gemini-3.6-flash`, `gemini-3.1-pro-preview` are the live 3-series endpoints. Picker order lists 3.6 first (“fast, free key”) then 3.7 (“newest”) — honest enough. 3.7 Flash page dated **2026-08-13**.
3. **OpenRouter Gemma** — compare page for `google/gemma-4-26b-a4b-it` (no `:free`) shows **$0.07 / $0.34 per M tokens**. DIG-DOORS already said the `:free` slug was **not** on the first screen of the $0 list. Label **“Gemma 4 — free”** is unproven and may bill.

**Not opened this pass (budget used):** official OpenAI page for `gpt-5.6-luna` / `terra` / `sol`; xAI `grok-4.6`; DeepSeek V4 ids. Treat those as **catalog-current, URL-unverified**. Do not claim they are fake.

### Docs that still describe a different picker

| File | What it says | Live code |
|------|----------------|-----------|
| `README.md` Features | Presets: OpenAI, OpenRouter, Groq, Ollama, custom | Catalog also has **Gemini, Hugging Face, Grok, DeepSeek, Mistral**. Gemini is first (free-key on-ramp). |
| `README.md` example table | OpenAI `gpt-5.6-luna`, OpenRouter `openrouter/free`, Groq `gpt-oss-20b`, Ollama | Same four. No Gemini row. |
| `fastlane/.../full_description.txt` | “OpenAI, OpenRouter, Groq, Ollama, **LM Studio**, or any OpenAI-compatible” | **No LM Studio preset.** Custom URL can hit it. Gemini missing. |
| `docs/DISTRIBUTION-XDA-TEMPLATE.md` | Same five-name list; `/browse` and attach with no Pro note | Catalog 10; `/browse` and attach **are** Pro in `ChatViewModel`. |
| `ARCHITECTURE.md` | Together, Fireworks, LM Studio, llama.cpp; “No image generation, no voice in v1” | Those hosts are Custom-only. `/imagine` and voice **shipped**. `targetSdk` text still **35**; gradle is 36 (not re-read gradle this pass beyond prior docs). APK “~2–5 MB” vs measured foss **1.6** / play **3.2**. |
| `SettingsRepository.PRESETS` | OpenAI / OpenRouter / Groq / Ollama / Custom, default model `gpt-5.6-luna` | **Zero call sites** except the list itself. Dead leftover of C-033. UI uses `ProviderCatalog`. |

C-033 BACKLOG row already lists the 10 catalog names. README/F-Droid/XDA were not swept when the picker grew.

**R-021.**

---

## 2. Paid flags and cost warnings

### What the UI actually shows

`ProviderSetupFields`: tagline in error color if `paid`; button “Get a key (can cost money)” vs “Get a free key”.

| Host | `paid` | Tagline / extra |
|------|--------|-----------------|
| Gemini | false | “Free key. Good at pictures too.” |
| Groq | false | “Free key.” Compound **model label** “can cost money” |
| OpenRouter | false | “Has free ones.” |
| Hugging Face | false | “Free key for many open models.” |
| xAI / OpenAI / DeepSeek | true | “This can cost money.” |
| Mistral | true | “Some plans cost money.” |
| Ollama / Custom | false | no money line |

Voice has a quiet line: “Voice uses your key — can cost money” (`Screens.kt`).

### Honesty gaps

1. **Default model is paid OpenAI.** `AppSettings.model` / DataStore fallback = `gpt-5.6-luna`. Free-key-first law says Gemini/Groq/OpenRouter lead. If DataStore is empty, the stored default is a billable id.
2. **OpenRouter `paid=false` + pictures.** `/imagine` uses `openai/gpt-image-2`. REVIEW already noted this. Host flag still says free. Pictures can bill with a “free” key that has credits, or 402 if it does not.
3. **Gemma 4 labeled free** — see §1. Possible surprise bill.
4. **Gemini pictures.** Tagline implies pictures come with the free key. Quota / paid image models exist; no second line on `/imagine`.
5. **`/video` has no money line.** Sora is ~$1 / 10s in `VIDEO-MONETIZATION.md` (2026-08-09, not re-priced today). xAI Imagine is paid (~$0.04/image on their page; video separate). User can tap `/video` on OpenAI with only the generic OpenAI paste warning from onboarding — maybe days earlier.
6. **Groq Compound** — host stays free (correct for a free Groq key). Model row warns. DIG-DOORS: built-in web search **$5 / 1000**. OK if WIRE kept the label (it did).
7. **Custom `paid=false`** — user-typed URL might be OpenAI. Acceptable: they typed it.
8. **Ollama “this phone or PC”** — no RAM warning. Compat matrix already paints on-phone Ollama red/yellow. Tagline does not. Theme law wants plain “this phone is low on memory,” not a scare poster. Small gap, not a fight-word issue.

Paid-provider tests exist (`ProviderCatalogTest` xAI + `free-key providers stay marked not paid` for gemini/groq/openrouter). The test **locks in** OpenRouter `paid=false`. Any honesty fix must update that test on purpose.

**R-022.**

---

## 3. Sora sunset copy

**Code (honest after the date):**

- `resolveVideoModel("openai"|"custom")` → `sora-2` only if `nowMs < SORA_SUNSET_MS`, else `null`.
- User then gets: “OpenAI cannot make videos. Switch to Google Gemini.”
- Tests: sunset−1 vs sunset.

**Copy (not honest before the date):**

- No onboarding / Settings / `/video` line that OpenAI video **ends 24 Sep 2026**.
- `cannotMakeVideosLine` never mentions Sora or the date.
- `docs/DIG-DOORS-LEFTOVERS-2026-08-21.md` correctly said “no Ready ticket — calendar, not a bug today.” Today is still 2026-08-21 (**34 days**). A user-facing calendar line is now a product honesty item, not a new API.
- `docs/VIDEO-MONETIZATION.md` (2026-08-09): “OpenAI will likely announce a Sora replacement before shutdown.” Official deprecation table replacement = **none**. That sentence is a guess presented as likely fact. Also wants `/video` **Pro-gated** with 1/week free — **never-gate list + live code both leave `/video` free**. Stale sales doc; do not paste into Play.

Do **not** invent a post-Sora OpenAI video id.

**R-023.**

---

## 4. README vs catalog (and store-label twins)

README Features that **match code** (spot-check, not a phone run):

- Streaming + non-stream fallback, failover, per-conversation model, trimmer, Stop
- Named keys (`NamedKeyStore` constructed in `AppContainer`, used on send)
- `/imagine` ungated; `/video` ungated; `/browse` Pro; attach Pro
- Voice (phone box + TTS slot)
- Templates free=1
- Forks (`forkConversation` + long-press)
- Chat backup Pro; settings JSON export (no secrets in encoder)
- Memory Pro (`getMemoryPrompt` only if Pro)
- Compat matrix; report/acceptable-use (C-032 Done)

README Features that **lag or overclaim**:

| Claim | Truth |
|-------|--------|
| Preset list of 4+custom | Catalog has 10; Gemini missing from README |
| Overlay | Wired + Pro-gated in Settings. **Not in README Features** (only in layout as OverlayService) |
| “Four-agent team” heading | TEAM.md is **five** (DEBUG added H-011) |
| Settings JSON “keys never leave the device” | Export **omits** keys (good). Phrase is true, easy to read as “the file has your keys but encrypted.” |

F-Droid `full_description.txt` extra problems:

- Lists `/browse` and attach with **no Pro**
- “No tracking” on the foss file is flavor-true; do not copy that sentence onto Play (AdMob).
- LM Studio named as if it were a preset.

Play listing draft (`docs/PLAY-LISTING-DRAFT.md`): everyday, no provider list, no fight words. **Best user-facing copy in the tree.** Do not “refresh” it from README.

**R-021** covers the sweep.

---

## 5. Theme-law user-facing hype

Banned list (fighter, underdog, champ, bout, ring, weigh-in, kick out, King’s Road, power user, vim of AI, for the people, still standing, named enemies, …) grepped in `app/src` kt/xml, `fastlane`, `strings.xml`, `privacy` not fully dumped.

| Surface | Result |
|---------|--------|
| `strings.xml` `app_name` | **BYO AI** |
| Fastlane title + 80-char line | Locked everyday line. Pass. |
| Play listing draft | Pass. Explicit do-not list. |
| In-app setup copy | “Pick who you talk to / Paste your key / can cost money.” Pass. |
| Pro errors | “pay once to unlock.” Hardware-store. Pass. |
| XDA template | “Why this over the official ChatGPT app?” — comparison, not a poster, but **names ChatGPT as the other guy**. Theme law: do not name ChatGPT/Agora as enemies on listing. XDA is user-facing. Soft fail. |
| `docs/COMPETITIVE-DIFFERENTIATION.md` | No BACKSTAGE stamp. “big dogs,” “power user,” “vim of AI,” **Markdown ✅** for LiteChat (C-008 reverted — plain `Text`). Internal, but copy-paste bait. |
| `docs/KINGS-ROAD-THEME.md` / shine / sales | Stamped backstage or “DO NOT USE IN LISTING.” Leave them. |

No WIRE ticket to sprinkle Honda metaphors. Only strip XDA rivalry line if that template is still the post people will publish (**fold into R-021**).

---

## 6. BACKLOG Done vs dead code (2026-08-21 re-probe)

Old 2026-08-15 audit said overlay / MemoryManager / NamedKeyStore / forks / settings JSON were dead. **That audit is stale.**

| Ticket | Probe | Now |
|--------|-------|-----|
| C-015 overlay | `onStartCommand` → `showOverlay()`; Settings switch Pro-gated | **WORKS (wired)** |
| C-020 memory | `AppContainer.memoryManager`; `send()` injects prompt if Pro | **WORKS** |
| C-023 named keys | `namedKeyStore.getActiveKey()` on send | **WORKS** |
| C-024 forks | `ChatRepository.forkConversation`; `onFork` | **WORKS** |
| C-022 settings JSON | `exportSettingsJson` / import, no secrets | **WORKS** |
| C-013 /browse Pro | `ChatViewModel` ~440 | **gated** |
| C-016 attach Pro | `attachImage` ~1192 | **gated** |
| C-014 backup Pro | export/import ~1114 / 1143 | **gated** |
| C-012 templates | `FREE_TEMPLATE_LIMIT = 1` | **gated** |
| P-002/P-005/P-006/P-009/P-011 | search / `/search` / `/recall` / folders / `/edit` | **gated** |
| `/imagine` `/video` | no `isPro` on those branches | **ungated** (matches never-gate) |
| C-032 report | Done in BACKLOG; not re-walked line-by-line this pass | trust WIRE+REVIEW unless DEBUG says else |
| C-008 markdown | Idea / reverted | README does not claim markdown. **COMPETITIVE-DIFFERENTIATION still does.** |
| C-011 Pollinations | `grep Pollinations` in `*.kt` = **0** | Optional AC never built. Ticket Done without the optional. Fine if docs do not advertise it. BACKLOG AC checkbox still listed. |
| `SettingsRepository.PRESETS` | only definition | **DEAD** leftover |
| VIDEO-MONETIZATION “Pro-gate video” | `/video` has no Pro check | Doc claims a product that was **not** built (and must not be, never-gate) |
| ARCHITECTURE “no imagine/voice v1” | both exist | **Stale architecture essay** |

Feature-audit recipe trap “Pro-gated in BACKLOG ≠ gated in code” is **no longer true** for browse/backup/overlay/attach/memory. Do not re-open those as dead.

**R-024** = docs/dead-list hygiene only (PRESETS comment or delete is WIRE after Approve; DIG does not touch `app/**`).

---

## 7. What not to build (this pass)

- New Sora model name
- OpenRouter video door (still not the same as video-input docs)
- Pollinations (optional, extra host, honesty cost)
- Pro-gating `/imagine` or `/video`
- Fight-word listing copy
- Flipping any ticket Ready

---

## Tickets (status = Research until `LITECHAT-PROOF`)

Added to `docs/BACKLOG.md`: **R-021** docs vs catalog · **R-022** cost flags · **R-023** Sora user line · **R-024** stale Done-docs / dead PRESETS.

Cost of the copy work: **$0**. Cost of getting the flags wrong: user API bill.

---

## Sources

- Code: `ProviderCatalog.kt`, `ProviderSetupFields.kt`, `SettingsRepository.kt` (default model + `PRESETS`), `ChatViewModel.kt` (gates), `Screens.kt` (overlay + voice line), `OpenAiCompatibleClient.kt` (Sora path names only)
- Tests: `ProviderCatalogTest.kt`
- Docs: README, ARCHITECTURE, PLAY-LISTING-DRAFT, fastlane en-US, XDA template, VIDEO-MONETIZATION, DIG-DOORS-LEFTOVERS, THEME-SHOW-DONT-TELL, BACKLOG, TEAM
- Web (3): OpenAI deprecations; Gemini models + 3.7 Flash; OpenRouter Gemma 4 compare
