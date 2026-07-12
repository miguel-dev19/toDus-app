package cu.todus.app.data.remote.iq.s3
import org.jivesoftware.smack.packet.IQ

class GetUrlIQ(val url: String = "", val du: String = "", val status: Int = 200) : IQ("query", "todus:gurl") {
    companion object { const val NAMESPACE = "todus:gurl" }
    init { type = Type.get }
    override fun getIQChildElementBuilder(builder: IQChildElementXmlStringBuilder): IQChildElementXmlStringBuilder {
        builder.attribute("url", url).rightAngleBracket()
        return builder
    }
}
