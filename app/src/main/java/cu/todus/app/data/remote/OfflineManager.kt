package cu.todus.app.data.remote

import android.util.Log
import cu.todus.app.data.local.dao.ChatDao
import cu.todus.app.data.local.dao.MessageDao
import cu.todus.app.data.local.entity.MessageEntity
import kotlinx.coroutines.*

class OfflineManager(
    private val xmppClient: XmppClient,
    private val messageDao: MessageDao,
    private val chatDao: ChatDao
) {
    companion object { private const val TAG = "OfflineManager" }
    
    suspend fun downloadOfflineMessages(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val offlineIq = ToDusProtocol.buildOfflineIq()
            xmppClient.sendIq(offlineIq)
            
            // Los mensajes offline se reciben por incomingMessages
            // El parseo ya está en XmppClient.startMessageReader()
            
            Result.success(0) // Se procesan asíncronamente
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
            Result.failure(e)
        }
    }
}
