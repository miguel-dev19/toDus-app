package cu.todus.app.data.remote.iq.offline
import org.jivesoftware.smack.packet.ExtensionElement
import org.jivesoftware.smack.util.XmlStringBuilder

class OfflineEndElement : ExtensionElement {
    override fun getElementName() = OfflineElement.ELEMENT_END
    override fun getNamespace() = OfflineElement.NAMESPACE
    override fun toXML(enclosingNamespace: String?): CharSequence {
        return XmlStringBuilder(this).rightAngleBracket().closeElement(this)
    }
}
