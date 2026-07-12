package cu.todus.app.data.remote.iq.status
import org.jivesoftware.smack.packet.IQ

class DeleteStatusIQ(val statusId: String, val code: String = "") : IQ("query", "td:status:delete") {
    companion object { const val NAMESPACE = "td:status:delete" }
    init { type = Type.set }
    override fun getIQChildElementBuilder(builder: IQChildElementXmlStringBuilder): IQChildElementXmlStringBuilder {
        builder.attribute("id", statusId).rightAngleBracket()
        return builder
    }
}
