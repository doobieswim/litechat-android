# LiteChat — coding-agent handoff

**Codeword:** `LITECHAT-WIRE`  
If the human says that word (alone or in a sentence), you are the **coding agent**: read this file fully, then `docs/TEAM.md` + `docs/BACKLOG.md`, load skill `android-byok-chat-apps` if available, implement the next **Ready** ticket (default **C-001** unless they name another id). Do not open a research epic.

**Read this first.** Then code. Do not re-litigate architecture unless the human overrides.

| Field | Value |
|-------|--------|
| Product | **LiteChat** — thin BYOK OpenAI-compatible chat for weak Android phones |
| Repo root | `/opt/data/workspace/byok-chat-android` |
| GitHub | **public**: `github.com/flamingspade1995-coder/litechat-android` (CI: `.github/workflows/build.yml` — static-verify + assembleRelease + 20MB APK gate) |
| Package | `com.litechat.android` |
| Skill (patterns) | Hermes skill `android-byok-chat-apps` |
| Sister role | **Research agent** owns digs, history, competitor/OSS notes → writes `docs/` + backlog items. **Review agent** (`LITECHAT-REVIEW`) reviews your work read-only and writes `docs/REVIEW.md`. **You** implement tickets and fix review issues. |
| Team protocol | [`docs/TEAM.md`](./docs/TEAM.md) |
| Ticket queue | [`docs/BACKLOG.md`](./docs/BACKLOG.md) |
| Architecture | [`ARCHITECTURE.md`](./ARCHITECTURE.md) · research notes [`RESEARCH.md`](./RESEARCH.md) |

---

## 60-second product truth

```
Phone = UI + key store + SSE client
Brain = user's OpenAI-compatible API (cloud or LAN Ollama on a PC)
NOT = Hermes/OpenClaw/Node/Termux/GGUF on device
```

HenWorks-style means: **Play one-tap, onboarding, ads + one-time Pro, BYOK, no Termux for users** — copy the **business shell**, never the **agent runtime**.

Capability tiers (do not blur):

| Tier | What | LiteChat? |
|------|------|-----------|
| **A** | Thin chat + remote brain | **YES — only default** |
| **B** | On-device agent gateway | Separate SKU / never silent |
| **C** | Tiny local model toy | Out of scope v1 |
| **D** | Local 7B+ | Never market on 4GB daily driver |

---

## What already works (do not rewrite)

- Kotlin + Jetpack Compose + Material 3 dark UI  
- OkHttp SSE `OpenAiCompatibleClient` + **stream → non-stream fallback** (numAi-plus pattern)  
- Room conversations/messages  
- Encrypted API key + DataStore settings  
- AdMob banner stub + Play Billing one-time Pro stub  
- Onboarding **2-step**: free-RAM **compat matrix** → key/provider  
- Settings: device chip + matrix toggle  
- Input cap `MAX_INPUT_CHARS = 32_000` (gpt_mobile Constraints crash class)  
- `largeHeap=false`, arm64 split, R8 release  
- CI workflow: `.github/workflows/build.yml`  
- Static verify possible without SDK (Python file/symbol checks)

### Map

```
app/src/main/java/com/litechat/android/
  LiteChatApp.kt, MainActivity.kt
  data/
    api/OpenAiCompatibleClient.kt   ← SSE + fallback
    db/                             ← Room
    prefs/                          ← DataStore + SecureStore
    billing/BillingRepository.kt
    AppContainer.kt
  ui/
    ChatViewModel.kt
    Screens.kt                      ← chat / onboarding / settings
    CompatMatrix.kt
    theme/Theme.kt
  util/DeviceCompat.kt              ← free-RAM bands + matrix data
docs/                               ← research outputs (you consume, rarely invent)
```

Cloned dig sources (read-only reference):  
`/opt/data/workspace/{numAi,numAi-plus,ReOldAi}`

---

## Hard constraints (fail the PR if violated)

1. **No** WebView chat shell, RN, Flutter, bundled Node/Python/proot, on-device GGUF.  
2. **No** trust-all SSL.  
3. **No** marketing as Hermes / OpenClaw / “run 7B on 4GB.”  
4. Keep **one** shared OkHttpClient; cancel streams on Stop / clear.  
5. Prefer **small diffs**; no architecture tourism.  
6. If host has **no Android SDK**: implement + static verify; do not claim APK built. Real build = laptop or CI.  
7. Never log API keys.  
8. Ads: **one** banner max; no interstitial-on-send.  
9. `minSdk` stays 26+ unless research ticket says otherwise with rationale.

---

## How to take a ticket

1. Open [`docs/BACKLOG.md`](./docs/BACKLOG.md) — pick highest **Ready** item (or human-named id).  
2. Read linked research docs for that ticket only.  
3. Implement minimal code.  
4. Update ticket status → **Done** + one-line “what changed.”  
5. Run verification (below).  
6. If blocked on unknown fact → write `docs/QUESTIONS-FOR-RESEARCH.md` stub item; **do not invent product policy**.

### Suggested first coding sprint (in order)

| ID | Ticket |
|----|--------|
| C-001 | Lazy-init MobileAds only when `!isPro` |
| C-002 | Product flavors `play` / `foss` |
| C-003 | Gate “Dev: mark Pro” to `BuildConfig.DEBUG` only |
| C-004 | Per-baseUrl stream-broken preference cache |
| C-005 | GET `/models` picker (optional UI) |

Research-owned (do not start coding until **Ready**): Play privacy HTML copy, 4PDA distribution notes, markdown renderer cost study.

---

## Verification checklist

### Always (no SDK)

```bash
# From repo root — adjust/extend if you add a scripts/verify_static.py
test -f app/src/main/java/com/litechat/android/data/api/OpenAiCompatibleClient.kt
grep -q allowNonStreamFallback app/src/main/java/com/litechat/android/data/api/OpenAiCompatibleClient.kt
grep -q 'largeHeap="false"' app/src/main/AndroidManifest.xml
grep -RIn --include='*.kt' -E 'WebView|trustAll|setHostnameVerifier' app/src && exit 1 || true
```

### When SDK present

```bash
./gradlew assembleDebug
./gradlew assembleRelease   # play flavor when C-002 lands
# Install on 4GB-class device; note idle vs streaming RSS if possible
```

### Done means

- [ ] Ticket acceptance criteria checked  
- [ ] No new heavy deps without BACKLOG note  
- [ ] README / BACKLOG status updated if user-visible  

---

## Collaboration with research agent

| Research agent does | Coding agent does |
|---------------------|-------------------|
| OSS digs, weak-RAM history, Play policy notes | Kotlin/Gradle/UI |
| Writes `docs/*.md`, marks backlog **Ready** with AC | Implements **Ready** tickets |
| Proposes steals (patterns, not code dumps) | Ports minimal pattern into LiteChat style |
| Does not drive-by rewrite app modules | Does not open new multi-day research rabbit holes |

**Handoff artifact format** (research → you): each Ready ticket in BACKLOG has:

- Goal (1–2 sentences)  
- Acceptance criteria (checkbox list)  
- Files likely touched  
- Out of scope  
- Research links  

**You → research** when stuck: append to `docs/QUESTIONS-FOR-RESEARCH.md`:

```markdown
### Q-00N — short title
- Blocked ticket: C-00X
- What I need to know:
- What I already tried:
```

---

## Prompt paste for a fresh coding session

**Minimum (codeword only works if agent can see this repo / HANDOFF):**

```
LITECHAT-WIRE
```

**Full paste (safe if session has no project context):**

```
LITECHAT-WIRE
Repo: /opt/data/workspace/byok-chat-android
Read HANDOFF.md + docs/TEAM.md + docs/BACKLOG.md.
Load skill android-byok-chat-apps if available.
Implement the next Ready ticket (default C-001).
No agent runtimes / local LLMs. Small diffs. python3 scripts/verify_static.py
Mark ticket Done; list files changed.
```

---

## Prompt paste for research session (human → research agent)

```
LITECHAT-DIG
Repo: /opt/data/workspace/byok-chat-android
Read docs/TEAM.md + docs/BACKLOG.md + HANDOFF.md.
Do NOT implement app features unless asked — write docs and Ready tickets
with acceptance criteria for the coding agent.
Topic: <TOPIC>
Prefer extreme-depth when constraints are weak-RAM/packaging (history + modern).
```

---

## Prompt paste for review session (human → review agent)

```
LITECHAT-REVIEW
Repo: /opt/data/workspace/byok-chat-android
Read docs/REVIEW.md + docs/TEAM.md + docs/BACKLOG.md.
Review ticket <ID> (or most recent Done).
You are READ-ONLY: do not edit app/** or any source file.
Write your verdict (Approve / Issues) to docs/REVIEW.md.
```

---

## Non-goals (v1)

- Multi-modal vision pipeline beyond simple future stub  
- Accounts, sync, vendor cloud  
- Bundled inference  
- iOS  
- Perfect markdown (plain text OK until cost study says otherwise)

---

*Last handoff cut: post compat-matrix + stream-fallback. Coding agent starts at C-001.*
