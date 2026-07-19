package cu.todus.app.ui.screens.welcome

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cu.todus.app.R
import cu.todus.app.ui.theme.ToDusColors
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(onContinue: () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Logo de la app
                    Image(
                        painter = painterResource(id = R.drawable.todus_logo),
                        contentDescription = "toDus",
                        modifier = Modifier.size(120.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Título
                    Text(
                        "¡Bienvenido a toDus!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = ToDusColors.Red,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Subtítulo
                    Text(
                        "Una aplicación de mensajería\npensada para ti.",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(64.dp))

                    // Botón
                    Button(
                        onClick = onContinue,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ToDusColors.Red)
                    ) {
                        Text(
                            "Comenzar",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Términos y condiciones
                    Text(
                        "Al continuar aceptas nuestros\nTérminos y Condiciones de uso de la plataforma.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
