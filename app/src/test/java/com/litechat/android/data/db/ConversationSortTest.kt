package com.litechat.android.data.db

import org.junit.Assert.assertEquals
import org.junit.Test

class ConversationSortTest {

    private fun conv(id: String, pinned: Boolean = false, updatedAt: Long = 0L) =
        ConversationEntity(id = id, title = id, createdAt = 0L, updatedAt = updatedAt, pinned = pinned)

    @Test
    fun `pinned conversations sort before unpinned`() {
        val list = listOf(conv("a", pinned = false), conv("b", pinned = true))
        assertEquals(listOf("b", "a"), ConversationSort.pinnedFirst(list).map { it.id })
    }

    @Test
    fun `unpinned chats keep recency order`() {
        val list = listOf(conv("old", updatedAt = 100), conv("new", updatedAt = 200))
        assertEquals(listOf("new", "old"), ConversationSort.pinnedFirst(list).map { it.id })
    }

    @Test
    fun `pinned group is ordered by recency too`() {
        val list = listOf(
            conv("p-old", pinned = true, updatedAt = 100),
            conv("u-new", pinned = false, updatedAt = 999),
            conv("p-new", pinned = true, updatedAt = 200),
        )
        assertEquals(
            listOf("p-new", "p-old", "u-new"),
            ConversationSort.pinnedFirst(list).map { it.id }
        )
    }

    @Test
    fun `empty list stays empty`() {
        assertEquals(emptyList<String>(), ConversationSort.pinnedFirst(emptyList()).map { it.id })
    }
}
