package cu.todus.app.data.local

import android.content.Context
import androidx.room.*

@Database(entities = [MessageEntity::class, ChatEntity::class, ContactEntity::class], version = 1, exportSchema = false)
abstract class ToDusDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun chatDao(): ChatDao
    abstract fun contactDao(): ContactDao

    companion object {
        @Volatile private var INSTANCE: ToDusDatabase? = null
        fun getInstance(context: Context): ToDusDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, ToDusDatabase::class.java, "todus_db").build().also { INSTANCE = it }
            }
        }
    }
}
