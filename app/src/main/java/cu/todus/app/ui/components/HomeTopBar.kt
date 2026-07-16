package cu.todus.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import cu.todus.app.data.remote.ConnectionState
import cu.todus.app.ui.theme.ToDusColors

@Composable
fun HomeTopBar(connectionState: ConnectionState, userName: String, userAvatar: String? = null, onProfileClick: () -> Unit) {
    val indicatorColor = when (connectionState) {
        ConnectionState.CONNECTED -> ToDusColors.Green
        ConnectionState.CONNECTING, ConnectionState.RECONNECTING -> ToDusColors.Orange
        ConnectionState.DISCONNECTED, ConnectionState.FAILED -> ToDusColors.Error
    }
    
    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(start = 16.dp, end = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("toDus", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Surface(onClick = onProfileClick, shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                Row(modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(userName, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 100.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.BottomEnd) {
                        Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                            if (userAvatar != null) AsyncImage(model = userAvatar, contentDescription = "Foto", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            else Text(userName.first().uppercase(), fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                        }
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(indicatorColor))
                    }
                }
            }
        }
    }
}
