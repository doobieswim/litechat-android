# R-007 — Image generation for LiteChat (thin BYOK client)

Date: 2026-08-09
Status: Research → Done

## TL;DR

Image generation is viable for LiteChat with minimal APK impact (~200KB) and same API key. Two paths: (A) OpenAI /v1/images/generations using existing BYOK key — simplest, ~50 lines; (B) Pollinations AI free API — zero cost, no key. Recommended: Path A with Coil for display, slash-command trigger (/imagine).

## API options

### A. OpenAI Images API — SAME key as chat

| Detail | Value |
|--------|-------|
| Endpoint | POST https://api.openai.com/v1/images/generations |
| Auth | Same Authorization: Bearer header as chat |
| Models | gpt-image-2 (latest), gpt-image-1, dall-e-3, dall-e-2 |
| Cost | ~$0.04/image (1024x1024, gpt-image-2) |
| Response | {"data": [{"b64_json": "base64..."}]} |

**This is the simplest path.** LiteChat already has an API key + OkHttpClient.

### B. Pollinations AI — free, zero-key

- URL: https://image.pollinations.ai/prompt/{encoded_prompt}?width=1024&height=1024&model=flux
- GET request returns image/png directly
- No API key needed

Great free fallback for FOSS flavor.

### C. Other APIs (separate keys — not recommended for v1)

Stability AI, Replicate, FAL.ai all require separate API keys. Don't add.

## APK size impact

| Dep | Size |
|-----|------|
| coil-compose 3.5.0 | ~150KB |
| coil-network-okhttp 3.5.0 | ~40KB (reuses LiteChat's OkHttpClient) |
| **Total** | **~200KB** |

No other new dependencies needed.

## UI pattern

**Recommended: /imagine slash command**

User types "/imagine a cat in a spacesuit" → API call → image appears as chat bubble.

~80 lines of code total (API call + ViewModel + Compose UI).

## Implementation sketch

```kotlin
// In OpenAiCompatibleClient.kt:
suspend fun generateImage(prompt: String): ByteArray {
    // POST /v1/images/generations with same auth header
    // Decode b64_json from response
}

// In ChatViewModel.kt:
if (userMessage.startsWith("/imagine ")) {
    val imageBytes = client.generateImage(prompt)
    // Save to cache, insert as image message
}

// In Screens.kt:
AsyncImage(model = ImageRequest.Builder(ctx).data(File(path)).build(), ...)
```

## Cost warning

Show cost notice before first use: ~$0.04/image. Gate behind "I understand" checkbox in settings per user preference for paid APIs.

## C-011 Ready ticket

See BACKLOG.md: C-011 — Image generation via /imagine slash command. Ready for coding agent.