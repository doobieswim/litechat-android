# DIG — why pictures died, and what the keys/models actually are

Date: 2026-08-21  
Role: `LITECHAT-DIG`  
Phone fact: `/imagine` showed `Unexpected char 0x2076 at 0 in x-goog-api-key` and printed the key on screen.  
Does **not** change B-001 (already Ready). Status of new ticket: **Research** until PROOF.

---

## Everyday summary

Pictures failed **on the phone**, before Google. A junk character **⁶** (tiny six) got stuck on the front of the key. The HTTP library refused to send it. That is not “Gemini cannot draw.”

A key that starts with **`AQ.`** might still work. Old keys often started with `AIza`. A Jul 2026 **forum** thread (not a Google product page) says Studio also mints `AQ.` keys. Do not tell people to throw `AQ.` keys away just because of the prefix. If Google then 401s, show Google’s words.

The picture **model names we already ship** still match Google’s list today (checked 2026-08-21).

---

## 1. The ⁶ character (proven)

- Unicode U+2076 = superscript six. Looks like a small `⁶`.
- Gboard / paste put it in front of the key. The Settings box can hide it (field scrolled).
- OkHttp `Headers` only allow normal header bytes (roughly space through `~`). U+2076 is illegal.
- `header("x-goog-api-key", apiKey)` throws **before** any network call.
- Same throw on `Authorization: Bearer $apiKey` (Settings **Test**).
- **Do not** use OkHttp `addUnsafeNonAscii` to “fix” this. That would send junk to Google. Strip the junk.

B-001 already says: strip on save + strip before every header. DIG agrees. No new Ready child.

OkHttp’s throw message **includes the full header value**. That is the key. Phone screenshot proved it. Our chat then does `e.message.take(200)` (`ChatViewModel.kt` `/imagine` catch ~717 and ~722). The bubble showed the key. That is a key-leak, not just a failed picture.

---

## 2. `AQ.` vs `AIza` (do not gate on prefix)

**Forum, not Google docs.** Google AI Developers thread, Jul 2026 (Uphold_Brasil / Payal_Sharma2): some people now get keys that start with `AQ.` instead of `AIzaSy`. One reply says Studio mints “Authentication Key (AQ)” keys. That is **not** an opened Google product page. Treat it as a field report.

Everyday rule for WIRE / copy:

- Do **not** reject a key because it starts with `AQ.` instead of `AIza`.
- Never tell the user “your key is wrong because it is not AIza.”
- After B-001, a clean `AQ.` key is allowed to *try* Google. If Google 401s, show Google’s words, not ours guessing the prefix.

---

## 3. Picture models (Google, page updated 2026-08-14)

Go-to picture model: **Gemini 3.1 Flash Image** (`gemini-3.1-flash-image`, “Nano Banana 2”).

| Name on Google | Id | Already in our catalog? |
|----------------|----|-------------------------|
| Nano Banana 2 | `gemini-3.1-flash-image` | yes (first pick) |
| Nano Banana 2 Lite | `gemini-3.1-flash-lite-image` | yes (fallback) |
| Nano Banana Pro | `gemini-3-pro-image` | yes (fallback) |
| Nano Banana (2.5) | `gemini-2.5-flash-image` | yes (fallback) |

Chat Flash ids `gemini-3.6-flash` / `gemini-3.7-flash` are for **talk**, not `/imagine`. `/imagine` must keep using the **image** ids above, not the chat model in the picker.

Door stays native: `POST {v1beta}/models/{id}:generateContent` + **`x-goog-api-key` only**. No Bearer on that door. No OpenAI `/images/generations` for Gemini.

Google now also pushes an “Interactions API.” We do **not** need it for v1 pictures. Extra surface, extra RAM. Stay on generateContent until a later ticket.

Sources:

- https://ai.google.dev/gemini-api/docs/models (2026-08-14)
- https://ai.google.dev/gemini-api/docs/image-generation (2026-08-10)
- https://discuss.ai.google.dev/t/problemas-com-chave-de-api-do-gemini-formato-aq-gerado-em-vez-de-aizasy-requisicoes-do-generativeservice-bloqueadas/174977

---

## 4. What DIG is not doing

- Not coding B-001 (WIRE).
- Not flipping any ticket to Ready.
- Not baking an APK.
- Not changing picture model ids (they already match).

---

## 5. Proposed Research ticket

**R-019** — When a header is illegal, do not paste OkHttp’s exception (it contains the key) into the chat bubble or the red banner. Everyday line instead: “This key has a bad character. Delete it and paste again.”

PROOF **Approve** 2026-08-21. DIG stamped **Ready** same day.
