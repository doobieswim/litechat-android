package com.litechat.android.data.db

import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepository(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val messageFtsDao: MessageFtsDao,
) {
    fun observeConversations(): Flow<List<ConversationEntity>> =
        // P-014: pinned chats sort to the top (pure, unit-tested sort).
        conversationDao.observeAll().map { ConversationSort.pinnedFirst(it) }

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

    /** P-014: flip a conversation's pin flag (does not touch updatedAt). */
    suspend fun togglePin(id: String) {
        val existing = conversationDao.get(id) ?: return
        conversationDao.upsert(existing.copy(pinned = !existing.pinned))
    }

    /** P-009: put a chat in a folder, or null to take it out. */
    suspend fun setFolder(id: String, folderId: String?) {
        val existing = conversationDao.get(id) ?: return
        conversationDao.upsert(existing.copy(folderId = folderId))
    }

    suspend fun deleteConversation(id: String) {
        messageFtsDao.deleteForConversation(id)
        messageDao.deleteForConversation(id)
        conversationDao.delete(id)
    }

    suspend fun clearAll() {
        messageFtsDao.deleteAll()
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
        insertMessage(entity)
        touchConversation(conversationId)
        return entity
    }

    /**
     * C-024: fork a conversation at a message. Copies every message up to and
     * including [fromMessageId] into a new conversation; the copy of the fork
     * point keeps [MessageEntity.parentId] pointing at the original message so
     * the branch lineage is queryable. The branch is fully independent after
     * this (messages are copies, not references).
     */
    suspend fun forkConversation(
        conversationId: String,
        fromMessageId: String,
        model: String = "",
    ): ConversationEntity {
        val original = conversationDao.get(conversationId)
            ?: throw IllegalStateException("Conversation not found")
        val all = messageDao.listForConversation(conversationId)
        val forkPointCreatedAt = all.find { it.id == fromMessageId }?.createdAt
            ?: throw IllegalStateException("Message not found")
        val messages = all.filter { it.createdAt <= forkPointCreatedAt }
        val branch = createConversation(
            title = "Fork: ${original.title.take(70)}",
            model = model.ifBlank { original.model },
        )
        val now = System.currentTimeMillis()
        messages.forEachIndexed { i, msg ->
            insertMessage(
                msg.copy(
                    id = UUID.randomUUID().toString(),
                    conversationId = branch.id,
                    createdAt = now + i,  // keep order; parentId marks the fork point
                    parentId = if (msg.id == fromMessageId) msg.id else null,
                )
            )
        }
        return branch
    }

    suspend fun updateMessageContent(id: String, conversationId: String, content: String) {
        val list = messageDao.listForConversation(conversationId)
        val msg = list.find { it.id == id } ?: return
        val updated = msg.copy(content = content)
        messageDao.update(updated)
        indexMessage(updated)
        touchConversation(conversationId)
    }

    /** P-002: search every saved message. Empty / junk queries return nothing. */
    suspend fun searchMessages(raw: String, limit: Int = 80): List<SearchHit> {
        val query = FtsQuery.escape(raw.take(200)) ?: return emptyList()
        return messageFtsDao.search(query, limit)
    }

    private suspend fun insertMessage(entity: MessageEntity) {
        messageDao.insert(entity)
        indexMessage(entity)
    }

    private suspend fun indexMessage(entity: MessageEntity) {
        messageFtsDao.deleteByMessageId(entity.id)
        messageFtsDao.insert(
            MessageFtsEntity(
                messageId = entity.id,
                conversationId = entity.conversationId,
                content = entity.content,
            )
        )
    }

    suspend fun exportAsText(convId: String): String {
        val conv = conversationDao.get(convId) ?: return ""
        val msgs = messageDao.listForConversation(convId)
        return buildString {
            appendLine(conv.title)
            appendLine("=".repeat(conv.title.length))
            appendLine()
            msgs.forEach { msg ->
                appendLine(if (msg.role == "user") "You:" else "Assistant:")
                appendLine(msg.content)
                appendLine()
            }
        }
    }
}
