package cu.todus.app.data.local.dao

import androidx.room.*
import cu.todus.app.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE chatJid = :chatJid ORDER BY timestamp ASC")
    fun getMessages(chatJid: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE chatJid = :chatJid ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessage(chatJid: String): MessageEntity?

    @Query("SELECT COUNT(*) FROM messages WHERE chatJid = :chatJid AND state = 'received'")
    suspend fun getUnreadCount(chatJid: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    @Query("UPDATE messages SET state = :state WHERE id = :messageId")
    suspend fun updateState(messageId: String, state: String)

    @Query("UPDATE messages SET state = 'delivered' WHERE id = :messageId")
    suspend fun markAsDelivered(messageId: String)

    @Query("UPDATE messages SET state = 'read' WHERE id = :messageId")
    suspend fun markAsRead(messageId: String)

    @Query("DELETE FROM messages WHERE chatJid = :chatJid")
    suspend fun deleteAll(chatJid: String)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteById(messageId: String)
}
