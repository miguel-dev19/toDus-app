package cu.todus.app.data.remote

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class S3Uploader(private val xmppClient: XmppClient) {
    private val okHttpClient = OkHttpClient()
    
    suspend fun uploadProfileImage(uri: Uri, context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            val data = context.contentResolver.openInputStream(uri)!!.readBytes()
            Result.success("") // Placeholder - se implementará después
        } catch (e: Exception) { Result.failure(e) }
    }
    
    suspend fun uploadImage(uri: Uri, context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            val data = context.contentResolver.openInputStream(uri)!!.readBytes()
            Result.success("")
        } catch (e: Exception) { Result.failure(e) }
    }
}
