package cu.todus.app.ui.screens.contacts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cu.todus.app.ToDusApp
import cu.todus.app.data.remote.ConnectionState
import cu.todus.app.data.remote.ProfileManager
import cu.todus.app.ui.components.ContactListItem
import kotlinx.coroutines.*

@Composable
fun ContactsScreen(onBack: () -> Unit, onContactClick: (String, String) -> Unit) {
    val context = LocalContext.current; val app = context.applicationContext as ToDusApp
    val db = remember { cu.todus.app.data.local.ToDusDatabase.getInstance(context) }
    var contacts by remember { mutableStateOf<List<ProfileManager.RosterContact>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }; var isRefreshing by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    fun loadContacts() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Cargar cache offline primero
                val cached = db.contactDao().getAllContactsOnce().map {
                    ProfileManager.RosterContact(phone = it.phone, alias = it.alias.ifEmpty { it.name }, photoUrl = it.avatarUrl)
                }
                if (cached.isNotEmpty()) { withContext(Dispatchers.Main) { contacts = cached; isLoading = false } }

                var retries = 0
                while (app.xmppClient.connectionState.value != ConnectionState.CONNECTED && retries < 30) { delay(500); retries++ }
                if (app.xmppClient.connectionState.value == ConnectionState.CONNECTED) {
                    val pm = ProfileManager(app.xmppClient, null, db.contactDao())
                    pm.getRosterWithToDusUsers().onSuccess { roster ->
                        withContext(Dispatchers.Main) { contacts = roster.sortedBy { it.alias.lowercase() }; isLoading = false; isRefreshing = false }
                    }.onFailure { withContext(Dispatchers.Main) { if (contacts.isEmpty()) errorMsg = it.message; isLoading = false; isRefreshing = false } }
                }
            } catch (e: Exception) { withContext(Dispatchers.Main) { if (contacts.isEmpty()) errorMsg = e.message; isLoading = false; isRefreshing = false } }
        }
    }

    // ARREGLO 2: Cargar contactos automáticamente al entrar
    LaunchedEffect(Unit) { loadContacts() }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(start = 4.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }; Text("Contactos", style = MaterialTheme.typography.titleMedium) }
                    IconButton(onClick = { isRefreshing = true; loadContacts() }, enabled = !isRefreshing) { if (isRefreshing) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) else Icon(Icons.Default.Refresh, "Recargar") }
                }
            }
        }
    ) { padding ->
        when {
            isLoading && contacts.isEmpty() -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            contacts.isEmpty() && !isLoading -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Person, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)); Spacer(modifier = Modifier.height(16.dp)); Text(if (errorMsg != null) errorMsg!! else "No hay contactos", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)); if (errorMsg != null) { Spacer(modifier = Modifier.height(8.dp)); TextButton(onClick = { isRefreshing = true; loadContacts() }) { Text("Reintentar") } } } }
            else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) { items(contacts, key = { it.phone }) { contact -> ContactListItem(name = contact.alias.ifEmpty { contact.phone }, phone = contact.phone, avatarUrl = contact.photoUrl.ifEmpty { null }, onClick = { onContactClick(contact.phone, contact.alias.ifEmpty { contact.phone }) }) } }
        }
    }
}
