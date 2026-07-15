package cu.todus.app.data.remote

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.*
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate

class S3Uploader(private val xmppClient: XmppClient) {
    
    /**
     * Sube una imagen de perfil a S3 usando HTTP PUT directo
     * Obtiene las URLs del servidor XMPP y sube sin depender de Smack
     */
    suspend fun uploadProfileImage(uri: Uri, context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            val data = context.contentResolver.openInputStream(uri)!!.readBytes()
            
            // 1. Obtener URLs de S3 via XMPP (usando el connection de Smack)
            val conn = xmppClient.connection ?: throw Exception("No hay conexion XMPP")
            val iqId = xmppClient.randomHexId(8)
            val requestXml = "<iq type=\"get\" id=\"$iqId\"><query xmlns=\"todus:purl\" type=\"5\" persistent=\"true\" size=\"${data.size}\" room=\"\"/></iq>"
            
            // Parsear y enviar IQ
            val parser = org.jivesoftware.smack.xml.XmlPullParserFactory.newInstance().newPullParser(requestXml.reader())
            val iq = org.jivesoftware.smack.util.PacketParserUtils.parseIQ(parser)
            val response = conn.sendIqRequestAndWaitForResponse(iq)
            val xml = response?.toXML()?.toString() ?: throw Exception("Sin respuesta del servidor")
            
            val putUrl = Regex("put='([^']+)'").find(xml)?.groupValues?.get(1)?.replace("&amp;", "&") ?: throw Exception("No PUT URL")
            val getUrl = Regex("get='([^']+)'").find(xml)?.groupValues?.get(1)?.replace("&amp;", "&") ?: throw Exception("No GET URL")
            
            // 2. Subir a S3 con HTTP PUT (sin OkHttp, usando HttpURLConnection)
            val url = URL(putUrl)
            val http = url.openConnection() as HttpURLConnection
            http.requestMethod = "PUT"
            http.setRequestProperty("Content-Type", "application/octet-stream")
            http.setRequestProperty("Content-Length", data.size.toString())
            http.doOutput = true
            http.connectTimeout = 30000
            http.readTimeout = 30000
            
            http.outputStream.use { it.write(data) }
            
            val code = http.responseCode
            if (code in 200..299) {
                Result.success(getUrl)
            } else {
                Result.failure(Exception("Error HTTP $code"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun uploadImage(uri: Uri, context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            val data = context.contentResolver.openInputStream(uri)!!.readBytes()
            
            val conn = xmppClient.connection ?: throw Exception("No hay conexion XMPP")
            val iqId = xmppClient.randomHexId(8)
            val requestXml = "<iq type=\"get\" id=\"$iqId\"><query xmlns=\"todus:purl\" type=\"4\" persistent=\"true\" size=\"${data.size}\" room=\"\"/></iq>"
            
            val parser = org.jivesoftware.smack.xml.XmlPullParserFactory.newInstance().newPullParser(requestXml.reader())
            val iq = org.jivesoftware.smack.util.PacketParserUtils.parseIQ(parser)
            val response = conn.sendIqRequestAndWaitForResponse(iq)
            val xml = response?.toXML()?.toString() ?: throw Exception("Sin respuesta del servidor")
            
            val putUrl = Regex("put='([^']+)'").find(xml)?.groupValues?.get(1)?.replace("&amp;", "&") ?: throw Exception("No PUT URL")
            val getUrl = Regex("get='([^']+)'").find(xml)?.groupValues?.get(1)?.replace("&amp;", "&") ?: throw Exception("No GET URL")
            
            val url = URL(putUrl)
            val http = url.openConnection() as HttpURLConnection
            http.requestMethod = "PUT"
            http.setRequestProperty("Content-Type", "application/octet-stream")
            http.setRequestProperty("Content-Length", data.size.toString())
            http.doOutput = true
            http.connectTimeout = 30000
            http.readTimeout = 30000
            
            http.outputStream.use { it.write(data) }
            
            val code = http.responseCode
            if (code in 200..299) Result.success(getUrl)
            else Result.failure(Exception("Error HTTP $code"))
        } catch (e: Exception) { Result.failure(e) }
    }
}
