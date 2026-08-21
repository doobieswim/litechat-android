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

## Debug — 2026-08-21 — hunt (B-005–B-010)
**Symptom:** Independent tree hunt (no Gradle, no `app/**` edits). Did not re-open B-001 (Done) or B-004 (Done).
**Checked OK in this tree:** `/browse` `/imagine` `/video` `/edit` `/search` assign `streamJob`; Stop FAB is `isStreaming || isGeneratingImage` (`Screens.kt:588`); primary key is `PasswordVisualTransformation` (`ProviderSetupFields.kt:119`); `searchJob?.cancel()` + stale ignore (`ChatViewModel.kt:263-268`); attach copies to a temp file then samples (`ChatViewModel.kt:1201-1231`, no photo `readBytes`); Room has no `fallbackToDestructiveMigration` (`AppContainer.kt:23-34`).
**B-003 note:** peel landed in `SlashInput.kt:13-20` and `send()` uses it (`ChatViewModel.kt:417`). Ticket stays **Research** (IME, not proven as send() logic).
**B-002 note:** still **Research**. Composer attach/voice are `Modifier.size(40.dp)` (`Screens.kt:566-568`) — under 48.dp, may miss taps, not a full a11y proof.

## Debug — 2026-08-21 — B-010
**Symptom:** Chat can show `(empty response)` after retries.
**Steps:** Send a normal chat turn. Provider returns SSE with no `choices[0].delta.content` (or only empty role frames).
**Expected:** One non-stream retry, or an honest error. Not a blank bubble after 3 tries.
**Actual:** Empty acc + no `streamError` skips fallback (`OpenAiCompatibleClient.kt:169-171` needs `streamErr != null`). Then `ChatViewModel.kt:1024-1026` writes `(empty response)` and retries.
**Cause:** Not a one-line phone proof this session (no live stream). Parser only reads `delta.content` / `text` (`ChatSseParser.kt:71-78`).
**Ticket:** B-010 **Research**
**Next:** phone capture of a real empty bubble, or WIRE only if human names it.

## Debug — 2026-08-21 — B-009
**Symptom:** Overlay Send may do nothing / overlay cannot type.
**Steps:** Settings → Floating overlay on → type in the float box → Send.
**Expected:** Keyboard works; Send answers.
**Actual (code):** Send exists (`OverlayService.kt:139-207`) and caps paste (`:122`). Window is `FLAG_NOT_FOCUSABLE or FLAG_ALT_FOCUSABLE_IM` (`:224-225`). Empty input returns at `:141-142`. Fail path dumps `e.message` (`:199`).
**Cause:** Flags + IME are OEM-specific. Not proven on a phone this hunt. Not Ready.
**Ticket:** B-009 **Research**
**Next:** phone: can you type in the overlay? Does Send fire?

## Debug — 2026-08-21 — B-008
**Symptom:** “Clear memory” wipes stored facts with no confirm, even when not Pro.
**Steps:** Settings → Clear memory.
**Expected:** Pro-only, and a confirm like “Clear all chats?”.
**Actual:** Button always shown (`Screens.kt:1642-1645`). `clearMemory()` has no `isPro` check (`ChatViewModel.kt:1283-1286`). Chat clear uses a dialog (`Screens.kt:1676`).
**Cause:** Gate and confirm never wired on this path.
**Ticket:** B-008 **Ready**
**Next:** WIRE — gate + confirm. Do not touch B-004 memory JSON encode.

## Debug — 2026-08-21 — B-007
**Symptom:** Stop during `/video` does not stop. Failed video can sit until timeout (~5 min).
**Steps:** 1. `/video …` 2. Tap Stop while it is polling. Or: provider returns HTTP 401 on poll.
**Expected:** Stop cancels now. Bad HTTP fails now with an honest line.
**Actual:** `stopStreaming()` cancels `streamJob` + `activeCall` (`ChatViewModel.kt:389-392`). Poll loops use blocking `Thread.sleep` (`OpenAiCompatibleClient.kt:817` Veo 10s, `:846` xAI 5s, `:880` Sora 2s) inside `withContext(IO)` (`ChatViewModel.kt:638-644`). Sleep is not a cancel point; the next poll starts a **new** call. Veo poll never checks `isSuccessful` (`:802`) so 401 `{error}` with no `done` loops until `timeout` (`:778` default 300_000).
**Cause:** B-004 assigned `streamJob`; leftover is the poll sleep + ignored HTTP.
**Ticket:** B-007 **Ready**
**Next:** WIRE — `delay()` (cancellable) + fail on non-2xx poll. Out of scope: new APK unless asked.

## Debug — 2026-08-21 — B-006
**Symptom:** Secrets typed in Settings are visible.
**Steps:** Settings → Saved keys → type API key. Also: Backup password field.
**Expected:** Dots, like the main API key box.
**Actual:** Named-key value is a plain `OutlinedTextField` (`Screens.kt:1476-1480`). Backup password is plain (`Screens.kt:1607-1613`). Main paste box is already masked (`ProviderSetupFields.kt:119`) — that was B-004, do not re-open.
**Cause:** Mask pass never covered the add-key row or backup field.
**Ticket:** B-006 **Ready**
**Next:** WIRE — `PasswordVisualTransformation` on both.

## Debug — 2026-08-21 — B-005
**Symptom:** Chat backup can be a raw SQLite file. Anyone with the file reads every chat.
**Steps:** Pro → Backup chats with password box empty (label says optional) → share the file.
**Expected:** Encrypted export (P-003). Empty password refuses, or encrypts with a required pass.
**Actual:** Label “Backup password (optional)” (`Screens.kt:1612`). `exportChats` copies the DB bytes when pass is blank (`ChatViewModel.kt:1126-1130` `inn.copyTo(out)`). Process death after SAF also leaves `pendingBackupPass` empty → same plaintext path.
**Cause:** Optional pass + copyTo, not AES.
**Ticket:** B-005 **Ready**
**Next:** WIRE — require a pass (or refuse export); never `copyTo` the live DB.

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
