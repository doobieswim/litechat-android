# LOST / UNKNOWN git repos — archaeology log

Date: 2026-08-08  
Purpose: surface obscure, low-visibility, or “scene” repos related to  
**weak-device AI chat**, BYOK clients, legacy Android, and agent shells —  
then map them to LiteChat / 4GB product decisions.

> “Lost” here means: low stars, buried in forums (4PDA/XDA/Telegram),  
> forks of forks, WIP with BUGS.md, or known only inside retro-Android circles.

---

## Tier S — True weak-RAM / legacy trenches (highest relevance)

### 1. [gohoski/numAi](https://github.com/gohoski/numAi) ★~30
| | |
|--|--|
| **What** | OpenAI-compatible AI client for **Android 1.0+** |
| **Why lost** | Lives in Retro Android / 4PDA / OldMarket / XDA, not Play AI charts |
| **Stack** | Extremely lean Java; NNJSON; no Compose; TLS 1.2 crisis on <2.3 |
| **Lessons** | Opera Mini / Palm pattern reincarnated: **remote brain only**; HTTPS proxy for ancient TLS; thinking vs chat model split; free API quotas |
| **TODO left** | Markdown (README still lists), file attachments |
| **LiteChat takeaway** | Civilization T on steroids — proof thin clients still ship for dead platforms |

### 2. [levlandon/numAi-plus](https://github.com/levlandon/numAi-plus) ★0
| | |
|--|--|
| **What** | Modernized fork of numAi for **Android 4.0+** |
| **Why lost** | 0 stars; 19 commits ahead / 3 behind upstream |
| **Features** | Multi-chat SQLite, streaming + **auto-fallback if stream broken**, OpenRouter/Ollama/LM Studio presets, TTS, personalization |
| **LiteChat takeaway** | **Stream-with-fallback** is production-grade (matches broken-provider reality); multi-chat history on disk = Palm storage heap |

### 3. [YMP-CO/ReOldAi](https://github.com/YMP-CO/ReOldAi) ★~23
| | |
|--|--|
| **What** | Unofficial **Gemini** client for Android **2.3+** (API 8) |
| **Why lost** | Teen author; Boosty/YouTube scene; Russian+EN |
| **Stack** | Java, AppCompat 23, **Conscrypt** for modern TLS certs, GSON only |
| **Compatibility matrix** | Explicit 🟥🟧🟩 table — rare honesty about what actually works |
| **LiteChat takeaway** | Publish a **device capability matrix**; Conscrypt/TLS is the EMS of 2020s legacy |

### 4. [Mik-el/How-to-develop-and-backport-for-Android-2.1-in-2020](https://github.com/Mik-el/How-to-develop-and-backport-for-Android-2.1-in-2020)
| | |
|--|--|
| **What** | Template/guide numAi credits for extreme backport |
| **LiteChat takeaway** | Archaeology source for minSDK culture |

### 5. [shinovon/NNJSON](https://github.com/shinovon/NNJSON)
| | |
|--|--|
| **What** | Tiny JSON lib used by numAi (nnproject / J2ME-adjacent culture) |
| **LiteChat takeaway** | Size-obsessed parsing lineage from feature phones |

---

## Tier A — Related agent / on-device (not thin-chat, but important)

### 6. [ashokvarmamatta/ZeroClawAndroid](https://github.com/ashokvarmamatta/ZeroClawAndroid) ★~12
| | |
|--|--|
| **What** | 24/7 agent daemon: channels, tools, OpenAI-compatible **server on phone**, LiteRT offline Gemma |
| **Why semi-lost** | Dense BUGS.md, WIP WhatsApp native, Play TODO stripped from releases |
| **Known pain** | GPU SIGSEGV mid-range → **CPU default**; BUG-44 WhatsApp pairing; modular everything |
| **LiteChat takeaway** | Civilization **A** reference — do **not** bundle into daily-driver chat SKU; study modular opt-in + RAM gates |

### 7. [Mobile-Artificial-Intelligence/maid](https://github.com/Mobile-Artificial-Intelligence/maid) ★~2.6k
| | |
|--|--|
| **What** | Local llama.cpp + remote BYOK; Expo rewrite path recently |
| **Status** | Famous, not lost — but **maid-native** Kotlin thin fork was the real 4GB lesson |
| **LiteChat takeaway** | Flutter/RN/Expo ≠ weak-RAM default |

### 8. OpenClaw / Hermes Agent / HenWorks Android packages
| | |
|--|--|
| **What** | Commercialized fat agent bootstrap (~200MB first run) |
| **LiteChat takeaway** | Copy ads+Pro+BYOK shell only |

---

## Tier B — Obscure / 0★ / preservation / adjacent

| Repo | Notes |
|------|--------|
| [SUN-0v/ds2api-android](https://github.com/SUN-0v/ds2api-android) | DeepSeek web→OpenAI-compatible **local proxy on Android** — phone as gateway |
| [luohanxi937-afk/PalmForge](https://github.com/luohanxi937-afk/PalmForge) | “Codex-style” Android agent, OpenAI-compatible; **0★ name invokes Palm** |
| [MrHuaweiFan/WebGPT](https://github.com/MrHuaweiFan/WebGPT) | WebView ChatGPT wrapper — anti-pattern for RAM |
| [DanielBatesUK/chatgpt-android-app](https://github.com/DanielBatesUK/chatgpt-android-app) | **Archived** WebView; official app killed it |
| [uuhyy666/AiTalkForMe](https://github.com/uuhyy666/AiTalkForMe) | Samsung Text Call + LLM TTS — niche accessibility |
| [yutungh/voiceflow-keyboard-android](https://github.com/yutungh/voiceflow-keyboard-android) | Voice keyboard / OpenAI STT |
| [BATTLEMETAL/CineMatch](https://github.com/BATTLEMETAL/CineMatch) | Movie recommender + OpenAI; 0★ sample |
| [secondly-com/OpenPhone](https://github.com/secondly-com/OpenPhone) | AI-first Android OS fork claim — huge scope, not a chat client |
| [trek-boldly-go/aria-launcher](https://github.com/trek-boldly-go/aria-launcher) | AI launcher WIP |
| [mardillu/OpenAI-Client-Android](https://github.com/mardillu/OpenAI-Client-Android) | Lightweight lib; **we added streaming** in local clone |
| [Taewan-P/gpt_mobile](https://github.com/Taewan-P/gpt_mobile) | Known; paste OOM class issues |

### Forum-only / non-GitHub distribution (often “more lost”)
- **4PDA** topic for numAi  
- **XDA**: `APP-1-0-numai-ai-app-for-legacy-android`  
- **OldMarket** store listings  
- Telegram: `@AppDataEN`, `@retroandroidgroup`

These are where APKs live when GitHub stars stay low.

---

## Tier C — Theoretical / historical code that maps forward

| Lineage | Modern echo |
|---------|-------------|
| J2ME midlets + RMS | Room + small heap |
| Opera Mini OBML proxy | BYOK cloud + SSE |
| Palm HotSync | PC/Ollama as brain, phone as terminal |
| DOS multi-config.sys | Chat mode vs Agent mode |
| NNJSON / nnproject | Minimal JSON on constrained Java |

---

## What “unknown” still means (limits of this search)

1. **Deleted GitHub repos** — only survive as forks, caches, or APKs on 4PDA. Need Wayback + Softpedia-class APK mirrors.  
2. **Gitee / GitCode / Codeberg** Chinese/EU mirrors of BYOK clients — partial surface.  
3. **Private Telegram source drops** — not indexable.  
4. **Play Store only, no source** — not git.  
5. **Student course repos** renamed/deleted after semester.

Next archaeology if ordered:
- Clone numAi + ReOldAi and extract **TLS/HTTP stack sizes** + streaming code paths  
- Search 4PDA/XDA threads for “ChatGPT APK source”  
- Wayback `github.com/*/ChatGPT-Android` 404s  
- Gitee `openai android` low-star crawl  

---

## Priority recommendations for *our* product

| Priority | Action |
|----------|--------|
| 1 | ~~Steal numAi-plus stream fallback~~ **DONE** → `OpenAiCompatibleClient.streamChat` |
| 2 | ~~Publish LiteChat **compat matrix**~~ **DONE** → onboarding + Settings (`DeviceCompat`, `CompatMatrix`) |
| 3 | Keep ZeroClaw/OpenClaw ideas **opt-in separate SKU**, never default |
| 4 | Optional: vendor-neutral notes from OpenAI-Client-Android streaming PR |
| 5 | Document 4PDA/XDA as distribution channel for FOSS arm of weak devices |

### Dig completed
Local clones: `/opt/data/workspace/{numAi,numAi-plus,ReOldAi}`.  
Code notes: `docs/DIG-FINDINGS.md`.

---

## One-line map

```
numAi/ReOldAi  →  civilization T (remote, tiny)
ZeroClaw/Opclaw → civilization A (resident agent)
LiteChat target → T default, A never silent
```

*The lost repos already solved your product question in 2005 Opera Mini clothing.*
