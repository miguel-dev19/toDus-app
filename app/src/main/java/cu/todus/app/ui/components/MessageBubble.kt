package cu.todus.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cu.todus.app.ui.theme.ToDusColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessageBubble(text: String, time: Long, isMine: Boolean, state: String = "sent") {
    val bubbleColor = if (isMine) Color.White else ToDusColors.Red
    val textColor = if (isMine) Color.Black else Color.White
    val borderColor = if (isMine) MaterialTheme.colorScheme.outline.copy(alpha = 0.2f) else Color.Transparent
    val alignment = if (isMine) Alignment.End else Alignment.Start
    val shape = if (isMine) RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp) else RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)
    val timeColor = if (isMine) Color.Gray else Color.White.copy(alpha = 0.7f)

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp), horizontalAlignment = alignment) {
        Box(modifier = Modifier.widthIn(max = 280.dp).clip(shape).background(bubbleColor).then(if (isMine) Modifier.border(1.dp, borderColor, shape) else Modifier).padding(10.dp)) {
            Column {
                Text(text = text, color = textColor, fontSize = 15.sp, lineHeight = 20.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Row(modifier = Modifier.align(Alignment.End), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(time)), fontSize = 11.sp, color = timeColor)
                    if (isMine) {
                        when (state) {
                            "sending" -> Icon(Icons.Default.Schedule, "Enviando", modifier = Modifier.size(14.dp), tint = Color.Gray)
                            "sent" -> Icon(Icons.Default.Check, "Enviado", modifier = Modifier.size(14.dp), tint = Color.Gray)
                            "delivered" -> Icon(Icons.Default.DoneAll, "Entregado", modifier = Modifier.size(14.dp), tint = Color.Gray)
                            "read" -> Icon(Icons.Default.DoneAll, "Leído", modifier = Modifier.size(14.dp), tint = ToDusColors.Red)
                            else -> Icon(Icons.Default.Check, "Enviado", modifier = Modifier.size(14.dp), tint = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
