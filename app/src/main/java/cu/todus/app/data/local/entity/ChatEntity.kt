package cu.todus.app.data.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val jid: String,
    val name: String,
    val lastMessage: String = "",
    val lastTimestamp: Long = 0,
    val unreadCount: Int = 0,
    val avatarUrl: String = ""
)
