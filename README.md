# BYO AI

**Internal codename:** LiteChat (repo / package / agent words). Users see **BYO AI**.

**Chat with your own key. Works on 4GB phones. No monthly bill.**

Thin BYOK AI chat for Android — OpenAI-compatible APIs, ChatGPT-style UI, free with one small banner or pay once to remove it. Built for **4GB RAM** phones (and smaller).

HenWorks/Opclaw-style **product packaging** (Play one-tap, guided setup, BYOK, ads + Pro, no Termux for users) — **not** an Opclaw-style bundled Node/agent runtime.

| Goal | How |
|------|-----|
| Low RAM | Native Kotlin + Compose, single OkHttp client, no WebView chat shell |
| Small APK | R8 + resource shrink, `arm64-v8a` — foss release measures **1.6 MB**, play **3.2 MB** (CI, 2026-08-15) |
| Fast first launch | No extraction of runtimes — install and chat |
| BYOK | Encrypted API key → user-chosen base URL only |
| Monetization | AdMob banner + Play Billing one-time Pro |

See [ARCHITECTURE.md](./ARCHITECTURE.md) for the full design rationale.

## Features (v1)

**Chat**

- Streaming responses (`/v1/chat/completions` SSE) with automatic non-stream fallback on flaky providers
- Provider failover chain — if one provider fails, the next in your list is tried
- Per-conversation model memory; "Test connection" button in Settings
- Context trimmer keeps long chats inside the model's window (shows "earlier messages truncated")
- Stop cancels the stream for real

**Your key, your rules**

- Presets: OpenAI, OpenRouter, Groq, Ollama (local or LAN PC), custom base URL
- Save multiple named keys per provider and switch with one tap
- API key encrypted on the device; nothing is proxied through a vendor cloud

**Tools**

- `/imagine <prompt>` — image generation with your own key
- `/video <prompt>` — video generation (streamed to disk — RAM-safe)
- `/browse <url>` — fetch a page and let the model read it *(Pro)*
- Attach photos for vision models *(Pro)*
- Voice input using the built-in speech recognizer
- Prompt templates with `[variables]` (free: 1 built-in, Pro: unlimited)

**Your data**

- Conversations, per-conversation models, and **forks** (branch from any message)
- Chat backup/restore via the system file picker *(Pro)*
- Settings export/import (JSON — keys never leave the device)
- User memory — facts you repeat ("I prefer short answers") are remembered *(Pro)*

**Honesty**

- Free with one small banner; **pay once** to remove it — no monthly bill, ever
- Honest free-RAM compatibility matrix on first launch: if your phone is tight, it says so plainly
- In-app content reporting + acceptable-use screen (required for AI apps on Play)

## Disclaimer

BYO AI is an **unofficial, open-source client** for OpenAI-compatible APIs (OpenAI, OpenRouter, Groq, Ollama, and others). It is **not affiliated with, endorsed by, or connected to OpenAI, Google, Anthropic, or any AI provider.** You bring your own API key — BYO AI does not provide, proxy, or resell API access. All chat data travels directly between your device and the API server you configure.

[Privacy Policy](https://flamingspade1995-coder.github.io/litechat-android/privacy.html)

## Distribution

FOSS first, Play last. Full checklist and copy: [`docs/LAUNCH-PACK.md`](./docs/LAUNCH-PACK.md).

- **GitHub Releases + Obtanium** — signed `fossRelease` APK on the v1.0.0 tag
- **F-Droid** — build recipe audited; fdroiddata MR `metadata/com.byoai.chat.foss.yml`
- **XDA / 4PDA** — thread template: [`docs/DISTRIBUTION-XDA-TEMPLATE.md`](./docs/DISTRIBUTION-XDA-TEMPLATE.md)
- **r/androidafterlife** — weak-phone audience post
- **Play Store** — last; requires the $25 developer account (listing copy: [`docs/PLAY-LISTING-DRAFT.md`](./docs/PLAY-LISTING-DRAFT.md))

## Four-agent team (research + coding + proof + review)

| Doc | Who |
|-----|-----|
| **[HANDOFF.md](./HANDOFF.md)** | **Start here for the coding agent** |
| [docs/TEAM.md](./docs/TEAM.md) | Role split, file ownership, codewords |
| [docs/BACKLOG.md](./docs/BACKLOG.md) | Single ticket queue |
| [docs/QUESTIONS-FOR-RESEARCH.md](./docs/QUESTIONS-FOR-RESEARCH.md) | Coding → research blockers |
| [docs/QUESTIONS-FOR-HUMAN.md](./docs/QUESTIONS-FOR-HUMAN.md) | Product decisions |

```bash
python3 scripts/verify_static.py   # no Android SDK required
```

## Building — two flavors (C-002)

| Flavor | applicationId | Includes | Use for |
|--------|---------------|----------|---------|
| `play` | `com.byoai.chat` | AdMob banner + Play Billing (GMS) | Play Store / GMS devices |
| `foss` | `com.byoai.chat.foss` | **No** GMS/Play code at all (ads + billing stubbed) | Sideload, F-Droid-shaped builds, privacy |

```bash
./gradlew assemblePlayRelease   # com.byoai.chat
./gradlew assembleFossRelease   # com.byoai.chat.foss (side-by-side install)
./gradlew testPlayReleaseUnitTest testFossReleaseUnitTest
```

CI builds both on every push (static-verify first, then assemble, then a 20MB APK size gate, then artifact upload); tagging `v*` publishes a signed GitHub Release with both APKs (signing needs `KEYSTORE_FILE` / `KEYSTORE_PASSWORD` / `KEYSTORE_KEY_ALIAS` / `KEYSTORE_KEY_PASSWORD` secrets).

## Compatibility (free RAM bands)

Installed RAM is marketing. **Free RAM** is what `ActivityManager` reports. BYO AI is **Tier A** (thin UI + remote brain).

| Mode | &lt;1 GB free | 1–2 GB | 2–3.5 GB | ≥3.5 GB free |
|------|-------------|--------|----------|--------------|
| BYOK cloud / LAN chat (**this app**) | 🟩 | 🟩 | 🟩 | 🟩 |
| Banner ads (free tier) | 🟨 | 🟩 | 🟩 | 🟩 |
| LAN Ollama on PC (phone thin client) | 🟩 | 🟩 | 🟩 | 🟩 |
| Ollama *on this phone* | 🟥 | 🟨 | 🟨 | 🟨 |
| Full on-device agent (OpenClaw-class) | 🟥 | 🟥 | 🟨 | 🟨 |
| Local 7B+ LLM resident | 🟥 | 🟥 | 🟥 | 🟥 |

Onboarding step 1 highlights **your** column from live free RAM. Settings can show the matrix again.

## Build (dev machine / CI)

**Requirements:** JDK 17+, Android SDK 36, Android Studio Ladybug+ (or cmdline tools).

```bash
# Open folder in Android Studio, or:
./gradlew assembleDebug
./gradlew assembleRelease   # minified arm64 APK
```

Debug APK: `app/build/outputs/apk/debug/`  \
Release APK: `app/build/outputs/apk/release/`

GitHub Actions workflow: `.github/workflows/build.yml` builds both release flavors on push.

> The Hermes build host (4GB RAM, JDK 17, Android SDK 36) can build too — run flavors **one at a time** (parallel R8 passes OOM the box). RAM pre-flight: `free -m` before any build.

## Configure before Play release

1. **AdMob** — replace sample IDs in `defaultConfig` + `AndroidManifest` meta-data (play flavor only).
2. **Play Billing** — create managed product SKU `BYO_pro` (must match `PLAY_PRO_SKU` in code).
3. **Signing** — release keystore + `signingConfigs` (do not commit secrets).
4. **Privacy policy URL** — live before submission (key stored on device; sent only to user endpoint).

Already done: `applicationId` is `com.byoai.chat`; "Dev: mark Pro" is debug-only.

## Use

1. Install APK / run from Studio
2. Onboarding: paste API key, pick preset or custom base URL + model
3. Chat — Stop cancels the stream
4. Settings → Upgrade to Pro to hide ads

### Example endpoints

| Provider | Base URL | Model example |
|----------|----------|---------------|
| OpenAI | `https://api.openai.com/v1` | `gpt-4o-mini` |
| OpenRouter | `https://openrouter.ai/api/v1` | `openai/gpt-4o-mini` |
| Groq | `https://api.groq.com/openai/v1` | `llama-3.3-70b-versatile` |
| Ollama on phone | `http://127.0.0.1:11434/v1` | `llama3.2` |
| Ollama on LAN PC | `http://192.168.x.x:11434/v1` | your tag |

## Project layout

```
app/src/main/java/com/litechat/android/
  LiteChatApp.kt / MainActivity.kt
  data/
    api/OpenAiCompatibleClient.kt   # SSE streaming + fallback
    db/                             # Room conversations, forks, memory
    prefs/                          # DataStore + encrypted key + named keys
    context/                        # ContextTrimmer, MemoryManager
    billing/BillingRepository.kt    # one-time Pro
  ui/
    ChatViewModel.kt
    Screens.kt                      # chat, onboarding, settings
    CompatMatrix.kt                 # free-RAM matrix UI (ReOldAi pattern)
    OverlayService.kt               # floating chat bubble (Pro)
    theme/Theme.kt
  util/
    DeviceCompat.kt                 # ActivityManager bands + matrix data
    MediaCleanup.kt                 # generated-media disk caps
```

## Memory budget (targets)

| State | Target RSS (ballpark) |
|-------|------------------------|
| Cold idle | &lt; 80–120 MB |
| Active streaming | &lt; 150–200 MB |
| Avoid | WebView shells, RN, bundled Node (Opclaw-class) |

## Play Store checklist

- [ ] Privacy policy (no account; key on device; ads/billing SDKs disclosed)
- [ ] Data safety form (encrypted in transit to *user* API; AdMob may collect IDs)
- [ ] Unofficial / BYOK disclaimer (HenWorks Opclaw style)
- [ ] Content rating questionnaire
- [ ] Target API per Play requirements (targetSdk 36)
- [ ] AAB upload (Play generates ABI splits — can disable local ABI splits for AAB if needed)
- [ ] Pro SKU active in closed testing before production

## What this is not

- Not Hermes Agent / OpenClaw on device
- Not on-device GGUF inference
- Not a Termux wrapper

Those are different products. BYO AI is the **thin chat client** that belongs on a 4GB phone.

## License

MIT — use and ship freely. Replace branding for your store listing.
