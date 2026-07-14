package cu.todus.app.data.remote.iq.user

import org.jivesoftware.smack.packet.IQ

class GetUserInfoIQ(private val users: String) : IQ("query", "todus:users:getinfo") {
    init { type = Type.get }
    override fun getIQChildElementBuilder(builder: IQChildElementXmlStringBuilder): IQChildElementXmlStringBuilder {
        builder.attribute("users", users)
        builder.rightAngleBracket()
        return builder
    }
}
