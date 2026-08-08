# LiteChat two-agent team

Human owns product calls. Two Hermes roles share one repo.

| Codeword | Role |
|----------|------|
| **`LITECHAT-WIRE`** | Coding agent — implement Ready backlog tickets |
| **`LITECHAT-DIG`** | Research agent — docs + Ready tickets only, no app feature coding |

Repo path: `/opt/data/workspace/byok-chat-android`

```
┌──────────────┐     docs/BACKLOG.md      ┌──────────────┐
│   RESEARCH   │ ───────────────────────► │    CODING    │
│    AGENT     │   Ready tickets + AC     │    AGENT     │
│              │ ◄─────────────────────── │              │
└──────────────┘  QUESTIONS-FOR-RESEARCH  └──────────────┘
        │                                         │
        ▼                                         ▼
   docs/*.md                              app/**/*.kt
   RESEARCH.md                            gradle, CI
   dig clones                             verify build
```

## Roles

### Research agent (this session’s default when human says “research”)

**Owns**
- Competitor / lost-repo / forum archaeology  
- Weak-RAM and packaging theory → product rules  
- Play policy, monetization, distribution channel notes  
- Turning findings into **Ready** backlog tickets with acceptance criteria  
- Updating `RESEARCH.md`, `docs/LOST-REPOS.md`, `docs/DIG-FINDINGS.md`, etc.

**Must not**
- Large drive-by refactors of working app code  
- Claim APK/RSS numbers without measurement  
- Blur Tier A chat with Tier B/C/D agent/local-LLM products  

**Output contract**
1. Short finding summary in the right `docs/` file  
2. Zero or more backlog rows moved to **Ready** with AC  
3. Optional `docs/QUESTIONS-FOR-HUMAN.md` if product decision needed  

### Coding agent (hand off via `HANDOFF.md`)

**Owns**
- Kotlin, Compose, Gradle, manifests, CI green  
- Implementing **Ready** tickets only (or human-pinned id)  
- Static verify / assemble when SDK exists  
- Marking tickets **Done** with file list  

**Must not**
- Start open-ended research epics  
- Add heavy deps or new product tiers without a Ready ticket  
- Invent Play legal copy — ask research or human  

**Output contract**
1. Code + tests if cheap  
2. BACKLOG status update  
3. Blockers → `docs/QUESTIONS-FOR-RESEARCH.md`  

## Shared laws

1. **Tier A default** — thin BYOK chat.  
2. **4GB honesty** — free RAM ≠ sticker RAM; matrix stays truthful.  
3. **Small APK / low RSS** beats feature checklist.  
4. Single source of task truth: **`docs/BACKLOG.md`**.  
5. Architecture debates go to human; agents don’t fork the product vision.  

## File ownership (soft)

| Path | Primary | Secondary |
|------|---------|-----------|
| `app/**` | Coding | Research (read) |
| `*.gradle*` | Coding | Research (size/deps advice) |
| `docs/**` | Research | Coding (ticket status, QUESTIONS) |
| `HANDOFF.md` | Human / either (careful) | Keep stable |
| `README.md` | Either | User-facing accuracy |
| `RESEARCH.md` | Research | Coding reads only |
| `/opt/data/workspace/{numAi,numAi-plus,ReOldAi}` | Research clones | Coding read-only steals |

## Cadence (suggested)

1. Human: “Research X” → research agent → Ready tickets  
2. Human: paste coding prompt from HANDOFF → coding agent drains Ready  
3. Human: spot-check on device / Play  

Parallel OK if tickets don’t touch the same files; if conflict, coding wins on `app/**`, research wins on `docs/**` narrative (except BACKLOG status lines).

## Status vocabulary (BACKLOG)

| Status | Meaning |
|--------|---------|
| `Idea` | Not specified enough to code |
| `Research` | Research agent working |
| `Ready` | AC written — coding may start |
| `Doing` | Coding agent claimed |
| `Done` | Merged in tree + verified as far as host allows |
| `Blocked` | Waiting on human / external |

## Anti-patterns

- Both agents rewriting `OpenAiCompatibleClient` the same day without a ticket  
- Research pasting 2k-line historical essays into Kotlin comments  
- Coding “improving” copy that is legally sensitive (privacy policy) without Ready source text  
- Closing tickets Done without touching BACKLOG  
