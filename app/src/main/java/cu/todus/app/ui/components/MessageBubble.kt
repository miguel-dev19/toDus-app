package cu.todus.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import cu.todus.app.data.local.entity.MessageEntity
import cu.todus.app.ui.theme.ToDusColors
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessageBubble(text: String, time: Long, isMine: Boolean, state: String = "sent", mediaUrl: String? = null, mediaType: String = "text", onImageClick: (() -> Unit)? = null) {
    val bubbleColor = if (isMine) ToDusColors.White else ToDusColors.Red
    val textColor = if (isMine) Color.Black else Color.White
    val borderColor = if (isMine) MaterialTheme.colorScheme.outline.copy(alpha = 0.15f) else Color.Transparent
    val alignment = if (isMine) Alignment.End else Alignment.Start
    val shape = if (isMine) RoundedCornerShape(12.dp, 4.dp, 12.dp, 12.dp) else RoundedCornerShape(4.dp, 12.dp, 12.dp, 12.dp)

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp), horizontalAlignment = alignment) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(bubbleColor)
                .then(if (isMine) Modifier.border(1.dp, borderColor, shape) else Modifier)
                .padding(6.dp)
        ) {
            Column {
                // ⭐ Imagen / Video / Audio
                when {
                    mediaType == "image" && mediaUrl != null -> {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(mediaUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Imagen",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 250.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .then(if (onImageClick != null) Modifier.clickable { onImageClick() } else Modifier),
                            contentScale = ContentScale.Crop
                        )
                    }
                    mediaType == "video" && mediaUrl != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = mediaUrl,
                                contentDescription = "Video",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            // Botón de play superpuesto
                            Box(
                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(50)).background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PlayArrow, "Reproducir", tint = Color.White, modifier = Modifier.size(28.dp))
                            }
                        }
                    }
                    mediaType == "audio" -> {
                        // Miniatura de audio
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, "Reproducir", tint = if (isMine) ToDusColors.Red else Color.White, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text.ifEmpty { "Mensaje de voz" }, color = textColor, fontSize = 13.sp)
                                Text(formatDuration(mediaUrl), color = textColor.copy(alpha = 0.6f), fontSize = 11.sp)
                            }
                        }
                    }
                    else -> {
                        if (text.isNotEmpty()) {
                            Text(text = text, color = textColor, fontSize = 15.sp, lineHeight = 20.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Hora y estado
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(time)),
                        fontSize = 11.sp,
                        color = if (isMine) Color.Gray else Color.White.copy(alpha = 0.7f)
                    )
                    if (isMine) {
                        when (state) {
                            "sending" -> Icon(Icons.Default.Schedule, "Enviando", modifier = Modifier.size(14.dp), tint = Color.Gray)
                            "sent" -> Icon(Icons.Default.Check, "Enviado", modifier = Modifier.size(14.dp), tint = Color.Gray)
                            "delivered" -> Icon(Icons.Default.DoneAll, "Entregado", modifier = Modifier.size(14.dp), tint = Color.Gray)
                            "read" -> Icon(Icons.Default.DoneAll, "Leído", modifier = Modifier.size(14.dp), tint = ToDusColors.Green)
                            else -> Icon(Icons.Default.Check, "Enviado", modifier = Modifier.size(14.dp), tint = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

fun formatDuration(seconds: String?): String {
    val secs = seconds?.toIntOrNull() ?: 0
    val mins = secs / 60
    val remainingSecs = secs % 60
    return if (mins > 0) "${mins}:${remainingSecs.toString().padStart(2, '0')}" else "0:${remainingSecs.toString().padStart(2, '0')}"
}
