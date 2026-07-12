package cu.todus.app.data.remote

object XMPPUtils {
    fun fromUserNameToJID(userName: String?): String {
        if (userName == null) return ""
        return if (userName.contains("@")) userName else "$userName@im.todus.cu"
    }
    fun fromJIDToUserName(jid: String): String {
        val atIndex = jid.indexOf("@")
        if (atIndex >= 0 && jid.substring(atIndex + 1) == "im.todus.cu")
            return jid.substring(0, atIndex)
        return jid
    }
    fun isUser(uid: String) = !isMUC(uid) && !isChannel(uid)
    fun isMUC(uid: String) = uid.contains("muclight.im.todus.cu")
    fun isChannel(uid: String) = uid.contains("ch.todus.cu")
    fun needSendMarkers(uid: String) = isUser(uid)
}
