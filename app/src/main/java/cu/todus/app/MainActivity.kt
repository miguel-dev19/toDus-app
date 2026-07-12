package cu.todus.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import cu.todus.app.data.local.JwtManager
import cu.todus.app.data.remote.XmppClient
import cu.todus.app.ui.theme.ToDusTheme
import cu.todus.app.ui.navigation.NavGraph
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
    private lateinit var jwtManager: JwtManager
    private val xmppClient = XmppClient()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        jwtManager = JwtManager(this)
        
        val startDestination = if (jwtManager.isJwtValid()) {
            // JWT válido → conectar XMPP e ir a Home
            val phone = jwtManager.getPhone() ?: ""
            val jwt = jwtManager.getJwt() ?: ""
            CoroutineScope(Dispatchers.IO).launch {
                xmppClient.connect(phone, jwt)
            }
            "home"
        } else {
            // JWT inválido o no existe → ir a Welcome
            "welcome"
        }
        
        setContent {
            ToDusTheme {
                NavGraph(startDestination = startDestination)
            }
        }
    }
}
