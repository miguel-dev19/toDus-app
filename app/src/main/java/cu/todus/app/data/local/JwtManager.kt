package cu.todus.app.data.local

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject
import java.util.Base64

class JwtManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("todus_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_JWT = "jwt"
        private const val KEY_PHONE = "phone"
        private const val KEY_EXP = "jwt_exp"
    }
    
    fun saveJwt(jwt: String, phone: String) {
        prefs.edit()
            .putString(KEY_JWT, jwt)
            .putString(KEY_PHONE, phone)
            .putLong(KEY_EXP, getExpiration(jwt))
            .apply()
    }
    
    fun getJwt(): String? = prefs.getString(KEY_JWT, null)
    fun getPhone(): String? = prefs.getString(KEY_PHONE, null)
    
    fun isJwtValid(): Boolean {
        val jwt = getJwt() ?: return false
        val exp = prefs.getLong(KEY_EXP, 0)
        return exp > System.currentTimeMillis() / 1000
    }
    
    fun clear() {
        prefs.edit().clear().apply()
    }
    
    private fun getExpiration(jwt: String): Long {
        return try {
            val parts = jwt.split(".")
            val payload = String(Base64.getUrlDecoder().decode(parts[1]))
            JSONObject(payload).getLong("exp")
        } catch (e: Exception) {
            0
        }
    }
}
