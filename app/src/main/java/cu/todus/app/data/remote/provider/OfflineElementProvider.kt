package cu.todus.app.data.remote.provider
import cu.todus.app.data.remote.iq.offline.OfflineElement
import org.jivesoftware.smack.provider.ExtensionElementProvider
import org.xmlpull.v1.XmlPullParser
import java.util.Date

class OfflineElementProvider : ExtensionElementProvider<OfflineElement>() {
    override fun parse(parser: XmlPullParser, initialDepth: Int): OfflineElement {
        val ts = parser.getAttributeValue("", "ts") ?: "0"
        return OfflineElement(Date(ts.toLong()))
    }
}
