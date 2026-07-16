package cu.todus.app.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import cu.todus.app.ToDusApp
import cu.todus.app.data.local.ToDusDatabase
import cu.todus.app.ui.components.MessageBubble
import cu.todus.app.ui.theme.ToDusColors

@Composable
fun ChatScreen(chatJid: String, chatName: String, onBack: () -> Unit, onContactProfile: (String) -> Unit = {}) {
    val context = LocalContext.current; val app = context.applicationContext as ToDusApp
    val db = remember { ToDusDatabase.getInstance(context) }
    val viewModel = remember { ChatViewModel(chatJid, app.xmppClient, db.messageDao(), db.chatDao()) }
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val messageText by viewModel.messageText.collectAsStateWithLifecycle()
    val isLoadingOffline by viewModel.isLoadingOffline.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var lastSeen by remember { mutableStateOf("") }
    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1) }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(start = 4.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = MaterialTheme.colorScheme.onSurface) }
                    Row(modifier = Modifier.clickable { onContactProfile(chatJid) }.weight(1f).padding(end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                            Text(chatName.first().uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(chatName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
                            if (lastSeen.isNotEmpty()) Text(lastSeen, style = MaterialTheme.typography.labelSmall, color = if (lastSeen == "en linea") ToDusColors.Green else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    IconButton(onClick = { onContactProfile(chatJid) }) { Icon(Icons.Default.Info, "Info contacto", tint = MaterialTheme.colorScheme.onSurface) }
                }
            }
        },
        bottomBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                Row(modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {}, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.AttachFile, "Adjuntar", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) }
                    Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)) {
                        OutlinedTextField(value = messageText, onValueChange = { viewModel.onMessageTextChanged(it) }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), placeholder = { Text("Mensaje", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) }, textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface), maxLines = 5, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { viewModel.sendMessage() }, enabled = messageText.isNotBlank(), modifier = Modifier.size(40.dp)) { Icon(Icons.AutoMirrored.Filled.Send, "Enviar", tint = if (messageText.isNotBlank()) ToDusColors.Red else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)) }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoadingOffline) { Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)) { Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { CircularProgressIndicator(modifier = Modifier.size(14.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp); Spacer(modifier = Modifier.width(8.dp)); Text("Cargando mensajes...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) } } }
            if (messages.isEmpty() && !isLoadingOffline) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Inicia una conversacion", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)); Spacer(modifier = Modifier.height(4.dp)); Text("Los mensajes estan cifrados de extremo a extremo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) }
                }
            } else {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(messages, key = { it.id }) { msg -> MessageBubble(text = msg.body, time = msg.timestamp, isMine = msg.senderPhone == "me", state = msg.state) }
                }
            }
        }
    }
}
