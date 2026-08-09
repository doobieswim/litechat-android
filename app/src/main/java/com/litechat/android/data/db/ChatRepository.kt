package com.litechat.android.data.db

import java.util.UUID
import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
) {
    fun observeConversations(): Flow<List<ConversationEntity>> = conversationDao.observeAll()

    fun observeMessages(conversationId: String): Flow<List<MessageEntity>> =
        messageDao.observeForConversation(conversationId)

    suspend fun listMessages(conversationId: String): List<MessageEntity> =
        messageDao.listForConversation(conversationId)

    suspend fun createConversation(title: String = "New chat", model: String = ""): ConversationEntity {
        val now = System.currentTimeMillis()
        val entity = ConversationEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            model = model,
            createdAt = now,
            updatedAt = now,
        )
        conversationDao.upsert(entity)
        return entity
    }

    suspend fun renameConversation(id: String, title: String) {
        val existing = conversationDao.get(id) ?: return
        conversationDao.upsert(
            existing.copy(title = title.take(80), updatedAt = System.currentTimeMillis())
        )
    }

    suspend fun touchConversation(id: String) {
        val existing = conversationDao.get(id) ?: return
        conversationDao.upsert(existing.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteConversation(id: String) {
        messageDao.deleteForConversation(id)
        conversationDao.delete(id)
    }

    suspend fun clearAll() {
        messageDao.deleteAll()
        conversationDao.deleteAll()
    }

    suspend fun addMessage(conversationId: String, role: String, content: String): MessageEntity {
        val entity = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            role = role,
            content = content,
            createdAt = System.currentTimeMillis(),
        )
        messageDao.insert(entity)
        touchConversation(conversationId)
        return entity
    }

    suspend fun updateMessageContent(id: String, conversationId: String, content: String) {
        val list = messageDao.listForConversation(conversationId)
        val msg = list.find { it.id == id } ?: return
        messageDao.update(msg.copy(content = content))
        touchConversation(conversationId)
    }
}
