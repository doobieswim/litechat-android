package com.litechat.android.data.context

import com.litechat.android.data.api.ChatMessageDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContextTrimmerTest {

    private fun msg(role: String, n: Int): ChatMessageDto =
        ChatMessageDto(role, "m$n-" + "x".repeat(400)) // 100 tokens + overhead each

    @Test
    fun `never splits a user assistant turn pair`() {
        // 40 messages (20 pairs) x ~104 tokens = ~4160 total — below high water,
        // so force trimming with tiny budgets instead.
        val messages = mutableListOf<ChatMessageDto>()
        for (i in 0 until 40) {
            messages.add(msg("user", i))
            messages.add(msg("assistant", i))
        }
        val (kept, _) = ContextTrimmer.trim(messages, highWaterTokens = 100, lowWaterTokens = 50)
        val keptRoles = kept.map { it.role }
        // Every kept user must have its assistant (and vice versa) — check as pairs.
        assertTrue(keptRoles.size % 2 == 0)
        for (i in keptRoles.indices step 2) {
            assertEquals("user", keptRoles[i])
            assertEquals("assistant", keptRoles[i + 1])
        }
    }

    @Test
    fun `system prompt is always kept and counts against the budget`() {
        val system = ChatMessageDto("system", "s-" + "y".repeat(40_000)) // 10k tokens
        val user = msg("user", 0)
        val assistant = msg("assistant", 0)
        val (kept, _) = ContextTrimmer.trim(
            listOf(system, user, assistant),
            highWaterTokens = 20_000,
            lowWaterTokens = 2_000,
        )
        assertTrue(kept.any { it.role == "system" })
    }

    @Test
    fun `under budget keeps everything`() {
        val messages = listOf(msg("user", 0), msg("assistant", 0))
        val (kept, removed) = ContextTrimmer.trim(messages)
        assertEquals(2, kept.size)
        assertEquals(0, removed)
    }

    @Test
    fun `trailing unmatched user is not dropped as an orphan assistant`() {
        val messages = listOf(
            msg("user", 0), msg("assistant", 0),
            msg("user", 1), msg("assistant", 1),
            msg("user", 2),
        )
        val (kept, _) = ContextTrimmer.trim(messages, highWaterTokens = 1, lowWaterTokens = 150)
        assertEquals("user", kept.last().role)
        assertTrue(kept.any { it.content.startsWith("m2-") })
        assertTrue(kept.none { it.content.startsWith("m0-") })
    }
}
