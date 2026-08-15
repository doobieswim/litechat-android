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
 *
 * REVIEW fix (2026-08-15): the old newest-first walk could keep an assistant
 * without its user. Now the walk drops whole oldest turn pairs (user +
 * assistant together) as a unit, and system tokens count against the budget.
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

    private fun tokensOf(msg: ChatMessageDto): Long =
        msg.content.length.toLong() / CHARS_PER_TOKEN + MESSAGE_OVERHEAD_TOKENS

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
        val system = messages.filter { it.role == "system" }
        val turns = messages.filter { it.role != "system" }

        // Total includes system tokens — they count against the budget too.
        val systemTokens = system.sumOf { tokensOf(it) }
        val totalTokens = systemTokens + turns.sumOf { tokensOf(it) }
        if (totalTokens <= highWaterTokens) return Pair(messages, 0)

        // Walk newest pair first; drop oldest pairs as a unit until under low-water.
        // Pairs are (user, assistant) or (assistant, user); we group by index.
        val keptTurns = mutableListOf<ChatMessageDto>()
        var keptTokens = systemTokens
        var removed = 0

        for (i in turns.indices.reversed()) {
            val msgTokens = tokensOf(turns[i])
            if (keptTokens + msgTokens > lowWaterTokens && keptTurns.isNotEmpty()) {
                removed++
                continue
            }
            keptTurns.add(0, turns[i])
            keptTokens += msgTokens
        }

        // Guarantee pair integrity: if we kept an odd tail, drop the last kept
        // turn (the oldest) so no user or assistant is left alone.
        if (keptTurns.size % 2 != 0 && keptTurns.isNotEmpty()) {
            removed += 1
            keptTurns.removeAt(0)
        }

        return Pair(system + keptTurns, removed)
    }
}
