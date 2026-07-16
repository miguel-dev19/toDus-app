package cu.todus.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cu.todus.app.ToDusApp
import cu.todus.app.data.local.JwtManager
import cu.todus.app.data.local.ToDusDatabase
import cu.todus.app.data.remote.ConnectionState
import cu.todus.app.ui.components.ChatListItem
import cu.todus.app.ui.components.HomeTopBar
import cu.todus.app.ui.theme.ToDusColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(onChatClick: (String, String) -> Unit, onNewChat: () -> Unit, onProfileClick: () -> Unit = {}) {
    val context = LocalContext.current; val app = context.applicationContext as ToDusApp
    val db = remember { ToDusDatabase.getInstance(context) }; val jwtManager = remember { JwtManager(context) }
    val chats by db.chatDao().getAllChats().collectAsStateWithLifecycle(emptyList())
    val connectionState by app.xmppClient.connectionState.collectAsState()
    val userName = remember { jwtManager.getAlias() ?: jwtManager.getPhone() ?: "Usuario" }
    val userAvatar = remember { jwtManager.getAvatar() }

    Scaffold(
        topBar = { HomeTopBar(connectionState, userName, userAvatar, onProfileClick) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onNewChat, containerColor = ToDusColors.Red, contentColor = ToDusColors.White, shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Outlined.ChatBubbleOutline, "Nuevo Chat", modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Nuevo Chat")
            }
        }
    ) { padding ->
        if (chats.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.ChatBubbleOutline, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 80.dp)) {
                items(chats, key = { it.jid }) { chat ->
                    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(chat.lastTimestamp))
                    ChatListItem(name = chat.name.ifEmpty { chat.jid }, lastMessage = chat.lastMessage, time = timeStr, unreadCount = chat.unreadCount, avatarUrl = chat.avatarUrl.ifEmpty { null }, onClick = { onChatClick(chat.jid, chat.name.ifEmpty { chat.jid }) })
                }
            }
        }
    }
}
