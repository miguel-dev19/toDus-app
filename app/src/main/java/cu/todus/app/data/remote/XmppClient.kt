package cu.todus.app.data.remote

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID

enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED, RECONNECTING, FAILED }

class XmppClient(private val context: Context? = null) {
    private val toDusConnection = ToDusConnection()
    private val okHttpClient = OkHttpClient()
    private val networkMonitor = context?.let { NetworkMonitor(it) }
    
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState
    
    private val _incomingMessages = MutableSharedFlow<ToDusMessage>(replay = 0, extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<ToDusMessage> = _incomingMessages
    
    private var phone: String = ""
    private var jwt: String = ""
    private var running = false
    private var readerJob: Job? = null
    private var reconnectJob: Job? = null
    private var networkObserverJob: Job? = null
    private var reconnectAttempts = 0
    
    private var lastIqResponse = ""
    private var lastIqTime = 0L

    fun getLastIqResponse(): String = if (System.currentTimeMillis() - lastIqTime < 10000) lastIqResponse else ""

    suspend fun authenticate(phone: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val uuid = UUID.randomUUID().toString().replace("-", "")
            val pb = phone.toByteArray(); val sb = uuid.toByteArray().sliceArray(0..31)
            val body = byteArrayOf(0x0a, pb.size.toByte()) + pb + byteArrayOf(0x12, 32) + sb
            val req = Request.Builder().url("https://auth.todus.cu/v2/auth/token")
                .header("Content-Type", "application/x-protobuf").header("User-Agent", "ToDus 2.1.2 Auth")
                .post(body.toRequestBody("application/x-protobuf".toMediaType())).build()
            val resp = okHttpClient.newCall(req).execute()
            val jwt = Regex("""eyJ[a-zA-Z0-9_-]+\.[a-zA-Z0-9_-]+\.[a-zA-Z0-9_-]+""").find(resp.body?.string() ?: "")?.value
            if (jwt != null) Result.success(jwt) else Result.failure(Exception("JWT not found"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun connect(phone: String, jwt: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            this@XmppClient.phone = phone; this@XmppClient.jwt = jwt
            _connectionState.value = ConnectionState.CONNECTING
            toDusConnection.handshake(phone, jwt).onSuccess {
                _connectionState.value = ConnectionState.CONNECTED
                Log.d("XmppClient", "✅ Conectado")
                running = true; reconnectAttempts = 0
                startMessageReader()
                startReconnectWatcher()
                startNetworkObserver()
            }.onFailure { 
                Log.e("XmppClient", "❌ Handshake falló: ${it.exceptionOrNull()?.message}")
                _connectionState.value = ConnectionState.FAILED 
            }
            Result.success(Unit)
        } catch (e: Exception) { 
            Log.e("XmppClient", "❌ Error: ${e.message}")
            _connectionState.value = ConnectionState.FAILED; Result.failure(e) 
        }
    }

    fun sendMessage(to: String, text: String): String {
        val msgId = ToDusProtocol.randomHex(16)
        toDusConnection.sendRaw(ToDusProtocol.buildOutgoingMessage(to, text, msgId))
        Log.d("XmppClient", "📤 Enviado: $msgId")
        return msgId
    }

    fun sendIq(xml: String) {
        toDusConnection.sendRaw(xml)
        Log.d("XmppClient", "📤 IQ: ${xml.take(80)}...")
    }
    
    fun sendReceivedReceipt(to: String, msgId: String) = toDusConnection.sendRaw(ToDusProtocol.buildReceivedReceipt(to, msgId))
    fun sendDeliveredReceipt(to: String, msgId: String) = toDusConnection.sendRaw(ToDusProtocol.buildDeliveredReceipt(to, msgId))
    fun sendComposing(to: String) = toDusConnection.sendRaw(ToDusProtocol.buildComposing(to))
    fun sendComposingStopped(to: String) = toDusConnection.sendRaw(ToDusProtocol.buildComposingStopped(to))
    fun requestOfflineMessages() = sendIq(ToDusProtocol.buildOfflineIq())
    fun requestUserInfo(phone: String) = sendIq(ToDusProtocol.buildGetUserInfoIq(phone))
    fun requestRoster() = sendIq(ToDusProtocol.buildRosterListIq())

    private fun startMessageReader() {
        readerJob?.cancel()
        readerJob = CoroutineScope(Dispatchers.IO).launch {
            Log.d("XmppClient", "👂 Lector de mensajes iniciado")
            while (running) {
                try {
                    val data = toDusConnection.readRaw()
                    if (!data.isNullOrBlank()) {
                        Log.d("XmppClient", "📩 Recibido: ${data.take(100)}...")
                        processIncomingData(data)
                    }
                } catch (e: Exception) {
                    Log.e("XmppClient", "❌ Error lectura: ${e.message}")
                }
                delay(100)
            }
        }
    }

    private fun processIncomingData(data: String) {
        data.split(Regex("(?<=>)(?=<)")).forEach { stanza ->
            if (stanza.isBlank()) return@forEach
            
            // Guardar respuestas IQ
            if (stanza.contains("<iq ") && (
                stanza.contains("todus:users:getinfo") || stanza.contains("todus:roster:list") ||
                stanza.contains("t:offline") || stanza.contains("todus:block:get") ||
                stanza.contains("todus:privacy") || stanza.contains("todus:muclight:my_mucs")
            )) {
                lastIqResponse = stanza; lastIqTime = System.currentTimeMillis()
                Log.d("XmppClient", "📩 IQ recibida: ${stanza.take(100)}...")
            }
            
            when {
                ToDusProtocol.isMessage(stanza) -> {
                    val msg = ToDusProtocol.parseIncomingMessage(stanza)
                    if (msg != null) {
                        Log.d("XmppClient", "📩 Mensaje: ${msg.from} -> ${msg.body.take(30)}")
                        _incomingMessages.tryEmit(msg)
                    }
                }
                stanza.contains("<query xmlns=\"t:offline\"") -> {
                    Log.d("XmppClient", "📩 Offline messages")
                    ToDusProtocol.parseOfflineMessages(stanza).forEach { 
                        Log.d("XmppClient", "📩 Offline: ${it.from} -> ${it.body.take(30)}")
                        _incomingMessages.tryEmit(it) 
                    }
                }
                stanza.contains("<p ") -> {
                    Log.d("XmppClient", "📩 Presencia")
                    _incomingMessages.tryEmit(ToDusMessage(id = "", from = "", to = "", body = "", rawXml = stanza, isPresence = true))
                }
            }
        }
    }

    private fun startReconnectWatcher() {
        reconnectJob?.cancel()
        reconnectJob = CoroutineScope(Dispatchers.IO).launch {
            while (running) {
                delay(3000)
                try { toDusConnection.sendRaw(" ") }
                catch (_: Exception) {
                    Log.e("XmppClient", "❌ Keepalive falló")
                    if (running && phone.isNotEmpty() && jwt.isNotEmpty()) {
                        attemptReconnect()
                    }
                }
            }
        }
    }

    private fun startNetworkObserver() {
        networkObserverJob?.cancel()
        networkObserverJob = CoroutineScope(Dispatchers.IO).launch {
            networkMonitor?.state?.collect { netState ->
                if (!netState.isAvailable && running) {
                    Log.d("XmppClient", "📶 Red perdida")
                    _connectionState.value = ConnectionState.DISCONNECTED
                } else if (netState.isAvailable && _connectionState.value == ConnectionState.DISCONNECTED && running) {
                    Log.d("XmppClient", "📶 Red recuperada")
                    delay(1000)
                    attemptReconnect()
                }
            }
        }
    }

    private suspend fun attemptReconnect() {
        reconnectAttempts++
        if (reconnectAttempts > 10) {
            _connectionState.value = ConnectionState.FAILED
            return
        }
        _connectionState.value = ConnectionState.RECONNECTING
        val delay = (2000L * (1L shl minOf(reconnectAttempts, 5)))
        delay(delay)
        try {
            toDusConnection.close()
            toDusConnection.handshake(phone, jwt).onSuccess {
                _connectionState.value = ConnectionState.CONNECTED
                reconnectAttempts = 0
                running = true
                startMessageReader()
            }.onFailure {
                _connectionState.value = ConnectionState.DISCONNECTED
            }
        } catch (_: Exception) {
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }

    fun disconnect() {
        running = false
        readerJob?.cancel()
        reconnectJob?.cancel()
        networkObserverJob?.cancel()
        networkMonitor?.stop()
        toDusConnection.close()
        reconnectAttempts = 0
        _connectionState.value = ConnectionState.DISCONNECTED
    }
}
