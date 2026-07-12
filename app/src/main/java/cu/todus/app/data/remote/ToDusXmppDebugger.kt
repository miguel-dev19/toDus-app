package cu.todus.app.data.remote
import org.jivesoftware.smack.XMPPConnection
import org.jivesoftware.smack.debugger.AbstractDebugger

class ToDusXmppDebugger(connection: XMPPConnection?) : AbstractDebugger(connection) {
    override fun log(logMessage: String) {}
    override fun log(logMessage: String, throwable: Throwable) {}
}
