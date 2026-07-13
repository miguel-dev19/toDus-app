package cu.todus.app.data.remote

import android.util.Log
import cu.todus.app.data.local.dao.ChatDao
import cu.todus.app.data.local.dao.MessageDao
import cu.todus.app.data.local.entity.MessageEntity
import cu.todus.app.data.remote.iq.offline.GetOfflineIQ
import kotlinx.coroutines.*
import org.jivesoftware.smack.tcp.XMPPTCPConnection

class OfflineManager(
    private val connection: XMPPTCPConnection,
    private val messageDao: MessageDao,
    private val chatDao: ChatDao
) {
    companion object {
        private const val TAG = "OfflineManager"
    }
    
    suspend fun downloadOfflineMessages(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val iq = cu.todus.app.data.remote.iq.offline.GetOfflineIQ()
            iq.stanzaId = cu.todus.app.data.remote.randomHexId(8)
            
            val response = connection.sendIqRequestAndWaitForResponse(iq)
            val xml = response?.toXML()?.toString() ?: return@withContext Result.success(0)
            
            val messages = parseMessages(xml)
            var count = 0
            
            messages.forEach { msg ->
                messageDao.insert(
                    MessageEntity(
                        id = msg.id, chatJid = msg.from, senderPhone = msg.from,
                        body = msg.body, type = "text", state = "received",
                        timestamp = msg.timestamp
                    )
                )
                chatDao.updateLastMessage(msg.from, msg.body, msg.timestamp)
                chatDao.incrementUnread(msg.from)
                count++
            }
            
            Log.d(TAG, "Downloaded $count offline messages")
            Result.success(count)
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
            Result.failure(e)
        }
    }
    
    private fun parseMessages(xml: String): List<XmppClient.ToDusMessage> {
        val messages = mutableListOf<XmppClient.ToDusMessage>()
        val regex = Regex("""<message[^>]*from='([^']+)'[^>]*>.*?<body>(.*?)</body>.*?</message>""", RegexOption.DOT_MATCHES_ALL)
        regex.findAll(xml).forEach { match ->
            messages.add(
                XmppClient.ToDusMessage(
                    id = cu.todus.app.data.remote.randomHexId(16),
                    from = match.groupValues[1].split("@")[0],
                    body = match.groupValues[2],
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        return messages
    }
}
