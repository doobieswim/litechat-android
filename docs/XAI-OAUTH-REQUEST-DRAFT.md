# Draft — request to xAI for a SuperGrok OAuth client (BYO AI)

**Status:** ~~Draft for the human to send (H-012, 2026-08-21). Not sent by any agent.~~ **BLOCKED 2026-08-21 — waiting on human's Gmail address + app password.** Human said "i set the email" but the credentials never reached the session. Send steps are at the bottom of this file.
**Send to:** xAI developer support / API team (support@x.ai or console.x.ai help) — human picks the channel.
**Re-check before sending:** `https://docs.x.ai` for any new "register your app / OAuth client" page. If xAI now has a self-serve app-registration portal, use it instead of this email.

---

**Subject: OAuth device-code client request for a thin Android BYOK client (SuperGrok quota login)**

Hello xAI team,

I'm the developer of **BYO AI** (`com.byoai.chat`), a lightweight Android chat client (~1.6 MB, works on 4 GB phones) where users bring their own API keys. Full disclosure up front: this is the same ask Hermes Agent received — we are not affiliated with Hermes/Nous, we are not copying their `client_id`, and we are asking for our **own** OAuth client registration.

**What we'd like:** a **device-code OAuth client** (`client_id` + secret) so BYO AI users who already pay for **SuperGrok (or X Premium+)** can tap **"Sign in with Grok"** and use their existing subscription quota — no separate API key, no extra bill.

**Why it fits:** your docs and the Hermes integration show the device-code flow at `accounts.x.ai` / `auth.x.ai` already works for subscription quota. Our users are exactly the people who pay for SuperGrok and would love one-tap access on a cheap phone. We are happy to:

- Use the standard device-code flow + PKCE, browser-based authorization (no token handling outside the OS browser).
- Display only xAI-hosted branding and the official consent screen — no fake "Sign in with Grok" UI until you issue the client.
- Publish the client registration in our open-source repo (`https://github.com/doobieswim/litechat-android`) so it is auditable.
- Keep the existing API-key path as the fallback (paste-key + "can cost money" line).

**What we need from you:** a `client_id` for `com.byoai.chat` (device-code / public-client, no redirect URI needed) and any rate/scope notes. We understand some SuperGrok tiers may 403 on OAuth (issue #26847) — a short note on which tiers are supported would help us gate the button honestly.

Thank you for considering this — happy to answer any questions.

— BYO AI developer

---

## How to send (for a future session — run AFTER the human pastes two lines: their Gmail address + a 16-letter Gmail app password)

`himalaya` v2.1.0 is installed at `~/.local/bin/himalaya` (export PATH). Config: `~/.config/himalaya/config.toml` (chmod 600). Gmail app password: 2-Step Verification must be ON (myaccount.google.com → Security → App passwords).

```toml
[accounts.personal]
email = "YOU@GMAIL.COM"
display-name = "BYO AI developer"
default = true

backend.type = "imap"
backend.host = "imap.gmail.com"
backend.port = 993
backend.encryption.type = "tls"
backend.login = "YOU@GMAIL.COM"
backend.auth.type = "password"
backend.auth.cmd = "cat /opt/data/.gmail-app-pass"

message.send.backend.type = "smtp"
message.send.backend.host = "smtp.gmail.com"
message.send.backend.port = 587
message.send.backend.encryption.type = "start-tls"
message.send.backend.login = "YOU@GMAIL.COM"
message.send.backend.auth.type = "password"
message.send.backend.auth.cmd = "cat /opt/data/.gmail-app-pass"

folder.aliases.inbox = "INBOX"
folder.aliases.sent = "[Gmail]/Sent Mail"
folder.aliases.drafts = "[Gmail]/Drafts"
folder.aliases.trash = "[Gmail]/Trash"
```

Then send the draft body above with:

```bash
export PATH="$HOME/.local/bin:$PATH"
cat << 'EOF' | himalaya template send
From: YOU@GMAIL.COM
To: support@x.ai
Subject: OAuth device-code client request for a thin Android BYOK client (SuperGrok quota login)

<paste the draft body from the top of this file>
EOF
```

**Do NOT:** store the real Google password (app password only, in `/opt/data/.gmail-app-pass`, chmod 600); print the app password into chat; use Hermes's `client_id`; enable the OAuth build until xAI issues **this app** its own client.

