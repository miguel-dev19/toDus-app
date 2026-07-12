package cu.todus.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cu.todus.app.data.local.dao.ChatDao
import cu.todus.app.data.local.dao.ContactDao
import cu.todus.app.data.local.dao.MessageDao
import cu.todus.app.data.local.entity.ChatEntity
import cu.todus.app.data.local.entity.ContactEntity
import cu.todus.app.data.local.entity.MessageEntity

@Database(
    entities = [
        MessageEntity::class,
        ChatEntity::class,
        ContactEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ToDusDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun chatDao(): ChatDao
    abstract fun contactDao(): ContactDao
    
    companion object {
        @Volatile
        private var INSTANCE: ToDusDatabase? = null
        
        fun getInstance(context: Context): ToDusDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ToDusDatabase::class.java,
                    "todus_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
