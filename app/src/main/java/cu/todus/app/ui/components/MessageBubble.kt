package cu.todus.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import cu.todus.app.ui.theme.ToDusColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessageBubble(
    text: String,
    time: Long,
    isMine: Boolean,
    state: String = "sent",
    mediaUrl: String? = null,
    mediaType: String = "text",
    mediaSize: Long = 0,
    mediaDuration: Int = 0,
    isUploading: Boolean = false,
    uploadProgress: Float = 0f,
    onCancelUpload: (() -> Unit)? = null,
    onImageClick: (() -> Unit)? = null
) {
    val bubbleColor = if (isMine) ToDusColors.White else ToDusColors.Red
    val textColor = if (isMine) Color.Black else Color.White
    val borderColor = if (isMine) MaterialTheme.colorScheme.outline.copy(alpha = 0.15f) else Color.Transparent
    val alignment = if (isMine) Alignment.End else Alignment.Start
    val shape = if (isMine) RoundedCornerShape(12.dp, 4.dp, 12.dp, 12.dp) else RoundedCornerShape(4.dp, 12.dp, 12.dp, 12.dp)

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(bubbleColor)
                .then(if (isMine) Modifier.border(1.dp, borderColor, shape) else Modifier)
                .padding(if (mediaType == "text") 8.dp else 4.dp)
        ) {
            Column {
                when {
                    // ⭐ IMAGEN: sin título, con progreso circular + X en el centro
                    mediaType == "image" && mediaUrl != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp, max = 250.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            if (isUploading) {
                                // Estado de subida
                                Box(
                                    modifier = Modifier.fillMaxSize().background(Color.LightGray.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        // Progreso circular con X en el centro
                                        Box(contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(
                                                progress = uploadProgress,
                                                modifier = Modifier.size(48.dp),
                                                color = ToDusColors.Red,
                                                strokeWidth = 3.dp,
                                                trackColor = Color.White.copy(alpha = 0.5f)
                                            )
                                            IconButton(
                                                onClick = { onCancelUpload?.invoke() },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Close, "Cancelar", tint = Color.White, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        // Tamaño del archivo
                                        Text(
                                            formatSize(mediaSize),
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            } else {
                                // Imagen cargada
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current).data(mediaUrl).crossfade(300).build(),
                                    contentDescription = "Imagen",
                                    modifier = Modifier.fillMaxSize().then(if (onImageClick != null) Modifier.clickable { onImageClick() } else Modifier),
                                    contentScale = ContentScale.Crop,
                                    loading = {
                                        Box(modifier = Modifier.fillMaxSize().background(Color.LightGray.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = ToDusColors.Red, strokeWidth = 2.dp)
                                        }
                                    },
                                    error = {
                                        Box(modifier = Modifier.fillMaxSize().background(Color.LightGray.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.BrokenImage, "Error", tint = Color.Gray, modifier = Modifier.size(32.dp))
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // ⭐ VIDEO: duración en esquina inferior derecha, progreso circular + X en el centro
                    mediaType == "video" && mediaUrl != null -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp))
                        ) {
                            if (isUploading) {
                                Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(
                                                progress = uploadProgress,
                                                modifier = Modifier.size(48.dp),
                                                color = ToDusColors.Red,
                                                strokeWidth = 3.dp,
                                                trackColor = Color.White.copy(alpha = 0.5f)
                                            )
                                            IconButton(onClick = { onCancelUpload?.invoke() }, modifier = Modifier.size(24.dp)) {
                                                Icon(Icons.Default.Close, "Cancelar", tint = Color.White, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(formatSize(mediaSize), fontSize = 12.sp, color = Color.White)
                                    }
                                }
                            } else {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current).data(mediaUrl).crossfade(true).build(),
                                    contentDescription = "Video",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    loading = { Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray), contentAlignment = Alignment.Center) { CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp) } }
                                )
                                // Botón play centrado
                                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.6f)).align(Alignment.Center)) {
                                    Icon(Icons.Default.PlayArrow, "Reproducir", tint = Color.White, modifier = Modifier.fillMaxSize().padding(8.dp))
                                }
                                // ⭐ Duración en esquina inferior derecha
                                Box(modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).clip(RoundedCornerShape(4.dp)).background(Color.Black.copy(alpha = 0.7f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                    Text(formatDuration(mediaDuration), fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    // ⭐ AUDIO: muestra título y duración, sin thumbnail
                    (mediaType == "audio" || mediaType == "voice") -> {
                        Row(modifier = Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (isUploading) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(36.dp)) {
                                    CircularProgressIndicator(progress = uploadProgress, modifier = Modifier.size(36.dp), color = if (isMine) ToDusColors.Red else Color.White, strokeWidth = 2.dp)
                                    Icon(Icons.Default.Close, "Cancelar", tint = if (isMine) ToDusColors.Red else Color.White, modifier = Modifier.size(14.dp))
                                }
                            } else {
                                IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Default.PlayArrow, "Reproducir", tint = if (isMine) ToDusColors.Red else Color.White, modifier = Modifier.size(24.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text.ifEmpty { "Audio" }, color = textColor, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(formatDuration(mediaDuration), color = textColor.copy(alpha = 0.6f), fontSize = 11.sp)
                            }
                            Icon(Icons.Default.Mic, "Audio", tint = textColor.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
                        }
                    }

                    // ⭐ ARCHIVO: muestra título y tamaño
                    mediaType == "file" -> {
                        Row(modifier = Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (isUploading) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(36.dp)) {
                                    CircularProgressIndicator(progress = uploadProgress, modifier = Modifier.size(36.dp), color = if (isMine) ToDusColors.Red else Color.White, strokeWidth = 2.dp)
                                    Icon(Icons.Default.Close, "Cancelar", tint = if (isMine) ToDusColors.Red else Color.White, modifier = Modifier.size(14.dp))
                                }
                            } else {
                                Icon(Icons.Default.InsertDriveFile, "Archivo", tint = if (isMine) ToDusColors.Red else Color.White, modifier = Modifier.size(36.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text.ifEmpty { "Archivo" }, color = textColor, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(formatSize(mediaSize), color = textColor.copy(alpha = 0.6f), fontSize = 11.sp)
                            }
                        }
                    }

                    // ⭐ TEXTO normal
                    else -> {
                        if (text.isNotEmpty()) Text(text = text, color = textColor, fontSize = 15.sp, lineHeight = 20.sp)
                    }
                }

                // Hora y checks (siempre visibles si hay contenido)
                if (text.isNotEmpty() || mediaType != "text") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(time)), fontSize = 11.sp, color = if (isMine) Color.Gray else Color.White.copy(alpha = 0.7f))
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
}

fun formatSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    bytes < 1024 * 1024 * 1024 -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
    else -> "${"%.1f".format(bytes / (1024.0 * 1024.0 * 1024.0))} GB"
}

fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "${mins}:${secs.toString().padStart(2, '0')}"
}
