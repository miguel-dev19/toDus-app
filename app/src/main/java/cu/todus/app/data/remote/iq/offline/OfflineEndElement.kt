package cu.todus.app.data.remote.iq.offline
import org.jivesoftware.smack.packet.ExtensionElement
import org.jivesoftware.smack.util.XmlStringBuilder
import org.jivesoftware.smack.packet.XmlEnvironment

class OfflineEndElement : ExtensionElement {
    override fun getElementName() = OfflineElement.ELEMENT_END
    override fun getNamespace() = OfflineElement.NAMESPACE
    override fun toXML(): CharSequence = toXML(XmlEnvironment.EMPTY)
    override fun toXML(enclosingNamespace: XmlEnvironment): CharSequence {
        return XmlStringBuilder(this).rightAngleBracket().closeElement(this)
    }
}
