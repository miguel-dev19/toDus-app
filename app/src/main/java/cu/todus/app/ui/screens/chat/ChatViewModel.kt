package cu.todus.app.ui.screens.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cu.todus.app.data.local.dao.ChatDao
import cu.todus.app.data.local.dao.MessageDao
import cu.todus.app.data.local.entity.MessageEntity
import cu.todus.app.data.remote.ToDusMessage
import cu.todus.app.data.remote.XmppClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatJid: String,
    private val xmppClient: XmppClient,
    private val messageDao: MessageDao,
    private val chatDao: ChatDao
) : ViewModel() {
    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages
    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText
    private val _isLoadingOffline = MutableStateFlow(false)
    val isLoadingOffline: StateFlow<Boolean> = _isLoadingOffline
    var lastSeen by mutableStateOf("")
        private set
    private var lastComposingSent = 0L

    init { loadMessages(); observeIncoming(); requestOffline(); clearUnread() }

    private fun loadMessages() {
        viewModelScope.launch { messageDao.getMessages(chatJid).collect { _messages.value = it } }
    }

    private fun clearUnread() {
        viewModelScope.launch { chatDao.clearUnread(chatJid) }
    }

    private fun observeIncoming() {
        viewModelScope.launch {
            xmppClient.incomingMessages.collect { msg -> processMessage(msg) }
        }
    }

    private suspend fun processMessage(msg: ToDusMessage) {
        val sender = msg.from.split("@")[0]
        if (sender != chatJid && msg.from != chatJid) return
        
        when {
            msg.rawXml.contains("<csp ") -> lastSeen = "escribiendo..."
            msg.rawXml.contains("<csc ") -> lastSeen = "en linea"
            msg.isReceipt && msg.receiptMsgId != null -> {
                messageDao.updateState(msg.receiptMsgId, if (msg.rawXml.contains("<dd ")) "read" else "delivered")
            }
            msg.isPresence -> lastSeen = "en linea"
            msg.isDeliveryAck -> {
                val ackId = cu.todus.app.data.remote.ToDusProtocol.extractDeliveryAckMsgId(msg.rawXml)
                if (ackId != null) messageDao.updateState(ackId, "delivered")
            }
            msg.body.isNotEmpty() -> {
                messageDao.insert(MessageEntity(id = msg.id, chatJid = chatJid, senderPhone = sender,
                    body = msg.body, type = "text", state = "received", timestamp = msg.timestamp))
                chatDao.updateLastMessage(chatJid, msg.body, msg.timestamp)
                chatDao.incrementUnread(chatJid)
                if (sender.isNotEmpty()) xmppClient.sendReceivedReceipt(sender, msg.id)
            }
        }
    }

    private fun requestOffline() {
        viewModelScope.launch { _isLoadingOffline.value = true; xmppClient.requestOfflineMessages(); delay(2000); _isLoadingOffline.value = false }
    }

    fun onMessageTextChanged(text: String) {
        _messageText.value = text
        // Enviar "escribiendo..." cada 5 segundos
        val now = System.currentTimeMillis()
        if (text.isNotEmpty() && now - lastComposingSent > 5000) {
            lastComposingSent = now
            viewModelScope.launch { xmppClient.sendComposing(chatJid) }
        }
    }

    fun sendMessage() {
        val text = _messageText.value.trim()
        if (text.isEmpty()) return
        viewModelScope.launch {
            _messageText.value = ""
            xmppClient.sendComposingStopped(chatJid)
            val msgId = xmppClient.sendMessage(chatJid, text)
            messageDao.insert(MessageEntity(id = msgId, chatJid = chatJid, senderPhone = "me", body = text, type = "text", state = "sent", timestamp = System.currentTimeMillis()))
            chatDao.updateLastMessage(chatJid, text, System.currentTimeMillis())
        }
    }
}
