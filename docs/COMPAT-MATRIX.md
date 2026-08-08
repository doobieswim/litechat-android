# LiteChat compatibility matrix

Pattern stolen from **ReOldAi** (honest Android version table) and remapped to
**free RAM bands** — the real constraint on modern 4GB phones.

## Why free RAM, not installed GB

- Marketing sticker: “4GB phone”
- After GMS / OEM / launcher: often **1.5–2.5 GB free**
- SoftRAM-class fraud: claim miracle local 7B + agent on that residual heap

LiteChat surfaces `ActivityManager.MemoryInfo.availMem` and classifies:

| Band | Free RAM | Product message |
|------|----------|-----------------|
| TIGHT | &lt; 1 GB | Chat still designed for this; strip extras |
| COMFORTABLE | 1–2 GB | Default daily-driver band |
| ROOMY | 2–3.5 GB | Smooth multitask while streaming |
| GENEROUS | ≥ 3.5 GB | Chat fine; local 7B+ still not this app |

## Code

| Piece | Path |
|-------|------|
| Bands + matrix data | `app/.../util/DeviceCompat.kt` |
| Compose table + status card | `app/.../ui/CompatMatrix.kt` |
| Onboarding step 1 | `OnboardingScreen` in `Screens.kt` |
| Settings toggle | `SettingsScreen` |
| Empty chat chip | `ChatScreen` empty state |

## Legend

- 🟩 Recommended on this free-RAM band  
- 🟨 Works with caveats  
- 🟥 Do not expect / wrong product  

## Tiers (product language)

| Tier | Meaning |
|------|---------|
| A | Thin BYOK chat (LiteChat default) |
| B | Agent gateway, cloud brain only |
| C | Tiny on-device model toy |
| D | Heavy local LLM — not a 4GB daily driver |

Matrix rows encode A as green across bands; B/C/D degrade honestly.
