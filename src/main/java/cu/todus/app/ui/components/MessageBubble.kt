package cu.todus.app.ui.components
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cu.todus.app.ui.theme.ToDusColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessageBubble(text: String, time: Long, isMine: Boolean, state: String = "sent") {
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(time))
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 1.dp), horizontalAlignment = if (isMine) Alignment.End else Alignment.Start) {
        Surface(modifier = Modifier.widthIn(max = 280.dp), shape = if (isMine) RoundedCornerShape(8.dp, 8.dp, 0.dp, 8.dp) else RoundedCornerShape(8.dp, 8.dp, 8.dp, 0.dp), color = if (isMine) Color.White else ToDusColors.Red, border = if (isMine) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)) else null) {
            Column(modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 4.dp)) {
                Text(text, style = MaterialTheme.typography.bodyLarge, color = if (isMine) Color.Black else Color.White, modifier = Modifier.padding(end = 40.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    Text(timeStr, fontSize = 11.sp, color = if (isMine) Color.Gray.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.8f))
                    if (isMine) {
                        Spacer(modifier = Modifier.width(3.dp))
                        when (state) {
                            "sending" -> Icon(Icons.Default.Schedule, "Enviando", Modifier.size(13.dp), tint = Color.Gray.copy(alpha = 0.7f))
                            "sent" -> Icon(Icons.Default.Done, "Enviado", Modifier.size(13.dp), tint = Color.Gray.copy(alpha = 0.7f))
                            "received" -> Icon(Icons.Default.DoneAll, "Entregado", Modifier.size(15.dp), tint = Color.Gray.copy(alpha = 0.7f))
                            "read" -> Icon(Icons.Default.DoneAll, "Leido", Modifier.size(15.dp), tint = ToDusColors.Red)
                        }
                    }
                }
            }
        }
    }
}
