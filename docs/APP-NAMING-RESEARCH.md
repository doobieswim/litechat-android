# App naming research — what to call LiteChat

**Date:** 2026-08-15
**Agent:** LITECHAT-DIG (research)
**Answers:** `docs/QUESTIONS-FOR-HUMAN.md` H-001 (app name / applicationId)
**Feeds:** BACKLOG ticket **C-031** (rebrand) — Ready for coding once the human picks a name.

---

## 1. The ask

The product owner wants a name where **people know what they're getting**. What they're getting:

1. **AI chat** (obviously a chat app)
2. **Bring-your-own-key** — the user's own API key, no subscription, no vendor cloud
3. **Lightweight** — tiny, fast, runs on weak/old 4GB phones
4. **Private** — key stays on device, no account

Test: someone scrolling the Play Store sees the name + 80-char subtitle. Do they get all four in 2 seconds?

## 2. Method

- Web searches for each candidate name + `app / android / play store` (2026-08-15).
- Verdicts: ❌ taken (same or adjacent category) · ⚠️ contested/noisy · ✅ no app found.
- Caveat: web search ≠ Play Console search. **Final check must be done in the Play Console at submission time** (search the exact name; also check the name is not "too generic" per Play's naming policy).

## 3. Collision table (26 names checked)

| Name | Verdict | What's there |
|------|---------|--------------|
| **LiteChat** (current) | ❌ | `com.sbro.litechat` "LiteChat: Offline AI Chat" — a LIVE AI-chat app. Same name, same category. Also a Bluetooth "Litechat" messenger. **Must rename.** |
| OwnKey | ❌ | Ghana property-search app + a BYOK keyboard GitHub project |
| PocketKey | ❌ | Door/access-control apps, FIDO security keys |
| AnyKey | ❌ | Keyboard apps, "AnyKey Solutions/Tech", SDKs — crowded |
| KeyChat | ❌ | Bitcoin "super app" + translator keyboard |
| NanoChat | ❌ | AI chat app (`com.fcm.nanochat`) — same category |
| BareKey | ⚠️ | barekey.net laptop key-skin hardware brand (different category) |
| KeyLite / Keylite | ⚠️ | Keylite roof-window company (UK; app discontinued; different category) |
| YourKey | ❌ | Operto hotel/vacation-rental door app |
| EmberChat | ❌ | ember.js demo app + podcast + noise |
| BrickChat | ❌ | Databricks AI chat client + LEGO AI chatbot |
| FeatherChat | ⚠️ | Removed-from-Play app + GitHub demos + P2P concept — noisy |
| OwnAI | ❌ | ownai.com — "your own AI, your data" — **exact same pitch** |
| PrivateAI | ❌ | `us.valkon.privateai` on-device AI companion |
| PersonalAI | ❌ | Play app exists |
| KeyMind | ❌ | Private AI keyboard (TestFlight) + 2 consulting firms + Halo |
| FeatherAI | ❌ | feather-ai.com data-insights AI |
| SlimAI | ❌ | Calorie-tracker app (`com.slimai.app`) |
| WispAI | ❌ | AI note-taker + AI assistant apps |
| MineAI | ❌ | "MineAI™" — trademarked by Business Mine |
| LiteKey | ❌ | Chinese identity app ("LiteKey 轻钥") + LiteKey LLC + RatTek hardware |
| **KeyPocket** | ❌ | **A BYOK AI config manager** (keypocket.net "Access your own API keys") — same category, must avoid |
| OwnChat | ❌ | WhatsApp/Instagram automation apps (2 on Play) |
| EveryKey | ❌ | Proximity smart-key login app |
| VaultChat | ❌ | E2EE messengers (Play + App Store) |
| **BYO AI** | ✅ | No app found. Phrase is hot category lingo (ZDNet, RSS clients), not an app name |
| **BYOK Chat** | ✅ | No exact app found. "BYOK" appears in competitors' *descriptions* (UnboundChat, Maskan, Agora), never as the name |
| **2G AI** | ⚠️ | No app found. Distinctive; semantic risk (reads "old/slow") |
| **FeatherKey** | ⚠️ | No app found. Needs Play Console check. Clunky to say |

## 4. What the collision map teaches

1. **Every single-word AI name is gone** (OwnAI, PrivateAI, SlimAI, WispAI, FeatherAI, MineAI, PersonalAI, KeyMind…).
2. **Every "Key" name is gone** — and not by coincidence. Physical-key apps (doors, hotels, FIDO, password managers) ate the whole word-space. "KeyPocket" is even a BYOK AI tool already.
3. **The successful BYOK apps on Play today don't put "Key" in the name.** Agora, Maskan, UnboundChat — they name the *value* (community, privacy, freedom) and put the BYOK mechanic in the description. We should do the same.
4. Category-literal naming *works* when the phrase is free: the "BYOK" search term is exactly what key-owning users type. No app owns that name yet.

## 5. Historical naming lineage (how thin-client software got named)

Mapping old naming patterns to our choice, since "lightweight + user-owned" is a 40-year-old genre:

| Era | Pattern | Example | Lesson for us |
|-----|---------|---------|---------------|
| 90s shareware | platform+function | WinZip, WinAmp | Category-literal works when you're first — **BYO AI / BYOK Chat = "WinZip of AI chat"** |
| Palm (1998–2005) | "Pocket X" | PocketDex, Pocket Quicken | Naming the *form factor* (small/portable) — our "runs on weak phones" |
| J2ME / feature phones | brand+Mini | **Opera Mini** (2005) | The archetype: thin client named for its size. "Mini/Lite" pattern is now saturated |
| Password managers | ownership verbs | KeePass, Bitwarden, 1Password | You-own-the-secrets naming — the direct ancestor of BYOK. Their lesson: name the *feeling* (keep, guard), not the *mechanic* (key) |
| Modern BYOK apps | value words | Agora, Maskan, UnboundChat | Name the value; the "bring your own key" mechanic goes in the subtitle |
| Demoscene 4K/64K | size as brand | 64K intros | Size-as-brand is memorable but only if unique ("2G" style) |

**Conclusion:** the two viable strategies are (a) category-literal but free (**BYO AI**, **BYOK Chat** — the "WinZip move") or (b) value-word coined name. Strategy (a) wins here because the user explicitly wants clarity over cleverness.

## 6. Shortlist + recommendation

### 🥇 Recommended: **BYO AI**
- "BYO" = the everyday English phrase "bring your own" (BBQ culture: BYO beer, BYO wine). Warm, human, instantly readable.
- Says: you bring it (your key), it's AI, it's yours.
- Subtitle (Play): *"Chat with AI using your own key — no subscription, no account, runs on any phone."*
- Collision: none found. Easy to say, easy to spell, 6 chars for the icon.
- App id candidate: `com.byoai.chat` (or keep `com.litechat.android` — invisible to users).

### 🥈 Alternate: **BYOK Chat**
- The exact category term ("Bring Your Own Key"). Developers and key-owners get it in zero seconds; SEO-perfect for the search term "BYOK chat" (which currently surfaces competitors — we'd own the name).
- Colder/jargon-y for normies; "BYOK" also means enterprise *encryption-key management* to IT folks.
- Subtitle: *"Bring your own key. Chat with OpenAI, Google, OpenRouter & more — no subscription."*

### 🥉 Alternate: **2G AI**
- Retro charm: "AI that runs on a 2G phone." Extremely memorable, strong weak-phone story, demoscene "size as brand" energy.
- Risks: reads "old/slow" to some; doesn't say BYOK at all (subtitle must carry it).

### ❌ Rejected: **LiteKey / KeyPocket / FeatherKey / any "Key" coinage**
- Key-space is contaminated (see §4). KeyPocket is literally a BYOK tool already — the worst possible collision.

## 7. Technical constraints for the rename (C-031)

1. **Visible name ≠ app id.** Users see `android:label` (`strings.xml` `app_name`) + Play listing title. The applicationId is invisible. So the *cheap* rename is: strings.xml + Play listing + README + docs + privacy.html.
2. **applicationId is immutable after first Play release.** If we change `com.litechat.android`, do it NOW (pre-release, zero users). Recommend: **change applicationId** to match the new brand (`com.byoai.chat` / `com.byok.chat`), keep the Kotlin `namespace` and package as `com.litechat.android` (1-line Gradle change, zero code churn — renaming the package across ~100 files violates the small-diffs law and buys nothing).
3. Play title limit: **30 characters** — all candidates fit.
4. GitHub repo `flamingspade1995-coder/litechat-android` can stay as the internal codename; only user-facing branding needs the new name.
5. 453 repo references to "LiteChat/litechat" — mostly docs; scope C-031 to user-facing files, docs can note "internal codename LiteChat" once.

## 8. Open items (not blocking)

- [ ] Final name availability check in Play Console (search exact name) at submission time
- [ ] Domain check (byo.ai / byok.chat) if a landing page is wanted
- [ ] Icon/logo once the name is picked (out of scope for C-031)
- [ ] H-003 Pro price still needs a human answer (independent of name)

## 9. Decision

**Locked 2026-08-15:** **BYO AI**  
Short description: *Chat with your own key. Works on 4GB phones. No monthly bill.* (61/80)  
4GB is in the line under the name + a real screenshot. Not in the 6-letter title.  
applicationId (pre-Play only): `com.byoai.chat`  
Internal codename: LiteChat (repo / package may stay).

C-031 stays **Research** until `LITECHAT-PROOF` Approves the naming pack. Then Ready for WIRE.
