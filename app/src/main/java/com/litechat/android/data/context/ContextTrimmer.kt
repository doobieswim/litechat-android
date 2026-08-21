package com.litechat.android.data.context

import com.litechat.android.data.api.ChatMessageDto

/**
 * C-010: approximate token counting + conversation truncation.
 *
 * Uses 4 chars ≈ 1 token. Drops oldest (user, assistant) pairs as a unit.
 * A trailing unmatched user (the live question) stays glued on.
 * System prompt always kept and counts against the budget.
 */
object ContextTrimmer {

    const val HIGH_WATER_TOKENS = 24_000
    const val LOW_WATER_TOKENS = 14_000

    private const val CHARS_PER_TOKEN = 4
    private const val MESSAGE_OVERHEAD_TOKENS = 4

    private fun tokensOf(msg: ChatMessageDto): Long =
        msg.content.length.toLong() / CHARS_PER_TOKEN + MESSAGE_OVERHEAD_TOKENS

    fun trim(
        messages: List<ChatMessageDto>,
        highWaterTokens: Int = HIGH_WATER_TOKENS,
        lowWaterTokens: Int = LOW_WATER_TOKENS,
    ): Pair<List<ChatMessageDto>, Int> {
        val system = messages.filter { it.role == "system" }
        val turns = messages.filter { it.role != "system" }.toMutableList()
        val systemTokens = system.sumOf { tokensOf(it) }
        val totalTokens = systemTokens + turns.sumOf { tokensOf(it) }
        if (totalTokens <= highWaterTokens) return Pair(messages, 0)

        var removed = 0
        if (turns.firstOrNull()?.role == "assistant") {
            turns.removeAt(0)
            removed++
        }
        val trailingUser = if (turns.lastOrNull()?.role == "user") {
            turns.removeAt(turns.lastIndex)
        } else {
            null
        }

        fun used(): Long =
            systemTokens + turns.sumOf { tokensOf(it) } + (trailingUser?.let { tokensOf(it) } ?: 0L)

        while (turns.size >= 2 && used() > lowWaterTokens) {
            turns.removeAt(0)
            turns.removeAt(0)
            removed += 2
        }
        while (turns.isNotEmpty() && used() > lowWaterTokens) {
            turns.removeAt(0)
            removed++
        }

        val kept = system + turns + listOfNotNull(trailingUser)
        return Pair(kept, removed)
    }
}
