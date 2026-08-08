# Research notes — HenWorks packaging & low-RAM chat Android

Last researched: 2026-08-08. Sources: Play listings, henworks.com, openclaw-android, maid-native SPEC, ProAndroidDev streaming guide.

## 1. Who is HenWorks?

Play developer: **Hen Works** (WANG, HSING-KUO / Taiwan). Site: [henworks.com](https://henworks.com).

Public Android products (as of research date):

| App | Package | Role | Scale (approx.) |
|-----|---------|------|-----------------|
| **Hermes Agent - Android** | `com.hermesagent.android` | On-device Hermes Agent runtime + terminal | 10K+ installs, ~2.8K reviews, ~4.5–4.7★ |
| **Opclaw - OpenClaw on Android** | `com.opclaw.android` | On-device OpenClaw runtime + gateway + terminal | 1K+ installs, ads + Pro |

Also ships **TextLen** (Mac OCR) — different product line.

## 2. The “HenWorks way” (product pattern)

Both Android titles share the same commercial + packaging pattern:

1. **Play Store one-tap install** — end user never opens F-Droid Termux manually  
2. **BYOK / multi-provider** — no HenWorks cloud account; user pastes keys or uses supported sign-ins  
3. **Ads + one-time Pro** — “Hermes Pro / Opclaw Pro” removes ads permanently  
4. **Guided environment bootstrap** — first launch downloads/installs runtime (~**200 MB** claimed)  
5. **In-app install / update / repair** — self-check, rollback on failed update (Hermes 3.0.3 changelog)  
6. **Built-in terminal** — bash/Python/git style agent workspace  
7. **Local gateway + web UI** (Opclaw explicit; Hermes has dashboard)  
8. **Unofficial disclaimer** — independent client, not endorsed by upstream  
9. **Phone / tablet / Android TV** UI  
10. **Privacy posture on Play** — Hermes listing declares minimal/no data collection (verify before copying)

This is **runtime packaging of open-source agent frameworks**, not a thin ChatGPT clone.

### Hermes Agent Android (Play copy — feature set)

- Multi-model: OpenAI, Anthropic, Google, OpenRouter, local via **LiteRT**  
- Terminal + code execution on device  
- Gateways: Telegram, Slack, Discord  
- Web search/extract, Fal image gen, Edge TTS  
- Memory + multi-session  
- Pro = remove ads  
- Storage: ~200 MB initial setup  

### Opclaw (Play + henworks.com)

- OpenClaw agent platform on device  
- Local gateway + modern web chat UI  
- Dev tools: jq, ripgrep, gh, tmux…  
- Node runtime upgrades called out in changelogs  
- Same ads + one-time Pro model  
- Source pointer historically: community `openclaw-android` lineage (e.g. wangjazz fork / AidanPark upstream)  

### OpenClaw-on-Android tech reality (community)

- Avoids full **proot Ubuntu** (~700MB–1GB) by shipping a slimmer native/glibc/Node path  
- Still: **Node + agent package + optional tools** — hundreds of MB, multi-minute first install  
- Reviews complain when install stalls — bootstrap reliability is the hard product problem  
- Wrong shape for “simple chat on 4GB phones”

## 3. What “packaged like HenWorks” should mean for *this* product

| If you want… | Do this |
|--------------|---------|
| HenWorks **business + UX shell** | Ads, Pro IAP, BYOK onboarding, Play listing disclaimer, no Termux for user |
| HenWorks **agent runtime** | Bundle Node/Python/agent — **conflicts** with 4GB / small APK / instant launch |

**LiteChat decision:** copy the **shell**, not the **runtime**.

Product gap vs HenWorks agents: we intentionally do **not** ship terminal, gateway, skills, or MCP. That is a different SKU (and competes with their Hermes/Opclaw apps).

## 4. Thin BYOK chat — market & size benchmarks

| Approach | Typical download | Notes |
|----------|------------------|--------|
| maid-native (Kotlin Compose, OpenAI-compatible) | **~1.7 MB** signed arm64 | Gold standard for thin client; incremental markdown streaming |
| Maid (RN / historical Flutter) | ~20–30+ MB | Why they ported native |
| WebView wrapper of chat.openai.com | tiny shell, **huge WebView RAM** | Bad on 4GB |
| Flutter/RN chat clients | 15–40 MB | Avoid for low-end |
| On-device GGUF apps (SmolChat, PocketPal, MLC) | model GBs | Different product |
| HenWorks Hermes/Opclaw | ~200 MB env | Agent runtime |

## 5. Streaming architecture (production consensus)

From ProAndroidDev + maid-native SPEC:

```
Compose UI  ← StateFlow ← ViewModel ← Flow(SSE) ← OkHttp (IO)
```

Critical rules:

1. **OkHttp SSE**, not Retrofit one-shot JSON  
2. `.flowOn(Dispatchers.IO)`; cancel on Stop / leave screen  
3. **Conflate** UI updates — don’t recompose every byte  
4. Pre-create assistant message row; append to one `StringBuilder`  
5. Markdown: never O(n²) full re-parse per token (maid-native fixed with incremental `StreamingMarkdownState`)  
6. API key in **EncryptedSharedPreferences** / Keystore — never logs  

LiteChat already follows 1–4 and 6; markdown left plain-text for v1 size/RAM.

## 6. Monetization & ads on low-end devices

- **Banner only** (HenWorks pattern). Avoid interstitial-on-send.  
- AdMob banners can hurt **Android Vitals / jank / OOM** on weak devices — keep single banner, destroy when Pro, don’t stack multiple AdViews.  
- One-time **INAPP** Pro SKU; restore purchases; acknowledge purchase.  
- Ship a **FOSS / no-ads flavor** later if F-Droid matters (SDAI pattern: `full` vs `foss`).

## 7. Competitive positioning for LiteChat

| Competitor | Positioning |
|------------|-------------|
| HenWorks Hermes / Opclaw | Full agent + terminal — heavy |
| Official ChatGPT app | Account-locked, not BYOK multi-endpoint |
| gpt_mobile / ChatAir / maid-native | BYOK multi-provider clients — closest peers |
| Local LLM apps | Offline weights — RAM/storage heavy |

**LiteChat niche:** lightest possible BYOK OpenAI-compatible chat + ads/Pro Play packaging, 4GB-first.

## 8. Implications for our codebase

Already aligned:

- Native Compose + OkHttp SSE + Room + encrypted key + ads + Pro stubs  
- No Node/Termux/WebView shell  
- arm64 + R8 + largeHeap=false  

Done from research:

- **Compat matrix UX** (ReOldAi pattern) — free-RAM bands + live highlight in onboarding/Settings  
- **numAi-plus stream → non-stream fallback** in `OpenAiCompatibleClient`

Research-driven backlog (not done):

1. **Product flavors:** `play` (ads+billing) vs `foss` (no GMS)  
2. **Lazy-init MobileAds** only when `!isPro` (cut free-tier RAM a bit more)  
3. Optional **incremental markdown** only if needed (adds ~deps; maid paid ~0.4 MB for renderer bump)  
4. **GET /models** picker (maid-native parity)  
5. Play assets: privacy HTML, unofficial disclaimer, Pro SKU live in closed testing  
6. Do **not** market as Hermes/OpenClaw — different product; avoid trademark confusion  
7. Optional **per-baseUrl stream-broken cache** (numAi-plus)  

## 9. Verification status on this host

- Full `assembleRelease` **blocked**: no JDK / Android SDK; ~1 GB RAM VPS  
- Static structure checks: see session verification script (files, packages, SSE, billing, no heavy stacks)  
- Real proof = GitHub Actions or local Android Studio build + 4GB device RSS  

## 10. Lost-repo dig (2026-08-08)

Cloned: `numAi`, `numAi-plus`, `ReOldAi` under `/opt/data/workspace/`.

| Source | Steal |
|--------|--------|
| numAi | zero-deps discipline, HttpURLConnection SSE, 12s/40s timeouts, UA string |
| **numAi-plus** | **stream AUTO + non-stream fallback** → ported into `OpenAiCompatibleClient.streamChat` |
| ReOldAi | Conscrypt for legacy TLS; honest compat matrix UX |

Details: `docs/DIG-FINDINGS.md`, `docs/LOST-REPOS.md`.
