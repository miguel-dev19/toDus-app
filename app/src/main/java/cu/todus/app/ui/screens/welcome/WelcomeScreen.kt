package cu.todus.app.ui.screens.welcome

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cu.todus.app.ui.theme.ToDusColors
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(onContinue: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(300); visible = true }

    Box(modifier = Modifier.fillMaxSize().background(
        Brush.verticalGradient(colors = listOf(ToDusColors.Red, ToDusColors.RedLogo))
    )) {
        Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("toDus", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Mensajería instantánea\nsimple, rápida y segura", fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center, lineHeight = 24.sp)
                    Spacer(modifier = Modifier.height(64.dp))
                    Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White)) {
                        Text("Comenzar", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ToDusColors.Red)
                    }
                }
            }
        }
    }
}
