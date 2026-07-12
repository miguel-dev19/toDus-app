package cu.todus.app.data.remote

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.jivesoftware.smack.chat2.ChatManager
import org.jivesoftware.smack.packet.IQ
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jxmpp.jid.impl.JidCreate
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.util.UUID

class XmppClient {
    private var connection: XMPPTCPConnection? = null
    private var chatManager: ChatManager? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _incomingMessages = MutableSharedFlow<ToDusMessage>()
    val incomingMessages: SharedFlow<ToDusMessage> = _incomingMessages

    private val okHttpClient = OkHttpClient()

    data class ToDusMessage(
        val id: String, val from: String, val body: String,
        val timestamp: Long, val xml: String = ""
    )

    suspend fun authenticate(phone: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val uuid = UUID.randomUUID().toString().replace("-", "")
            val phoneBytes = phone.toByteArray()
            val secretBytes = uuid.toByteArray().sliceArray(0..31)
            val body = byteArrayOf(0x0a, phoneBytes.size.toByte()) + phoneBytes +
                    byteArrayOf(0x12, 32) + secretBytes

            val request = Request.Builder()
                .url("https://auth.todus.cu/v2/auth/token")
                .header("Content-Type", "application/x-protobuf")
                .header("User-Agent", "ToDus 2.1.2 Auth")
                .post(body.toRequestBody("application/x-protobuf".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val jwt = response.body?.string() ?: ""
            Result.success(jwt)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun connect(phone: String, jwt: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            _connectionState.value = ConnectionState.CONNECTING
            connection = ToDusXMPPFactoryConfiguration.create(phone)
            connection?.connect()
            _connectionState.value = ConnectionState.BEFORE_CONNECTED
            connection?.login(phone, jwt)
            _connectionState.value = ConnectionState.AUTHENTICATED
            chatManager = ChatManager.getInstance(connection)
            setupMessageListener()
            _connectionState.value = ConnectionState.CONNECTED
            Result.success(Unit)
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.DISCONNECTED
            Result.failure(e)
        }
    }

    private fun setupMessageListener() {
        chatManager?.addIncomingListener { _, message, _ ->
            val msg = ToDusMessage(
                id = message.stanzaId ?: "",
                from = message.from.asBareJid().toString().split("@")[0],
                body = message.body ?: "",
                timestamp = System.currentTimeMillis()
            )
            _incomingMessages.tryEmit(msg)
        }
    }

    suspend fun sendMessage(to: String, text: String): String {
        val msgId = randomHexId(16)
        val jid = JidCreate.bareFrom("$to@im.todus.cu")
        val msg = Message(jid, Message.Type.chat).apply { stanzaId = msgId; body = text }
        chatManager?.chatWith(jid)?.send(msg)
        return msgId
    }

    suspend fun sendIqAndWait(xml: String): String = withContext(Dispatchers.IO) {
        try {
            val stanza = org.jivesoftware.smack.packet.StanzaBuilder.buildStanza(xml)
            val response = connection?.sendIqRequestAndWaitForResponse(stanza as IQ)
            response?.toXML()?.toString() ?: ""
        } catch (e: Exception) {
            Log.e("XmppClient", "IQ error: ${e.message}")
            ""
        }
    }

    fun disconnect() {
        connection?.disconnect()
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    private fun randomHexId(len: Int = 16): String {
        val chars = "abcdef0123456789"
        return (1..len).map { chars.random() }.joinToString("")
    }
}
