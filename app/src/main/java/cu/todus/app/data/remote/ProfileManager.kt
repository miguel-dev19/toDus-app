package cu.todus.app.data.remote

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.*
import org.jivesoftware.smack.packet.IQ
import org.jivesoftware.smack.tcp.XMPPTCPConnection

class ProfileManager(private val connection: XMPPTCPConnection) {
    
    companion object {
        private const val TAG = "ProfileManager"
    }
    
    data class UserProfile(
        val username: String,
        val alias: String,
        val description: String,
        val photoUrl: String,
        val photoThumbnail: String,
        val toDusId: String,
        val official: Boolean,
        val exists: Boolean
    )
    
    private fun randomId(len: Int = 8) = (1..len).map { "abcdef0123456789".random() }.joinToString("")
    
    @Suppress("UNCHECKED_CAST")
    suspend fun getProfile(phone: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val iq = cu.todus.app.data.remote.iq.user.GetUserInfoIQ(phone)
            iq.stanzaId = randomId()
            val response: IQ? = connection.sendIqRequestAndWaitForResponse(iq) as? IQ
            val xml = response?.toXML()?.toString() ?: return@withContext Result.failure(Exception("No response"))
            
            val aliasEncoded = extractAttr(xml, "alias") ?: ""
            val description = extractAttr(xml, "description") ?: ""
            val photoUrl = extractAttr(xml, "pic_url") ?: ""
            val photoThumbnail = extractAttr(xml, "pic_thumb_url") ?: ""
            val toDusId = extractAttr(xml, "todus_id") ?: ""
            val official = extractAttr(xml, "official") == "1"
            val exists = extractAttr(xml, "exists") == "1"
            
            val alias = try {
                String(Base64.decode(aliasEncoded, Base64.DEFAULT))
            } catch (e: Exception) { aliasEncoded }
            
            Result.success(UserProfile(username = phone, alias = alias, description = description, photoUrl = photoUrl, photoThumbnail = photoThumbnail, toDusId = toDusId, official = official, exists = exists))
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
            Result.failure(e)
        }
    }
    
    private fun extractAttr(xml: String, attr: String): String? {
        return Regex("""$attr='([^']+)'""").find(xml)?.groupValues?.get(1)
    }
}
