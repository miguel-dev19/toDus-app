package cu.todus.app.data.remote.iq.push
import org.jivesoftware.smack.packet.IQ
import org.jivesoftware.smack.util.XmlStringBuilder
import org.jxmpp.jid.impl.JidCreate

class ToDusPushIQ(val id: String = "") : IQ("query", "todus:fb:push") {
    companion object { const val NAMESPACE = "todus:fb:push" }
    init { type = Type.set; setTo(JidCreate.from("im.todus.cu")) }
    override fun getIQChildElementBuilder(builder: IQChildElementXmlStringBuilder): IQChildElementXmlStringBuilder {
        builder.rightAngleBracket()
        val pushBuilder = XmlStringBuilder()
        pushBuilder.halfOpenElement("push_id").rightAngleBracket().escape(id).closeElement("push_id")
        builder.element(pushBuilder)
        return builder
    }
}
