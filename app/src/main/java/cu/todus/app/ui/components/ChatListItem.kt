package cu.todus.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import cu.todus.app.ui.theme.ToDusColors

@Composable
fun ChatListItem(
    name: String,
    lastMessage: String,
    time: String,
    unreadCount: Int = 0,
    isOnline: Boolean = false,
    avatarUrl: String? = null,
    onClick: () -> Unit
) {
    Surface(onClick = onClick, color = MaterialTheme.colorScheme.surface) {
        Row(modifier = Modifier.fillMaxWidth().height(72.dp).padding(end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.fillMaxHeight().aspectRatio(1f).padding(8.dp), contentAlignment = Alignment.Center) {
                Surface(modifier = Modifier.fillMaxSize(), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                    if (avatarUrl != null) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Foto",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(name.first().uppercase(), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (isOnline) Box(modifier = Modifier.align(Alignment.BottomEnd).size(14.dp).clip(CircleShape).background(ToDusColors.Green).border(2.dp, MaterialTheme.colorScheme.surface, CircleShape))
            }
            Column(modifier = Modifier.weight(1f).padding(vertical = 12.dp)) {
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(2.dp))
                Text(lastMessage, style = MaterialTheme.typography.bodyMedium, color = if (unreadCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 8.dp)) {
                Text(time, style = MaterialTheme.typography.labelSmall, color = if (unreadCount > 0) ToDusColors.Red else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                if (unreadCount > 0) { Spacer(modifier = Modifier.height(4.dp)); Badge(containerColor = ToDusColors.Red) { Text(if (unreadCount > 99) "99+" else unreadCount.toString(), fontSize = 10.sp) } }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(start = 72.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    }
}
