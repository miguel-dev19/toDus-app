package cu.todus.app.data.remote

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class S3Uploader(private val xmppClient: XmppClient) {
    private val okHttpClient = OkHttpClient()
    
    private suspend fun getUploadUrl(fileType: Int, size: Int): Pair<String, String> = withContext(Dispatchers.IO) {
        try {
            val conn = xmppClient.connection ?: throw Exception("No connection")
            val iq = cu.todus.app.data.remote.iq.s3.PutUrlIQ(fileType = fileType, size = size)
            iq.stanzaId = (1..8).map { "abcdef0123456789".random() }.joinToString("")
            val resp = conn.sendIqRequestAndWaitForResponse(iq)
            val xml = resp?.toXML()?.toString() ?: throw Exception("No response")
            val put = Regex("put='([^']+)'").find(xml)?.groupValues?.get(1)?.replace("&amp;", "&") ?: throw Exception("No PUT")
            val get = Regex("get='([^']+)'").find(xml)?.groupValues?.get(1)?.replace("&amp;", "&") ?: throw Exception("No GET")
            Pair(put, get)
        } catch (e: Exception) { throw e }
    }
    
    suspend fun uploadImage(uri: Uri, context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            val data = context.contentResolver.openInputStream(uri)!!.readBytes()
            val (putUrl, getUrl) = getUploadUrl(4, data.size)
            val req = Request.Builder().url(putUrl).put(data.toRequestBody("application/octet-stream".toMediaType())).build()
            if (okHttpClient.newCall(req).execute().isSuccessful) Result.success(getUrl) else Result.failure(Exception("Upload failed"))
        } catch (e: Exception) { Result.failure(e) }
    }
    
    suspend fun uploadProfileImage(uri: Uri, context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            val data = context.contentResolver.openInputStream(uri)!!.readBytes()
            val (putUrl, getUrl) = getUploadUrl(5, data.size)
            val req = Request.Builder().url(putUrl).put(data.toRequestBody("application/octet-stream".toMediaType())).build()
            if (okHttpClient.newCall(req).execute().isSuccessful) Result.success(getUrl) else Result.failure(Exception("Upload failed"))
        } catch (e: Exception) { Result.failure(e) }
    }
}
