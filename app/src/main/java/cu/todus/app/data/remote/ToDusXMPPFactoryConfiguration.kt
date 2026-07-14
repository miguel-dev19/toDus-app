package cu.todus.app.data.remote

import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration
import org.jxmpp.jid.impl.JidCreate
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

object ToDusXMPPFactoryConfiguration {
    const val HOST = "ws.todus.cu"
    const val PORT = 1756
    const val DOMAIN = "im.todus.cu"

    fun create(username: String): XMPPTCPConnection {
        val resource = username.md5() + "_Android"
        val config = XMPPTCPConnectionConfiguration.builder()
            .setHost(HOST)
            .setPort(PORT)
            .setXmppDomain(DOMAIN)
            .setResource(resource)
            .setSecurityMode(SecurityMode.ifpossible)
            .setCompressionEnabled(false)
            .setSendPresence(true)
            .setCustomSSLContext(createTrustAllSSLContext())
            .setHostnameVerifier { _, _ -> true }
            .setDebuggerFactory { ToDusXmppDebugger(it) }
            .setConnectTimeout(30000)
            .build()
        return XMPPTCPConnection(config)
    }

    private fun createTrustAllSSLContext(): SSLContext {
        val tm = object : X509TrustManager {
            override fun checkClientTrusted(c: Array<X509Certificate>?, a: String?) {}
            override fun checkServerTrusted(c: Array<X509Certificate>?, a: String?) {}
            override fun getAcceptedIssuers() = arrayOf<X509Certificate>()
        }
        return SSLContext.getInstance("TLS").apply { init(null, arrayOf(tm), SecureRandom()) }
    }

    private fun String.md5(): String {
        val digest = java.security.MessageDigest.getInstance("MD5")
        return digest.digest(toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
