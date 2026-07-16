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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cu.todus.app.ToDusApp
import cu.todus.app.data.local.ToDusDatabase
import cu.todus.app.ui.components.ContactListItem

@Composable
fun ContactsScreen(onBack: () -> Unit, onContactClick: (String, String) -> Unit) {
    val context = LocalContext.current
    val db = remember { ToDusDatabase.getInstance(context) }
    val contacts by db.contactDao().getAllContacts().collectAsStateWithLifecycle(emptyList())

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
        if (contacts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No hay contactos", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(contacts, key = { it.phone }) { contact ->
                    ContactListItem(
                        name = contact.name.ifEmpty { contact.phone },
                        phone = contact.phone,
                        avatarUrl = contact.avatarUrl.ifEmpty { null },
                        isRegistered = contact.isRegistered,
                        onClick = { onContactClick(contact.phone, contact.name.ifEmpty { contact.phone }) }
                    )
                }
            }
        }
    }
}
