# AGENTS.md — LiteChat

You are working in the **LiteChat** Android repo (thin BYOK AI chat, 4GB-first).

**Codeword `LITECHAT-WIRE` = coding mode.** Read HANDOFF.md and drain `docs/BACKLOG.md` Ready tickets (start C-001). Not a research session.

**Codeword `LITECHAT-REVIEW` = review mode.** Read `docs/REVIEW.md` + `docs/TEAM.md`, review the coding agent's most recent work, write verdict to `docs/REVIEW.md`. **READ-ONLY on `app/**` and gradle — you never edit source files.**

1. Read **[HANDOFF.md](./HANDOFF.md)** completely before editing.
2. Team rules: **[docs/TEAM.md](./docs/TEAM.md)** (3 roles: coding / research / review)
3. Work only from **[docs/BACKLOG.md](./docs/BACKLOG.md)** (`Ready` tickets).
4. Skill: `android-byok-chat-apps` (Hermes).
5. Verify: `python3 scripts/verify_static.py` (and Gradle if SDK exists).

**Product law:** Tier A thin client only. No bundled agent runtime, no on-device 7B, no WebView chat shell.
