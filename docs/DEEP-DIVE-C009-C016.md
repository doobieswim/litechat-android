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

### EveryTalk's dual-approach (subagent deep-dive)

EveryTalk's `PerformanceConfig.kt` (247 lines analyzed) uses two complementary features:

**Feature 1 — Height Placeholder:** Detects pending code blocks/tables in raw streaming text. Injects a `Spacer(height = estimatedDelta)` matching the final rendered component height BEFORE the block closes. So the item's measured height during streaming ≈ its final height — no snap when fence closes.

```
Streaming: "Here's the code:\n```kotlin\nfun hello()"  → [placeholder height = 28dp toolbar + 4dp padding]
Completed: "Here's the code:\n```kotlin\nfun hello()\n```" → [real CodeBlockCard = 28dp + 4dp] = same height!
```

**Feature 2 — Single-Swap Rendering:** Without this, the lifecycle is: streaming markdown → "bare" markdown (blink) → rich segmented render. Steps 2→3 cause a second jump. With single-swap, the system holds the streaming render until the parser emits the final segmented output, then atomically swaps.

### Code sketch

```kotlin
// Block detection in raw streaming text
val hasCodeBlock = text.contains("```") && text.count { it == '`' } % 2 != 0
val hasTable = text.lines().any { it.contains("|") && it.trimStart().startsWith("|") }

val placeholderHeight = buildList {
    if (hasCodeBlock) add(28.dp + 4.dp)  // toolbar + padding
    if (hasTable) add(8.dp)               // vertical margin
}.sum().takeIf { it > 0.dp }

Column {
    MarkdownContent(text)
    if (placeholderHeight != null) Spacer(Modifier.height(placeholderHeight))
}
```

### Gotchas (7 identified)

1. **Code fence detection is fragile** — single backticks in prose can look like fence openers. Use regex for triple-backtick patterns only.
2. **Placeholder height must match real component** — if toolbar height drifts, jump returns. Use shared constants or `onGloballyPositioned` measurement.
3. **Scroll position preservation** — single-swap must NOT auto-scroll if user is reading history. Check: `isUserAtBottom = visibleItems.lastOrNull()?.index == messages.lastIndex`
4. **Keyboard narrows viewport** — code blocks wrap differently at keyboard-width. Recalculate placeholder on width change.
5. **Orientation changes** — Activity recreation loses streaming render state. Streaming text must survive in ViewModel.
6. **Tables vary by column count** — static 8dp is a rough estimate. Count `|` separators for proportional sizing.
7. **animateItem() interference** — if used on message items, height change from placeholder removal animates the position → feels like a jump itself. Don't animate the last streaming item.

### Recommendation

Adopt EveryTalk's dual-approach: Layer 1 (Spacer placeholder) for height pre-allocation, Layer 2 (single atomic swap) for clean transition. **0 new dependencies. ~100 lines of Kotlin. ~1 day of engineering.**

---

## C-010: Token-budget context compression

*(Subagent research in progress — Reddit findings below)*

### Reddit findings

No specific Android implementation discussions on Reddit. Context window discussions are generic (GPT-4 = 32K/128K, Claude = 200K, Gemini = 1M). The 4-chars-per-token approximation is common knowledge but nobody has posted a clean Android Kotlin implementation. This is a gap LiteChat can fill.

### ChatPPP approach (from R-006 research)

ChatPPP triggers at 24,000 tokens, compresses to 14,000 via rolling summary. For v1 LiteChat, pure truncation (not summary) is sufficient:

```kotlin
fun trimToTokenBudget(messages: List<ChatMessageDto>, maxTokens: Int = 24000): List<ChatMessageDto> {
    val maxChars = maxTokens * 4 // 4 chars ≈ 1 token
    var total = 0
    val kept = mutableListOf<ChatMessageDto>()
    // Keep from newest to oldest until budget exceeded
    for (msg in messages.reversed()) {
        total += msg.content.length
        if (total > maxChars && kept.isNotEmpty()) break
        kept.add(0, msg)
    }
    return kept
}
```

### Context window reference

| Model | Context window | Safe budget (~75%) |
|-------|---------------|-------------------|
| GPT-4o-mini | 128K tokens | 96K tokens |
| GPT-4o | 128K tokens | 96K tokens |
| Claude 3.5 Sonnet | 200K tokens | 150K tokens |
| Gemini 2.5 Pro | 1M tokens | 750K tokens |
| DeepSeek-V3 | 128K tokens | 96K tokens |

**Recommended threshold:** 24K tokens (~96K chars) as default, configurable in settings.

**APK cost:** 0 KB (pure Kotlin math, no deps)
**Dev effort:** ~30 lines

---

## C-015: Floating chat overlay

*(Subagent research in progress — Reddit findings below)*

### Reddit findings

SYSTEM_ALERT_WINDOW confirmed as the standard approach for chat bubbles. Key Reddit-sourced caveats:

- **Android 10+ (API 29):** Permission auto-granted for Play Store installs, but **NOT for sideloaded apps** (FOSS flavor!). Sideloaded users must manually grant "Draw over other apps" in Settings.
- **Android 12+ (API 31):** Google introduced the **Bubbles API** as the official replacement for chat heads. Bubbles are a system-level feature (like Facebook Messenger chat heads) and don't need SYSTEM_ALERT_WINDOW. But: Bubbles API is more restrictive — you can't customize the look as freely.
- **"Kind of deprecated":** Several Reddit threads note SYSTEM_ALERT_WINDOW is soft-deprecated in favor of Bubbles, but still works on all API levels. Google hasn't announced a removal timeline.
- **Play Store policy:** Chat overlays are fine (Messenger, WhatsApp precedent). No policy flags unless used for ad overlay or phishing.
- **MIUI/Xiaomi:** Chinese ROMs often block SYSTEM_ALERT_WINDOW by default. Users must whitelist the app in "Permissions → Other Permissions → Display pop-up window."

### Implementation options

**Option A: SYSTEM_ALERT_WINDOW (Android 6+)**
- Works everywhere, simple, customizable
- Sideloaded users must grant manually (friction for FOSS)
- ~50 lines for a basic Compose overlay

**Option B: Android 12+ Bubbles API**
- Official, no special permission on Play Store
- Won't work on Android 8-11 (LiteChat's minSdk=26 supports 8+)
- More restrictive layout

**Option C: Both — SYSTEM_ALERT_WINDOW for 8-11, Bubbles for 12+**
- Most work (~150 lines)
- Best UX across all versions

**Recommendation:** Start with Option A (SYSTEM_ALERT_WINDOW). If users report permission friction on FOSS/sideloaded builds, add Option B as a fallback for Android 12+.

**APK cost:** 0 KB (platform APIs only)
**Dev effort:** ~100 lines (new `OverlayService.kt`, permission request in Settings, minimal Compose chat overlay)

---

## Priority after subagent results

| Ticket | Status | APK cost | Dev effort | Pro value |
|--------|--------|----------|------------|-----------|
| C-016 | → Ready (scope: image attach + vision) | 0 KB | Low | High |
| C-010 | → Ready | 0 KB | Low | Medium |
| C-009 | → Ready | 0 KB | Low-Medium | Medium |
| C-015 | → Ready (if Play policy OK) | 0 KB | Medium | High |

*Document will be updated with subagent findings when deleg_697e1ee6 completes.*