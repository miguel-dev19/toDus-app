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

    suspend fun handshake(phone: String, jwt: String): Result<ToDusSession> = withContext(Dispatchers.IO) {
        try {
            socket = createSSLSocket()
            socket?.connect(InetSocketAddress(HOST, PORT), CONNECT_TIMEOUT)
            socket?.soTimeout = READ_TIMEOUT
            reader = BufferedReader(InputStreamReader(socket!!.inputStream))
            writer = BufferedWriter(OutputStreamWriter(socket!!.outputStream))

            send(ToDusProtocol.buildStreamOpen())
            readUntil { it.contains("stream:features") }

            send(ToDusProtocol.buildAuthPacket(phone, jwt))
            val authResp = readUntil { ToDusProtocol.isAuthSuccess(it) || ToDusProtocol.isAuthFailure(it) }
            if (!ToDusProtocol.isAuthSuccess(authResp)) { close(); return@withContext Result.failure(Exception("Auth failed")) }

            send(ToDusProtocol.buildStreamOpen())
            readUntil { it.contains("stream:features") }

            val resource = "ToDus_${phone.takeLast(4)}"
            send(ToDusProtocol.buildBindIq(resource))
            val bindResp = readUntil { it.contains("jid") || it.contains("error") }
            val jid = ToDusProtocol.extractBindJid(bindResp) ?: "$phone@${ToDusProtocol.DOMAIN}/$resource"

            send(ToDusProtocol.buildSessionIq())
            readUntil { it.contains("result") || it.contains("error") }

            send(ToDusProtocol.buildPresence())

            Result.success(ToDusSession(socket!!, jid, reader!!, writer!!))
        } catch (e: Exception) { close(); Result.failure(e) }
    }

    fun sendRaw(xml: String) { try { send(xml) } catch (_: Exception) {} }
    
    fun readRaw(): String? {
        return try {
            socket?.soTimeout = 1000
            readResponse()
        } catch (e: SocketTimeoutException) { null } catch (e: Exception) { null }
    }

    private fun createSSLSocket(): SSLSocket {
        val tm = object : X509TrustManager {
            override fun checkClientTrusted(c: Array<out X509Certificate>?, a: String?) {}
            override fun checkServerTrusted(c: Array<out X509Certificate>?, a: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
        val ctx = SSLContext.getInstance("TLS")
        ctx.init(null, arrayOf(tm), SecureRandom())
        return ctx.socketFactory.createSocket() as SSLSocket
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

    private fun readResponse(): String {
        val sb = StringBuilder()
        try { var char: Int; while (reader?.read().also { char = it ?: -1 } != -1) { sb.append(char.toChar()) } } catch (_: SocketTimeoutException) {}
        return sb.toString()
    }

    fun close() { try { writer?.close() } catch (_: Exception) {}; try { reader?.close() } catch (_: Exception) {}; try { socket?.close() } catch (_: Exception) {} }
}

data class ToDusSession(val socket: SSLSocket, val jid: String, val reader: BufferedReader, val writer: BufferedWriter)
