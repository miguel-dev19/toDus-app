package cu.todus.app.data.local.dao
import androidx.room.*
import cu.todus.app.data.local.entity.ChatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats ORDER BY lastTimestamp DESC")
    fun getAllChats(): Flow<List<ChatEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chat: ChatEntity)
    @Query("UPDATE chats SET lastMessage = :text, lastTimestamp = :time WHERE jid = :jid")
    suspend fun updateLastMessage(jid: String, text: String, time: Long)
    @Query("UPDATE chats SET unreadCount = unreadCount + 1 WHERE jid = :jid")
    suspend fun incrementUnread(jid: String)
    @Query("UPDATE chats SET unreadCount = 0 WHERE jid = :jid")
    suspend fun clearUnread(jid: String)
}
