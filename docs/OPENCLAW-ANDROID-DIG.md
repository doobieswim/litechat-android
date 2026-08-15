# Opclaw — HenWorks' OpenClaw-on-Android (DIG deep-dive)

**Date:** 2026-08-15 · **Agent:** LITECHAT-DIG · **Status:** Research
**Clones (local):**
- `/opt/data/workspace/openclaw-android` (AidanPark, ★1734, MIT — "no proot, no Linux")
- `/opt/data/workspace/openclaw-termux` (mithun50, ★1678, MIT — Flutter app + terminal)

---

## TL;DR (plain words)

Opclaw is HenWorks' second "fat agent" app — the **APK itself is ~263 MB**. The app shell
is closed-source, but the whole category under it is open and enormous: the OpenClaw
framework has **~386K GitHub stars**, and a gold rush of community Android packagers
(★1734, ★1678, ★562, ★547, ★324, ★308, ★288, ★267, ★202…) all solve the same problem —
running a Node-based agent on a phone without installing Linux. HenWorks' marketing
("no proot, ~200MB, 3–10 min install, dashboard web UI, built-in terminal") matches the
best community approach (glibc-linker-only trick) wrapped in a one-tap Play app.

**For BYO AI:** this is the competition in the *other* lane. Their APK is ~165× our foss
APK (263 MB vs 1.6 MB). Nothing here changes the thin-client law — but it's the strongest
evidence yet that the agent lane is crowded and heavy, and ours is the honest, small one.

---

## 1. Play Store facts (verified 2026-08-15)

| Field | Value |
|-------|-------|
| App | Opclaw - OpenClaw on Android |
| Package | `com.opclaw.android` |
| Developer | Hen Works (WANG, HSING-KUO, New Taipei City, Taiwan) |
| Rating / downloads | 5.0★ · 1K+ (newer, smaller app than Hermes Agent) |
| APK size | **~263 MB** (v1.6.14, appbrain); ~285 MB at v0.3.4 (Apr 2026) |
| Min Android | 10 (API 29)+ |
| Monetization | Google Play Billing — one-time Pro pattern (same family as Hermes Agent) |
| Updated | Android 16 support + Billing update |

Play blurb: "Run AI agents directly on your Android device." henworks.com adds:
**no Linux install (skips proot-distro), ~200MB, one-line command, 3–10 min setup,
10+ providers (Claude/GPT/Gemini), phone / tablet / Android TV.**

## 2. The category underneath (open source, huge)

| Repo | ★ | What it is |
|------|---|-----------|
| `openclaw/openclaw` | **386,347** | The framework itself: "Your own personal AI assistant. Any OS. Any Platform." (MIT) |
| `openclaw/clawhub` | 9,310 | Skill + plugin registry for OpenClaw |
| `VoltAgent/awesome-openclaw-skills` | 51,955 | 5,400+ skills collection |
| `AidanPark/openclaw-android` | 1,734 | **No-proot Android packaging** (the approach HenWorks' claims match) |
| `mithun50/openclaw-termux` | 1,678 | Standalone **Flutter app** + built-in terminal + web dashboard; npm + Termux CLI |
| `rohanarun/phoneclaw` | 562 | OpenClaw/Clawdbot that automates Android phones |
| `marshallrichards/ClawPhone` | 547 | Scripts/tweaks for OpenClaw/Claude Code/Codex/Hermes on Android |
| `androidmalware/OpenClaw_Termux` | 324 | Install OpenClaw + control via WhatsApp |
| `yuga-hashimoto/openclaw-assistant` | 308 | Voice assistant (wake word) |
| `AbuZar-Ansarii/Clawbot` | 288 | Turn old Android into 24/7 agent |
| `Mohd-Mursaleen/openclaw-android` | 267 | "Native, no root, no Ubuntu, no proot" |
| `JunWan666/openclaw-termux-zh` | 202 | Chinese localization of openclaw-termux |

## 3. Packaging engineering (the interesting part)

**The no-proot trick (AidanPark, matches HenWorks' claims):**
- Standard way = proot-distro full Linux (700MB–1GB overhead, 20–30 min).
- This way = install **only the glibc dynamic linker (ld.so)** on Termux, letting the
  Node-based OpenClaw run against bionic libc. Overhead **~200MB**, setup **3–10 min**,
  native speed (no proot translation layer).

**Claw App (AidanPark) — the HenWorks-shaped shell, open:**
- Native Android Gradle project: `android/app` + `www` (WebView dashboard) +
  `terminal-emulator` + `terminal-view`.
- One-tap in-app setup: bootstrap + Node.js + OpenClaw installed from inside the app.
- Built-in dashboard: gateway control, runtime info, tool management.
- Patches dir shows the surgery needed to run the Node agent on Android:
  `glibc-compat.js`, `argon2-stub.js`, `glibc-libs`, `termux-compat.h`, `spawn.h`,
  `systemctl` stub, `patch-paths.sh`, `apply-patches.sh`.
- Scripts: `install-glibc.sh`, `install-nodejs.sh`, `install-chromium.sh`,
  `install-playwright.sh`, `install-code-server.sh`, `setup-env.sh` — a full runtime
  bootstrap.

**mithun50/openclaw-termux (Flutter):**
- Flutter app + built-in terminal + web dashboard + optional dev tools, one-tap setup.
- Also ships as npm package (`openclaw-termux`) and Termux CLI. Node 22, Android 10+.
- Monetization: **sponsor banner (Bloome, an "AI clone" platform)** — affiliate-style.

## 4. Monetization in this lane

- **HenWorks**: ads + one-time Pro (Play Billing) — same shape as Hermes Agent and us.
- **openclaw-termux**: sponsor/affiliate (Bloome).
- **Chinese forks**: localization-first distribution.
- Framework itself: MIT, free.

## 5. What this means for BYO AI

**Validates our lane.** The agent lane: 263 MB APKs, multi-minute installs, patch
surgery, sponsorship banners, a 386K-star framework and dozens of packagers fighting
over it. Our lane: 1.6 MB, install → chat in seconds, $4.99 once. The honest compat
matrix (Tier A green, B/D red) is exactly right.

**Useful in positioning (show, don't tell):** a screenshot with the real numbers
(1.6 MB vs 263 MB) without naming rivals — the SHINE doc already allows "weigh-in
numbers in a screenshot with no trash talk."

**Design references if we ever do Tier 2:**
- BYO-Sync / "run on my PC" (R-016 P-007): reuse HenWorks companion mesh patterns
  (NaCl QR pairing, idempotent merge) — see `docs/HENWORKS-HERMES-AGENT-DIG.md`.
- Guided first-run UX: the one-tap bootstrap + self-check pattern is the strong part
  of the HenWorks shell; our onboarding just needs the caveman provider picker (C-033).

**Avoid (unchanged law):** no bundled Node/agent/terminal/proot in the chat app; no
local-model marketing on 4GB phones; no sponsor banners (we do ads + one-time Pro only).

## 6. Sources

- Play: `https://play.google.com/store/apps/details?id=com.opclaw.android`
- HenWorks site (Opclaw product page): `https://henworks.com/`
- No-proot packaging: `https://github.com/AidanPark/openclaw-android`
- Flutter app: `https://github.com/mithun50/openclaw-termux`
- Framework: `https://github.com/openclaw/openclaw` · ClawHub: `https://github.com/openclaw/clawhub`
- APK size: appbrain listing for `com.opclaw.android` (263.38 MB, v1.6.14)
