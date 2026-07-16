package cu.todus.app.data.remote

import java.util.Base64

object ToDusProtocol {
    const val STREAM_NS = "jc"
    const val STREAM_XMLNS = "x1"
    const val SASL_NS = "urn:ietf:params:xml:ns:xmpp-sasl"
    const val BIND_NS = "urn:ietf:params:xml:ns:xmpp-bind"
    const val SESSION_NS = "urn:ietf:params:xml:ns:xmpp-session"
    const val MSG_NS = "jc"
    const val BODY_K_NS = "x8"
    const val DOMAIN = "im.todus.cu"

    fun buildStreamOpen(): String = "<?xml version=\"1.0\"?><stream:stream to=\"$DOMAIN\" xmlns=\"$STREAM_NS\" xmlns:stream=\"$STREAM_XMLNS\" version=\"1.0\">"
    
    fun buildAuthPacket(phone: String, jwt: String): String {
        val authBytes = "\u0000$phone\u0000$jwt".toByteArray()
        return "<auth xmlns=\"$SASL_NS\" mechanism=\"PLAIN\">${Base64.getEncoder().encodeToString(authBytes)}</auth>"
    }
    
    fun isAuthSuccess(response: String): Boolean = response.contains("<ok") || response.contains("<success")
    fun isAuthFailure(response: String): Boolean = response.contains("<failure")
    
    fun buildBindIq(resource: String): String {
        val iqId = randomHex(8)
        return "<iq type=\"set\" id=\"$iqId\"><bind xmlns=\"$BIND_NS\"><resource>$resource</resource></bind></iq>"
    }
    
    fun buildSessionIq(): String {
        val iqId = randomHex(8)
        return "<iq type=\"set\" id=\"$iqId\"><session xmlns=\"$SESSION_NS\"/></iq>"
    }
    
    fun buildPresence(): String = "<presence/>"
    
    fun buildOutgoingMessage(to: String, body: String, msgId: String = randomHex(16)): String =
        "<m to=\"$to@$DOMAIN\" t=\"c\" i=\"$msgId\" xmlns=\"$MSG_NS\"><k xmlns=\"$BODY_K_NS\"/><b>${escapeXml(body)}</b></m>"
    
    fun buildReceivedReceipt(to: String, originalMsgId: String): String {
        val ackId = randomHex(16)
        return "<m to=\"$to@$DOMAIN\" t=\"c\" i=\"$ackId\" xmlns=\"$MSG_NS\"><rd xmlns=\"$BODY_K_NS\" i=\"$originalMsgId\"/></m>"
    }
    
    fun buildDeliveredReceipt(to: String, originalMsgId: String): String {
        val ackId = randomHex(16)
        return "<m to=\"$to@$DOMAIN\" t=\"c\" i=\"$ackId\" xmlns=\"$MSG_NS\"><dd xmlns=\"$BODY_K_NS\" i=\"$originalMsgId\"/></m>"
    }
    
    fun buildOfflineIq(): String = "<iq type=\"get\" id=\"off_${randomHex(8)}\"><query xmlns=\"t:offline\"/></iq>"
    
    fun buildOfflineConfirmIq(messageIds: List<String>): String {
        val ids = messageIds.joinToString("") { "<id>$it</id>" }
        return "<iq type=\"set\" id=\"offdel_${randomHex(8)}\"><query xmlns=\"t:offline:del\">$ids</query></iq>"
    }
    
    fun buildGetUserInfoIq(phone: String): String = "<iq type=\"get\" id=\"prof_${randomHex(8)}\"><query xmlns=\"todus:users:getinfo\" users=\"$phone\"/></iq>"
    fun buildRosterListIq(): String = "<iq type=\"get\" id=\"rost_${randomHex(8)}\"><query xmlns=\"todus:roster:list:2\"/></iq>"
    fun buildS3UploadIq(fileType: Int, size: Long): String = "<iq type=\"get\" id=\"s3_${randomHex(8)}\"><query xmlns=\"todus:purl\" type=\"$fileType\" persistent=\"true\" size=\"$size\" room=\"\"/></iq>"
    fun buildPrivacyQueryIq(): String = "<iq type=\"get\" id=\"priv_${randomHex(8)}\"><query xmlns=\"todus:privacy\"/></iq>"
    fun buildBlockUserIq(phone: String): String = "<iq type=\"set\" id=\"blk_${randomHex(8)}\" to=\"$DOMAIN\"><query xmlns=\"todus:block:set\" jid=\"$phone@$DOMAIN\"/></iq>"
    
    fun parseIncomingMessage(xml: String): ToDusMessage? {
        return try {
            val id = extractAttribute(xml, "i") ?: return null
            val from = extractAttribute(xml, "f") ?: ""
            val to = extractAttribute(xml, "o") ?: ""
            val type = extractAttribute(xml, "t") ?: "c"
            val body = extractBody(xml) ?: ""
            val isReceipt = xml.contains("<rd ") || xml.contains("<dd ")
            val receiptMsgId = if (isReceipt) extractReceiptMsgId(xml) else null
            ToDusMessage(id = id, from = from, to = to, body = body, type = type, rawXml = xml,
                isReceipt = isReceipt, receiptMsgId = receiptMsgId,
                isComposing = xml.contains("<csp "),
                isPresence = xml.contains("<p "),
                isDeliveryAck = xml.contains("<tdack "))
        } catch (e: Exception) { null }
    }
    
    fun parseOfflineMessages(xml: String): List<ToDusMessage> {
        val messages = mutableListOf<ToDusMessage>()
        Regex("""<m\b.*?</m>""", RegexOption.DOT_MATCHES_ALL).findAll(xml).forEach { match ->
            parseIncomingMessage(match.value)?.let { msg ->
                val ts = Regex("""<todus_offline ts='(\d+)'/>""").find(match.value)?.groupValues?.get(1)?.toLongOrNull()
                messages.add(if (ts != null) msg.copy(timestamp = ts) else msg)
            }
        }
        return messages
    }
    
    fun extractBody(xml: String): String? = Regex("""<b[^>]*>(.*?)</b>""", RegexOption.DOT_MATCHES_ALL).find(xml)?.groupValues?.get(1)?.let { unescapeXml(it) }
    fun extractAttribute(xml: String, attrName: String): String? = Regex("""$attrName\s*=\s*['"]([^'"]*)['"]""").find(xml)?.groupValues?.get(1)
    fun extractBindJid(xml: String): String? = Regex("""<jid>(.*?)</jid>""").find(xml)?.groupValues?.get(1)
    fun extractReceiptMsgId(xml: String): String? = Regex("""<rd[^>]*i=['"]([^'"]*)['"]""").find(xml)?.groupValues?.get(1) ?: Regex("""<dd[^>]*i=['"]([^'"]*)['"]""").find(xml)?.groupValues?.get(1)
    fun extractDeliveryAckMsgId(xml: String): String? = Regex("""<tdack[^>]*mi=['"]([^'"]*)['"]""").find(xml)?.groupValues?.get(1)
    fun isMessage(xml: String): Boolean = xml.contains("<m ")
    fun isReceipt(xml: String): Boolean = xml.contains("<rd ") || xml.contains("<dd ")
    
    fun randomHex(length: Int): String {
        val chars = "abcdef0123456789"
        return (1..length).map { chars.random() }.joinToString("")
    }
    
    private fun escapeXml(text: String): String = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;")
    private fun unescapeXml(text: String): String = text.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&apos;", "'")
}
