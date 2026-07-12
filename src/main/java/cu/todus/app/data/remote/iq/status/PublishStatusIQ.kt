package cu.todus.app.data.remote.iq.status
import org.jivesoftware.smack.packet.IQ

class PublishStatusIQ(val content: String, val code: String = "") : IQ("query", "td:status:publish") {
    companion object { const val NAMESPACE = "td:status:publish" }
    init { type = Type.set }
    override fun getIQChildElementBuilder(builder: IQChildElementXmlStringBuilder): IQChildElementXmlStringBuilder {
        builder.rightAngleBracket().escape(content)
        return builder
    }
}
