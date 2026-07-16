package cu.todus.app.data.remote

import android.util.Base64
import kotlinx.coroutines.*

class ProfileManager(private val xmppClient: XmppClient) {

    data class UserProfile(
        val username: String,
        val alias: String,
        val description: String,
        val photoUrl: String,
        val photoThumbUrl: String,
        val toDusId: String,
        val official: Boolean,
        val exists: Boolean
    )
    
    data class RosterContact(
        val phone: String,
        val alias: String,
        val todusId: String = "",
        val bio: String = "",
        val photoUrl: String = "",
        val photoThumbUrl: String = "",
        val official: Boolean = false
    )

    suspend fun getProfile(phone: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            xmppClient.sendIq(ToDusProtocol.buildGetUserInfoIq(phone))
            
            var response = ""
            val start = System.currentTimeMillis()
            while (response.isEmpty() && System.currentTimeMillis() - start < 5000) {
                delay(200)
                response = xmppClient.getLastIqResponse()
            }
            
            if (response.isNotEmpty() && response.contains("<user ")) {
                return@withContext Result.success(parseUserInfo(response, phone))
            }
            
            Result.success(UserProfile(username = phone, alias = phone, description = "",
                photoUrl = "", photoThumbUrl = "", toDusId = "", official = false, exists = true))
        } catch (e: Exception) { Result.failure(e) }
    }
    
    /**
     * Obtiene la lista de contactos SOLO de los que usan ToDus.
     * Primero pide el roster, luego verifica uno por uno con getInfo.
     */
    suspend fun getRosterWithToDusUsers(): Result<List<RosterContact>> = withContext(Dispatchers.IO) {
        try {
            // 1. Obtener roster
            xmppClient.sendIq(ToDusProtocol.buildRosterListIq())
            
            var response = ""
            val start = System.currentTimeMillis()
            while (response.isEmpty() && System.currentTimeMillis() - start < 5000) {
                delay(200)
                response = xmppClient.getLastIqResponse()
            }
            
            if (response.isEmpty()) return@withContext Result.success(emptyList())
            
            val rawContacts = ToDusProtocol.parseRosterContacts(response)
            
            // 2. Verificar cada contacto
            val todusUsers = mutableListOf<RosterContact>()
            
            for ((phone, aliasB64) in rawContacts) {
                val name = try { String(Base64.decode(aliasB64, Base64.DEFAULT)) } catch (e: Exception) { aliasB64 }
                
                // Pedir info del usuario
                xmppClient.sendIq(ToDusProtocol.buildGetUserInfoIq(phone))
                delay(300)
                
                var infoResponse = ""
                val infoStart = System.currentTimeMillis()
                while (infoResponse.isEmpty() && System.currentTimeMillis() - infoStart < 3000) {
                    delay(200)
                    infoResponse = xmppClient.getLastIqResponse()
                }
                
                if (infoResponse.isNotEmpty() && infoResponse.contains("<user ")) {
                    val exists = extractAttr(infoResponse, "exists")
                    
                    // Solo incluir si existe en ToDus
                    if (exists == "1") {
                        val todusId = extractAttr(infoResponse, "todus_id") ?: ""
                        val desc = extractAttr(infoResponse, "description") ?: ""
                        val picUrl = extractAttr(infoResponse, "pic_url") ?: ""
                        val picThumb = extractAttr(infoResponse, "pic_thumb_url") ?: ""
                        
                        // El alias en getInfo puede ser diferente al del roster
                        val aliasInfo = extractAttr(infoResponse, "alias")
                        val displayName = if (aliasInfo != null) {
                            try { String(Base64.decode(aliasInfo, Base64.DEFAULT)) } catch (e: Exception) { name }
                        } else name
                        
                        todusUsers.add(RosterContact(
                            phone = phone,
                            alias = displayName,
                            todusId = todusId,
                            bio = desc,
                            photoUrl = picUrl,
                            photoThumbUrl = picThumb
                        ))
                    }
                }
            }
            
            Result.success(todusUsers)
        } catch (e: Exception) { Result.failure(e) }
    }
    
    suspend fun getBlockedList(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            xmppClient.sendIq(ToDusProtocol.buildBlockListIq())
            var response = ""
            val start = System.currentTimeMillis()
            while (response.isEmpty() && System.currentTimeMillis() - start < 5000) { delay(200); response = xmppClient.getLastIqResponse() }
            val blist = extractAttr(response, "blist") ?: ""
            Result.success(blist.split(",").filter { it.isNotEmpty() })
        } catch (e: Exception) { Result.success(emptyList()) }
    }
    
    private fun parseUserInfo(xml: String, phone: String): UserProfile {
        val aliasB64 = extractAttr(xml, "alias") ?: ""
        val alias = try { String(Base64.decode(aliasB64, Base64.DEFAULT)) } catch (e: Exception) { phone }
        return UserProfile(
            username = phone,
            alias = alias,
            description = extractAttr(xml, "description") ?: "",
            photoUrl = extractAttr(xml, "pic_url") ?: "",
            photoThumbUrl = extractAttr(xml, "pic_thumb_url") ?: "",
            toDusId = extractAttr(xml, "todus_id") ?: "",
            official = extractAttr(xml, "official") == "1",
            exists = extractAttr(xml, "exists") == "1"
        )
    }
    
    private fun extractAttr(xml: String, attr: String): String? {
        return Regex("$attr='([^']*)'").find(xml)?.groupValues?.get(1)
    }
}
