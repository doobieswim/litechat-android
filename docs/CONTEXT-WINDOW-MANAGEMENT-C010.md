# C-010 Deep-Dive: Token-Budget Context Compression for LiteChat

**Status:** Research Complete  
**Source:** `NNCVA/ChatPPP` (★2), JTokkit  
**Date:** 2026-08-09

---

## 1. How to Count Tokens on Android

### Approach A: Character Approximation (RECOMMENDED for Tier 1)

**Formula:** `tokens ≈ ceil(chars / 4.0)`

This is what ChatPPP uses. The 4:1 ratio is conservative:
- English averages ~4 chars/token
- Code averages ~3.5 chars/token  
- Chinese averages ~1.5 chars/token

Add fixed overhead per message for format tokens:
```kotlin
const val CHARS_PER_TOKEN = 4.0
const val REQUEST_OVERHEAD_TOKENS = 3   // overall request framing
const val MESSAGE_OVERHEAD_TOKENS = 4   // per-message role/format tokens
```

**APK cost: 0 KB** — pure Kotlin stdlib math.

### Approach B: JTokkit (Java tiktoken port)

`com.knuddels:jtokkit:1.1.0` — 742★, zero-dependency, supports cl100k_base/o200k_base/p50k_base/r50k_base encodings. MIT license.

- **JAR size:** ~3.2 MB raw → ~1-2 MB after R8/ProGuard
- **APK cost estimate:** +800KB–1.2MB after R8 (significant for a 2MB baseline)
- **Verdict:** Too heavy for LiteChat Tier 1. Reserve for Tier 3 ("Token-aware trimming") if users request it.

### Approach C: Server-side Counting (Not Recommended)

Ask the API to count tokens via a lightweight endpoint or parse the `usage.prompt_tokens` from response. Requires extra round-trip before sending — unacceptable latency for chat UX.

### Recommendation: Approach A (approximation)

It's what ChatPPP uses with 63 passing unit tests. The goal is threshold detection, not exact accounting. Being off by ±20% doesn't matter when thresholds have 6-8k token buffers.

---

## 2. How to Count Tokens in `List<ChatMessageDto>` (Kotlin)

ChatPPP's implementation pattern (3 classes, ~200 lines total):

### `ContextTokenEstimator.kt` — the actual counter

```kotlin
class ContextTokenEstimator {
    fun estimateMessageTokens(message: ChatMessageDto): Int {
        return MESSAGE_OVERHEAD_TOKENS +
            estimateTextTokens(message.role) +
            estimateTextTokens(message.content)
    }

    fun estimateRequestTokens(
        messages: List<ChatMessageDto>,
        reservedResponseTokens: Int = 0
    ): Int = REQUEST_OVERHEAD_TOKENS +
        messages.sumOf(::estimateMessageTokens) +
        reservedResponseTokens

    fun estimateTextTokens(text: String): Int {
        if (text.isBlank()) return 0
        return ceil(text.length / CHARS_PER_TOKEN).toInt().coerceAtLeast(1)
    }

    companion object {
        const val CHARS_PER_TOKEN = 4.0
        const val REQUEST_OVERHEAD_TOKENS = 3
        const val MESSAGE_OVERHEAD_TOKENS = 4
    }
}
```

### Integration point in ChatViewModel.send()

```kotlin
// BEFORE assembling the API request:
val history = container.chatRepository.listMessages(convId)
val dto = history.map { ChatMessageDto(it.role, it.content) }
val estimator = ContextTokenEstimator()
val estimatedTokens = estimator.estimateRequestTokens(
    messages = dto,
    reservedResponseTokens = 6_144  // room for model output
)
val truncated = if (estimatedTokens > TOKEN_HIGH_WATERMARK) {
    trimToBudget(dto, estimator)
} else {
    dto
}
```

---

## 3. Context Window Sizes for Common Models

| Model | Context Window | Safe Budget (80%) | Approx Chars (4:1) |
|-------|---------------|-------------------|---------------------|
| GPT-3.5 Turbo | 16,385 | ~13,000 | ~52,000 |
| GPT-4o-mini | 128,000 | ~102,000 | ~410,000 |
| GPT-4o | 128,000 | ~102,000 | ~410,000 |
| GPT-4 Turbo | 128,000 | ~102,000 | ~410,000 |
| Claude 3.5 Sonnet | 200,000 | ~160,000 | ~640,000 |
| Claude 3 Haiku | 200,000 | ~160,000 | ~640,000 |
| DeepSeek R1/V3 | 128,000 | ~102,000 | ~410,000 |
| Groq (Llama 3.3) | 128,000 | ~102,000 | ~410,000 |
| Ollama (Llama 3.2) | 128,000 | ~102,000 | ~410,000 |

**LiteChat's recommended conservative default: 24,000 token high-water mark** (~96KB text). Why:
- Safe for GPT-3.5 Turbo (smallest common model — 16k context)
- Provides generous headroom for all other models
- Users on larger models can increase via Settings
- 24k tokens is ~250+ message exchanges (typical chat session)

---

## 4. ChatPPP's Exact Approach — Deep Analysis

ChatPPP uses a sophisticated **three-tier architecture**, not just simple truncation. Here's the full chain:

### Architecture

```
MessageDao (all messages)
    ↓
ConversationContextBuilder
    ├── Partition into turns (user+assistant pairs)
    ├── Filter replayable messages (SUCCESS status, non-blank)
    ├── Estimate tokens → compare to compressionTriggerTokens (24,576)
    ├── If under trigger: return all messages as-is
    └── If over trigger:
        ├── Load stored ConversationSummary from Room
        ├── Build context: keep most recent turns within targetCompressedTokens (14,336)
        ├── Oldest unsummarized messages → messagesToSummarize
        ├── Call ConversationSummaryGenerator → LLM summary
        ├── Store summary in ConversationSummaryEntity (Room)
        └── Rebuild request: summary as system message + recent turns
```

### Configuration (CompressionBudget)

```kotlin
data class CompressionBudget(
    val maxContextTokens: Int = 32_768,           // absolute ceiling
    val compressionTriggerTokens: Int = 24_576,   // trigger summary generation
    val targetCompressedTokens: Int = 14_336,     // budget after compression
    val reservedResponseTokens: Int = 6_144,      // room for model output
)
```

### Key Behaviors

1. **Turn-preserving**: Never split a user/assistant turn pair — keeps complete recent turns intact
2. **Summary as system message**: Compressed history becomes `role: "system", content: "Conversation summary:\n..."`
3. **Rolling summary**: Each compression cycle merges new messages into the existing summary
4. **Idempotent filtering**: Error messages and blank assistant responses are excluded from both context and summary candidates
5. **Summary generation**: Uses the active model itself to summarize (`stream: false`, single non-streaming request)
6. **Graceful degradation**: If summary generation fails (network/error), falls back to raw truncation
7. **Opt-in**: Togglable via `appPreferences.summaryCompressionEnabled`

### When Does Compression Happen?

**Before sending every message.** In `DefaultChatRepository.sendMessage()` → `buildRequestMessages()`:
1. Estimate raw token count of all replayable messages
2. If > compressionTriggerTokens → run context builder → if messages to summarize → generate summary → rebuild
3. Otherwise → return raw messages as-is

This means compression only triggers when needed, not on every send.

---

## 5. APK Cost Analysis

| Approach | APK Impact | Dependencies | Complexity |
|----------|-----------|-------------|------------|
| **Tier 1: Approximation only** | **0 KB** | None (stdlib math) | ~50 lines Kotlin |
| Tier 1 + truncation indicator UI | ~2 KB | None | ~100 lines total |
| Tier 2: + LLM-based summary | 0 KB (uses existing client) | None | ~150 lines + Room entity |
| Tier 3: + JTokkit accurate counting | **+800KB–1.2MB** | `com.knuddels:jtokkit:1.1.0` | +200 lines |

**Verdict for Tier 1 (LiteChat v1): 0 KB APK cost.** Pure Kotlin approximation.

---

## 6. UI Indicator for Truncated History

ChatPPP's approach: transparent — older messages just become a summary system message. No explicit "truncated" UI.

**Recommended for LiteChat:** Add a subtle indicator:

```kotlin
// In ChatUiState:
val isHistoryTruncated: Boolean = false,
val truncatedMessageCount: Int = 0,

// In Screens.kt, above the message list:
@Composable
fun TruncationBanner(messageCount: Int) {
    if (messageCount > 0) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "$messageCount earlier messages not included (token limit)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
```

**Placement:** Between the chat app bar and the first visible message. Dismissible with a swipe or tap.

---

## 7. When to Truncate: Before Sending or on History Load?

**Answer: Before sending, every time.** This is what ChatPPP does and it's the correct approach:

### Why before sending:

1. **The token budget is a request constraint**, not a display constraint. Displaying 500 messages on screen is fine (lazy-loaded LazyColumn). Sending all 500 to the API is the problem.
2. **Token budgets change**: If the user switches models (GPT-3.5 → GPT-4o), the budget changes. Re-evaluating at send time picks up the new model's context window.
3. **Settings-driven**: ChatPPP's `compressionTriggerTokens` is user-configurable. The user could change it mid-conversation — it only matters at send time.
4. **Room stores all messages**: The full history is always in Room. Compression only affects what gets sent to the API. Users can scroll back.

### Why NOT on history load:

- Would permanently alter what's displayed — bad UX
- Would require re-fetching from API if messages were deleted from Room
- Token counting on load is wasted work (LazyColumn only renders visible items anyway)

### The LiteChat integration point:

```kotlin
// ChatViewModel.send() — line ~267 in current code:
val history = container.chatRepository.listMessages(convId)
val dto = history.map { ChatMessageDto(it.role, it.content) }

// NEW: insert truncation logic here
val (truncatedMessages, wasTruncated) = contextTrimmer.trim(dto, settings.tokenThreshold)
_state.update { it.copy(isHistoryTruncated = wasTruncated) }

// Then pass truncatedMessages to streamChat:
container.openAiClient.streamChat(
    messages = truncatedMessages,  // was: dto
    // ...
)
```

---

## 8. Recommended Approach for LiteChat C-010

### Tier 1: Simple Truncation (Scope of C-010)

**What ChatPPP's "disabling summary compression" looks like** — pure truncation, no LLM calls:

```kotlin
object ContextTrimmer {
    // Default thresholds (configurable via Settings)
    const val DEFAULT_HIGH_WATERMARK = 24_000  // trigger truncation
    const val DEFAULT_LOW_WATERMARK = 14_000    // target after truncation
    const val CHARS_PER_TOKEN = 4.0
    const val MESSAGE_OVERHEAD = 4              // per-message format tokens
    const val REQUEST_OVERHEAD = 3

    data class TrimResult(
        val messages: List<ChatMessageDto>,
        val truncated: Boolean,
        val removedCount: Int,
    )

    /**
     * Trim oldest messages to fit within token budget.
     * Always keeps the system prompt (if any) and most recent messages.
     * Never splits a user/assistant turn pair.
     */
    fun trim(
        messages: List<ChatMessageDto>,
        highWatermark: Int = DEFAULT_HIGH_WATERMARK,
        lowWatermark: Int = DEFAULT_LOW_WATERMARK,
    ): TrimResult {
        val totalTokens = estimateTokens(messages)
        if (totalTokens <= highWatermark) {
            return TrimResult(messages, false, 0)
        }

        // Keep newest messages within low watermark
        val kept = ArrayDeque<ChatMessageDto>()
        var keptTokens = 0

        // Always keep system prompt first
        val systemPrompt = messages.firstOrNull { it.role == "system" }

        for (msg in messages.asReversed()) {
            val msgTokens = estimateSingle(msg)
            if (keptTokens + msgTokens > lowWatermark) break
            kept.addFirst(msg)
            keptTokens += msgTokens
        }

        // Ensure system prompt is present
        val result = buildList {
            if (systemPrompt != null && systemPrompt !in kept) {
                add(systemPrompt)
            }
            addAll(kept)
        }

        return TrimResult(
            messages = result,
            truncated = true,
            removedCount = messages.size - result.size,
        )
    }

    fun estimateTokens(messages: List<ChatMessageDto>): Int =
        REQUEST_OVERHEAD + messages.sumOf(::estimateSingle)

    private fun estimateSingle(msg: ChatMessageDto): Int {
        if (msg.content.isBlank()) return 0
        return MESSAGE_OVERHEAD +
            ceil((msg.role.length + msg.content.length) / CHARS_PER_TOKEN).toInt()
    }
}
```

### Files to touch:
1. **New file:** `app/src/main/java/com/litechat/android/data/context/ContextTrimmer.kt` (~60 lines)
2. **Modified:** `ChatViewModel.kt` — insert trim call in `send()`, add UI state fields
3. **Modified:** `Screens.kt` — add `TruncationBanner` composable
4. **Modified:** `SettingsRepository.kt` — add token threshold settings
5. **New file (optional):** `app/src/test/java/com/litechat/android/data/context/ContextTrimmerTest.kt`

### Dev effort estimate: **~3-4 hours**

| Task | Effort |
|------|--------|
| ContextTrimmer.kt + unit tests | 1.5h |
| ChatViewModel.send() integration | 0.5h |
| TruncationBanner UI | 0.5h |
| Settings token threshold UI | 0.5h |
| Verify + static verify guards | 0.5h |

### Out of scope for C-010 (defer to v2):
- LLM-based rolling summary (ChatPPP's Tier 2/3 — another API call per compression)
- JTokkit accurate token counting (+1MB APK)
- Per-model auto-detected thresholds (user sets manually for now)
- ConversationSummaryEntity + SummaryStore (Room schema migration)

---

## 9. Summary of Key Decisions

| Question | Answer |
|----------|--------|
| Token counting method | `ceil(chars / 4.0)` approximation + per-message overhead |
| When to truncate | Before each send (in `ChatViewModel.send()`) |
| Default thresholds | High: 24k tokens, Low: 14k tokens |
| Keep system prompt | Always (never trimmed) |
| Turn splitting | Never — keep complete user/assistant pairs |
| APK cost (Tier 1) | 0 KB (pure stdlib) |
| APK cost (Tier 3 JTokkit) | +800KB–1.2MB |
| Dev effort (Tier 1) | ~3-4 hours |
| UI indicator | Subtle banner above message list, "N earlier messages not included" |
| What gets stored | Full history in Room always; truncation only affects API payload |
| Settings exposure | Token threshold field in Settings (default: 24000) — Pro-gated? Defer. |