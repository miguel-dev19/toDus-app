package cu.todus.app.data.remote.iq.offline
import org.jivesoftware.smack.packet.ExtensionElement
import org.jivesoftware.smack.util.XmlStringBuilder
import java.util.Date

class OfflineElement(val stamp: Date) : ExtensionElement {
    companion object { const val ELEMENT = "todus_offline"; const val ELEMENT_END = "todus_end_offline"; const val NAMESPACE = "jc" }
    override fun getElementName() = ELEMENT
    override fun getNamespace() = NAMESPACE
    override fun toXML(enclosingNamespace: String?): CharSequence {
        return XmlStringBuilder(this).attribute("ts", stamp.time.toString()).rightAngleBracket().closeElement(this)
    }
}
