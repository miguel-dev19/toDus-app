package cu.todus.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import cu.todus.app.data.remote.ConnectionState
import cu.todus.app.ui.theme.ToDusColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(connectionState: ConnectionState, userName: String, userAvatar: String? = null, onProfileClick: () -> Unit = {}) {
    val indicatorColor = when (connectionState) {
        ConnectionState.CONNECTED -> ToDusColors.Green
        ConnectionState.CONNECTING, ConnectionState.RECONNECTING -> ToDusColors.Orange
        ConnectionState.DISCONNECTED, ConnectionState.FAILED -> ToDusColors.Error
    }

    TopAppBar(
        title = {
            Text("toDus", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        },
        actions = {
            // Avatar con indicador de conexión
            Surface(
                onClick = onProfileClick,
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(start = 12.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(userName, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 100.dp), color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.BottomEnd) {
                        Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color.White.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                            if (userAvatar != null) AsyncImage(model = userAvatar, contentDescription = "Foto", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            else Text(userName.first().uppercase(), fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(indicatorColor).padding(2.dp))
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ToDusColors.Red,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}
