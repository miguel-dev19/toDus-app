package cu.todus.app.data.remote

import java.util.Base64

object ToDusProtocol {
    const val STREAM_NS = "jc"
    const val STREAM_XMLNS = "x1"
    const val SASL_NS = "x2"
    const val BIND_NS = "urn:ietf:params:xml:ns:xmpp-bind"
    const val SESSION_NS = "x5"
    const val BODY_K_NS = "x8"
    const val COMPOSING_NS = "uc1"
    const val ATTR_TO = "o"
    const val ATTR_FROM = "f"
    const val ATTR_ID = "i"
    const val ATTR_TYPE = "t"
    const val ATTR_MECHANISM = "e"
    const val ELEM_MESSAGE = "m"
    const val ELEM_BODY = "b"
    const val ELEM_AUTH = "ah"
    const val ELEM_SESSION = "s1"
    const val ELEM_IQ = "iq"
    const val ELEM_K = "k"
    const val ELEM_RD = "rd"
    const val ELEM_DD = "dd"
    const val ELEM_CSP = "csp"
    const val TYPE_SET = "set"
    const val TYPE_GET = "get"
    const val TYPE_CHAT = "c"
    const val MECHANISM_PLAIN = "PLAIN"

    fun buildStreamOpen(domain: String = "im.todus.cu"): String {
        return "<?xml version=\"1.0\"?><stream:stream $ATTR_TO=\"$domain\" xmlns=\"$STREAM_NS\" xmlns:stream=\"$STREAM_XMLNS\" v=\"1.0\">"
    }

    fun buildAuthPacket(phone: String, jwt: String): String {
        val authBytes = "\u0000$phone\u0000$jwt".toByteArray()
        val authBase64 = Base64.getEncoder().encodeToString(authBytes)
        return "<$ELEM_AUTH xmlns=\"$SASL_NS\" $ATTR_MECHANISM=\"$MECHANISM_PLAIN\">$authBase64</$ELEM_AUTH>"
    }

    fun isAuthSuccess(response: String): Boolean = response.contains("<ok") && response.contains(SASL_NS)

    fun buildBindIq(resource: String = "ToDus"): String {
        val iqId = "bind_${randomHex(8)}"
        return "<$ELEM_IQ $ATTR_TYPE=\"$TYPE_SET\" $ATTR_ID=\"$iqId\"><bind xmlns=\"$BIND_NS\"><resource>$resource</resource></bind></$ELEM_IQ>"
    }

    fun buildSessionIq(): String {
        val iqId = "session_${randomHex(8)}"
        return "<$ELEM_IQ $ATTR_TYPE=\"$TYPE_SET\" $ATTR_ID=\"$iqId\"><$ELEM_SESSION xmlns=\"$SESSION_NS\"/></$ELEM_IQ>"
    }

    fun buildPresence(): String = "<presence/>"

    fun buildOutgoingMessage(to: String, body: String, msgId: String = randomHex(16)): String {
        return "<$ELEM_MESSAGE $ATTR_TO=\"$to@im.todus.cu\" $ATTR_TYPE=\"$TYPE_CHAT\" $ATTR_ID=\"$msgId\" xmlns=\"$STREAM_NS\"><$ELEM_K xmlns=\"$BODY_K_NS\"/><$ELEM_BODY>${escapeXml(body)}</$ELEM_BODY></$ELEM_MESSAGE>"
    }

    fun buildOfflineIq(): String = "<$ELEM_IQ $ATTR_TYPE=\"$TYPE_GET\" $ATTR_ID=\"off_${randomHex(8)}\"><query xmlns=\"t:offline\"/></$ELEM_IQ>"

    fun buildGetUserInfoIq(phone: String): String = "<$ELEM_IQ $ATTR_TYPE=\"$TYPE_GET\" $ATTR_ID=\"prof_${randomHex(8)}\"><query xmlns=\"todus:users:getinfo\" users=\"$phone\"/></$ELEM_IQ>"

    fun buildLastIq(phone: String): String = "<$ELEM_IQ $ATTR_TYPE=\"$TYPE_GET\" $ATTR_ID=\"last_${randomHex(8)}\" $ATTR_TO=\"$phone@im.todus.cu\"><query xmlns=\"todus:last:2\"/></$ELEM_IQ>"

    fun parseIncomingMessage(xml: String): ToDusMessage? {
        return try {
            val id = extractAttribute(xml, ATTR_ID) ?: return null
            val from = extractAttribute(xml, ATTR_FROM) ?: ""
            val to = extractAttribute(xml, ATTR_TO) ?: ""
            val body = extractBody(xml) ?: ""
            ToDusMessage(id = id, from = from, to = to, body = body, timestamp = System.currentTimeMillis(), rawXml = xml)
        } catch (e: Exception) { null }
    }

    fun parseOfflineMessages(xml: String): List<ToDusMessage> {
        val messages = mutableListOf<ToDusMessage>()
        val msgRegex = Regex("""<m\b.*?</m>""", RegexOption.DOT_MATCHES_ALL)
        msgRegex.findAll(xml).forEach { match ->
            parseIncomingMessage(match.value)?.let { msg ->
                val tsRegex = Regex("""<todus_offline ts='(\d+)'/>""")
                val ts = tsRegex.find(match.value)?.groupValues?.get(1)?.toLongOrNull()
                messages.add(if (ts != null) msg.copy(timestamp = ts) else msg)
            }
        }
        return messages
    }

    fun extractBody(xml: String): String? {
        val regex = Regex("""<b[^>]*>(.*?)</b>""", RegexOption.DOT_MATCHES_ALL)
        return regex.find(xml)?.groupValues?.get(1)?.let { unescapeXml(it) }
    }

    fun extractAttribute(xml: String, attrName: String): String? {
        val regex = Regex("""$attrName\s*=\s*['"]([^'"]*)['"]""")
        return regex.find(xml)?.groupValues?.get(1)
    }

    fun extractBindJid(xml: String): String? {
        val regex = Regex("""<jid>(.*?)</jid>""")
        return regex.find(xml)?.groupValues?.get(1)
    }

    fun parseUserInfo(xml: String): Map<String, String> {
        val info = mutableMapOf<String, String>()
        val regex = Regex("""<user\b([^>]*)>""")
        val match = regex.find(xml) ?: return info
        val attrs = match.groupValues[1]
        listOf("alias", "official", "description", "username", "pic_url", "exists", "todus_id").forEach { attr ->
            Regex("""$attr='([^']*)'""").find(attrs)?.groupValues?.get(1)?.let { info[attr] = it }
        }
        return info
    }

    fun parseLastSeen(xml: String): Long? {
        return Regex("""last='(-?\d+)'""").find(xml)?.groupValues?.get(1)?.toLongOrNull()
    }

    fun escapeXml(text: String): String = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    fun unescapeXml(text: String): String = text.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
    fun randomHex(length: Int): String = (1..length).map { "abcdef0123456789".random() }.joinToString("")
}

data class ToDusMessage(
    val id: String, val from: String, val to: String = "", val body: String,
    val timestamp: Long = System.currentTimeMillis(), val rawXml: String = ""
)
