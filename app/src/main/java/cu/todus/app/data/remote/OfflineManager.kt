package cu.todus.app.data.remote

import android.util.Log
import cu.todus.app.data.local.dao.ChatDao
import cu.todus.app.data.local.dao.MessageDao
import cu.todus.app.data.local.entity.MessageEntity
import kotlinx.coroutines.*
import org.jivesoftware.smack.packet.IQ
import org.jivesoftware.smack.tcp.XMPPTCPConnection

class OfflineManager(
    private val connection: XMPPTCPConnection,
    private val messageDao: MessageDao,
    private val chatDao: ChatDao
) {
    companion object { private const val TAG = "OfflineManager" }
    private fun randomId(len: Int = 16) = (1..len).map { "abcdef0123456789".random() }.joinToString("")
    
    @Suppress("UNCHECKED_CAST")
    suspend fun downloadOfflineMessages(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val iq = cu.todus.app.data.remote.iq.offline.GetOfflineIQ()
            iq.stanzaId = randomId(8)
            val response: IQ? = connection.sendIqRequestAndWaitForResponse(iq) as? IQ
            val xml = response?.toXML()?.toString() ?: return@withContext Result.success(0)
            val messages = parseMessages(xml)
            
            // FILTRAR: solo chats privados (t='c'), ignorar grupos (t='gc')
            val privateMessages = messages.filter { it.type != "group" }
            
            var count = 0
            privateMessages.forEach { msg ->
                messageDao.insert(MessageEntity(
                    id = msg.id, chatJid = msg.chatJid, senderPhone = msg.from,
                    body = msg.body, type = msg.type, state = "received",
                    timestamp = msg.timestamp
                ))
                chatDao.updateLastMessage(msg.chatJid, msg.body, msg.timestamp)
                chatDao.incrementUnread(msg.chatJid)
                count++
            }
            
            Log.d(TAG, "Downloaded $count private messages (${messages.size} total, ${messages.size - count} groups ignored)")
            Result.success(count)
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
            Result.failure(e)
        }
    }
    
    private fun parseMessages(xml: String): List<ParsedMessage> {
        val messages = mutableListOf<ParsedMessage>()
        
        val msgRegex = Regex(
            """<m[^>]*o=['"]([^'"]+)['"][^>]*f=['"]([^'"]+)['"][^>]*i=['"]([^'"]+)['"][^>]*t=['"]([^'"]+)['"][^>]*>.*?<todus_offline\s+ts=['"](\d+)['"].*?<b>(.*?)</b>""",
            RegexOption.DOT_MATCHES_ALL
        )
        
        msgRegex.findAll(xml).forEach { match ->
            val fromFull = match.groupValues[2]
            val msgId = match.groupValues[3]
            val type = match.groupValues[4]       // "c" = chat, "gc" = grupo
            val ts = match.groupValues[5].toLongOrNull() ?: System.currentTimeMillis()
            val body = match.groupValues[6].trim()
            val senderPhone = extractPhone(fromFull)
            val chatJid = senderPhone  // Para chats privados, el JID es el número
            
            // Solo guardar si no es grupo y tiene contenido
            if (type == "c" && body.isNotEmpty() && body != ".") {
                messages.add(ParsedMessage(
                    id = msgId, chatJid = chatJid, from = senderPhone,
                    body = body, timestamp = ts, type = "text"
                ))
            }
        }
        
        return messages
    }
    
    private fun extractPhone(fromFull: String): String {
        return when {
            fromFull.contains("/") -> {
                fromFull.split("/").getOrNull(1)?.split("@")?.getOrNull(0) 
                    ?: fromFull.split("@")[0]
            }
            else -> fromFull.split("@")[0]
        }
    }
}

data class ParsedMessage(
    val id: String,
    val chatJid: String,
    val from: String,
    val body: String,
    val timestamp: Long,
    val type: String = "text"
)
