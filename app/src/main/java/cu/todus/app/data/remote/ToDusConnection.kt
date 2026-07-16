package cu.todus.app.data.remote

import kotlinx.coroutines.*
import java.io.*
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import javax.net.ssl.*
import java.security.cert.X509Certificate
import java.security.SecureRandom

class ToDusConnection {
    companion object {
        const val HOST = "ws.todus.cu"
        const val PORT = 1756
        const val CONNECT_TIMEOUT = 10000
        const val READ_TIMEOUT = 30000
    }

    private var socket: SSLSocket? = null
    private var reader: BufferedReader? = null
    private var writer: BufferedWriter? = null
    private var closed = false

    suspend fun handshake(phone: String, jwt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf(object : X509TrustManager {
                override fun checkClientTrusted(c: Array<out X509Certificate>?, a: String?) {}
                override fun checkServerTrusted(c: Array<out X509Certificate>?, a: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }), SecureRandom())

            socket = sslContext.socketFactory.createSocket() as SSLSocket
            socket?.connect(InetSocketAddress(HOST, PORT), CONNECT_TIMEOUT)
            socket?.soTimeout = READ_TIMEOUT
            reader = BufferedReader(InputStreamReader(socket!!.inputStream))
            writer = BufferedWriter(OutputStreamWriter(socket!!.outputStream))

            // Stream
            send(ToDusProtocol.buildStreamOpen())
            readUntil { it.contains("stream:features") }

            // SASL
            send(ToDusProtocol.buildAuthPacket(phone, jwt))
            val authResp = readUntil { ToDusProtocol.isAuthSuccess(it) || it.contains("failure") }
            if (!ToDusProtocol.isAuthSuccess(authResp)) throw Exception("Auth failed")

            // Reiniciar stream
            send(ToDusProtocol.buildStreamOpen())
            readUntil { it.contains("stream:features") }

            // Bind
            val resource = "ToDus_${phone.takeLast(4)}"
            send(ToDusProtocol.buildBindIq(resource))
            val bindResp = readUntil { it.contains("jid") }
            val jid = ToDusProtocol.extractBindJid(bindResp) ?: "$phone@im.todus.cu/$resource"

            // Session
            send(ToDusProtocol.buildSessionIq())
            readUntil { it.contains("result") }

            // Presence
            send(ToDusProtocol.buildPresence())

            Result.success(jid)
        } catch (e: Exception) {
            close()
            Result.failure(e)
        }
    }

    fun sendRaw(xml: String) {
        try { send(xml) } catch (_: Exception) {}
    }

    fun readRaw(): String? {
        return try {
            socket?.soTimeout = 500
            val sb = StringBuilder()
            try {
                var char: Int
                while (reader?.read().also { char = it ?: -1 } != -1) sb.append(char.toChar())
            } catch (_: SocketTimeoutException) {}
            sb.toString().ifEmpty { null }
        } catch (e: Exception) { null }
    }

    private fun send(data: String) { writer?.write(data); writer?.flush() }

    private fun readUntil(predicate: (String) -> Boolean): String {
        val sb = StringBuilder()
        var char: Int
        while (reader?.read().also { char = it ?: -1 } != -1) {
            sb.append(char.toChar())
            val text = sb.toString()
            if (predicate(text) && (text.endsWith(">") || text.endsWith("/>"))) break
        }
        return sb.toString()
    }

    fun close() {
        if (closed) return
        closed = true
        try { writer?.close() } catch (_: Exception) {}
        try { reader?.close() } catch (_: Exception) {}
        try { socket?.close() } catch (_: Exception) {}
    }
}
