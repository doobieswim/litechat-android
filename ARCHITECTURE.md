# LiteChat — Architecture (4GB-phone first)

## Product (HenWorks-style packaging, not Opclaw runtime)

| Opclaw (HenWorks) | This app (LiteChat) |
|---|---|
| Bundles Node + agent platform | **No local runtime** |
| Terminal + gateway + ~200MB | Chat client only, **~2–5 MB APK** |
| First-run extraction of deps | **Instant first launch** |
| Heavy RAM (agent + Node) | **Tens of MB RSS** idle/chat |
| Ads + one-time Pro | Same monetization model |
| BYOK / subscriptions | BYOK OpenAI-compatible only |
| End user needs no Termux | Same |

“Packaged the HenWorks way” here means: **Play Store one-tap install, guided onboarding, ads + Pro IAP, BYOK, no Termux** — **not** shipping a Linux/Node environment inside the APK.

## Why native Kotlin + Compose (not WebView / RN / Flutter / bundled Node)

| Approach | Typical APK | RAM on weak phones | Verdict |
|---|---|---|---|
| Opclaw-style Node bundle | 50–200+ MB | High, OOMs on 4GB | Wrong product |
| React Native / Flutter | 15–40+ MB | Medium–high | Avoid |
| WebView + local HTML | 1–3 MB shell + Chromium cost | WebView often 100–300 MB | Risky on 4GB |
| **Kotlin + Compose + OkHttp** | **~2–5 MB** (maid-native ~1.7 MB) | **Low** | **Chosen** |

Reference: [maid-native](https://github.com/HatsyRei/maid-native) proves OpenAI-compatible streaming chat can ship as a **~1.7 MB** signed arm64 APK with native UI.

## Stack (minimal)

- **Language / UI:** Kotlin, Jetpack Compose, Material 3
- **HTTP / SSE:** OkHttp only (no Retrofit, no Ktor engine bloat)
- **JSON:** kotlinx.serialization (R8-friendly)
- **Prefs:** DataStore Preferences (base URL, model, UI)
- **Secrets:** `EncryptedSharedPreferences` (API key)
- **History:** Room (SQLite) — conversations + messages; prune old blobs
- **Ads:** Google Mobile Ads (banner) — gated behind `!isPro`
- **IAP:** Play Billing Library 7.x — one-time **Pro** unlock (remove ads)
- **minSdk:** 26 (Android 8); **targetSdk:** 35; **abi:** `arm64-v8a` only for release Play/AAB splits
- **No:** Termux, proot, Node, Python, local GGUF, WebView chat shell, heavy markdown engines

## Memory rules (4GB devices)

1. Stream tokens into one `StringBuilder`; update UI with **conflated** Flow (don’t recompose every byte).
2. Cap visible history window (e.g. load last N messages; page older on scroll).
3. Bound markdown / plain-text rendering; prefer plain text + light code blocks v1.
4. Single OkHttpClient singleton; cancel call on leave chat / Stop.
5. No image generation, no voice, no background agent services in v1.
6. R8 full mode + resource shrinking; release `arm64-v8a` only (or Play AAB splits).
7. Banner ads only (no interstitial on every send — bad UX + RAM spikes).

## API surface

OpenAI-compatible `POST {baseUrl}/chat/completions` with:

- `Authorization: Bearer <key>`
- `stream: true` → SSE `data: {...}` until `[DONE]`
- Fields: `model`, `messages[]`, optional `temperature`, `max_tokens`

Works with: OpenAI, OpenRouter, Groq, Together, Fireworks, local **Ollama** (`http://127.0.0.1:11434/v1` or LAN IP), LM Studio, llama.cpp server, etc.

## App flows

1. **First launch** → Onboarding: paste API key, base URL preset, model id → Save → Chat
2. **Chat** → drawer of conversations, streaming assistant bubble, Stop, New chat
3. **Settings** → key, base URL, model, temperature, clear history, restore purchases
4. **Free** → small bottom banner; **Pro** (one-time) → no ads + optional future perks

## Security / privacy

- API key never leaves device except as `Authorization` to **user-chosen** base URL
- No LiteChat cloud account
- No analytics required for core; if added later, opt-in only
- Clear “unofficial / BYOK” Play listing copy (HenWorks/Opclaw style disclaimer pattern)

## What we deliberately do not build (v1)

- Full Hermes/OpenClaw agent on device
- Tool calling / skills / terminal
- On-device LLM weights
- Multi-modal attachments (optional later, memory-costly)
- Sync across devices

## Build environment note

This Hermes host has **no Android SDK/JDK** and ~1 GB RAM — unsuitable for Gradle assemble. Source is complete under this repo; build on a dev machine or GitHub Actions (see README).
