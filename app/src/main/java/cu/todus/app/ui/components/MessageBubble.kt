package cu.todus.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cu.todus.app.ui.theme.ToDusColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessageBubble(text: String, time: Long, isMine: Boolean, state: String = "sent") {
    val bubbleColor = if (isMine) ToDusColors.Red else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface
    val alignment = if (isMine) Alignment.End else Alignment.Start
    val shape = if (isMine) {
        RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp)
    } else {
        RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier.widthIn(max = 280.dp).clip(shape).background(bubbleColor).padding(10.dp)
        ) {
            Column {
                Text(
                    text = text,
                    color = textColor,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(time)),
                        fontSize = 11.sp,
                        color = if (isMine) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isMine) {
                        when (state) {
                            "sent" -> Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp), tint = Color.White.copy(alpha = 0.7f))
                            "delivered" -> Icon(Icons.Default.DoneAll, null, modifier = Modifier.size(14.dp), tint = Color.White.copy(alpha = 0.7f))
                            "read" -> Icon(Icons.Default.DoneAll, null, modifier = Modifier.size(14.dp), tint = ToDusColors.Green)
                        }
                    }
                }
            }
        }
    }
}
