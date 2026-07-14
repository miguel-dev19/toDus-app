package cu.todus.app.ui.screens.contacts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cu.todus.app.ToDusApp
import cu.todus.app.data.local.PhoneContactSync
import cu.todus.app.data.local.ToDusDatabase
import cu.todus.app.data.local.entity.ContactEntity
import cu.todus.app.data.remote.ProfileManager
import cu.todus.app.ui.components.ContactListItem
import kotlinx.coroutines.*

@Composable
fun ContactsScreen(onBack: () -> Unit, onContactClick: (String, String) -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as ToDusApp
    val db = remember { ToDusDatabase.getInstance(context) }
    val contacts by db.contactDao().getAllContacts().collectAsStateWithLifecycle(emptyList())
    var isSyncing by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            // 1. Leer contactos del teléfono
            val phoneSync = PhoneContactSync(context, 53)
            val phoneContacts = phoneSync.getPhoneContacts()
            
            // 2. Guardar en BD temporalmente (sin isInRoster)
            phoneContacts.forEach { contact ->
                db.contactDao().insert(contact.copy(isInRoster = false))
            }
            
            // 3. Verificar cuáles usan toDus (consultar al servidor)
            app.xmppClient.connection?.let { conn ->
                val profileManager = ProfileManager(conn)
                phoneContacts.forEach { contact ->
                    profileManager.getProfile(contact.phone).onSuccess { profile ->
                        if (profile.exists) {
                            // Actualizar con datos reales de toDus
                            db.contactDao().insert(
                                ContactEntity(
                                    phone = contact.phone,
                                    alias = profile.alias.ifEmpty { contact.alias },
                                    toDusId = profile.toDusId,
                                    avatarUrl = profile.photoThumbnail.ifEmpty { profile.photoUrl },
                                    isInRoster = true  // ← Solo los que usan toDus
                                )
                            )
                        }
                    }
                }
            }
            
            isSyncing = false
        }
    }
    
    // Mostrar solo los que están en toDus
    val toDusContacts = remember(contacts) {
        contacts.filter { it.isInRoster }
    }
    
    val grouped = remember(toDusContacts) {
        toDusContacts.sortedBy { it.alias.lowercase() }.groupBy { it.alias.first().uppercase() }
    }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(start = 4.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = MaterialTheme.colorScheme.onSurface) }
                    Text("Contactos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    ) { padding ->
        if (isSyncing) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Buscando contactos en toDus...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else if (toDusContacts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No tienes contactos en toDus", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                grouped.forEach { (letter, contactsForLetter) ->
                    item {
                        Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), modifier = Modifier.fillMaxWidth()) {
                            Text(letter, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                        }
                    }
                    items(contactsForLetter, key = { it.phone }) { contact ->
                        ContactListItem(
                            name = contact.alias.ifEmpty { contact.phone },
                            bio = contact.toDusId,
                            avatarUrl = contact.avatarUrl.ifEmpty { null },
                            onClick = { onContactClick(contact.phone, contact.alias.ifEmpty { contact.phone }) }
                        )
                    }
                }
            }
        }
    }
}
