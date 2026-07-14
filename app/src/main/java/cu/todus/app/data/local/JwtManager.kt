package cu.todus.app.data.local

import android.content.Context
import android.content.SharedPreferences
import cu.todus.app.data.remote.XmppClient
import kotlinx.coroutines.*
import org.json.JSONObject
import java.util.Base64

class JwtManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("todus_prefs", Context.MODE_PRIVATE)
    private val xmppClient = XmppClient()
    
    companion object {
        private const val KEY_JWT = "jwt"
        private const val KEY_PHONE = "phone"
        private const val KEY_EXP = "jwt_exp"
        private const val KEY_ALIAS = "alias"
        private const val KEY_AVATAR = "avatar"
        private const val KEY_TODUS_ID = "todus_id"
    }
    
    fun saveJwt(jwt: String, phone: String) {
        prefs.edit()
            .putString(KEY_JWT, jwt)
            .putString(KEY_PHONE, phone)
            .putLong(KEY_EXP, getExpiration(jwt))
            .apply()
    }
    
    fun saveProfile(alias: String, avatar: String = "", todusId: String = "") {
        prefs.edit()
            .putString(KEY_ALIAS, alias)
            .putString(KEY_AVATAR, avatar)
            .putString(KEY_TODUS_ID, todusId)
            .apply()
    }
    
    fun getJwt(): String? = prefs.getString(KEY_JWT, null)
    fun getPhone(): String? = prefs.getString(KEY_PHONE, null)
    fun getAlias(): String? = prefs.getString(KEY_ALIAS, null)
    fun getAvatar(): String? = prefs.getString(KEY_AVATAR, null)
    fun getToDusId(): String? = prefs.getString(KEY_TODUS_ID, null)
    
    fun isJwtValid(): Boolean {
        val jwt = getJwt() ?: return false
        val exp = prefs.getLong(KEY_EXP, 0)
        return exp > (System.currentTimeMillis() / 1000) + 300
    }
    
    fun isJwtExpired(): Boolean {
        val exp = prefs.getLong(KEY_EXP, 0)
        return exp > 0 && exp <= System.currentTimeMillis() / 1000
    }
    
    suspend fun revalidateJwt(): Result<String> {
        val phone = getPhone() ?: return Result.failure(Exception("No phone saved"))
        return try {
            val result = xmppClient.authenticate(phone)
            result.onSuccess { jwt -> saveJwt(jwt, phone) }
            result
        } catch (e: Exception) { Result.failure(e) }
    }
    
    fun clear() { prefs.edit().clear().apply() }
    
    private fun getExpiration(jwt: String): Long {
        return try {
            val parts = jwt.split(".")
            val payload = String(Base64.getUrlDecoder().decode(parts[1]))
            JSONObject(payload).getLong("exp")
        } catch (e: Exception) { 0 }
    }
}
