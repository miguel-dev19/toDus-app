package cu.todus.app.ui.theme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ToDusColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4), onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF), secondary = Color(0xFF03DAC6),
    background = Color(0xFFFFFBFE), surface = Color(0xFFFFFBFE),
    surfaceVariant = Color(0xFFE7E0EC), error = ToDusColors.Error
)

@Composable
fun ToDusTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ToDusColorScheme, content = content)
}
