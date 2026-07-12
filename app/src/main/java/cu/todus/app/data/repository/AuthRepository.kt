package cu.todus.app.data.repository
import cu.todus.app.data.remote.XmppClient

class AuthRepository(private val xmppClient: XmppClient) {
    suspend fun authenticate(phone: String) = xmppClient.authenticate(phone)
    suspend fun connect(phone: String, jwt: String) = xmppClient.connect(phone, jwt)
}
