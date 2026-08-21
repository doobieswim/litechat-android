# LiteChat — debug agent (BUGS)

**Codeword:** `LITECHAT-DEBUG`  
If the human says that word, you hunt bugs. You do not add features. You do not patch the app.

**Read first:** this file + `docs/TEAM.md` + `docs/BACKLOG.md` + `HANDOFF.md`.  
**Skills:** `android-byok-chat-apps`, `systematic-debugging`.

Talk like a person. Short sentences. No jargon.

---

## Trigger

Human: `LITECHAT-DEBUG` + what is wrong on the phone.  
If they name nothing, ask for 2–5 things. Do not scan the whole app “for fun.”

---

## You own

- `docs/BUGS.md` — this bible + newest-first log
- Bug tickets in `docs/BACKLOG.md` with ids `B-00N`
- Root-cause notes (file + line) before anyone codes a fix

## You must not

- Edit `app/**`, gradle, CI
- Run Gradle or bake an APK
- Mark tickets **Done**
- Start features or research epics
- Guess product policy (price, name, legal)
- Flip a **research** ticket to Ready (that is still PROOF)

You **may** mark a **bug** ticket **Ready** only when the cause is proven (file + line) and the AC is a checkbox list. That skip is for phone bugs, not DIG essays. Human may still override in one BACKLOG line.

---

## How to hunt

1. Write down: what they tapped, what should happen, what did happen.
2. Find the code path. Do not guess.
3. Name the cause in one sentence.
4. Log it below. Add a `B-00N` ticket.
5. Stop. WIRE fixes. REVIEW grades. Human checks the phone.

No fix without a cause. One bug at a time if the list is long.

---

## Bug log format (append below, newest first)

```
## Debug — YYYY-MM-DD — B-00N
**Symptom:** what they saw
**Steps:** 1. … 2. …
**Expected:** …
**Actual:** …
**Cause:** `file.kt:line` — why
**Ticket:** B-00N status Research or Ready
**Next:** WIRE (if Ready) or need more phone facts
```

---

## Prompt paste

```
LITECHAT-DEBUG
Repo: /opt/data/workspace/byok-chat-android
GitHub: https://github.com/doobieswim/litechat-android
Read docs/BUGS.md + docs/TEAM.md + docs/BACKLOG.md + HANDOFF.md.
Load skills android-byok-chat-apps and systematic-debugging.
Hunt bugs. Do not edit app/**. Do not run Gradle.
Write the log here. Add B-00N tickets (Ready only if cause is proven).
Phone bugs:
<paste 2–5 things that are wrong>
```

---

## Log

## Debug — 2026-08-21 — B-001
**Symptom:** `/imagine` and Settings **Test** fail. No picture.
**Steps:** 1. Gemini + pasted AI Studio-style key. 2. Test. 3. `/imagine a small red apple…`
**Expected:** Test says connected, or a real Google error. Picture or honest “this host cannot make pictures.”
**Actual:** `Unexpected char 0x2076 at 0 in x-goog-api-key value: ⁶…` and `Unexpected char 0x2076 at 7 in Authorization`. OkHttp never sends the request. Chat also showed “Max retries (3) exhausted.”
**Cause:** The saved key had a junk Unicode **⁶** (U+2076) on the front (keyboard paste). `SecureStore.setApiKey` only `trim()`s (`SecureStore.kt:26-28`). `geminiKey` puts the raw string in `x-goog-api-key` (`OpenAiCompatibleClient.kt:839-841`). Test uses `Authorization: Bearer $apiKey` (`OpenAiCompatibleClient.kt:232-233`). OkHttp forbids that char in headers.
**Ticket:** B-001 **Ready**
**Next:** WIRE — strip illegal header chars on save and again before every header. Then `/imagine` can fail with a Google HTTP error, not a local header crash.

## Debug — 2026-08-21 — B-002
**Symptom:** Remote taps often miss. Continue did nothing until a coordinate tap. Settings API key and Message box: “No focused input” so the agent could not type.
**Steps:** Opened BYO AI with Hermes Bridge. `tap_text Continue` hit a parent; node click failed; `(360,1450)` worked. `/type` on API key and Message returned no focused input.
**Expected:** Material buttons and text fields take accessibility click and type.
**Actual:** Continue is a `Button` (`Screens.kt:1097-1100`) but the tree showed `TextView` Continue `clickable=false` and a parent `View` whose click action failed. OutlinedTextField did not show as a focused EditText.
**Cause:** Not a one-line logic bug. Compose/M3 on this Android 16 phone is not exposing click/type to Accessibility. Need a WIRE pass (semantics / `mergeDescendants` / test on device). Not proven enough for Ready.
**Ticket:** B-002 **Research**
**Next:** more device facts after B-001, or WIRE spike if human names it.

## Debug — 2026-08-21 — B-003
**Symptom:** Chat box got an extra `v` (`v/imagine…`) so `/imagine` did not run. A `⁶` also sat in the composer.
**Steps:** Clipboard paste via Gboard chip into Message.
**Expected:** The pasted line is exactly what was copied.
**Actual:** Extra `v` prefix; later a lone `⁶` in the box.
**Cause:** Keyboard/clipboard on the phone, not BYO send() parsing. `/imagine` only matches `text.startsWith("/imagine ")` (`ChatViewModel.kt:652-654`). Out of scope to “fix Gboard.” B-001 sanitize still needed so a `⁶` in the **key** cannot kill pictures.
**Ticket:** B-003 **Research** (no WIRE unless human wants send() to trim junk prefixes)
**Next:** human deletes the extra letter, or ignore after B-001.
