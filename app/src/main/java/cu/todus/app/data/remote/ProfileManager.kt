package cu.todus.app.data.remote

import android.content.Context
import android.util.Base64
import cu.todus.app.data.local.JwtManager
import cu.todus.app.data.local.dao.ContactDao
import cu.todus.app.data.local.entity.ContactEntity
import kotlinx.coroutines.*

class ProfileManager(
    private val xmppClient: XmppClient,
    private val contactDao: ContactDao? = null,
    private val jwtManager: JwtManager? = null
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

    suspend fun getProfile(phone: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            xmppClient.sendIq(ToDusProtocol.buildGetUserInfoIq(phone))
            val response = waitForIqResponse()
            if (response.isNotEmpty() && response.contains("<user ")) {
                Result.success(parseUserInfo(response, phone))
            } else {
                Result.success(UserProfile(username = phone, alias = phone, description = "",
                    photoUrl = "", photoThumbUrl = "", toDusId = "", official = false, exists = true))
            }
        } catch (e: Exception) { Result.failure(e) }
    }
    
    /**
     * Obtiene contactos que usan ToDus.
     * Primero obtiene el roster, luego verifica cada uno.
     * Los guarda en Room para acceso offline.
     */
    suspend fun getRosterWithToDusUsers(): Result<List<RosterContact>> = withContext(Dispatchers.IO) {
        try {
            // 1. Obtener roster
            xmppClient.sendIq(ToDusProtocol.buildRosterListIq())
            val response = waitForIqResponse(5000)
            if (response.isEmpty()) return@withContext Result.success(emptyList())
            
            val rawContacts = ToDusProtocol.parseRosterContacts(response)
            
            // 2. Verificar cada contacto
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
                        
                        // Guardar en Room
                        contactDao?.insert(ContactEntity(phone = phone, name = displayName,
                            alias = displayName, avatarUrl = picThumb.ifEmpty { picUrl },
                            todusId = todusId, isRegistered = true))
                    }
                }
            }
            
            Result.success(todusUsers)
        } catch (e: Exception) { Result.failure(e) }
    }
    
    /**
     * Carga contactos desde Room (offline) mientras se actualiza del servidor.
     */
    suspend fun getCachedContacts(): List<RosterContact> {
        return contactDao?.getAllContactsOnce()?.map { entity ->
            RosterContact(phone = entity.phone, alias = entity.alias.ifEmpty { entity.name },
                todusId = entity.todusId, photoUrl = entity.avatarUrl)
        } ?: emptyList()
    }
    
    suspend fun deleteContact(phone: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            xmppClient.sendIq(ToDusProtocol.buildRosterDeleteIq(phone))
            contactDao?.delete(ContactEntity(phone = phone, name = ""))
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
    
    suspend fun getBlockedList(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            xmppClient.sendIq(ToDusProtocol.buildBlockListIq())
            val response = waitForIqResponse()
            val blist = extractAttr(response, "blist") ?: ""
            Result.success(blist.split(",").filter { it.isNotEmpty() })
        } catch (e: Exception) { Result.success(emptyList()) }
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
