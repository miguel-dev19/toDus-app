package cu.todus.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
                    // ⭐ Imagen con placeholder y carga progresiva
                    (mediaType == "image" || mediaType == "sticker") && mediaUrl != null -> {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(mediaUrl)
                                .crossfade(300)
                                .build(),
                            contentDescription = "Imagen",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp, max = 250.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .then(if (onImageClick != null) Modifier.clickable { onImageClick() } else Modifier),
                            contentScale = ContentScale.Crop,
                            loading = {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(Color.LightGray.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = if (isMine) ToDusColors.Red else Color.White.copy(alpha = 0.7f),
                                        strokeWidth = 2.dp
                                    )
                                }
                            },
                            error = {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(Color.LightGray.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.BrokenImage,
                                        "Error",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        )
                    }
                    // ⭐ Video con miniatura y botón de play
                    mediaType == "video" && mediaUrl != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(mediaUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Video",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                loading = {
                                    Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                                    }
                                }
                            )
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .align(Alignment.Center)
                            ) {
                                Icon(Icons.Default.PlayArrow, "Reproducir", tint = Color.White, modifier = Modifier.fillMaxSize().padding(8.dp))
                            }
                        }
                    }
                    // ⭐ Audio / Nota de voz
                    mediaType == "audio" || mediaType == "voice" -> {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { /* Reproducir */ },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    "Reproducir",
                                    tint = if (isMine) ToDusColors.Red else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text.ifEmpty { "Mensaje de voz" },
                                    color = textColor,
                                    fontSize = 14.sp,
                                    maxLines = 1
                                )
                                Text(
                                    mediaUrl ?: "",
                                    color = textColor.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                Icons.Default.Mic,
                                "Voz",
                                tint = textColor.copy(alpha = 0.4f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    // ⭐ Documento
                    mediaType == "file" -> {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.InsertDriveFile,
                                "Archivo",
                                tint = if (isMine) ToDusColors.Red else Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text.ifEmpty { "Archivo" }, color = textColor, fontSize = 14.sp, maxLines = 1)
                                Text(mediaUrl ?: "", color = textColor.copy(alpha = 0.6f), fontSize = 11.sp, maxLines = 1)
                            }
                        }
                    }
                    // ⭐ Contacto compartido
                    mediaType == "contact" -> {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person,
                                "Contacto",
                                tint = if (isMine) ToDusColors.Red else Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text.ifEmpty { "Contacto" }, color = textColor, fontSize = 14.sp)
                        }
                    }
                    // ⭐ Texto normal
                    else -> {
                        if (text.isNotEmpty()) {
                            Text(text = text, color = textColor, fontSize = 15.sp, lineHeight = 20.sp)
                        }
                    }
                }

                // ⭐ Hora y checks (siempre visibles)
                if (text.isNotEmpty() || mediaType != "text") {
                    Spacer(modifier = Modifier.height(4.dp))
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
}
