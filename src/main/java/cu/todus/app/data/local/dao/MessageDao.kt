package cu.todus.app.data.local.dao
import androidx.room.*
import cu.todus.app.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE chatJid = :chatJid ORDER BY timestamp ASC")
    fun getMessages(chatJid: String): Flow<List<MessageEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)
    @Query("UPDATE messages SET state = :state WHERE id = :msgId")
    suspend fun updateState(msgId: String, state: String)
}
