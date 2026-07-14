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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        jwtManager = JwtManager(this)
        val app = application as ToDusApp
        
        var startDestination = "welcome"
        
        if (jwtManager.isJwtValid()) {
            startDestination = "home"
            val phone = jwtManager.getPhone() ?: ""
            val jwt = jwtManager.getJwt() ?: ""
            CoroutineScope(Dispatchers.IO).launch {
                app.xmppClient.connect(phone, jwt)
            }
        } else if (jwtManager.getPhone() != null && jwtManager.isJwtExpired()) {
            startDestination = "home"
            CoroutineScope(Dispatchers.IO).launch {
                jwtManager.revalidateJwt().onSuccess { jwt ->
                    val phone = jwtManager.getPhone() ?: return@launch
                    app.xmppClient.connect(phone, jwt)
                }.onFailure {
                    jwtManager.clear()
                    runOnUiThread { setContent { ToDusTheme { NavGraph(startDestination = "welcome") } } }
                }
            }
        }
        
        setContent {
            ToDusTheme {
                NavGraph(startDestination = startDestination)
            }
        }
    }
}
