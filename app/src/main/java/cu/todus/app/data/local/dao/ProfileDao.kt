package cu.todus.app.data.local.dao

import androidx.room.*
import cu.todus.app.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles WHERE phone = :phone")
    suspend fun getProfile(phone: String): ProfileEntity?

    @Query("SELECT * FROM profiles WHERE phone = :phone")
    fun getProfileFlow(phone: String): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles ORDER BY alias ASC")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE alias != '' ORDER BY alias ASC")
    suspend fun getAllProfilesOnce(): List<ProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: ProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(profiles: List<ProfileEntity>)

    @Query("UPDATE profiles SET alias = :alias, description = :description, photoUrl = :photoUrl, photoThumbUrl = :photoThumbUrl, todusId = :todusId, official = :official, \"exists\" = :exists, lastUpdated = :lastUpdated WHERE phone = :phone")
    suspend fun updateProfile(phone: String, alias: String, description: String, photoUrl: String, photoThumbUrl: String, todusId: String, official: Boolean, exists: Boolean, lastUpdated: Long = System.currentTimeMillis())

    @Delete
    suspend fun delete(profile: ProfileEntity)

    @Query("DELETE FROM profiles WHERE lastUpdated < :olderThan")
    suspend fun deleteOldProfiles(olderThan: Long = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000)
}
