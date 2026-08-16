package com.litechat.android.data.db

import org.junit.Assert.assertEquals
import org.junit.Test

class ConversationFolderFilterTest {

    private fun conv(id: String, folderId: String? = null) =
        ConversationEntity(
            id = id,
            title = id,
            createdAt = 0L,
            updatedAt = 0L,
            folderId = folderId,
        )

    @Test
    fun `null folder shows all chats`() {
        val list = listOf(conv("a"), conv("b", "work"))
        assertEquals(listOf("a", "b"), ConversationSort.inFolder(list, null).map { it.id })
    }

    @Test
    fun `named folder hides the rest`() {
        val list = listOf(conv("a"), conv("b", "work"), conv("c", "work"))
        assertEquals(listOf("b", "c"), ConversationSort.inFolder(list, "work").map { it.id })
    }
}
