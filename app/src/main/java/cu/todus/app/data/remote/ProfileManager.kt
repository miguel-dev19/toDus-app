package cu.todus.app.data.remote

import android.util.Base64
import kotlinx.coroutines.*

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
            val xml = ToDusProtocol.buildGetUserInfoIq(phone)
            xmppClient.sendIq(xml)

            delay(2000)

            Result.success(
                UserProfile(
                    username = phone,
                    alias = phone,
                    description = "",
                    photoUrl = "",
                    toDusId = "",
                    official = false,
                    exists = true
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
