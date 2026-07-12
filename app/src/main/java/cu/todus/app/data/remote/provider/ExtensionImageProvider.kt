package cu.todus.app.data.remote.provider
import cu.todus.app.data.remote.extension.image.ExtensionImage
import org.jivesoftware.smack.packet.XmlEnvironment
import org.jivesoftware.smack.provider.ExtensionElementProvider
import org.xmlpull.v1.XmlPullParser

class ExtensionImageProvider : ExtensionElementProvider<ExtensionImage>() {
    override fun parse(parser: XmlPullParser, initialDepth: Int, xmlEnvironment: XmlEnvironment): ExtensionImage {
        return ExtensionImage().apply {
            id = parser.getAttributeValue("", "i") ?: ""
            messageId = parser.getAttributeValue("", "mi") ?: ""
            url = parser.getAttributeValue("", "url") ?: ""
            name = parser.getAttributeValue("", "n") ?: ""
            size = parser.getAttributeValue("", "s")?.toLongOrNull() ?: 0
            hash = parser.getAttributeValue("", "h") ?: ""
            width = parser.getAttributeValue("", "w")?.toLongOrNull() ?: 0
            height = parser.getAttributeValue("", "he")?.toLongOrNull() ?: 0
            thumbnail = parser.getAttributeValue("", "tnail") ?: ""
        }
    }
}
