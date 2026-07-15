package cu.todus.app.data.remote.iq.last

import org.jivesoftware.smack.packet.IQ
import org.jxmpp.jid.Jid

class LastIQ(val uid: Jid? = null, val last: Long = -1) : IQ("query", "todus:last:2") {
    companion object { const val ELEMENT = "query"; const val NAMESPACE = "todus:last:2" }
    init { type = Type.get; if (uid != null) setTo(uid) }
    override fun getIQChildElementBuilder(builder: IQChildElementXmlStringBuilder): IQChildElementXmlStringBuilder { builder.rightAngleBracket(); return builder }
}
