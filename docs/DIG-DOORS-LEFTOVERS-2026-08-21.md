# DIG — leftover doors after the 2026-08-21 WIRE

Date: 2026-08-21  
Role: `LITECHAT-DIG`  
Scope: official pages only. No `app/**`. Ticket stays **Research**.

WIRE already coded host-based `/imagine` `/video` `/edit` STT/TTS. REVIEW **Approve**. These are the leftovers that would still 404 or bill by surprise.

## 1. xAI `/edit` is JSON, not OpenAI multipart (proven)

**Source:** https://docs.x.ai/developers/model-capabilities/images/editing (updated 2026-08-13)

xAI says the OpenAI SDK `images.edit()` is **not** supported because it uses `multipart/form-data`. Their door is:

- `POST https://api.x.ai/v1/images/edits`
- `Content-Type: application/json`
- Body: `model`, `prompt`, `image: { url, type }` (public URL **or** `data:image/png;base64,...`)
- Model: `grok-imagine-image-2.0`

Our WIRE still posts OpenAI multipart to that URL. That will fail. **R-020.**

Cost: xAI Imagine is paid (about $0.04 / image on their page). Flag before anyone pastes a Grok key.

## 2. OpenAI Sora shuts 2026-09-24 (proven)

**Source:** https://developers.openai.com/api/docs/guides/video-generation

`sora-2`, `sora-2-pro`, and the Videos API **sunset 2026-09-24**. No replacement id on that page. After that date, honest line is “OpenAI cannot make videos.” Do not invent a new Sora name.

Until then `sora-2` on `POST /v1/videos` is still the documented door. No Ready ticket — calendar, not a bug today (today is 2026-08-21).

## 3. Groq Compound is chat-OK but tools cost money (proven)

**Source:** https://console.groq.com/docs/compound/systems/compound

`groq/compound` is a real chat id (`chat.completions`). Built-in web search is **$5 / 1000** requests (advanced $8). Code execution is billed by the hour. Groq’s picker tagline is still “Free key.” Compound is not a free-as-in-beer tool.

Not a Ready ticket. If WIRE keeps Compound in the list, the label must say it can cost money.

Llama 3.1 / 3.3 are gone from Groq’s production table (only GPT-OSS 20B/120B + Whisper). Already remapped in WIRE.

## 4. OpenRouter free chat names (weak)

The `$0` models page mixes embeddings, TTS, and chat. Seen free **chat-shaped** ids: `liquid/lfm-2.5-2.6b:free`. `dots-studio/dots-3-note-preview:free` **goes away 2026-09-30**. Our picker `google/gemma-4-26b-a4b-it:free` was **not** on the first screen of that page — do not claim it is current without a dedicated model URL.

`openrouter/free` auto-route is still the safest first pick. No Ready to reshuffle the OpenRouter list.

OpenRouter **pictures** stay `POST /api/v1/images` (already WIRE). OpenRouter **video generation** is not the same as their video-input docs. Keep refuse.

## 5. What not to build

- Gemini native image **edit** (generateContent with an image part) — not researched this pass.
- Groq TTS (Orpheus is preview, priced per character) — keep refuse.
- Hugging Face / DeepSeek / Mistral / Ollama pictures — still no matching door.

## Ticket

**R-020** — xAI `/edit` JSON body. Files: `OpenAiCompatibleClient.editImage`. Status: Research until `LITECHAT-PROOF`.
