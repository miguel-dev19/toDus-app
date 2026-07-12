package cu.todus.app.ui.screens.contacts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cu.todus.app.ui.components.ContactListItem

@Composable
fun ContactsScreen(onBack: () -> Unit, onContactClick: (String, String) -> Unit) {
    val contacts = remember { mutableStateListOf<Any>() }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(start = 4.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                    Text("Contactos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        if (contacts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No tienes contactos", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(contacts.size) { /* Cargar de la BD */ }
            }
        }
    }
}
