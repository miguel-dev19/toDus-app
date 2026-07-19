package cu.todus.app.data.remote

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileInputStream

class S3Uploader(private val xmppClient: XmppClient) {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    data class S3Urls(val putUrl: String, val getUrl: String)

    /**
     * Pipeline completo: solicitar URL → subir → devolver URL pública
     */
    suspend fun uploadFile(
        context: Context,
        uri: Uri,
        fileType: Int = 4 // 4 = imagen, 3 = video, 2 = audio
    ): Result<S3Urls> = withContext(Dispatchers.IO) {
        try {
            // 1. Leer archivo
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("No se pudo abrir el archivo"))
            val bytes = inputStream.readBytes()
            inputStream.close()
            val size = bytes.size.toLong()

            // 2. Solicitar URL de subida
            xmppClient.sendIq(ToDusProtocol.buildS3UploadIq(fileType, size))
            
            // Esperar respuesta
            var iqResponse = ""
            val start = System.currentTimeMillis()
            while (iqResponse.isEmpty() && System.currentTimeMillis() - start < 10000) {
                delay(200)
                iqResponse = xmppClient.getLastIqResponse()
            }

            if (!iqResponse.contains("todus:purl")) {
                return@withContext Result.failure(Exception("No se recibió respuesta del servidor S3"))
            }

            val putUrl = ToDusProtocol.extractAttribute(iqResponse, "put")
            val getUrl = ToDusProtocol.extractAttribute(iqResponse, "get")
                ?: return@withContext Result.failure(Exception("URLs no encontradas en respuesta"))

            // 3. Subir archivo a S3
            val requestBody = bytes.toRequestBody("application/octet-stream".toMediaType())
            val request = Request.Builder()
                .url(putUrl)
                .put(requestBody)
                .header("Content-Length", size.toString())
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Error subiendo: ${response.code}"))
            }

            Result.success(S3Urls(putUrl, getUrl))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Subir imagen de perfil
     */
    suspend fun uploadProfileImage(uri: Uri, context: Context, phone: String, jwt: String): Result<String> {
        return uploadFile(context, uri, 5).map { it.getUrl }
    }

    /**
     * Subir desde archivo ya comprimido
     */
    suspend fun uploadCompressedFile(file: File, fileType: Int = 4): Result<S3Urls> {
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

            val putUrl = ToDusProtocol.extractAttribute(iqResponse, "put")
            val getUrl = ToDusProtocol.extractAttribute(iqResponse, "get")
                ?: return Result.failure(Exception("URLs no encontradas"))

            val requestBody = bytes.toRequestBody("application/octet-stream".toMediaType())
            val request = Request.Builder()
                .url(putUrl)
                .put(requestBody)
                .header("Content-Length", size.toString())
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return Result.failure(Exception("Error subiendo: ${response.code}"))
            }

            Result.success(S3Urls(putUrl, getUrl))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
