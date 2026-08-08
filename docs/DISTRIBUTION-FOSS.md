# FOSS distribution notes — R-003

Date: 2026-08-08  
Status: Research → Done  
Related: C-002 (foss flavor), R-001 (Play listing)

## Where weak-device users actually get APKs

The LiteChat FOSS arm targets the same audience as numAi: users running old/weak Android phones who find apps through channels the Play Store ignores.

### Channel hierarchy (by reach → LiteChat FOSS)

| # | Channel | Reach | Effort | LiteChat status |
|---|---------|-------|--------|-----------------|
| 1 | **GitHub Releases** | High (devs) | Already done | fossRelease APK builds ✓ |
| 2 | **Obtanium** | Medium (power users) | Zero — auto from GitHub | Works with GitHub Releases ✓ |
| 3 | **F-Droid** | High (FOSS users) | Medium — reproducible build + metadata | foss flavor ready, metadata TBD |
| 4 | **IzzyOnDroid** | Medium (F-Droid alternative) | Low — F-Droid compatible, faster review | Lower friction than F-Droid |
| 5 | **4PDA** | High (RU/CIS retro-Android) | Low — forum post + APK link | Worth a post |
| 6 | **XDA Developers** | Medium (enthusiast) | Low — forum thread | Worth a thread |
| 7 | **Reddit r/androidafterlife** | Low-medium (pocket audience) | Low — self-post | numAi author uses this channel |
| 8 | **Telegram @AppDataEN** | Low (niche) | Zero — link drop | Existing numAi channel |
| 9 | **OldMarket** | Low (Russian alt-store) | Unknown | numAi uses it (id=410) |
| 10 | **Play Store (foss flavor as separate listing)** | High | Medium — separate package name ($25 fee) | Consider post-v1 |

---

## Channel deep-dive

### GitHub Releases + Obtanium (primary)

**How it works:** Tag a release → CI attaches foss APK → Obtanium users auto-update.

**Strengths:**
- Zero infrastructure cost
- Obtanium handles version checking, downloads, and update notifications
- Already working — fossRelease builds in CI

**Weaknesses:**
- Requires user to know Obtanium exists
- No discovery (users must already know about LiteChat)

**Recommendation:** Primary distribution. Add Obtanium metadata URL to README.

### F-Droid

**Requirements:**
- Build must be reproducible (deterministic, no network at build time)
- No proprietary dependencies (foss flavor already meets this)
- F-Droid metadata YAML (`com.litechat.android.foss.yml`)
- Build recipe in F-Droid's server infrastructure
- Review process: days to weeks

**LiteChat readiness:**
- foss flavor: ✓ (no GMS, pure OSS deps)
- Reproducible build: partial (Gradle caching, Android SDK versions — needs F-Droid server-side verification)
- Metadata: TBD

**Recommendation:** Submit after GitHub Releases proves demand. Don't block v1 on F-Droid approval.

### IzzyOnDroid

**Why it matters:** Faster approvals than F-Droid, same F-Droid client compatibility, accepts direct APK uploads.

**Requirements:**
- Open source (✓)
- APK builds from source (✓)
- No trackers (foss flavor: ✓; play flavor has AdMob — exclude)

**Recommendation:** Submit foss APK to IzzyOnDroid first (days vs F-Droid's weeks). Users get F-Droid client access immediately.

### 4PDA (4pda.to)

**The numAi playbook:**
1. Author creates topic 1116157
2. Posts APK + changelog + screenshots
3. Community discusses bugs, feature requests
4. Author engages directly (Russian-language forum)

**LiteChat approach:**
- Post in **existing ChatGPT thread** (topic 1073274, 3500+ posts) for visibility
- OR create a dedicated LiteChat topic
- Post in **English with Russian summary** (Google Translate acceptable for first post)
- Link to GitHub Releases + F-Droid when available

**Key insight:** 4PDA users expect **direct APK downloads**, not Play Store links. Many use Android without Google services.

### XDA Developers

**The numAi playbook:**
- Thread title: `[APP][1.0+] numAi — AI client for legacy Android`
- Posts APK + source link + compatibility notes
- Forum format: `[APP][minSdk] AppName — Short description`

**LiteChat thread template:**
```
[APP][8.0+] LiteChat — Thin BYOK AI chat client for weak Android devices

LiteChat is an open-source, lightweight ChatGPT-compatible client designed for 
phones with 3–4 GB RAM. Uses your own API key — no cloud account needed.

- minSdk 26 (Android 8.0+), optimized for 4 GB devices
- OpenAI / OpenRouter / Groq / Ollama compatible
- Stream + non-stream fallback (numAi-plus pattern)
- Play version with AdMob + one-time Pro; FOSS version without GMS
- GitHub: [link]
- APK (foss, arm64): [link]
```

### Reddit r/androidafterlife

The numAi author's Reddit post got engagement. This subreddit is specifically for running modern software on old Android. LiteChat fits perfectly.

**Post strategy:** One self-post linking to GitHub. Don't spam. Engage in comments.

---

## Store listing hygiene: Play vs FOSS

| Aspect | Play Store listing | FOSS APK description (GitHub/F-Droid/4PDA) |
|--------|-------------------|-------------------------------------------|
| **Tone** | Professional, Play policy-compliant | Technical, honest about limitations |
| **Monetization** | "Contains ads + one-time Pro upgrade" | "No ads, no IAPs, no GMS" |
| **API key** | "You must provide your own API key" | "BYOK — your key, your endpoint, your billing" |
| **Compatibility** | "Requires Android 8.0+" | "Tested on 3 GB devices; see compatibility matrix" |
| **Disclaimer** | "Not affiliated with OpenAI" | "Independent FOSS client — no warranties" |
| **Screenshots** | Polished, no API keys visible | Real device screenshots, "your mileage may vary" |

**The FOSS audience expects honesty, not polish.** A compatibility matrix (already in-app) + real download sizes + known limitations builds more trust than marketing copy.

---

## Historical: pre-Play Android distribution (the culture LiteChat inherits)

### Android Market era (2008–2012)

Before Google Play was unified, Android had:
- **Android Market** (Google, 2008) — the default
- **SlideME** (2008) — alternative market for non-Google devices
- **Amazon Appstore** (2011) — Kindle Fire ecosystem
- **GetJar** (2004–2014) — cross-platform Java/Symbian/Android store
- **Aptoide** (2009–present) — decentralized app store, popular in regions without Google Play

SlideME and GetJar are dead. Aptoide still exists but has trust issues (modified APKs).

### Why sideloading became "fringe"

1. **Google Play Services lock-in (2012+):** Apps that depend on GMS can't run without Play Store infrastructure. FOSS flavor solves this by design — no GMS deps.
2. **"Unknown sources" fear (2010s):** Android's scary warning dialog trained users to fear sideloading. Google relaxed this in Android 13+.
3. **Play Protect monopoly:** Google's scanner treats non-Play APKs as suspicious by default.
4. **App Bundles (2018+):** Google's AAB format makes it harder to distribute APKs directly. LiteChat uses APK splits, not AAB — intentional for FOSS distribution.

### The 2026 landscape

Sideloading is **returning** through:
- EU Digital Markets Act (DMA) — forces alternative stores
- F-Droid growth (4,000+ apps)
- Obtanium's "direct from GitHub" model
- Increasing distrust of Play Store review quality

LiteChat's FOSS arm rides this wave. The combination of "no GMS" + "Obtanium-compatible GitHub Releases" is the modern equivalent of the SlideME/Aptoide era, minus the sketchy third-party stores.

---

## Pre-Play era channel equivalents (historical map)

| 2008–2012 channel | Died because | Modern equivalent for LiteChat |
|--------------------|-------------|-------------------------------|
| SlideME | Play Store monopoly | F-Droid |
| GetJar | Cross-platform irrelevance | GitHub Releases |
| Aptoide | Trust issues, modified APKs | Obtanium (cryptographic verification) |
| Amazon Appstore | Kindle-only, never grew | IzzyOnDroid (curated, trusted) |
| Carrier stores (Verizon V CAST) | Carriers gave up on apps | N/A — carriers don't matter in 2026 |

---

## Recommendations

### Immediate (v1 launch)
1. ✅ GitHub Releases + APK upload (already done)
2. Add Obtanium metadata to README (one-line change)
3. Post on r/androidafterlife (one self-post)
4. Create XDA thread with APK link

### Short-term (post-v1)
5. Submit to IzzyOnDroid (fast F-Droid access)
6. 4PDA post in ChatGPT thread or new topic
7. Start F-Droid metadata PR (longer review cycle)

### Long-term (v2+)
8. Consider separate Play listing for foss flavor (different package name: `com.litechat.android.foss`)
9. F-Droid official inclusion

### No new backlog ticket needed
Distribution is operational, not a coding task. The only code change is README.md Obtanium link — trivial enough to include in any nearby ticket.

---

## One-line distribution strategy

```
FOSS APK → GitHub Releases → Obtanium (auto-update) → F-Droid (eventual)
              ↓
       4PDA / XDA / Reddit (discovery, not delivery)
```