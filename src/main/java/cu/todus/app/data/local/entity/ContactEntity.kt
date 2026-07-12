package cu.todus.app.data.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val phone: String,
    val alias: String = "",
    val toDusId: String = "",
    val avatarUrl: String = "",
    val isInRoster: Boolean = true
)
