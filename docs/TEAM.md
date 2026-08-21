# LiteChat five-agent team

Human owns product calls. Five Hermes roles share one repo. H-007 locked four; **H-011** added DEBUG (2026-08-21).

| Codeword | Role |
|----------|------|
| **`LITECHAT-WIRE`** | Coding agent — implement Ready backlog tickets |
| **`LITECHAT-DIG`** | Research agent — docs + tickets, no app feature coding. Does **not** flip Research → Ready until PROOF Approves |
| **`LITECHAT-PROOF`** | Research reviewer — grades DIG docs/tickets, writes `docs/RESEARCH-REVIEW.md` only |
| **`LITECHAT-REVIEW`** | Code reviewer — read-only code review, writes `docs/REVIEW.md`, does NOT edit `app/**` |
| **`LITECHAT-DEBUG`** | Bug hunter — writes `docs/BUGS.md` + `B-00N` tickets. Does **not** edit `app/**`. No Gradle. |

Repo path: `/opt/data/workspace/byok-chat-android`

```
DIG writes docs + ticket stays Research
        │
        ▼
PROOF  →  docs/RESEARCH-REVIEW.md
        │
        ├── Approve  → DIG/human may set Ready
        └── Issues   → DIG fixes → PROOF again
                │
                ▼
         WIRE takes Ready only
                │
                ▼
         REVIEW grades code → docs/REVIEW.md

Phone bugs (separate lane):
DEBUG → docs/BUGS.md + B-00N
        ├── cause proven → DEBUG may set Ready (no PROOF)
        └── not proven   → stay Research / ask human
                │
                ▼
         WIRE → REVIEW → human checks the phone
```

## Roles

### Research agent (this session’s default when human says “research”)

**Owns**
- Competitor / lost-repo / forum archaeology  
- Weak-RAM and packaging theory → product rules  
- Play policy, monetization, distribution channel notes  
- Turning findings into backlog tickets with acceptance criteria (**status stays `Research` until `LITECHAT-PROOF` Approves**)
- Updating `RESEARCH.md`, `docs/LOST-REPOS.md`, `docs/DIG-FINDINGS.md`, etc.


**Must not**
- Large drive-by refactors of working app code  
- Claim APK/RSS numbers without measurement  
- Blur Tier A chat with Tier B/C/D agent/local-LLM products  

**Output contract**
1. Short finding summary in the right `docs/` file  
2. Zero or more backlog rows left **Research** with AC — Ready only after PROOF Approve  
3. Optional `docs/QUESTIONS-FOR-HUMAN.md` if product decision needed  

### Proof agent (hand off via `docs/RESEARCH-REVIEW.md`)

**Trigger:** codeword `LITECHAT-PROOF`.

**Owns**
- Read-only grade of DIG docs + `R-` tickets
- Checklist: scope, grounding, product laws, cost flags, theme (`THEME-SHOW-DONT-TELL.md`), ticket AC, usefulness
- Writing findings to `docs/RESEARCH-REVIEW.md` only

**Must not**
- Edit `app/**`, gradle, or DIG essays
- Mark tickets Ready/Done
- Start a new research epic or run Gradle

**Output contract**
1. `docs/RESEARCH-REVIEW.md` verdict: **Approve** / **Issues**
2. Issues go back to DIG. Every research ticket needs Approve before Ready (human may override in BACKLOG in one line).

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

### Debug agent (hand off via `docs/BUGS.md`)

**Trigger:** codeword `LITECHAT-DEBUG`. Read `docs/BUGS.md`, then hunt the bugs the human named.

**Owns**
- Root-cause hunt on named phone bugs (what they tapped / should / did)
- `docs/BUGS.md` log (newest first)
- `B-00N` backlog tickets with AC

**Must not**
- Edit `app/**`, gradle, or CI
- Run Gradle or bake an APK
- Mark tickets **Done** or start features
- Flip DIG research tickets to Ready

**May** mark a **bug** ticket **Ready** only when cause is proven (`file.kt:line`) and AC is checkboxes. PROOF is not required for those. Human may override in one BACKLOG line.

**Output contract**
1. Dated log in `docs/BUGS.md`
2. `B-00N` tickets (Research or Ready)
3. Stop. WIRE fixes.

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

### How a ticket flows through all five

1. Human: "Research X" → DIG → ticket stays **Research** with AC  
2. Human: `LITECHAT-PROOF` → `docs/RESEARCH-REVIEW.md` verdict  
3. If Issues → DIG fixes → (repeat 2). If Approve → DIG/human sets **Ready**  
4. Human: paste WIRE prompt → coding implements → **Done**  
5. Human: `LITECHAT-REVIEW` → `docs/REVIEW.md`  
6. If code issues → WIRE fixes → (repeat 5)  
7. Human: spot-check on device / Play  

**Bug lane:** Human: `LITECHAT-DEBUG` + symptoms → `docs/BUGS.md` + `B-00N` → WIRE if Ready → REVIEW → phone check.  

## Shared laws

1. **Tier A default** — thin BYOK chat.  
2. **4GB honesty** — free RAM ≠ sticker RAM; matrix stays truthful.  
3. **Small APK / low RSS** beats feature checklist.  
4. Single source of task truth: **`docs/BACKLOG.md`**.  
5. Architecture debates go to human; agents don’t fork the product vision.  

## File ownership (soft)

| Path | Primary | Secondary |
|------|---------|-----------|
| `app/**` | Coding | Research (read), Review (read), Debug (read) |
| `*.gradle*` | Coding | Research (size/deps advice), Review (read), Debug (read) |
| `docs/**` | Research | Coding (ticket status, QUESTIONS), Proof (`RESEARCH-REVIEW.md` only), Review (`REVIEW.md` only), Debug (`BUGS.md` + `B-` lines in BACKLOG) |
| `HANDOFF.md` | Human / either (careful) | Keep stable |
| `README.md` | Either | User-facing accuracy |
| `RESEARCH.md` | Research | Coding reads only |
| `docs/REVIEW.md` | Review | Coding (fix list) |
| `docs/RESEARCH-REVIEW.md` | Proof | DIG reads Issues and fixes *other* docs |
| `docs/BUGS.md` | Debug | Coding (fix list) |
| `/opt/data/workspace/{numAi,numAi-plus,ReOldAi}` | Research clones | Coding read-only steals |

## Cadence (suggested)

1. Human: “Research X” → DIG → ticket stays Research  
2. Human: `LITECHAT-PROOF` → `docs/RESEARCH-REVIEW.md`  
3. Approve → Ready. Issues → DIG fixes  
4. Human: paste WIRE prompt from HANDOFF → drain Ready  
5. Human: `LITECHAT-REVIEW` → `docs/REVIEW.md`; issues → WIRE fixes  
6. Human: `LITECHAT-DEBUG` when the APK runs but features are wrong → `docs/BUGS.md`  
7. Human: spot-check on device / Play

Parallel OK if tickets don’t touch the same files; if conflict, coding wins on `app/**`, research wins on `docs/**` narrative (except BACKLOG status lines). Review is always read-only on `app/**`. Proof is always read-only on DIG essays.

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
- Debug agent editing source files or running Gradle (it hunts; WIRE patches)
- Debug agent marking DIG research tickets Ready
- Proof agent rewriting DIG docs or marking Ready
- DIG marking Ready without a PROOF Approve
- Coding “fixing” a review issue without acknowledging it in REVIEW.md
- Research pasting 2k-line historical essays into Kotlin comments
- Coding “improving” copy that is legally sensitive (privacy policy) without Ready source text
- Closing tickets Done without touching BACKLOG
