package cu.todus.app.data.remote.extension.contact
import org.jivesoftware.smack.packet.ExtensionElement
import org.jivesoftware.smack.util.XmlStringBuilder

class ExtensionContact : ExtensionElement {
    companion object { const val ELEMENT = "contact"; const val NAMESPACE = "contact:n" }
    var id: String = ""; var messageId: String = ""; var name: String = ""; var number: String = ""
    override fun getElementName() = ELEMENT
    override fun getNamespace() = NAMESPACE
    override fun toXML(enclosingNamespace: String?): CharSequence {
        return XmlStringBuilder(this).attribute("i", id).attribute("mi", messageId)
            .attribute("n", name).attribute("num", number).closeEmptyElement()
    }
}
