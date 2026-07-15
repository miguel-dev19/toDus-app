package cu.todus.app.data.remote

import android.util.Log
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class UserService {
    private val okHttpClient = OkHttpClient()
    
    suspend fun updateProfile(jwt: String, alias: String, description: String = "", photoUrl: String = "", photoThumbnail: String = ""): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val bytes = mutableListOf<Byte>()
            if (alias.isNotEmpty()) { val b = alias.toByteArray(); bytes.add(0x0a); bytes.add(b.size.toByte()); bytes.addAll(b.toList()) }
            if (description.isNotEmpty()) { val b = description.toByteArray(); bytes.add(0x12); bytes.add(b.size.toByte()); bytes.addAll(b.toList()) }
            if (photoUrl.isNotEmpty()) { val b = photoUrl.toByteArray(); bytes.add(0x1a); bytes.add(b.size.toByte()); bytes.addAll(b.toList()) }
            if (photoThumbnail.isNotEmpty()) { val b = photoThumbnail.toByteArray(); bytes.add(0x22); bytes.add(b.size.toByte()); bytes.addAll(b.toList()) }
            val body = bytes.toByteArray()
            val request = Request.Builder().url("https://auth.todus.cu/v2/todus/users.me").header("Authorization", jwt).header("Content-Type", "application/x-protobuf").header("User-Agent", "ToDus 2.1.2 Auth").post(body.toRequestBody("application/x-protobuf".toMediaType())).build()
            val response = okHttpClient.newCall(request).execute()
            Result.success(response.isSuccessful)
        } catch (e: Exception) { Log.e("UserService", "Error: ${e.message}"); Result.failure(e) }
    }
}
