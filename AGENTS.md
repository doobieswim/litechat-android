# AGENTS.md — LiteChat

You are working in the **LiteChat** Android repo (thin BYOK AI chat, 4GB-first).

**Codeword `LITECHAT-WIRE` = coding mode.** Read HANDOFF.md and drain `docs/BACKLOG.md` Ready tickets. Not a research session.

**Codeword `LITECHAT-DIG` = research mode.** Read TEAM.md + BACKLOG. Write docs + Research tickets. Do **not** set Ready until `LITECHAT-PROOF` Approves.

**Codeword `LITECHAT-PROOF` = research-review mode.** Read `docs/RESEARCH-REVIEW.md` + TEAM.md. Grade DIG. Write verdict to `docs/RESEARCH-REVIEW.md` only. **Do not edit DIG docs or `app/**`.**

**Codeword `LITECHAT-REVIEW` = code-review mode.** Read `docs/REVIEW.md` + `docs/TEAM.md`, review the coding agent's most recent work, write verdict to `docs/REVIEW.md`. **READ-ONLY on `app/**` and gradle.**

**Codeword `LITECHAT-DEBUG` = bug-hunt mode.** Read `docs/BUGS.md` + TEAM + BACKLOG. Hunt named phone bugs. Write `docs/BUGS.md` + `B-00N` tickets. **Do not edit `app/**`. No Gradle.**

1. Read **[HANDOFF.md](./HANDOFF.md)** completely before editing.
2. Team rules: **[docs/TEAM.md](./docs/TEAM.md)** (5 roles: coding / research / proof / review / debug)
3. Work only from **[docs/BACKLOG.md](./docs/BACKLOG.md)** (`Ready` tickets for WIRE).
4. Skill: `android-byok-chat-apps` (Hermes).
5. Verify: `python3 scripts/verify_static.py` (and Gradle if SDK exists).

**Product law:** Tier A thin client only. No bundled agent runtime, no on-device 7B, no WebView chat shell. User-facing words: `docs/THEME-SHOW-DONT-TELL.md`.

