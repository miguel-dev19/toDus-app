package cu.todus.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import cu.todus.app.data.local.dao.ChatDao
import cu.todus.app.data.local.dao.ContactDao
import cu.todus.app.data.local.dao.MessageDao
import cu.todus.app.data.local.dao.ProfileDao
import cu.todus.app.data.local.entity.ChatEntity
import cu.todus.app.data.local.entity.ContactEntity
import cu.todus.app.data.local.entity.MessageEntity
import cu.todus.app.data.local.entity.ProfileEntity

@Database(
    entities = [MessageEntity::class, ChatEntity::class, ContactEntity::class, ProfileEntity::class],
    version = 3,
    exportSchema = false
)
abstract class ToDusDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun chatDao(): ChatDao
    abstract fun contactDao(): ContactDao
    abstract fun profileDao(): ProfileDao

    companion object {
        @Volatile
        private var INSTANCE: ToDusDatabase? = null

        // ⭐ FIX: Migraciones que NO borran datos
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE messages ADD COLUMN mediaUrl TEXT")
                db.execSQL("ALTER TABLE messages ADD COLUMN isReceipt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE messages ADD COLUMN receiptMsgId TEXT")
                db.execSQL("ALTER TABLE messages ADD COLUMN isComposing INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE messages ADD COLUMN isPresence INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE messages ADD COLUMN isDeliveryAck INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS profiles (
                        phone TEXT PRIMARY KEY NOT NULL, 
                        alias TEXT, 
                        description TEXT, 
                        photoUrl TEXT, 
                        photoThumbUrl TEXT, 
                        todusId TEXT, 
                        official INTEGER NOT NULL DEFAULT 0, 
                        "exists" INTEGER NOT NULL DEFAULT 0, 
                        lastUpdated INTEGER NOT NULL DEFAULT 0
                    )
                """)
            }
        }

        fun getInstance(context: Context): ToDusDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ToDusDatabase::class.java,
                    "todus_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}