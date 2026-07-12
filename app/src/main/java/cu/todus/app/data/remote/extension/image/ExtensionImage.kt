package cu.todus.app.data.remote.extension.image
import org.jivesoftware.smack.packet.ExtensionElement
import org.jivesoftware.smack.util.XmlStringBuilder

class ExtensionImage : ExtensionElement {
    companion object { const val ELEMENT = "image"; const val NAMESPACE = "image:n" }
    var id: String = ""; var messageId: String = ""; var url: String = ""; var name: String = ""
    var size: Long = 0; var hash: String = ""; var width: Long = 0; var height: Long = 0; var thumbnail: String = ""
    override fun getElementName() = ELEMENT
    override fun getNamespace() = NAMESPACE
    override fun toXML(enclosingNamespace: String?): CharSequence {
        return XmlStringBuilder(this).attribute("i", id).attribute("mi", messageId)
            .attribute("url", url).attribute("n", name).attribute("s", size.toInt())
            .attribute("h", hash).attribute("w", width.toInt()).attribute("he", height.toInt())
            .attribute("tnail", thumbnail).closeEmptyElement().toString()
    }
}
