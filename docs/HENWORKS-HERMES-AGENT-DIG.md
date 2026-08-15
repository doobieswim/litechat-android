# HenWorks Hermes Agent — Android (DIG deep-dive)

**Date:** 2026-08-15 · **Agent:** LITECHAT-DIG · **Status:** Research
**Clones (local):**
- `/opt/data/workspace/Hermes-agent-android-PC-companion-app` (official, AGPL-3.0)
- `/opt/data/workspace/Hermes-Agent-On-Android` (AbuZar-Ansarii, ★189, MIT — community)

---

## TL;DR (plain words)

HenWorks sells the **fat-agent lane**: a full Hermes agent runtime living on your phone,
with a built-in terminal, ~200MB setup, and a 5–10 minute first install. It is the exact
**Tier B/D** shape our thin-client law says BYO AI must not become. But their **business
shell** (one-time Pro removes ads, guided first-run, self-check/repair, BYOK, no cloud
account, unofficial disclaimer) is a working copy of the same shell we are copying — and
their new **phone↔PC mesh** feature is a strong validation of the "remote brain" idea
behind our LAN Ollama path and the future BYO-Sync (P-007).

**The main app shell is closed-source.** HenWorks publishes only the PC-side companion
(open, AGPL-3.0), their website, and two casino games. The community repo shows the
Termux/proot packaging the app wraps.

---

## 1. Play Store facts (verified 2026-08-15)

| Field | Value |
|-------|-------|
| App | Hermes Agent - Android |
| Package | `com.hermesagent.android` |
| Developer | Hen Works — WANG, HSING-KUO, New Taipei City, Taiwan |
| Rating / reviews | 4.5★ · 3.13K reviews · 10K+ downloads |
| Monetization | Contains ads · in-app purchases · **Hermes Pro = one-time purchase to remove all ads permanently** |
| Updated | Aug 10, 2026 (v3.0.4) |
| Requirements | ~200MB storage for initial setup; internet; BYOK |
| Website | henworks.com |

Play-listed features:
- **Code Execution** — run scripts/commands on-device
- **AI Image Generation** — Fal.ai integration
- **Memory System** — persistent memory across conversations
- **Session Management** — multiple concurrent sessions with resume
- **Hermes Pro** — one-time, removes ads
- Built on the open-source hermes-agent framework; no cloud account

Providers: OpenAI (GPT-4/4o), Anthropic (Claude), Google (Gemini), OpenRouter,
**local models via LiteRT-LM**.

v3.0.4 changelog ("Install and repair reliability"): detects and sets aside a broken
system copy of a crypto library that blocked self-repair; "Repair install" restores;
"Check install" shows progress; setup no longer blocks chatting after an update.
→ The **install/check/repair lifecycle** is a core HenWorks shell feature.

Reviews (notable): "super simple UI doesn't trip you up on install… self check lets you
know what's missing"; a new-app trust worry → user ran the APK through VirusTotal.

## 2. The company (henworks.com)

Taiwanese dev shop (New Taipei City). Bilingual site. Product portfolio:
- **Opclaw** — "run AI coding agents on Android": **no Linux needed (skips proot-distro)**,
  ~200MB, one-line command, 3–10 min install, 10+ providers, phone/tablet/Android TV.
- **TextLen** — Mac OCR studio (32 languages, layout rebuild, local REST API).
- **Hermes Agent - Android** — Play only, not featured on the site.
- GitHub org (`HenWorks`, a user account): 4 public repos → `HenWorks-front-pages`
  (site, 34MB), `Hermes-agent-android-PC-companion-app` (★44), `MySlotMachine`,
  `slot-machine` (casino/slot games — H5 gambling is in their portfolio).

## 3. Repo landscape

| Repo | Status | Meaning |
|------|--------|---------|
| `HenWorks/Hermes-agent-android-PC-companion-app` | **open** (AGPL-3.0, ★44, 278KB) | The PC half of the phone app — the only real architecture artifact |
| `HenWorks/HenWorks-front-pages` | open | Marketing site source |
| App shell (`com.hermesagent.android`) | **closed** | Wraps hermes-agent framework (MIT, Nous Research) — framework open, shell private |
| `AbuZar-Ansarii/Hermes-Agent-On-Android` (★189) | open (MIT) | Community Termux/proot installer — the packaging the shell wraps |

## 4. Architecture findings (from the open companion)

**Phone ↔ PC mesh ("Run on Computer"):**
- Phone app dispatches a task → the **PC's Hermes** runs it → result returns to the phone.
- `mesh_broker.py`: always-on broker with pair / push / poll / ack / pull / push_session.
- `companion_web.py`: 127.0.0.1 browser console (pairing QR, status, task history).
- LAN/mDNS discovery (`zeroconf`); binds the LAN/Tailscale IP — **never 0.0.0.0**.

**Desktop Handoff (conversation sync):**
- Move whole conversations computer ↔ phone; merges are **idempotent by-id upsert +
  natural-key message dedup** (no duplicates either direction).
- Exports read only `state.db` + `memories/`; **never** `auth.json` / `.env`; refuses
  escaping symlinks.

**Security model (open-sourced on purpose):**
- End-to-end NaCl `Box` (Curve25519 + XSalsa20-Poly1305) between paired devices.
- Pairing: scan a QR in person; the QR carries only **public key + host/port**;
  private keys never leave the device.
- "Security rests on the keys, not on the protocol being secret."

**Packaging:**
- Companion ships as PyInstaller one-click binaries (.app / .exe / .AppImage), CI matrix
  macOS/Windows/Linux, `v*` tags → GitHub Release. Also `hermes plugins install
  HenWorks/...` one-liner.
- Phone side (from community clone): Termux one-liner `curl … | bash` →
  `proot-distro install ubuntu` → apt python3/nodejs → `git clone
  NousResearch/hermes-agent` → pip install → run. ~500 lines of shell, v0.10.0-pinned.
  HenWorks' own Opclaw marketing claims they **skip proot-distro** ("no Linux needed") —
  a leaner glibc-only install, still ~200MB.

## 5. Monetization

- Free tier: banner ads; **Hermes Pro = one-time purchase** to remove ads (exactly our
  $4.99 shape; they named it "Pro" too).
- Images: **Fal.ai** integration (a paid third-party image API; the user pays Fal, the app
  takes a cut or keys). We chose BYOK `/imagine` instead — provider-paid, zero vendor.
- No cloud account; BYOK. Unofficial-client disclaimer on Play.

## 6. Steal list (what BYO AI should copy)

| Pattern | HenWorks implementation | BYO AI action |
|---------|------------------------|---------------|
| **Install/check/repair lifecycle** | "Check install" + "Repair install" + self-check progress | Thin client doesn't need repair, but the **guided first-run** idea maps to our onboarding — keep it 2 taps max (C-033 direction) |
| **Remote brain mesh** | Phone → PC Hermes task dispatch (LAN/Tailscale, encrypted) | Validates LAN Ollama today; future **BYO-Sync / "run on my PC"** Pro tier (R-016 P-007) can reuse the pairing + broker design |
| **Idempotent conversation merge** | by-id upsert + natural-key dedup | Direct reference for BYO-Sync merge semantics |
| **QR pairing + NaCl Box** | Curve25519 + XSalsa20-Poly1305, public-key-only QR | Reference if we ever ship LAN pairing (overlay/mesh) |
| **Secrets-safe export** | handoff exports never touch auth.json/.env | Already matched: C-022 settings export excludes keys; chat backup is DB-only |
| **One-time Pro removes ads** | "Hermes Pro" | Already matched ($4.99 once, `BYO_pro`) — same lane, same words |
| **Multi-language presence** | READMEs in en/zh-TW/zh-CN/ja/ko/es | Supports i18n-first research (H-009 languages free) |
| **Honest install size disclosure** | Play lists "~200MB storage" plainly | We disclose 1.6MB + free-RAM matrix — same honesty law, better number |

## 7. Avoid list (why BYO AI must NOT follow them)

- **On-device agent runtime + terminal** (Node/Python/proot, ~200MB env) → Tier B/D;
  stacked OOM risk on 4GB daily drivers; multi-minute first install; "stuck progress"
  review complaints.
- **Local models via LiteRT-LM** — on-device inference on weak phones is Tier C/D
  marketing (SoftRAM-class if spun as "runs on 4GB").
- **Casino/slot games** in the portfolio — nothing to steal, and a brand-context caution
  for "developer trust" positioning (we compete on honesty).
- Their lane ≠ our lane: they win on *agent power*, we win on *instant chat on a $30
  phone*. The honest compat matrix already draws this line (Tier A green / B-D red).

## 8. Sources

- Play listing: `https://play.google.com/store/apps/details?id=com.hermesagent.android`
- Site: `https://henworks.com/`
- Companion repo: `https://github.com/HenWorks/Hermes-agent-android-PC-companion-app`
- Community Termux repo: `https://github.com/AbuZar-Ansarii/Hermes-Agent-On-Android`
- Framework (open, MIT): `https://hermes-agent.nousresearch.com/docs/`
- F-Droid fork of Hermes agent for Android: `com.mobilefork.hermesagent` (watch list)
