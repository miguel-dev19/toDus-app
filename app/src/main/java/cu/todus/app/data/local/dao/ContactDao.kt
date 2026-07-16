package cu.todus.app.data.local.dao

import androidx.room.*
import cu.todus.app.data.local.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts ORDER BY name ASC")
    suspend fun getAllContactsOnce(): List<ContactEntity>

    @Query("SELECT * FROM contacts WHERE phone = :phone")
    suspend fun getContact(phone: String): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contacts: List<ContactEntity>)

    @Query("UPDATE contacts SET name = :name, alias = :alias, avatarUrl = :avatarUrl, todusId = :todusId, isRegistered = :isRegistered WHERE phone = :phone")
    suspend fun updateInfo(phone: String, name: String, alias: String, avatarUrl: String, todusId: String, isRegistered: Boolean)

    @Delete
    suspend fun delete(contact: ContactEntity)

    @Query("DELETE FROM contacts")
    suspend fun deleteAll()
}
