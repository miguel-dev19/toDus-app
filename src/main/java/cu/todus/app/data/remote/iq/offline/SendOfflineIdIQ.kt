package cu.todus.app.data.remote.iq.offline
import org.jivesoftware.smack.packet.IQ

class SendOfflineIdIQ(private val idArray: List<String>) : IQ("query", "t:offline:del") {
    init { type = Type.set }
    override fun getIQChildElementBuilder(builder: IQChildElementXmlStringBuilder): IQChildElementXmlStringBuilder {
        builder.rightAngleBracket()
        idArray.forEach { builder.element("id", it) }
        return builder
    }
}
