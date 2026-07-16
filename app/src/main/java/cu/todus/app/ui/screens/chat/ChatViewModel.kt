package cu.todus.app.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cu.todus.app.data.local.dao.ChatDao
import cu.todus.app.data.local.dao.MessageDao
import cu.todus.app.data.local.entity.MessageEntity
import cu.todus.app.data.remote.ToDusProtocol
import cu.todus.app.data.remote.XmppClient
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

    init { loadMessages(); observeIncoming(); requestOffline() }

    private fun loadMessages() {
        viewModelScope.launch { messageDao.getMessages(chatJid).collect { _messages.value = it } }
    }

    private fun observeIncoming() {
        viewModelScope.launch {
            xmppClient.incomingMessages.collect { msg ->
                // Ignorar recibos, presencias, etc.
                if (msg.isReceipt || msg.isPresence || msg.isDeliveryAck) {
                    // Procesar confirmaciones
                    if (msg.receiptMsgId != null) {
                        messageDao.markAsDelivered(msg.receiptMsgId)
                    }
                    return@collect
                }

                val sender = msg.from.split("@")[0]
                if (sender == chatJid || msg.from == chatJid) {
                    val entity = MessageEntity(
                        id = msg.id, chatJid = chatJid, senderPhone = sender,
                        body = msg.body, type = msg.type, state = "received",
                        timestamp = msg.timestamp
                    )
                    messageDao.insert(entity)
                    chatDao.updateLastMessage(chatJid, msg.body, msg.timestamp)
                    chatDao.incrementUnread(chatJid)
                }
            }
        }
    }

    private fun requestOffline() {
        viewModelScope.launch {
            _isLoadingOffline.value = true
            xmppClient.requestOfflineMessages()
            _isLoadingOffline.value = false
        }
    }

    fun onMessageTextChanged(text: String) { _messageText.value = text }

    fun sendMessage() {
        val text = _messageText.value.trim()
        if (text.isEmpty()) return
        viewModelScope.launch {
            _messageText.value = ""
            val msgId = xmppClient.sendMessage(chatJid, text)
            messageDao.insert(MessageEntity(id = msgId, chatJid = chatJid, senderPhone = "me", body = text, type = "text", state = "sent", timestamp = System.currentTimeMillis()))
            chatDao.updateLastMessage(chatJid, text, System.currentTimeMillis())
        }
    }
}
