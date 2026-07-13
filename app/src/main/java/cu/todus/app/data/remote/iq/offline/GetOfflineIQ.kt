package cu.todus.app.data.remote.iq.offline

import org.jivesoftware.smack.packet.IQ

class GetOfflineIQ : IQ("query", "t:offline") {
    init { type = Type.get }
    
    override fun getIQChildElementBuilder(builder: IQChildElementXmlStringBuilder): IQChildElementXmlStringBuilder {
        builder.rightAngleBracket()
        return builder
    }
}
