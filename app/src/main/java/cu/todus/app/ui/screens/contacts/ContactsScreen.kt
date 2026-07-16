package cu.todus.app.ui.screens.contacts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cu.todus.app.ToDusApp
import cu.todus.app.data.remote.ProfileManager
import cu.todus.app.ui.components.ContactListItem
import kotlinx.coroutines.*

@Composable
fun ContactsScreen(onBack: () -> Unit, onContactClick: (String, String) -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as ToDusApp
    var contacts by remember { mutableStateOf<List<ProfileManager.RosterContact>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            // Esperar conexión
            var retries = 0
            while (app.xmppClient.connectionState.value != ConnectionState.CONNECTED && retries < 20) {
                delay(500); retries++
            }
            
            if (app.xmppClient.connectionState.value == ConnectionState.CONNECTED) {
                val pm = ProfileManager(app.xmppClient)
                
                // Solicitar roster
                app.xmppClient.requestRoster()
                delay(2000)
                
                pm.getRoster().onSuccess { roster ->
                    contacts = roster
                }.onFailure {
                    errorMsg = "Error al cargar contactos"
                }
            }
        } catch (e: Exception) {
            errorMsg = e.message
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(start = 4.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                    Text("Contactos", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Cargando contactos...")
                }
            }
        } else if (contacts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(if (errorMsg != null) errorMsg!! else "No hay contactos", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(contacts, key = { it.phone }) { contact ->
                    ContactListItem(
                        name = contact.alias.ifEmpty { contact.phone },
                        phone = contact.phone,
                        avatarUrl = contact.photoUrl.ifEmpty { null },
                        isRegistered = true,
                        onClick = { onContactClick(contact.phone, contact.alias.ifEmpty { contact.phone }) }
                    )
                }
            }
        }
    }
}
