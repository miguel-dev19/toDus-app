package cu.todus.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import cu.todus.app.data.local.JwtManager
import cu.todus.app.data.local.ToDusDatabase
import cu.todus.app.data.local.entity.ChatEntity
import cu.todus.app.data.remote.OfflineManager
import cu.todus.app.ui.theme.ToDusTheme
import cu.todus.app.ui.navigation.NavGraph
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
    private lateinit var jwtManager: JwtManager
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNeededPermissions()
        jwtManager = JwtManager(this)
        val app = application as ToDusApp
        val db = ToDusDatabase.getInstance(this)
        var startDestination = "welcome"

        // Callback para crear chats automáticamente con mensajes offline
        app.xmppClient.onOfflineMessage = { jid, name, message, timestamp ->
            CoroutineScope(Dispatchers.IO).launch {
                val existing = db.chatDao().getChat(jid)
                if (existing == null) {
                    db.chatDao().insert(ChatEntity(jid = jid, name = name, lastMessage = message, lastTimestamp = timestamp))
                } else {
                    db.chatDao().updateLastMessage(jid, message, timestamp)
                }
            }
        }

        if (jwtManager.isJwtValid()) {
            startDestination = "home"
            val phone = jwtManager.getPhone() ?: ""
            val jwt = jwtManager.getJwt() ?: ""
            CoroutineScope(Dispatchers.IO).launch {
                app.xmppClient.connect(phone, jwt)
                OfflineManager(app.xmppClient, db.messageDao(), db.chatDao()).downloadOfflineMessages()
            }
        } else if (jwtManager.getPhone() != null && jwtManager.isJwtExpired()) {
            startDestination = "home"
            CoroutineScope(Dispatchers.IO).launch {
                jwtManager.revalidateJwt().onSuccess { jwt ->
                    val phone = jwtManager.getPhone() ?: return@launch
                    app.xmppClient.connect(phone, jwt)
                    OfflineManager(app.xmppClient, db.messageDao(), db.chatDao()).downloadOfflineMessages()
                }.onFailure {
                    jwtManager.clear()
                    runOnUiThread { recreate() }
                }
            }
        }

        setContent {
            ToDusTheme { NavGraph(startDestination = startDestination) }
        }
    }

    private fun requestNeededPermissions() {
        val permissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.READ_CONTACTS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissions.isNotEmpty()) requestPermissionLauncher.launch(permissions.toTypedArray())
    }
}
