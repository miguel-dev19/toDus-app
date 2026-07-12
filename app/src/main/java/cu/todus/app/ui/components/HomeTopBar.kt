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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cu.todus.app.data.remote.ConnectionState
import cu.todus.app.ui.theme.ToDusColors

@Composable
fun HomeTopBar(connectionState: ConnectionState, userName: String, onProfileClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(start = 16.dp, end = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(when (connectionState) {
                        ConnectionState.CONNECTED, ConnectionState.AUTHENTICATED -> ToDusColors.Green
                        ConnectionState.CONNECTING, ConnectionState.RECONNECTING, ConnectionState.BEFORE_CONNECTED -> ToDusColors.Orange
                        ConnectionState.WAITING_FOR_CONNECTION -> ToDusColors.Gray
                        ConnectionState.DISCONNECTED -> ToDusColors.Error
                    }))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(when (connectionState) {
                        ConnectionState.CONNECTED, ConnectionState.AUTHENTICATED -> "toDus"
                        ConnectionState.CONNECTING -> "Conectando..."
                        ConnectionState.RECONNECTING -> "Reconectando..."
                        ConnectionState.WAITING_FOR_CONNECTION -> "Esperando red..."
                        else -> "Sin conexion"
                    }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
            Surface(onClick = onProfileClick, shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                Row(modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(userName, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 100.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                        Text(userName.first().uppercase(), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
