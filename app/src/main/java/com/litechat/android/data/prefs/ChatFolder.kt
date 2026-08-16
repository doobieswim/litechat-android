package com.litechat.android.data.prefs

import kotlinx.serialization.Serializable

/**
 * P-009: a named folder. Chats point at [id] via ConversationEntity.folderId.
 */
@Serializable
data class ChatFolder(
    val id: String,
    val name: String,
)
