package cu.todus.app.data.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatJid: String,
    val senderPhone: String,
    val body: String,
    val type: String = "text",
    val state: String = "sent",
    val timestamp: Long,
    val mediaUrl: String = "",
    val mediaSize: Long = 0,
    val thumbnail: String = ""
)
