package cu.todus.app.data.local.dao

import androidx.room.*
import cu.todus.app.data.local.entity.ChatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats ORDER BY lastTimestamp DESC")
    fun getAllChats(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE jid = :jid")
    suspend fun getChat(jid: String): ChatEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chat: ChatEntity)

    @Query("UPDATE chats SET lastMessage = :message, lastTimestamp = :timestamp WHERE jid = :jid")
    suspend fun updateLastMessage(jid: String, message: String, timestamp: Long)

    @Query("UPDATE chats SET unreadCount = 0 WHERE jid = :jid")
    suspend fun clearUnread(jid: String)

    @Query("UPDATE chats SET unreadCount = unreadCount + 1 WHERE jid = :jid")
    suspend fun incrementUnread(jid: String)

    @Query("UPDATE chats SET name = :name, avatarUrl = :avatarUrl WHERE jid = :jid")
    suspend fun updateInfo(jid: String, name: String, avatarUrl: String)

    @Delete
    suspend fun delete(chat: ChatEntity)
}
