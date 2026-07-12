package cu.todus.app.ui.screens.phone
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cu.todus.app.ui.theme.ToDusColors

@Composable
fun PhoneInputScreen(onBack: () -> Unit, onContinue: (String) -> Unit) {
    var phone by remember { mutableStateOf("") }
    val isValid = phone.length == 8 && phone.startsWith("5")
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
            Spacer(modifier = Modifier.height(32.dp))
            Text("Ingresar numero", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Por favor, ingresa tu numero de telefono para continuar", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(32.dp))
            Row(modifier = Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(modifier = Modifier.width(100.dp).height(56.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("+53", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(value = phone, onValueChange = { if (it.length <= 8) phone = it.filter { c -> c.isDigit() } }, modifier = Modifier.weight(1f).focusRequester(focusRequester), textStyle = MaterialTheme.typography.titleMedium, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
            }
        }
        Column(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(24.dp, 32.dp)) {
            Button(onClick = { onContinue("+53$phone") }, modifier = Modifier.fillMaxWidth().height(52.dp), enabled = isValid, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = ToDusColors.Red)) {
                Text("Continuar", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
