package com.litechat.android.data.db

/**
 * P-014: pure conversation ordering — pinned chats first, then recency.
 *
 * Kept as a pure function (not raw SQL) so pin sorting is unit-testable
 * without a Room database. The DAO query stays `ORDER BY updatedAt DESC`;
 * this pass re-sorts with the pin flag on top.
 */
object ConversationSort {
    fun pinnedFirst(list: List<ConversationEntity>): List<ConversationEntity> =
        list.sortedWith(
            compareByDescending<ConversationEntity> { it.pinned }
                .thenByDescending { it.updatedAt }
        )

    /** P-009: null folderId = All chats. Else only that folder. */
    fun inFolder(list: List<ConversationEntity>, folderId: String?): List<ConversationEntity> =
        if (folderId == null) list else list.filter { it.folderId == folderId }
}
