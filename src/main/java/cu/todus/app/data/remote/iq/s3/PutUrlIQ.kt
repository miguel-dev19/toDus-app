package cu.todus.app.data.remote.iq.s3
import org.jivesoftware.smack.packet.IQ

class PutUrlIQ(
    val room: String = "", val fileType: Int = TYPE_PICTURE, val persistent: Boolean = true,
    val size: Int = 0, val put: String = "", val get: String = "", val status: Int = 200
) : IQ("query", "todus:purl") {
    companion object {
        const val NAMESPACE = "todus:purl"
        const val TYPE_FILE = 0; const val TYPE_VOICE = 1; const val TYPE_AUDIO = 2
        const val TYPE_VIDEO = 3; const val TYPE_PICTURE = 4; const val TYPE_PROFILE = 5; const val TYPE_THUMBNAIL = 6
    }
    init { type = Type.get }
    override fun getIQChildElementBuilder(builder: IQChildElementXmlStringBuilder): IQChildElementXmlStringBuilder {
        builder.attribute("type", fileType).attribute("persistent", persistent)
        builder.attribute("size", size).attribute("room", room)
        builder.rightAngleBracket()
        return builder
    }
}
