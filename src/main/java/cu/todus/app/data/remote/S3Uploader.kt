package cu.todus.app.data.remote

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType

class S3Uploader(private val xmppClient: XmppClient) {
    private val okHttpClient = OkHttpClient()

    suspend fun uploadImageUri(uri: Uri, context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val data = inputStream!!.readBytes()
            inputStream.close()

            val iqId = randomHexId(8)
            val requestXml = """<iq type="get" id="$iqId"><query xmlns="todus:purl" type="4" persistent="true" size="${data.size}" room=""/></iq>"""
            val responseXml = xmppClient.sendIqAndWait(requestXml)
            val putUrl = extractAttr(responseXml, "put")?.replace("&amp;", "&") ?: throw Exception("No PUT URL")
            val getUrl = extractAttr(responseXml, "get")?.replace("&amp;", "&") ?: throw Exception("No GET URL")

            val request = Request.Builder().url(putUrl)
                .put(data.toRequestBody("application/octet-stream".toMediaType()))
                .header("Content-Length", data.size.toString()).build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) Result.success(getUrl)
            else Result.failure(Exception("Upload failed: ${response.code}"))
        } catch (e: Exception) { Result.failure(e) }
    }
}

fun extractAttr(xml: String, attr: String): String? {
    val regex = Regex("""$attr='([^']+)'""")
    return regex.find(xml)?.groupValues?.get(1)
}
fun randomHexId(len: Int = 8): String {
    val chars = "abcdef0123456789"
    return (1..len).map { chars.random() }.joinToString("")
}
