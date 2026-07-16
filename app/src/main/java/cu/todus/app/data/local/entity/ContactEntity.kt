package cu.todus.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey
    val phone: String,
    val name: String,
    val alias: String = "",
    val avatarUrl: String = "",
    val todusId: String = "",
    val isRegistered: Boolean = false
)
