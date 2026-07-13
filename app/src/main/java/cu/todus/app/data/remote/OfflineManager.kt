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
            var count = 0
            messages.forEach { msg ->
                messageDao.insert(MessageEntity(
                    id = msg.id, chatJid = msg.from, senderPhone = msg.from,
                    body = msg.body, type = msg.type, state = "received",
                    timestamp = msg.timestamp
                ))
                chatDao.updateLastMessage(msg.from, msg.body, msg.timestamp)
                chatDao.incrementUnread(msg.from)
                count++
            }
            Log.d(TAG, "Downloaded $count offline messages (${messages.size} parsed, ${count} saved)")
            Result.success(count)
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
            Result.failure(e)
        }
    }
    
    private fun parseMessages(xml: String): List<XmppClient.ToDusMessage> {
        val messages = mutableListOf<XmppClient.ToDusMessage>()
        
        // Patrón corregido: buscar <m> que contengan <b> (mensajes reales, no confirmaciones)
        val msgRegex = Regex(
            """<m[^>]*f=['"]([^'"]+)['"][^>]*i=['"]([^'"]+)['"][^>]*t=['"]([^'"]+)['"][^>]*>.*?<todus_offline\s+ts=['"](\d+)['"].*?<b>(.*?)</b>""",
            RegexOption.DOT_MATCHES_ALL
        )
        
        msgRegex.findAll(xml).forEach { match ->
            val fromFull = match.groupValues[1]
            val msgId = match.groupValues[2]
            val type = match.groupValues[3]  // "c" = chat, "gc" = grupo
            val ts = match.groupValues[4].toLongOrNull() ?: System.currentTimeMillis()
            val body = match.groupValues[5].trim()
            
            // Extraer solo el número de teléfono del remitente
            val from = extractPhone(fromFull)
            
            if (body.isNotEmpty() && body != ".") {
                messages.add(XmppClient.ToDusMessage(
                    id = msgId,
                    from = from,
                    body = body,
                    timestamp = ts,
                    type = if (type == "gc") "group" else "text"
                ))
            }
        }
        
        // También buscar mensajes sin <todus_offline>
        val simpleRegex = Regex(
            """<m[^>]*f=['"]([^'"]+)['"][^>]*i=['"]([^'"]+)['"][^>]*>.*?<b>(.*?)</b>""",
            RegexOption.DOT_MATCHES_ALL
        )
        
        simpleRegex.findAll(xml).forEach { match ->
            val fromFull = match.groupValues[1]
            val msgId = match.groupValues[2]
            val body = match.groupValues[3].trim()
            val from = extractPhone(fromFull)
            
            // Evitar duplicados
            if (body.isNotEmpty() && body != "." && messages.none { it.id == msgId }) {
                messages.add(XmppClient.ToDusMessage(
                    id = msgId, from = from, body = body,
                    timestamp = System.currentTimeMillis()
                ))
            }
        }
        
        return messages
    }
    
    private fun extractPhone(fromFull: String): String {
        // Formatos posibles:
        // "5351430352@im.todus.cu" → "5351430352"
        // "uuid@muclight.im.todus.cu/5354090599@im.todus.cu" → "5354090599"
        return when {
            fromFull.contains("/") -> {
                val parts = fromFull.split("/")
                parts.getOrNull(1)?.split("@")?.getOrNull(0) ?: fromFull.split("@")[0]
            }
            else -> fromFull.split("@")[0]
        }
    }
}
