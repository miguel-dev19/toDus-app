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
        
        // Determinar pantalla inicial
        var startDestination = "welcome"
        
        if (jwtManager.getPhone() != null) {
            // Hay teléfono guardado → intentar revalidar o usar JWT existente
            if (jwtManager.isJwtValid()) {
                // JWT aún válido → ir directo a Home
                startDestination = "home"
                connectXmpp()
            } else if (jwtManager.isJwtExpired()) {
                // JWT expirado → revalidar automáticamente
                startDestination = "home"  // Ir a Home mientras revalida
                revalidateAndConnect()
            }
            // Si no hay JWT pero sí teléfono → ir a Home (se revalidará allí)
        }
        
        setContent {
            ToDusTheme {
                NavGraph(startDestination = startDestination)
            }
        }
    }
    
    private fun connectXmpp() {
        val phone = jwtManager.getPhone() ?: return
        val jwt = jwtManager.getJwt() ?: return
        CoroutineScope(Dispatchers.IO).launch {
            xmppClient.connect(phone, jwt)
        }
    }
    
    private fun revalidateAndConnect() {
        CoroutineScope(Dispatchers.IO).launch {
            jwtManager.revalidateJwt().onSuccess { jwt ->
                val phone = jwtManager.getPhone() ?: return@launch
                xmppClient.connect(phone, jwt)
            }.onFailure {
                // Si falla la revalidación, limpiar y mostrar login
                jwtManager.clear()
                runOnUiThread {
                    setContent {
                        ToDusTheme {
                            NavGraph(startDestination = "welcome")
                        }
                    }
                }
            }
        }
    }
}
