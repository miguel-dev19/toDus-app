package cu.todus.app.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cu.todus.app.data.local.dao.ChatDao
import cu.todus.app.data.local.dao.MessageDao
import cu.todus.app.data.local.entity.MessageEntity
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
    
    init {
        loadMessages()
        observeIncoming()
    }
    
    private fun loadMessages() {
        viewModelScope.launch {
            messageDao.getMessages(chatJid).collect { msgs ->
                _messages.value = msgs
            }
        }
    }
    
    private fun observeIncoming() {
        viewModelScope.launch {
            xmppClient.incomingMessages.collect { msg ->
                if (msg.from == chatJid) {
                    messageDao.insert(
                        MessageEntity(
                            id = msg.id,
                            chatJid = chatJid,
                            senderPhone = msg.from,
                            body = msg.body,
                            type = "text",
                            state = "received",
                            timestamp = msg.timestamp
                        )
                    )
                    chatDao.updateLastMessage(chatJid, msg.body, msg.timestamp)
                    chatDao.clearUnread(chatJid)
                }
            }
        }
    }
    
    fun onMessageTextChanged(text: String) {
        _messageText.value = text
    }
    
    fun sendMessage() {
        val text = _messageText.value.trim()
        if (text.isEmpty()) return
        
        viewModelScope.launch {
            _messageText.value = ""
            val msgId = xmppClient.sendMessage(chatJid, text)
            messageDao.insert(
                MessageEntity(
                    id = msgId,
                    chatJid = chatJid,
                    senderPhone = "me",
                    body = text,
                    type = "text",
                    state = "sent",
                    timestamp = System.currentTimeMillis()
                )
            )
            chatDao.updateLastMessage(chatJid, text, System.currentTimeMillis())
        }
    }
}
