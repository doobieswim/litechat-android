# Play listing pack — R-001

Date: 2026-08-08  
Status: Research → Done  
Related: R-003 (distribution), C-007 (Settings wiring)

## Privacy policy outline

LiteChat is a thin BYOK client. For Play Console Data Safety declaration:

### What we do

- **No data collected by us.** The app does not send any data to LiteChat developers or any third-party server we control.
- **API key:** Stored encrypted on-device using Android Keystore (Tink/AES-GCM via `EncryptedSharedPreferences`). Never transmitted to anyone except the user's configured base URL.
- **Chat messages:** Send directly from device → user's configured OpenAI-compatible API endpoint. Never pass through our servers.
- **Device diagnostics:** None. No Firebase, no Crashlytics, no analytics SDK.

### Play Data Safety form (recommended answers)

| Section | Answer | Justification |
|---------|--------|---------------|
| Data collection | "No data collected" | We don't collect anything — the user owns their key and endpoint |
| Data shared | "No data shared with third parties" | No third-party SDKs beyond AdMob (play flavor only), which has its own declaration |
| Encryption | "Data encrypted in transit" | OkHttp uses TLS to user's endpoint |
| Data deletion | "No data to delete" | Nothing on our servers |

### Privacy policy URL

For "No data collected" apps on Play, a privacy policy is still required. Host a single HTML page:

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>LiteChat Privacy Policy</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <style>
    body { max-width: 640px; margin: 0 auto; padding: 20px; font-family: system-ui; color: #eee; background: #1a1a2e; }
    h1 { border-bottom: 1px solid #333; padding-bottom: 8px; }
    a { color: #7c9; }
  </style>
</head>
<body>
  <h1>LiteChat Privacy Policy</h1>
  <p><strong>Last updated:</strong> [DATE]</p>

  <h2>What LiteChat does</h2>
  <p>LiteChat is an unofficial, open-source chat client that connects to
  OpenAI-compatible API servers using <strong>your own API key</strong>.</p>

  <h2>Data we collect</h2>
  <p><strong>We collect nothing.</strong> LiteChat does not use any analytics,
  crash reporting, or telemetry services. The app does not contain any tracking
  SDKs (apart from Google AdMob in the Play Store version — see below).</p>

  <h2>Data you enter</h2>
  <ul>
    <li><strong>API key:</strong> Stored encrypted on your device using Android
    Keystore. Only sent directly to the API server you configure (base URL).</li>
    <li><strong>Chat messages:</strong> Stored locally in an on-device SQLite
    database. Sent only to your configured API server during conversations.</li>
    <li><strong>Settings:</strong> Base URL, model name, temperature — stored
    locally in Android DataStore.</li>
  </ul>

  <h2>Data shared with third parties</h2>
  <p>None for the FOSS version. The Play Store version includes Google AdMob
  for banner ads. AdMob has its own data collection practices governed by
  Google's privacy policy.</p>

  <h2>Data security</h2>
  <p>All network requests use TLS (HTTPS). Your API key is encrypted at rest
  using Android's hardware-backed Keystore where available.</p>

  <h2>Your rights</h2>
  <p>Since we collect no data, there is nothing to request access to, correct,
  or delete. All your data stays on your device.</p>

  <h2>Contact</h2>
  <p>For questions: [CONTACT_EMAIL or GitHub Issues link]</p>
</body>
</html>
```

---

## Unofficial BYOK disclaimer

The disclaimer must appear in:
1. Play Store "About this app" section (first paragraph)
2. In-app Settings → About (or dedicated "About" screen)
3. README.md

### Draft (Play Store description opening)

> LiteChat is an **unofficial, open-source client** for OpenAI-compatible APIs
> (OpenAI, OpenRouter, Groq, Ollama, and others). It is **not affiliated with,
> endorsed by, or connected to OpenAI, Google, Anthropic, or any AI provider.**
> You bring your own API key — LiteChat does not provide, proxy, or resell API
> access. All chat data travels directly between your device and the API server
> you configure.

### Historical context: the "unofficial client" tradition

The BYOK disclaimer traces back to:

- **ICQ clones (1996–2001):** Miranda, Licq, SIM-IM — all declared "not affiliated with Mirabilis / AOL." The key legal insight: using an API ≠ endorsing the vendor.
- **Palm HotSync conduits:** Third-party apps that synced with Outlook/Notes always disclaimed "not affiliated with Palm, Inc. or Microsoft." The disclaimer was a shield against the platform owner.
- **Winamp skins / plugins (1998–2004):** Nullsoft tolerated the ecosystem as long as third-parties didn't claim endorsement.
- **Steve Jobs' "sherlocking" (2002):** Third-party devs learned to disclaim affiliation after Apple absorbed their features into the OS.
- **OpenAI API launch (2020):** Official API means third-party clients are explicitly permitted, reducing legal risk — but the disclaimer remains good practice.

The pattern: **state what you are NOT, then state what you ARE.** "Not affiliated with X" → "An independent BYOK client that connects your key to X-compatible servers."

---

## C-007 ticket (Ready for coding)

### C-007 — Wire privacy link and disclaimer in Settings

- **Status:** Ready  
- **Goal:** Add privacy policy link and unofficial BYOK disclaimer to Settings/About section so the app is Play Store submission-ready.  
- **AC:**
  - [ ] Settings screen has "Privacy Policy" link opening `https://[HOST]/litechat-privacy.html` in the device browser (or bundled HTML as fallback)
  - [ ] Settings screen shows the BYOK disclaimer text (one paragraph, not scrollable-to-oblivion)
  - [ ] README.md includes the disclaimer wording
  - [ ] Both links/strings are excluded from `verify_static.py` word-count caps (they're UX copy, not code bloat)
- **Touch:** `Screens.kt` (Settings section), `README.md`  
- **Research:** `docs/PLAY-LISTING-DRAFT.md` — privacy HTML template + disclaimer text  
- **Out of scope:** actual Play Store submission, hosting the privacy page, contact email setup

---

## Play Store "About this app" meta

Category: Productivity (or Tools)  
Tags: "chat", "openai", "api", "byok", "lightweight"  
Content rating: Everyone (no user-generated content sharing, no chat rooms)  
Target audience: 18+ (BYOK requires API key ownership = credit card)

---

*Disclaimer: This is a research deliverable, not legal advice. Review with a lawyer before Play submission if operating as a business.*