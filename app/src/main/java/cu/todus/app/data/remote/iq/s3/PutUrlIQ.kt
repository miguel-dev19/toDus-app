package cu.todus.app.data.remote.iq.s3

import org.jivesoftware.smack.packet.IQ

class PutUrlIQ(
    val room: String = "", val fileType: Int = 4, val persistent: Boolean = true,
    val size: Int = 0, val put: String = "", val get: String = "", val status: Int = 200
) : IQ("query", "todus:purl") {
    companion object { const val NAMESPACE = "todus:purl" }
    init { type = Type.get }
    override fun getIQChildElementBuilder(builder: IQChildElementXmlStringBuilder): IQChildElementXmlStringBuilder {
        builder.attribute("type", fileType).attribute("persistent", persistent).attribute("size", size).attribute("room", room).rightAngleBracket()
        return builder
    }
}
