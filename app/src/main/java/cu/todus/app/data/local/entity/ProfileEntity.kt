package cu.todus.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey
    val phone: String,
    val alias: String = "",
    val description: String = "",
    val photoUrl: String = "",
    val photoThumbUrl: String = "",
    val todusId: String = "",
    val official: Boolean = false,
    @ColumnInfo(name = "exists")
    val exists: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)
