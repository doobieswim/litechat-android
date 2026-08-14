# LiteChat three-agent team

Human owns product calls. Three Hermes roles share one repo.

| Codeword | Role |
|----------|------|
| **`LITECHAT-WIRE`** | Coding agent — implement Ready backlog tickets |
| **`LITECHAT-DIG`** | Research agent — docs + Ready tickets only, no app feature coding |
| **`LITECHAT-REVIEW`** | Review agent — read-only code review, writes report, does NOT edit `app/**` |

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
                                                 │
                                  code + ticket Done
                                                 ▼
                                        ┌──────────────┐
                                        │    REVIEW    │
                                        │    AGENT     │
                                        └──────────────┘
                                              │ read-only
                                              ▼
                                      docs/REVIEW.md
                                      + issues → coding
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

### Review agent (hand off via `docs/REVIEW.md`)

**Trigger:** codeword `LITECHAT-REVIEW`. Read `docs/REVIEW.md`, then review the coding agent's most recent work.

**Owns**
- Read-only review of `app/**` and `*.gradle*` changes for a named ticket (or most recent `Done`)
- Verifying acceptance criteria against the code
- Checking the 4GB/low-RSS/small-APK laws and `HANDOFF.md` hard constraints
- Writing findings to `docs/REVIEW.md` (issues → fix list for coding agent)
- Optionally running `python3 scripts/verify_static.py` to check basics

**Must not**
- **Edit `app/**`, gradle, or any source file** — review agent is read-only by design (Option B: gatekeeper)
- Start new features or mark tickets Done
- Rewrite working code for style preferences

**Output contract**
1. `docs/REVIEW.md` verdict per ticket: **Approve** / **Issues** (numbered, with file + line + why + suggested fix)
2. Fix list handed back to coding agent (coding fixes them, then re-reviews if human asks)
3. Never touches BACKLOG status except adding `Reviewing` note when human requests

### How a ticket flows through all three (suggested)

1. Human: "Research X" → research agent → Ready ticket with AC
2. Human: paste coding prompt → coding agent implements → ticket `Done`
3. Human: "Review C-00X" → review agent → `docs/REVIEW.md` verdict
4. If issues → coding agent fixes → (repeat 3)
5. Human: spot-check on device / Play

## Shared laws

1. **Tier A default** — thin BYOK chat.  
2. **4GB honesty** — free RAM ≠ sticker RAM; matrix stays truthful.  
3. **Small APK / low RSS** beats feature checklist.  
4. Single source of task truth: **`docs/BACKLOG.md`**.  
5. Architecture debates go to human; agents don’t fork the product vision.  

## File ownership (soft)

| Path | Primary | Secondary |
|------|---------|-----------|
| `app/**` | Coding | Research (read), Review (read) |
| `*.gradle*` | Coding | Research (size/deps advice), Review (read) |
| `docs/**` | Research | Coding (ticket status, QUESTIONS), Review (REVIEW.md only) |
| `HANDOFF.md` | Human / either (careful) | Keep stable |
| `README.md` | Either | User-facing accuracy |
| `RESEARCH.md` | Research | Coding reads only |
| `docs/REVIEW.md` | Review | Coding (fix list) |
| `/opt/data/workspace/{numAi,numAi-plus,ReOldAi}` | Research clones | Coding read-only steals |

## Cadence (suggested)

1. Human: “Research X” → research agent → Ready tickets
2. Human: paste coding prompt from HANDOFF → coding agent drains Ready
3. Human: “Review C-00X” → review agent → `docs/REVIEW.md` verdict; issues → coding agent fixes
4. Human: spot-check on device / Play

Parallel OK if tickets don’t touch the same files; if conflict, coding wins on `app/**`, research wins on `docs/**` narrative (except BACKLOG status lines). Review agent is always read-only on `app/**` so it never conflicts — it only writes `docs/REVIEW.md`.

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
- Review agent editing source files (it must stay read-only on `app/**`)
- Coding “fixing” a review issue without acknowledging it in REVIEW.md
- Research pasting 2k-line historical essays into Kotlin comments
- Coding “improving” copy that is legally sensitive (privacy policy) without Ready source text
- Closing tickets Done without touching BACKLOG
