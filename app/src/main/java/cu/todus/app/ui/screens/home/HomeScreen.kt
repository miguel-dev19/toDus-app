package cu.todus.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cu.todus.app.data.remote.ConnectionState
import cu.todus.app.ui.components.ChatListItem
import cu.todus.app.ui.components.HomeTopBar
import cu.todus.app.ui.theme.ToDusColors

@Composable
fun HomeScreen(onChatClick: (String, String) -> Unit, onNewChat: () -> Unit) {
    val connectionState = remember { mutableStateOf(ConnectionState.CONNECTED) }
    val chats = remember { mutableStateListOf<Any>() }

    Scaffold(
        topBar = { HomeTopBar(connectionState.value, "Usuario") {} },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewChat, containerColor = ToDusColors.Red, shape = CircleShape) {
                Icon(Icons.Default.Add, "Nuevo Chat", tint = ToDusColors.White)
            }
        }
    ) { padding ->
        if (chats.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No tienes conversaciones", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 80.dp)) {
                items(chats.size) { }
            }
        }
    }
}
