package cu.todus.app.data.remote.provider
import cu.todus.app.data.remote.extension.contact.ExtensionContact
import org.jivesoftware.smack.provider.ExtensionElementProvider
import org.xmlpull.v1.XmlPullParser

class ExtensionContactProvider : ExtensionElementProvider<ExtensionContact>() {
    override fun parse(parser: XmlPullParser, initialDepth: Int): ExtensionContact {
        return ExtensionContact().apply {
            id = parser.getAttributeValue("", "i") ?: ""
            messageId = parser.getAttributeValue("", "mi") ?: ""
            name = parser.getAttributeValue("", "n") ?: ""
            number = parser.getAttributeValue("", "num") ?: ""
        }
    }
}
