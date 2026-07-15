package cu.todus.app.data.remote

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class S3Uploader(private val xmppClient: XmppClient) {
    private val okHttpClient = OkHttpClient()
    
    suspend fun uploadImage(uri: Uri, context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val data = inputStream!!.readBytes(); inputStream.close()
            val iqId = (1..8).map { "abcdef0123456789".random() }.joinToString("")
            val requestXml = "<iq type=\"get\" id=\"$iqId\"><query xmlns=\"todus:purl\" type=\"4\" persistent=\"true\" size=\"${data.size}\" room=\"\"/></iq>"
            val responseXml = xmppClient.sendIqAndWait(requestXml)
            val putUrl = Regex("put='([^']+)'").find(responseXml)?.groupValues?.get(1)?.replace("&amp;", "&") ?: throw Exception("No PUT URL")
            val getUrl = Regex("get='([^']+)'").find(responseXml)?.groupValues?.get(1)?.replace("&amp;", "&") ?: throw Exception("No GET URL")
            val request = Request.Builder().url(putUrl).put(data.toRequestBody("application/octet-stream".toMediaType())).header("Content-Length", data.size.toString()).build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) Result.success(getUrl) else Result.failure(Exception("Upload failed"))
        } catch (e: Exception) { Result.failure(e) }
    }
    
    suspend fun uploadProfileImage(uri: Uri, context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val data = inputStream!!.readBytes(); inputStream.close()
            val iqId = (1..8).map { "abcdef0123456789".random() }.joinToString("")
            val requestXml = "<iq type=\"get\" id=\"$iqId\"><query xmlns=\"todus:purl\" type=\"5\" persistent=\"true\" size=\"${data.size}\" room=\"\"/></iq>"
            val responseXml = xmppClient.sendIqAndWait(requestXml)
            val putUrl = Regex("put='([^']+)'").find(responseXml)?.groupValues?.get(1)?.replace("&amp;", "&") ?: throw Exception("No PUT URL")
            val getUrl = Regex("get='([^']+)'").find(responseXml)?.groupValues?.get(1)?.replace("&amp;", "&") ?: throw Exception("No GET URL")
            val request = Request.Builder().url(putUrl).put(data.toRequestBody("application/octet-stream".toMediaType())).header("Content-Length", data.size.toString()).build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) Result.success(getUrl) else Result.failure(Exception("Upload failed"))
        } catch (e: Exception) { Result.failure(e) }
    }
}
