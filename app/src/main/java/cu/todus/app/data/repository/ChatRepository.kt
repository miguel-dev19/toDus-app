package cu.todus.app.data.repository

import cu.todus.app.data.local.dao.ChatDao
import cu.todus.app.data.local.dao.MessageDao
import cu.todus.app.data.local.entity.ChatEntity
import cu.todus.app.data.local.entity.MessageEntity
import cu.todus.app.data.remote.XmppClient
import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val xmppClient: XmppClient,
    private val messageDao: MessageDao,
    private val chatDao: ChatDao
) {
    fun getMessages(chatJid: String): Flow<List<MessageEntity>> = messageDao.getMessages(chatJid)
    fun getAllChats(): Flow<List<ChatEntity>> = chatDao.getAllChats()

    suspend fun sendMessage(to: String, text: String): String {
        val msgId = xmppClient.sendMessage(to, text)
        messageDao.insert(MessageEntity(id = msgId, chatJid = to, senderPhone = "me", body = text, timestamp = System.currentTimeMillis()))
        chatDao.updateLastMessage(to, text, System.currentTimeMillis())
        return msgId
    }
}
