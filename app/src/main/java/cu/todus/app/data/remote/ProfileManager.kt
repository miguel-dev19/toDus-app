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
        val photoThumbUrl: String,
        val toDusId: String,
        val official: Boolean,
        val exists: Boolean
    )
    
    data class RosterContact(
        val phone: String,
        val alias: String,
        val todusId: String = "",
        val photoUrl: String = "",
        val official: Boolean = false
    )

    suspend fun getProfile(phone: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            xmppClient.sendIq(ToDusProtocol.buildGetUserInfoIq(phone))
            
            var response = ""
            val startTime = System.currentTimeMillis()
            while (response.isEmpty() && System.currentTimeMillis() - startTime < 5000) {
                delay(200)
                response = xmppClient.getLastIqResponse()
            }
            
            if (response.isNotEmpty() && response.contains("<user ")) {
                val aliasB64 = extractAttr(response, "alias") ?: ""
                val alias = try { String(Base64.decode(aliasB64, Base64.DEFAULT)) } catch (e: Exception) { phone }
                val picUrl = extractAttr(response, "pic_url") ?: ""
                val picThumb = extractAttr(response, "pic_thumb_url") ?: ""
                val todusId = extractAttr(response, "todus_id") ?: ""
                val official = extractAttr(response, "official") == "1"
                val exists = extractAttr(response, "exists") == "1"
                val desc = extractAttr(response, "description") ?: ""
                
                return@withContext Result.success(UserProfile(
                    username = phone, alias = alias, description = desc,
                    photoUrl = picUrl, photoThumbUrl = picThumb,
                    toDusId = todusId, official = official, exists = exists
                ))
            }
            
            Result.success(UserProfile(
                username = phone, alias = phone, description = "",
                photoUrl = "", photoThumbUrl = "", toDusId = "",
                official = false, exists = true
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getRoster(): Result<List<RosterContact>> = withContext(Dispatchers.IO) {
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
                val result = contacts.map { (phone, aliasB64) ->
                    val name = try { String(Base64.decode(aliasB64, Base64.DEFAULT)) } catch (e: Exception) { aliasB64 }
                    RosterContact(phone = phone, alias = name)
                }
                return@withContext Result.success(result)
            }
            
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getBlockedList(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            xmppClient.sendIq(ToDusProtocol.buildBlockListIq())
            
            var response = ""
            val startTime = System.currentTimeMillis()
            while (response.isEmpty() && System.currentTimeMillis() - startTime < 5000) {
                delay(200)
                response = xmppClient.getLastIqResponse()
            }
            
            val blist = extractAttr(response, "blist") ?: ""
            Result.success(blist.split(",").filter { it.isNotEmpty() })
        } catch (e: Exception) {
            Result.success(emptyList())
        }
    }
    
    private fun extractAttr(xml: String, attr: String): String? {
        val regex = Regex("$attr='([^']*)'")
        return regex.find(xml)?.groupValues?.get(1)
    }
}
