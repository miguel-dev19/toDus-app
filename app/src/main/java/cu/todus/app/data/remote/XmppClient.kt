package cu.todus.app.data.remote

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID

enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED, RECONNECTING, FAILED }

class XmppClient {
    private val toDusConnection = ToDusConnection()
    private val okHttpClient = OkHttpClient()
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState
    private val _incomingMessages = MutableSharedFlow<ToDusMessage>(replay = 0, extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<ToDusMessage> = _incomingMessages
    private var phone: String = ""
    private var jwt: String = ""
    private var running = false
    private var readerJob: Job? = null
    private var reconnectJob: Job? = null

    fun randomHexId(len: Int = 16): String = (1..len).map { "abcdef0123456789".random() }.joinToString("")

    suspend fun authenticate(phone: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val uuid = UUID.randomUUID().toString().replace("-", "")
            val phoneBytes = phone.toByteArray()
            val secretBytes = uuid.toByteArray().sliceArray(0..31)
            val body = byteArrayOf(0x0a, phoneBytes.size.toByte()) + phoneBytes + byteArrayOf(0x12, 32) + secretBytes
            val request = Request.Builder().url("https://auth.todus.cu/v2/auth/token")
                .header("Content-Type", "application/x-protobuf").header("User-Agent", "ToDus 2.1.2 Auth")
                .post(body.toRequestBody("application/x-protobuf".toMediaType())).build()
            val response = okHttpClient.newCall(request).execute()
            Result.success(response.body?.string() ?: "")
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun connect(phone: String, jwt: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            this@XmppClient.phone = phone; this@XmppClient.jwt = jwt
            _connectionState.value = ConnectionState.CONNECTING
            val result = toDusConnection.handshake(phone, jwt)
            result.onSuccess {
                _connectionState.value = ConnectionState.CONNECTED
                running = true
                startMessageReader()
                startReconnectWatcher()
            }.onFailure { _connectionState.value = ConnectionState.FAILED }
            result.map { Unit }
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.FAILED
            Result.failure(e)
        }
    }

    fun sendMessage(to: String, text: String): String {
        val msgId = ToDusProtocol.randomHex(16)
        val xml = ToDusProtocol.buildOutgoingMessage(to, text, msgId)
        toDusConnection.sendRaw(xml)
        return msgId
    }

    fun sendIq(xml: String) { toDusConnection.sendRaw(xml) }

    private fun startMessageReader() {
        readerJob = CoroutineScope(Dispatchers.IO).launch {
            while (running) {
                try {
                    val data = toDusConnection.readRaw()
                    if (!data.isNullOrBlank()) {
                        val msg = ToDusProtocol.parseIncomingMessage(data)
                        if (msg != null) _incomingMessages.tryEmit(msg)
                        // Also parse offline messages
                        if (data.contains("<query xmlns=\"t:offline\"")) {
                            ToDusProtocol.parseOfflineMessages(data).forEach { _incomingMessages.tryEmit(it) }
                        }
                    }
                } catch (_: Exception) {}
                delay(100)
            }
        }
    }

    private fun startReconnectWatcher() {
        reconnectJob?.cancel()
        reconnectJob = CoroutineScope(Dispatchers.IO).launch {
            while (running) {
                delay(3000)
                try {
                    toDusConnection.sendRaw(" ")
                } catch (_: Exception) {
                    if (running && phone.isNotEmpty() && jwt.isNotEmpty()) {
                        _connectionState.value = ConnectionState.RECONNECTING
                        try {
                            toDusConnection.close()
                            connect(phone, jwt)
                        } catch (_: Exception) { _connectionState.value = ConnectionState.DISCONNECTED }
                    }
                }
            }
        }
    }

    fun disconnect() {
        running = false
        readerJob?.cancel()
        reconnectJob?.cancel()
        toDusConnection.close()
        _connectionState.value = ConnectionState.DISCONNECTED
    }
}
