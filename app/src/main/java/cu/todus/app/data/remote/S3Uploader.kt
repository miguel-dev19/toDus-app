package cu.todus.app.data.remote

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL

class S3Uploader(private val xmppClient: XmppClient) {
    
    /**
     * Obtiene URLs de S3 usando la conexión XMPP existente
     * Método simplificado que no depende de Smack para parsear
     */
    private suspend fun getS3Urls(fileType: Int, fileSize: Int): Pair<String, String> = withContext(Dispatchers.IO) {
        val conn = xmppClient.connection ?: throw Exception("No hay conexion XMPP")
        val iqId = xmppClient.randomHexId(8)
        val xml = "<iq type=\"get\" id=\"$iqId\"><query xmlns=\"todus:purl\" type=\"$fileType\" persistent=\"true\" size=\"$fileSize\" room=\"\"/></iq>"
        
        // Usar el método raw de Smack que sí funciona
        val stanza = org.jivesoftware.smack.packet.StanzaBuilder.buildStanza(xml)
        val response = conn.sendStanzaAndWaitForResponse(stanza)
        val respXml = response?.toXML()?.toString() ?: throw Exception("Sin respuesta")
        
        val putUrl = Regex("put='([^']+)'").find(respXml)?.groupValues?.get(1)?.replace("&amp;", "&") ?: throw Exception("No PUT URL")
        val getUrl = Regex("get='([^']+)'").find(respXml)?.groupValues?.get(1)?.replace("&amp;", "&") ?: throw Exception("No GET URL")
        Pair(putUrl, getUrl)
    }
    
    suspend fun uploadProfileImage(uri: Uri, context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            val data = context.contentResolver.openInputStream(uri)!!.readBytes()
            val (putUrl, getUrl) = getS3Urls(5, data.size)
            
            val url = URL(putUrl)
            val http = url.openConnection() as HttpURLConnection
            http.requestMethod = "PUT"
            http.setRequestProperty("Content-Type", "application/octet-stream")
            http.setRequestProperty("Content-Length", data.size.toString())
            http.doOutput = true
            http.connectTimeout = 30000
            http.outputStream.use { it.write(data) }
            
            if (http.responseCode in 200..299) Result.success(getUrl)
            else Result.failure(Exception("HTTP ${http.responseCode}"))
        } catch (e: Exception) { Result.failure(e) }
    }
    
    suspend fun uploadImage(uri: Uri, context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            val data = context.contentResolver.openInputStream(uri)!!.readBytes()
            val (putUrl, getUrl) = getS3Urls(4, data.size)
            
            val url = URL(putUrl)
            val http = url.openConnection() as HttpURLConnection
            http.requestMethod = "PUT"
            http.setRequestProperty("Content-Type", "application/octet-stream")
            http.setRequestProperty("Content-Length", data.size.toString())
            http.doOutput = true
            http.connectTimeout = 30000
            http.outputStream.use { it.write(data) }
            
            if (http.responseCode in 200..299) Result.success(getUrl)
            else Result.failure(Exception("HTTP ${http.responseCode}"))
        } catch (e: Exception) { Result.failure(e) }
    }
}
