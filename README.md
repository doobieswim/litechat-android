# BYO AI

**Internal codename:** LiteChat (repo / package / agent words). Users see **BYO AI**.

**Chat with your own key. Works on 4GB phones. No monthly bill.**

Thin BYOK AI chat for Android — OpenAI-compatible APIs, ChatGPT-style UI, ads + one-time Pro. Built for **4GB RAM** phones.

HenWorks/Opclaw-style **product packaging** (Play one-tap, guided setup, BYOK, ads + Pro, no Termux for users) — **not** an Opclaw-style bundled Node/agent runtime.

| Goal | How |
|------|-----|
| Low RAM | Native Kotlin + Compose, single OkHttp client, no WebView chat shell |
| Small APK | R8 + resource shrink, `arm64-v8a` only, no RN/Flutter/Node |
| Fast first launch | No extraction of runtimes — install and chat |
| BYOK | Encrypted API key → user-chosen base URL only |
| Monetization | AdMob banner + Play Billing one-time Pro |

See [ARCHITECTURE.md](./ARCHITECTURE.md) for the full design rationale.

## Disclaimer

BYO AI is an **unofficial, open-source client** for OpenAI-compatible APIs (OpenAI, OpenRouter, Groq, Ollama, and others). It is **not affiliated with, endorsed by, or connected to OpenAI, Google, Anthropic, or any AI provider.** You bring your own API key — BYO AI does not provide, proxy, or resell API access. All chat data travels directly between your device and the API server you configure.

[Privacy Policy](https://flamingspade1995-coder.github.io/litechat-android/privacy.html)

## Two-agent team (research + coding)

| Doc | Who |
|-----|-----|
| **[HANDOFF.md](./HANDOFF.md)** | **Start here for the coding agent** |
| [docs/TEAM.md](./docs/TEAM.md) | Role split, file ownership |
| [docs/BACKLOG.md](./docs/BACKLOG.md) | Single ticket queue |
| [docs/QUESTIONS-FOR-RESEARCH.md](./docs/QUESTIONS-FOR-RESEARCH.md) | Coding → research blockers |
| [docs/QUESTIONS-FOR-HUMAN.md](./docs/QUESTIONS-FOR-HUMAN.md) | Product decisions |

```bash
python3 scripts/verify_static.py   # no Android SDK required
```

## Building — two flavors (C-002)

| Flavor | Includes | Use for |
|--------|----------|---------|
| `play` | AdMob banner + Play Billing (GMS) | Play Store / GMS devices |
| `foss` | **No** GMS/Play code at all (ads + billing stubbed) | Sideload, F-Droid-shaped builds, privacy |

```bash
./gradlew assemblePlayRelease   # com.litechat.android
./gradlew assembleFossRelease   # com.litechat.android.foss (side-by-side install)
./gradlew testPlayReleaseUnitTest testFossReleaseUnitTest
```

CI builds both on every push; tagging `v*` publishes a signed GitHub Release with both APKs (signing needs `KEYSTORE_FILE` / `KEYSTORE_PASSWORD` / `KEYSTORE_KEY_ALIAS` / `KEYSTORE_KEY_PASSWORD` secrets).

## Features (v1)

- Streaming chat (`/v1/chat/completions` SSE) + **non-stream fallback** on flaky providers
- Presets: OpenAI, OpenRouter, Groq, Ollama local, Custom
- Conversation list (Room/SQLite)
- Encrypted API key storage
- Banner ads when not Pro
- One-time **Pro** unlock (remove ads)
- Dark Material 3 UI
- **Honest free-RAM compatibility matrix** on first launch (ReOldAi-style)

## Compatibility (free RAM bands)

Installed RAM is marketing. **Free RAM** is what `ActivityManager` reports. LiteChat is **Tier A** (thin UI + remote brain).

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

**Requirements:** JDK 17+, Android SDK 35, Android Studio Ladybug+ (or cmdline tools).

```bash
# Open folder in Android Studio, or:
gradle wrapper --gradle-version 8.11.1   # once, if wrapper jar missing
./gradlew assembleDebug
./gradlew assembleRelease   # minified arm64 APK
```

Debug APK: `app/build/outputs/apk/debug/`  
Release APK: `app/build/outputs/apk/release/`

GitHub Actions workflow: `.github/workflows/build.yml` builds release APK on push.

> This Hermes environment has **no Android SDK** and ~1 GB RAM — build on your laptop or CI.

## Configure before Play release

1. **Application id** — change `applicationId` in `app/build.gradle.kts` if desired.
2. **AdMob** — replace sample IDs in `defaultConfig` + `AndroidManifest` meta-data.
3. **Play Billing** — create managed product SKU `BYO_pro` (or change `PLAY_PRO_SKU`).
4. **Signing** — release keystore + `signingConfigs` (do not commit secrets).
5. **Privacy policy URL** — required for Play (key stored on device; sent only to user endpoint).
6. Remove or gate **Dev: mark Pro** button in `SettingsScreen` for production.

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
    api/OpenAiCompatibleClient.kt   # SSE streaming
    db/                             # Room conversations
    prefs/                          # DataStore + encrypted key
    billing/BillingRepository.kt    # one-time Pro
  ui/
    ChatViewModel.kt
    Screens.kt                      # chat, onboarding, settings
    CompatMatrix.kt                 # free-RAM matrix UI (ReOldAi pattern)
    theme/Theme.kt
  util/
    DeviceCompat.kt                 # ActivityManager bands + matrix data
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
- [ ] Target API per Play requirements
- [ ] AAB upload (Play generates ABI splits — can disable local ABI splits for AAB if needed)
- [ ] Pro SKU active in closed testing before production

## What this is not

- Not Hermes Agent / OpenClaw on device  
- Not on-device GGUF inference  
- Not a Termux wrapper  

Those are different products. LiteChat is the **thin chat client** that belongs on a 4GB phone.

## License

MIT — use and ship freely. Replace branding for your store listing.
