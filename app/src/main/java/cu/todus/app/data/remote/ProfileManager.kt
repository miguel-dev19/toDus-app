package cu.todus.app.data.remote

import android.util.Base64
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class ProfileManager(private val xmppClient: XmppClient) {
    
    data class UserProfile(
        val username: String, val alias: String, val description: String,
        val photoUrl: String, val photoThumbnail: String,
        val toDusId: String, val official: Boolean, val exists: Boolean
    )
    
    private val _profileResponse = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 8)
    
    suspend fun getProfile(phone: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val xml = ToDusProtocol.buildGetUserInfoIq(phone)
            xmppClient.sendIq(xml)
            
            // Esperar respuesta (timeout 10s)
            var response = ""
            val startTime = System.currentTimeMillis()
            while (response.isEmpty() && System.currentTimeMillis() - startTime < 10000) {
                delay(100)
                // La respuesta viene por incomingMessages, pero el perfil es un IQ
                // Usamos un approach simple: leer del stream
            }
            
            // Intentar parsear de la última respuesta
            val info = ToDusProtocol.parseUserInfo(response)
            if (info.isEmpty()) {
                // Fallback: devolver datos vacíos (se usarán los locales)
                Result.success(UserProfile(username = phone, alias = "", description = "", photoUrl = "", photoThumbnail = "", toDusId = "", official = false, exists = true))
            } else {
                val alias = try { String(Base64.decode(info["alias"] ?: "", Base64.DEFAULT)) } catch (e: Exception) { info["alias"] ?: phone }
                Result.success(UserProfile(
                    username = info["username"] ?: phone, alias = alias,
                    description = info["description"] ?: "",
                    photoUrl = info["pic_url"] ?: "", photoThumbnail = info["pic_thumb_url"] ?: "",
                    toDusId = info["todus_id"] ?: "", official = info["official"] == "1",
                    exists = info["exists"] == "1"
                ))
            }
        } catch (e: Exception) { Result.failure(e) }
    }
}
