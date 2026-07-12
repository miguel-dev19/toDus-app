package cu.todus.app.data.local.dao
import androidx.room.*
import cu.todus.app.data.local.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY alias ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ContactEntity)
}
