package com.litechat.android.data.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val model: String = "",  // C-018: per-conversation model binding
    val createdAt: Long,
    val updatedAt: Long,
    /** P-014: pinned conversations sort to the top of the drawer. */
    val pinned: Boolean = false,
    /** P-009: optional folder id. Null = no folder (shows under All). */
    val folderId: String? = null,
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val role: String, // user | assistant | system
    val content: String,
    /** C-024: parent message id for conversation forks. */
    val parentId: String? = null,
    val createdAt: Long,
)

/** P-002: one search row per message. Only [content] is indexed. */
@Fts4(
    tokenizer = FtsOptions.TOKENIZER_UNICODE61,
    notIndexed = ["messageId", "conversationId"],
)
@Entity(tableName = "messages_fts")
data class MessageFtsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    val rowid: Int = 0,
    val messageId: String,
    val conversationId: String,
    val content: String,
)

/** P-002: one hit from a search. Grouped by conversation in the UI. */
data class SearchHit(
    val messageId: String,
    val conversationId: String,
    val conversationTitle: String,
    val role: String,
    val content: String,
    val createdAt: Long,
)

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun get(id: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ConversationEntity)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM conversations")
    suspend fun deleteAll()
}

@Dao
interface MessageDao {
    @Query(
        """
        SELECT * FROM messages
        WHERE conversationId = :conversationId
        ORDER BY createdAt ASC
        """
    )
    fun observeForConversation(conversationId: String): Flow<List<MessageEntity>>

    @Query(
        """
        SELECT * FROM messages
        WHERE conversationId = :conversationId
        ORDER BY createdAt ASC
        """
    )
    suspend fun listForConversation(conversationId: String): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MessageEntity)

    @Update
    suspend fun update(entity: MessageEntity)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteForConversation(conversationId: String)

    @Query("DELETE FROM messages")
    suspend fun deleteAll()
}

@Dao
interface MessageFtsDao {
    @Insert
    suspend fun insert(entity: MessageFtsEntity)

    @Query("DELETE FROM messages_fts WHERE messageId = :messageId")
    suspend fun deleteByMessageId(messageId: String)

    @Query("DELETE FROM messages_fts WHERE conversationId = :conversationId")
    suspend fun deleteForConversation(conversationId: String)

    @Query("DELETE FROM messages_fts")
    suspend fun deleteAll()

    @Query(
        """
        SELECT m.id AS messageId,
               m.conversationId AS conversationId,
               c.title AS conversationTitle,
               m.role AS role,
               m.content AS content,
               m.createdAt AS createdAt
        FROM messages_fts
        JOIN messages AS m ON m.id = messages_fts.messageId
        JOIN conversations AS c ON c.id = m.conversationId
        WHERE messages_fts MATCH :query
        ORDER BY m.createdAt DESC
        LIMIT :limit
        """
    )
    suspend fun search(query: String, limit: Int = 80): List<SearchHit>
}

@Database(
    entities = [ConversationEntity::class, MessageEntity::class, MessageFtsEntity::class],
    version = 4,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun messageFtsDao(): MessageFtsDao

    companion object {
        /** P-014: v1 → v2 adds the pinned column (never destructive on 4GB). */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE conversations ADD COLUMN pinned INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        /** P-002: v2 → v3 adds the FTS search table and fills it from existing messages. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE VIRTUAL TABLE IF NOT EXISTS `messages_fts` USING FTS4(" +
                        "`messageId`, `conversationId`, `content`, " +
                        "notindexed=`messageId`, notindexed=`conversationId`, " +
                        "tokenize=unicode61)"
                )
                db.execSQL(
                    "INSERT INTO messages_fts(messageId, conversationId, content) " +
                        "SELECT id, conversationId, content FROM messages"
                )
            }
        }

        /** P-009: v3 → v4 adds folderId (null = All). Never a wipe. */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE conversations ADD COLUMN folderId TEXT")
            }
        }
    }
}
