package cu.todus.app.data.remote

import android.util.Base64
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class ProfileManager(private val xmppClient: XmppClient) {

    data class UserProfile(
        val username: String,
        val alias: String,
        val description: String,
        val photoUrl: String,
        val toDusId: String,
        val official: Boolean,
        val exists: Boolean
    )

    suspend fun getProfile(phone: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            // 1. Pedir info al servidor
            xmppClient.sendIq(ToDusProtocol.buildGetUserInfoIq(phone))
            
            // 2. Esperar respuesta (máximo 5 segundos)
            var response = ""
            val startTime = System.currentTimeMillis()
            while (response.isEmpty() && System.currentTimeMillis() - startTime < 5000) {
                delay(200)
                response = xmppClient.getLastIqResponse()
            }
            
            // 3. Intentar parsear
            if (response.isNotEmpty() && response.contains("<user ")) {
                val aliasB64 = extractAttribute(response, "alias") ?: ""
                val alias = try { String(Base64.decode(aliasB64, Base64.DEFAULT)) } catch (e: Exception) { phone }
                val picUrl = extractAttribute(response, "pic_url") ?: ""
                val todusId = extractAttribute(response, "todus_id") ?: ""
                val official = extractAttribute(response, "official") == "1"
                val exists = extractAttribute(response, "exists") == "1"
                
                return@withContext Result.success(UserProfile(
                    username = phone, alias = alias, description = "",
                    photoUrl = picUrl, toDusId = todusId,
                    official = official, exists = exists
                ))
            }
            
            // 4. Fallback: devolver datos básicos
            Result.success(UserProfile(
                username = phone, alias = phone, description = "",
                photoUrl = "", toDusId = "", official = false, exists = true
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getRoster(): Result<List<Pair<String, String>>> = withContext(Dispatchers.IO) {
        try {
            xmppClient.sendIq(ToDusProtocol.buildRosterListIq())
            
            var response = ""
            val startTime = System.currentTimeMillis()
            while (response.isEmpty() && System.currentTimeMillis() - startTime < 5000) {
                delay(200)
                response = xmppClient.getLastIqResponse()
            }
            
            if (response.isNotEmpty()) {
                val contacts = ToDusProtocol.parseRosterContacts(response)
                return@withContext Result.success(contacts)
            }
            
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun extractAttribute(xml: String, attr: String): String? {
        val regex = Regex("$attr='([^']*)'")
        return regex.find(xml)?.groupValues?.get(1)
    }
}
