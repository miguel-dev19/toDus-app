package cu.todus.app.data.remote

import android.content.Context
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.Socket
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.UUID
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

class S3Uploader(private val xmppClient: XmppClient) {
    
    companion object {
        private fun trustAllSSL(): SSLContext {
            val tm = object : X509TrustManager {
                override fun checkClientTrusted(c: Array<X509Certificate>?, a: String?) {}
                override fun checkServerTrusted(c: Array<X509Certificate>?, a: String?) {}
                override fun getAcceptedIssuers() = arrayOf<X509Certificate>()
            }
            return SSLContext.getInstance("TLS").apply { init(null, arrayOf(tm), SecureRandom()) }
        }
    }
    
    private suspend fun getS3UrlsViaSocket(phone: String, jwt: String, fileType: Int, fileSize: Int): Pair<String, String> = withContext(Dispatchers.IO) {
        var socket: Socket? = null
        var ssl: java.net.Socket? = null
        var writer: OutputStreamWriter? = null
        var reader: BufferedReader? = null
        
        try {
            val sslContext = trustAllSSL()
            val factory = sslContext.socketFactory
            
            socket = Socket("ws.todus.cu", 1756)
            socket.soTimeout = 30000
            ssl = factory.createSocket(socket, "ws.todus.cu", 1756, true)
            writer = OutputStreamWriter(ssl.getOutputStream())
            reader = BufferedReader(InputStreamReader(ssl.getInputStream()))
            
            fun readUntil(vararg markers: String): String {
                val sb = StringBuilder()
                val buf = CharArray(4096)
                var tries = 0
                while (tries < 50) {
                    if (reader!!.ready()) {
                        val len = reader.read(buf)
                        if (len > 0) sb.append(buf, 0, len)
                        val s = sb.toString()
                        for (m in markers) if (s.contains(m)) return s
                    } else { Thread.sleep(100); tries++ }
                }
                return sb.toString()
            }
            
            // Stream open
            writer.write("<?xml version=\"1.0\"?><stream:stream to=\"im.todus.cu\" xmlns=\"jc\" xmlns:stream=\"x1\" version=\"1.0\">")
            writer.flush(); readUntil("stream:features")
            
            // SASL
            val auth = Base64.encodeToString("\u0000$phone\u0000$jwt".toByteArray(), Base64.NO_WRAP)
            writer.write("<auth xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\" mechanism=\"PLAIN\">$auth</auth>")
            writer.flush(); readUntil("<ok")
            
            // Reiniciar stream
            writer.write("<?xml version=\"1.0\"?><stream:stream to=\"im.todus.cu\" xmlns=\"jc\" xmlns:stream=\"x1\" version=\"1.0\">")
            writer.flush(); readUntil("stream:features")
            
            // Bind
            val bindId = UUID.randomUUID().toString().replace("-", "").take(8)
            writer.write("<iq type=\"set\" id=\"$bindId\"><bind xmlns=\"urn:ietf:params:xml:ns:xmpp-bind\"><resource>s3up</resource></bind></iq>")
            writer.flush(); readUntil("</iq>")
            
            // Session
            val sessId = UUID.randomUUID().toString().replace("-", "").take(8)
            writer.write("<iq type=\"set\" id=\"$sessId\"><session xmlns=\"urn:ietf:params:xml:ns:xmpp-session\"/></iq>")
            writer.flush(); readUntil("</iq>")
            
            // Solicitar URLs S3
            val iqId = UUID.randomUUID().toString().replace("-", "").take(8)
            writer.write("<iq type=\"get\" id=\"$iqId\"><query xmlns=\"todus:purl\" type=\"$fileType\" persistent=\"true\" size=\"$fileSize\" room=\"\"/></iq>")
            writer.flush()
            val resp = readUntil("</iq>")
            
            val putUrl = Regex("put='([^']+)'").find(resp)?.groupValues?.get(1)?.replace("&amp;", "&") ?: throw Exception("No PUT URL")
            val getUrl = Regex("get='([^']+)'").find(resp)?.groupValues?.get(1)?.replace("&amp;", "&") ?: throw Exception("No GET URL")
            
            Pair(putUrl, getUrl)
        } finally {
            try { writer?.close() } catch (_: Exception) {}
            try { reader?.close() } catch (_: Exception) {}
            try { ssl?.close() } catch (_: Exception) {}
            try { socket?.close() } catch (_: Exception) {}
        }
    }
    
    suspend fun uploadProfileImage(uri: Uri, context: Context, phone: String, jwt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val data = context.contentResolver.openInputStream(uri)!!.readBytes()
            val (putUrl, getUrl) = getS3UrlsViaSocket(phone, jwt, 5, data.size)
            
            val url = URL(putUrl)
            val http = url.openConnection() as HttpURLConnection
            http.requestMethod = "PUT"
            http.setRequestProperty("Content-Type", "application/octet-stream")
            http.setRequestProperty("Content-Length", data.size.toString())
            http.doOutput = true
            http.connectTimeout = 30000
            http.readTimeout = 30000
            http.outputStream.use { it.write(data) }
            
            if (http.responseCode in 200..299) Result.success(getUrl)
            else Result.failure(Exception("HTTP ${http.responseCode}"))
        } catch (e: Exception) { Result.failure(e) }
    }
    
    suspend fun uploadImage(uri: Uri, context: Context, phone: String, jwt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val data = context.contentResolver.openInputStream(uri)!!.readBytes()
            val (putUrl, getUrl) = getS3UrlsViaSocket(phone, jwt, 4, data.size)
            
            val url = URL(putUrl)
            val http = url.openConnection() as HttpURLConnection
            http.requestMethod = "PUT"
            http.setRequestProperty("Content-Type", "application/octet-stream")
            http.setRequestProperty("Content-Length", data.size.toString())
            http.doOutput = true
            http.connectTimeout = 30000
            http.readTimeout = 30000
            http.outputStream.use { it.write(data) }
            
            if (http.responseCode in 200..299) Result.success(getUrl)
            else Result.failure(Exception("HTTP ${http.responseCode}"))
        } catch (e: Exception) { Result.failure(e) }
    }
}
