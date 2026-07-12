package cu.todus.app.data.remote.extension.chunk
import org.jivesoftware.smack.packet.ExtensionElement
import org.jivesoftware.smack.util.XmlStringBuilder

class ExtensionChunk(val originalId: String, val index: Int, val total: Int) : ExtensionElement {
    companion object { const val ELEMENT = "chunk"; const val NAMESPACE = "msg:fragment:n" }
    override fun getElementName() = ELEMENT
    override fun getNamespace() = NAMESPACE
    override fun toXML(enclosingNamespace: String?): CharSequence {
        return XmlStringBuilder(this).attribute("originalId", originalId)
            .attribute("index", index.toString()).attribute("total", total.toString())
            .rightAngleBracket().closeElement(this)
    }
}
