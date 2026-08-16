package com.litechat.android.data.prefs

/**
 * P-009: a named folder. Chats point at [id] via ConversationEntity.folderId.
 */
data class ChatFolder(
    val id: String,
    val name: String,
)
