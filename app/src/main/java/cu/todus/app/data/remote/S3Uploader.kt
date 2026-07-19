package cu.todus.app.data.remote

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class S3Uploader(private val xmppClient: XmppClient) {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    data class S3Urls(val putUrl: String, val getUrl: String)

    suspend fun uploadFile(context: Context, uri: Uri, fileType: Int = 4): Result<S3Urls> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("No se pudo abrir el archivo"))
            val bytes = inputStream.readBytes()
            inputStream.close()
            val size = bytes.size.toLong()

            xmppClient.sendIq(ToDusProtocol.buildS3UploadIq(fileType, size))

            var iqResponse = ""
            val start = System.currentTimeMillis()
            while (iqResponse.isEmpty() && System.currentTimeMillis() - start < 10000) {
                delay(200)
                iqResponse = xmppClient.getLastIqResponse()
            }

            if (!iqResponse.contains("todus:purl")) {
                return@withContext Result.failure(Exception("No se recibió respuesta del servidor S3"))
            }

            val putUrl = ToDusProtocol.extractAttribute(iqResponse, "put") ?: ""
            val getUrl = ToDusProtocol.extractAttribute(iqResponse, "get") ?: ""
            if (putUrl.isEmpty() || getUrl.isEmpty()) {
                return@withContext Result.failure(Exception("URLs no encontradas en respuesta"))
            }

            val requestBody = bytes.toRequestBody("application/octet-stream".toMediaType())
            val request = Request.Builder().url(putUrl).put(requestBody)
                .header("Content-Length", size.toString()).build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Error subiendo: ${response.code}"))
            }

            Result.success(S3Urls(putUrl, getUrl))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProfileImage(uri: Uri, context: Context, phone: String, jwt: String): Result<String> {
        return uploadFile(context, uri, 5).map { it.getUrl }
    }

    suspend fun uploadCompressedFile(file: File, fileType: Int = 4): Result<S3Urls> = withContext(Dispatchers.IO) {
        try {
            val bytes = file.readBytes()
            val size = bytes.size.toLong()

            xmppClient.sendIq(ToDusProtocol.buildS3UploadIq(fileType, size))

            var iqResponse = ""
            val start = System.currentTimeMillis()
            while (iqResponse.isEmpty() && System.currentTimeMillis() - start < 10000) {
                delay(200)
                iqResponse = xmppClient.getLastIqResponse()
            }

            val putUrl = ToDusProtocol.extractAttribute(iqResponse, "put") ?: ""
            val getUrl = ToDusProtocol.extractAttribute(iqResponse, "get") ?: ""
            if (putUrl.isEmpty() || getUrl.isEmpty()) {
                return@withContext Result.failure(Exception("URLs no encontradas"))
            }

            val requestBody = bytes.toRequestBody("application/octet-stream".toMediaType())
            val request = Request.Builder().url(putUrl).put(requestBody)
                .header("Content-Length", size.toString()).build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Error subiendo: ${response.code}"))
            }

            Result.success(S3Urls(putUrl, getUrl))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
