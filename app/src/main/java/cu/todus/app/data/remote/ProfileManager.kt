package cu.todus.app.data.remote

import android.util.Base64
import cu.todus.app.data.local.dao.ContactDao
import cu.todus.app.data.local.dao.ProfileDao
import cu.todus.app.data.local.entity.ContactEntity
import cu.todus.app.data.local.entity.ProfileEntity
import kotlinx.coroutines.*

class ProfileManager(
    private val xmppClient: XmppClient,
    private val profileDao: ProfileDao? = null,
    private val contactDao: ContactDao? = null
) {

    data class UserProfile(
        val username: String, val alias: String, val description: String,
        val photoUrl: String, val photoThumbUrl: String,
        val toDusId: String, val official: Boolean, val exists: Boolean
    )
    
    data class RosterContact(
        val phone: String, val alias: String, val todusId: String = "",
        val bio: String = "", val photoUrl: String = "",
        val photoThumbUrl: String = "", val official: Boolean = false
    )

    /**
     * Obtiene perfil: primero intenta Room (cache), luego servidor.
     */
    suspend fun getProfile(phone: String, forceRefresh: Boolean = false): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            // 1. Intentar cache
            if (!forceRefresh) {
                val cached = profileDao?.getProfile(phone)
                if (cached != null && System.currentTimeMillis() - cached.lastUpdated < 3600000) { // 1 hora
                    return@withContext Result.success(UserProfile(
                        username = cached.phone, alias = cached.alias, description = cached.description,
                        photoUrl = cached.photoUrl, photoThumbUrl = cached.photoThumbUrl,
                        toDusId = cached.todusId, official = cached.official, exists = cached.exists
                    ))
                }
            }
            
            // 2. Pedir al servidor
            xmppClient.sendIq(ToDusProtocol.buildGetUserInfoIq(phone))
            val response = waitForIqResponse()
            
            if (response.isNotEmpty() && response.contains("<user ")) {
                val profile = parseUserInfo(response, phone)
                
                // Guardar en Room
                profileDao?.insert(ProfileEntity(
                    phone = phone, alias = profile.alias, description = profile.description,
                    photoUrl = profile.photoUrl, photoThumbUrl = profile.photoThumbUrl,
                    todusId = profile.toDusId, official = profile.official, exists = profile.exists
                ))
                
                Result.success(profile)
            } else {
                // Devolver datos básicos
                Result.success(UserProfile(username = phone, alias = phone, description = "",
                    photoUrl = "", photoThumbUrl = "", toDusId = "", official = false, exists = true))
            }
        } catch (e: Exception) { Result.failure(e) }
    }
    
    /**
     * Obtiene contactos que usan ToDus y guarda en Room.
     */
    suspend fun getRosterWithToDusUsers(): Result<List<RosterContact>> = withContext(Dispatchers.IO) {
        try {
            xmppClient.sendIq(ToDusProtocol.buildRosterListIq())
            val response = waitForIqResponse(5000)
            if (response.isEmpty()) return@withContext Result.success(emptyList())
            
            val rawContacts = ToDusProtocol.parseRosterContacts(response)
            val todusUsers = mutableListOf<RosterContact>()
            
            for ((phone, aliasB64) in rawContacts) {
                val name = try { String(Base64.decode(aliasB64, Base64.DEFAULT)) } catch (e: Exception) { aliasB64 }
                
                xmppClient.sendIq(ToDusProtocol.buildGetUserInfoIq(phone))
                delay(300)
                
                val infoResponse = waitForIqResponse(3000)
                
                if (infoResponse.isNotEmpty() && infoResponse.contains("<user ")) {
                    val exists = extractAttr(infoResponse, "exists")
                    
                    if (exists == "1") {
                        val todusId = extractAttr(infoResponse, "todus_id") ?: ""
                        val desc = extractAttr(infoResponse, "description") ?: ""
                        val picUrl = extractAttr(infoResponse, "pic_url") ?: ""
                        val picThumb = extractAttr(infoResponse, "pic_thumb_url") ?: ""
                        
                        val aliasInfo = extractAttr(infoResponse, "alias")
                        val displayName = if (aliasInfo != null) {
                            try { String(Base64.decode(aliasInfo, Base64.DEFAULT)) } catch (e: Exception) { name }
                        } else name
                        
                        val contact = RosterContact(phone = phone, alias = displayName, todusId = todusId,
                            bio = desc, photoUrl = picUrl, photoThumbUrl = picThumb)
                        todusUsers.add(contact)
                        
                        // Guardar en Room: ContactEntity + ProfileEntity
                        contactDao?.insert(ContactEntity(phone = phone, name = displayName,
                            alias = displayName, avatarUrl = picThumb.ifEmpty { picUrl },
                            todusId = todusId, isRegistered = true))
                        
                        profileDao?.insert(ProfileEntity(phone = phone, alias = displayName,
                            description = desc, photoUrl = picUrl, photoThumbUrl = picThumb,
                            todusId = todusId, exists = true))
                    }
                }
            }
            
            Result.success(todusUsers)
        } catch (e: Exception) { Result.failure(e) }
    }
    
    /**
     * Carga perfiles desde Room (offline).
     */
    suspend fun getCachedProfiles(): List<UserProfile> {
        return profileDao?.getAllProfilesOnce()?.map { entity ->
            UserProfile(username = entity.phone, alias = entity.alias,
                description = entity.description, photoUrl = entity.photoUrl,
                photoThumbUrl = entity.photoThumbUrl, toDusId = entity.todusId,
                official = entity.official, exists = entity.exists)
        } ?: emptyList()
    }
    
    /**
     * Observa el perfil de un usuario (Flow reactivo desde Room).
     */
    fun observeProfile(phone: String) = profileDao?.getProfileFlow(phone)
    
    suspend fun deleteContact(phone: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            xmppClient.sendIq(ToDusProtocol.buildRosterDeleteIq(phone))
            contactDao?.delete(ContactEntity(phone = phone, name = ""))
            profileDao?.delete(ProfileEntity(phone = phone))
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
    
    private suspend fun waitForIqResponse(timeout: Long = 5000): String {
        var response = ""
        val start = System.currentTimeMillis()
        while (response.isEmpty() && System.currentTimeMillis() - start < timeout) {
            delay(200)
            response = xmppClient.getLastIqResponse()
        }
        return response
    }
    
    private fun parseUserInfo(xml: String, phone: String): UserProfile {
        val aliasB64 = extractAttr(xml, "alias") ?: ""
        val alias = try { String(Base64.decode(aliasB64, Base64.DEFAULT)) } catch (e: Exception) { phone }
        return UserProfile(username = phone, alias = alias,
            description = extractAttr(xml, "description") ?: "",
            photoUrl = extractAttr(xml, "pic_url") ?: "",
            photoThumbUrl = extractAttr(xml, "pic_thumb_url") ?: "",
            toDusId = extractAttr(xml, "todus_id") ?: "",
            official = extractAttr(xml, "official") == "1",
            exists = extractAttr(xml, "exists") == "1")
    }
    
    private fun extractAttr(xml: String, attr: String): String? {
        return Regex("$attr='([^']*)'").find(xml)?.groupValues?.get(1)
    }
}
