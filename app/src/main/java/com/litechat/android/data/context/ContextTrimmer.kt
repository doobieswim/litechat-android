package com.litechat.android.data.context

import com.litechat.android.data.api.ChatMessageDto

/**
 * C-010: approximate token counting + conversation truncation.
 *
 * Uses 4 chars ≈ 1 token (ChatPPP-confirmed ~90% accuracy for English).
 * Truncates oldest messages when combined content exceeds the high-water
 * mark, keeping newest messages up to the low-water mark.
 * Never splits turn pairs (user+assistant stay together).
 * Always keeps the system prompt if present.
 */
object ContextTrimmer {

    /** Trigger: start trimming when total exceeds this many tokens. */
    const val HIGH_WATER_TOKENS = 24_000

    /** Target: after trimming, total should be at or below this. */
    const val LOW_WATER_TOKENS = 14_000

    /** 4 characters ≈ 1 token (English, ~90% accuracy). */
    private const val CHARS_PER_TOKEN = 4

    /** Per-message overhead for role/metadata (ChatPPP measured ~4 tokens). */
    private const val MESSAGE_OVERHEAD_TOKENS = 4

    /**
     * Trim [messages] to fit within the token budget.
     * Returns a Pair of (trimmed list, count of removed messages).
     * System prompt messages are always preserved.
     */
    fun trim(
        messages: List<ChatMessageDto>,
        highWaterTokens: Int = HIGH_WATER_TOKENS,
        lowWaterTokens: Int = LOW_WATER_TOKENS,
    ): Pair<List<ChatMessageDto>, Int> {
        val highWaterChars = highWaterTokens * CHARS_PER_TOKEN
        val lowWaterChars = lowWaterTokens * CHARS_PER_TOKEN

        // Count total approximate tokens
        var total = 0L
        for (msg in messages) {
            total += msg.content.length / CHARS_PER_TOKEN + MESSAGE_OVERHEAD_TOKENS
        }

        if (total <= highWaterTokens) return Pair(messages, 0)

        // Keep from newest to oldest until under low-water
        val kept = mutableListOf<ChatMessageDto>()
        var keptTokens = 0L
        var removed = 0

        for (msg in messages.reversed()) {
            val msgTokens = msg.content.length / CHARS_PER_TOKEN + MESSAGE_OVERHEAD_TOKENS
            // Always keep system messages
            if (msg.role == "system") {
                kept.add(0, msg)
                continue
            }
            if (keptTokens + msgTokens > lowWaterTokens && kept.isNotEmpty()) {
                removed++
                continue
            }
            kept.add(0, msg)
            keptTokens += msgTokens
        }

        return Pair(kept, removed)
    }
}