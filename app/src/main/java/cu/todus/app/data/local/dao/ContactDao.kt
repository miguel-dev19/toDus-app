package cu.todus.app.data.local.dao

import androidx.room.*
import cu.todus.app.data.local.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts WHERE isInRoster = 1 ORDER BY alias ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>
    @Query("SELECT * FROM contacts ORDER BY alias ASC")
    fun getAllContactsIncludingNonRoster(): Flow<List<ContactEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ContactEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contacts: List<ContactEntity>)
    @Query("SELECT * FROM contacts WHERE phone = :phone LIMIT 1")
    suspend fun getByPhone(phone: String): ContactEntity?
    @Query("UPDATE contacts SET alias = :alias, avatarUrl = :avatarUrl, toDusId = :toDusId WHERE phone = :phone")
    suspend fun updateInfo(phone: String, alias: String, avatarUrl: String, toDusId: String)
    @Delete
    suspend fun delete(contact: ContactEntity)
}
