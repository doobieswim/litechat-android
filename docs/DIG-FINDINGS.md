# DIG — code-level archaeology of lost weak-device AI clients

Date: 2026-08-08  
Clones (local):

- `/opt/data/workspace/numAi` (gohoski/numAi)
- `/opt/data/workspace/numAi-plus` (levlandon/numAi-plus)
- `/opt/data/workspace/ReOldAi` (YMP-CO/ReOldAi)

---

## 1. Scale comparison

| Project | ~Java LOC (app) | External deps | minSdk | Network |
|---------|-----------------|---------------|--------|---------|
| **numAi** | ~7.7k (incl. ~2.5k NNJSON) | **none** (`dependencies {}`) | **1** | `HttpURLConnection` |
| **numAi-plus** | ~11.5k | still lean Java UI | **14?** (4.0+) | same + stream modes |
| **ReOldAi** | ~1.4k | AppCompat 23, **Conscrypt**, Gson | **8** (2.2) | `HttpURLConnection` + AsyncTask |
| LiteChat | Kotlin Compose stack | OkHttp, Room, Billing, AdMob | 26+ | OkHttp SSE |

**numAi is the DOS of Android AI clients:** zero Maven deps, ProGuard+resource shrink on release, bundled JSON parser.

---

## 2. numAi — hard facts from source

### Architecture (from AGENTS.md + tree)
- 3 Activities only — no Fragments, no support library
- Packages: `api/`, `model/`, `data/`, `ui/`, `util/`, `search/`
- Threading: raw `Thread` + `Handler(main)` — **no AsyncTask** (API 1)
- Config: `SharedPreferences` + **`commit()`** not `apply()` (API 1)

### HTTP (`ApiClient.java`)
- Manual redirect loop (max 5), preserves **Set-Cookie** across redirects (Cloudflare/Bing PoW path)
- Timeouts: connect **12s**, read **40s**
- User-Agent: `numAi/<version> (https://github.com/gohoski/numAi)`
- Returns **streaming body** as `InputStream` wrapped in `ConnectionInputStream` (keeps connection alive until stream drained)
- 1 KiB read buffer when buffering full body

### Chat (`ApiService.java`)
- Always `"stream": true` in request body
- Thinking flags **per provider base URL**:
  - OpenRouter → `reasoning.enabled`
  - Together → `chat_template_kwargs.thinking`
  - DashScope → `enable_thinking`
  - default → `reasoning_effort: "high"`
- Optional tools: `web_search`, `web_fetch` (when enabled)
- Vision: content array with `image_url` parts

### SSE (`MainActivity.readStream`)
- Line loop; accepts `data:` (with or without space after colon — check `startsWith("data:")`)
- Parses `choices[0].delta.content` + tool_calls accumulation
- Thinking tags in content: `<think>...</think>` state machine

### SSL
- `SSLDisabler` trusts all + hostname always true — **TLS 1.0 context** for ancient devices
- README admits real fix is **HTTPS→HTTP reverse proxy** for API 1–7; rejects TLS 1.2 backports as out of scope

### Build
```
minSdk 1, target/compile 25, Gradle 2.3.2 era
minifyEnabled true, shrinkResources true
dependencies {}
```

---

## 3. numAi-plus — the production upgrade

### Streaming modes
Settings: **AUTO | ON | OFF**  
Per-provider memory: `STREAM_SUPPORT_TRUE/FALSE` after success/failure.

### Stream → non-stream fallback (gold)
On error message containing any of:
`socket closed | unexpected | malformed | timeout | json | stream | connection | eof | reset`

Then:
1. Mark provider stream support = false  
2. Delete placeholder assistant message  
3. Toast streaming fallback  
4. Retry with `streamOverride = Boolean.FALSE`  

```java
// MainActivity ~1955-1991
currentResponseRetriedWithoutStreaming = true;
config.setProviderStreamSupport(url, STREAM_SUPPORT_FALSE);
// ... cleanup placeholder ...
requestAssistantResponse(thinking, false, Boolean.FALSE);
```

### SSE parse
```java
if (!line.startsWith("data: ")) continue;
String jsonData = line.substring(6).trim();
if ("[DONE]".equals(jsonData)) break;
// delta.content + extractJSONReasoning(delta)
// UI throttle: UPDATE_DELAY_MS between adapter updates
```

### Other
- Multi-chat SQLite (`ChatDatabaseHelper` / `ChatRepository`)
- Provider presets: VoidAI, Ollama, NavyAI, OpenRouter, Baseten, Gemini, Together, Upstage, LM Studio
- TTS sanitizer for spoken replies
- Image attach → JPEG base64 `data:image/jpeg;base64,...`

---

## 4. ReOldAi — Gemini-only legacy path

### TLS strategy (opposite of numAi trust-all)
```java
// AIApplication.onCreate
Security.insertProviderAt(Conscrypt.newProvider(), 1);
```
Modern certs on API 9+ without rewriting HTTPS stack.

### Network
- **Non-streaming** full-body Gemini `generateContent`
- AsyncTask (API 8+ OK for them)
- Timeouts: connect 10s, read 45s
- Roles: user / **model** (Gemini schema, not assistant)
- generationConfig: temperature, topK, topP
- Safety rejection path explicit string

### Size
~1.4k LOC app Java — smallest of the three; fewer features = less mud.

### Honest product surface
README compatibility matrix by Android version — copy this UX pattern.

---

## 5. Patterns to steal for LiteChat (ordered)

| # | Pattern | Source | LiteChat action |
|---|---------|--------|-----------------|
| 1 | **Stream fallback** after stream-class errors | numAi-plus | Implement in ViewModel/client |
| 2 | **Per-baseUrl stream capability cache** | numAi-plus | DataStore flag map |
| 3 | **UI throttle** on token paint | numAi-plus UPDATE_DELAY | Already partial; keep ≤50–100ms |
| 4 | **Provider-specific reasoning fields** | numAi | Optional advanced settings |
| 5 | **Compat matrix** in onboarding | ReOldAi | **DONE** — `DeviceCompat` + onboarding step 1 |
| 6 | **Cookie/redirect manual** | numAi | Only if free CF-gated APIs matter |
| 7 | Zero-deps / R8 | numAi | Keep arm64 + R8 + icons-core |
| 8 | Conscrypt | ReOldAi | Not needed at minSdk 26 |
| 9 | Trust-all SSL | numAi | **Never** on modern LiteChat production |

---

## 6. Anti-patterns observed

| Anti-pattern | Where | Why bad for 4GB modern |
|--------------|-------|------------------------|
| Trust-all SSL | numAi SSLDisabler | Attack surface; only justified for API 1 museum pieces |
| Full-body Gemini only | ReOldAi | Higher latency; larger peak RAM for long replies |
| AsyncTask | ReOldAi | Fine on 2.3; LiteChat uses coroutines |
| WebView clients | archived ChatGPT wrappers | High RSS |

---

## 7. Forum / distribution dig notes

numAi README points to:
- 4PDA topic 1116157  
- XDA: `APP-1-0-numai-ai-app-for-legacy-android`  
- OldMarket store id=410  
- Telegram AppDataEN / retroandroidgroup  

These are the **real discovery surfaces** for lost APKs when GitHub stars ≈ 0–30.

---

## 8. Bottom line

The lost repos prove three civilizations in code:

1. **numAi** — absolute minimum resident set; stream always; museum TLS  
2. **numAi-plus** — same physics + **admission control for broken streams** + multi-chat disk  
3. **ReOldAi** — Conscrypt honesty + non-stream Gemini + tiny LOC  

LiteChat should be **numAi-plus physics on modern Kotlin**:  
OkHttp SSE default, **automatic non-stream retry**, provider capability memory, disk history, never resident weights.

---

## 9. Files of interest (local clones)

```
numAi/app/src/main/java/io/github/gohoski/numai/api/ApiClient.java
numAi/app/src/main/java/io/github/gohoski/numai/api/ApiService.java
numAi/app/src/main/java/io/github/gohoski/numai/MainActivity.java  # readStream
numAi/app/src/main/java/io/github/gohoski/numai/util/SSLDisabler.java
numAi/AGENTS.md

numAi-plus/.../MainActivity.java   # fallback ~1955, readStream ~1994
numAi-plus/.../SettingsActivity.java  # STREAMING_MODE_*

ReOldAi/.../AIApplication.java     # Conscrypt
ReOldAi/.../MainActivity.java      # Gemini HttpURLConnection
```

---

## 7. HenWorks Hermes Agent — Android (2026-08-15)

Clones (local):
- `/opt/data/workspace/Hermes-agent-android-PC-companion-app` (official, AGPL-3.0, ★44)
- `/opt/data/workspace/Hermes-Agent-On-Android` (AbuZar-Ansarii, ★189, MIT)

**Key facts:** Play `com.hermesagent.android` (4.5★, 3.13K reviews, 10K+ downloads) is a
**closed-source shell** around the open hermes-agent framework (MIT, Nous Research).
~200MB env, built-in terminal, code execution, Fal.ai images, memory system, Hermes Pro
one-time removes ads. The open companion reveals the architecture: **phone↔PC mesh**
(NaCl Box E2E, QR public-key pairing, LAN/Tailscale broker, idempotent conversation
handoff). Community repo shows the underlying Termux/proot packaging (~500 lines of
shell, proot-distro ubuntu → clone hermes-agent → pip install).

**Verdict:** business shell worth copying (one-time Pro, guided first-run, secrets-safe
export — mostly already matched); agent runtime itself is the Tier B/D shape we refuse.
Full report: `docs/HENWORKS-HERMES-AGENT-DIG.md`.

---

## 8. Opclaw / OpenClaw-on-Android (2026-08-15)

Clones (local):
- `/opt/data/workspace/openclaw-android` (AidanPark, ★1734, MIT — no-proot packaging)
- `/opt/data/workspace/openclaw-termux` (mithun50, ★1678, MIT — Flutter app + terminal)

**Key facts:** HenWorks' Opclaw (`com.opclaw.android`, 5.0★, 1K+) has a **~263 MB APK** —
a closed shell around the open OpenClaw framework (openclaw/openclaw ★386K). HenWorks'
"no proot, ~200MB, 3–10 min" marketing matches the community's glibc-ld.so-only trick
(no full Linux distro). Packaging = native shell + WebView dashboard + bundled terminal
emulator + Node-runtime patches (glibc-compat, argon2-stub, systemctl stub). Flutter
alternative (openclaw-termux) monetizes via sponsor banner (Bloome).

**Verdict:** agent lane = crowded, fat gold rush (dozens of packagers); 263 MB vs our
1.6 MB is the honest-contrast number. Thin-client law unchanged. Full report:
`docs/OPENCLAW-ANDROID-DIG.md`.
