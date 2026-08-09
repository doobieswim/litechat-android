# R-009 — Deep-dive: C-009, C-010, C-015, C-016

Date: 2026-08-09
Status: Research → Done (subagents pending for C-009/C-010/C-015 details)

---

## C-016: Cloud OCR via vision model

### Approach: Image attachment + GPT-4V/Claude Vision

The original brief ("snapshot screen → OCR via vision model") requires screen capture, which is complex. The simpler, equally powerful approach:

**User takes a normal screenshot** (Power+VolDown on Android) → taps `/ocr` in LiteChat → attaches the screenshot from gallery → LiteChat sends it to GPT-4V/Claude Vision via existing API key → text extracted and inserted into chat.

### Why this avoids MediaProjection hell

| Approach | Permission needed | User friction |
|----------|------------------|---------------|
| MediaProjection API | System dialog every time | High — "Start recording or casting?" scary dialog |
| Normal screenshot + attach | None (gallery picker) | Low — normal Android screenshot + tap attach |
| View.drawToBitmap() | None | Only captures own app, not other apps |

MediaProjection requires `SYSTEM_ALERT_WINDOW` + a foreground service + user consent dialog every time. For a chat app, this is overkill. The gallery-attach approach is zero-permission and works on all Android versions.

### Vision API payload format

OpenAI Chat Completions with image (same endpoint, same key):

```json
{
  "model": "gpt-4o",
  "messages": [{
    "role": "user",
    "content": [
      {"type": "text", "text": "Extract all text from this screenshot"},
      {"type": "image_url", "image_url": {"url": "data:image/png;base64,iVBORw0KGgo..."}}
    ]
  }]
}
```

Claude Vision uses a different format but same concept (base64 in content blocks). Gemini also supports base64 images.

### Implementation sketch

```kotlin
// In ChatViewModel.kt — add to send() detection:
if (text.startsWith("/ocr")) {
    // Show image picker, then:
    val bitmap = /* from gallery picker */
    val bos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, bos)
    val b64 = Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP)
    // Send to vision model with prompt "Extract all text from this image"
}
```

### APK cost

| Dep | Size |
|-----|------|
| Android Bitmap API | Platform (zero) |
| Base64 (android.util) | Platform (zero) |
| Gallery picker (ActivityResultContracts) | Platform (zero) |
| **Total** | **0 KB** |

### Dev effort: Low (~60 lines)
- Image picker integration: ~20 lines
- Base64 encoding: ~5 lines
- Modified chat request for vision format: ~15 lines
- UI for image preview before sending: ~20 lines

### Provider support

| Provider | Vision model | Format |
|----------|-------------|--------|
| OpenAI | gpt-4o, gpt-4o-mini | `image_url` with `data:image/...;base64,...` |
| Anthropic | Claude 3.5 Sonnet, Claude 4 Opus | `source` block with `type: base64` |
| Google | Gemini 1.5 Flash, 2.5 Pro | `inline_data` with base64 |
| OpenRouter | Routes to above | Depends on model |

### Recommendation

**Promote C-016 to Ready with scope change:** Instead of screen capture, implement image attachment with vision model support. This is more generally useful (users can attach ANY image, not just screenshots) and has zero permission complexity. The `/ocr` command can be a convenience wrapper that pre-fills "Extract all text from this image" as the prompt.

---

## C-009: Streaming height placeholders

*(Subagent research in progress — placeholder section)*

**Expected findings:**
- EveryTalk's `PerformanceConfig.kt` approach: pre-allocate estimated height for streaming messages, swap to real rendering on completion
- Compose `AnimatedContent` with `SizeTransform` as an alternative
- `SubcomposeLayout` for pre-measuring
- The key insight: LazyColumn recalculates item heights when content changes, causing visible jumps. Fix: reserve space based on streaming text length estimate.

**Expected APK cost:** 0 KB (no new deps)
**Expected dev effort:** ~40 lines

---

## C-010: Token-budget context compression

*(Subagent research in progress — placeholder section)*

**Expected findings:**
- Simple approximation: 4 characters ≈ 1 token (works well for English, ~90% accuracy)
- ChatPPP approach: count characters in all messages, trigger at ~96K chars (≈24K tokens), trim oldest messages to ~56K chars (≈14K tokens)
- Implementation: sum `message.content.length` across all messages before sending, trim from front if over threshold
- Context window reference: GPT-4o-mini = 128K tokens, Claude 3.5 Sonnet = 200K, DeepSeek = 128K

**Expected APK cost:** 0 KB (pure Kotlin math)
**Expected dev effort:** ~30 lines

---

## C-015: Floating chat overlay

*(Subagent research in progress — placeholder section)*

**Expected findings:**
- `SYSTEM_ALERT_WINDOW` requires user to grant "Draw over other apps" in Settings
- Android 10+ (API 29): permission auto-granted for apps installed from Play Store, manual for sideloaded
- Minimal implementation: `WindowManager.addView()` with a small ComposeView
- RAM impact: lightweight (~10-20MB for the overlay process)
- Play Store: no policy issues for chat overlays (common pattern: Messenger, WhatsApp)
- Battery: use `WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE` to avoid wake locks

**Expected APK cost:** 0 KB (platform APIs only)
**Expected dev effort:** ~100 lines (new OverlayService + permission handling)

---

## Priority after subagent results

| Ticket | Status | APK cost | Dev effort | Pro value |
|--------|--------|----------|------------|-----------|
| C-016 | → Ready (scope: image attach + vision) | 0 KB | Low | High |
| C-010 | → Ready | 0 KB | Low | Medium |
| C-009 | → Ready | 0 KB | Low-Medium | Medium |
| C-015 | → Ready (if Play policy OK) | 0 KB | Medium | High |

*Document will be updated with subagent findings when deleg_697e1ee6 completes.*