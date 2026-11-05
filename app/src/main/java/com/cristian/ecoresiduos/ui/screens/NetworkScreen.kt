package com.cristian.ecoresiduos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cristian.ecoresiduos.network.RemoteWaste

/** Respaldos reales: sin contenido ficticio de JSONPlaceholder. */
@Composable
fun NetworkScreen(
    server: String, publicKey: String, userEmail: String, connected: Boolean,
    localPending: Int, items: List<RemoteWaste>, message: String, loading: Boolean,
    onConfigure: (String, String) -> Unit, onSignUp: (String, String) -> Unit,
    onLogin: (String, String) -> Unit, onLogout: () -> Unit,
    onSync: () -> Unit, onList: () -> Unit, onDetail: (String) -> Unit,
    onRestore: () -> Unit, onDelete: (String) -> Unit, bottomPadding: Dp
) {
    var url by remember(server) { mutableStateOf(server) }
    var key by remember(publicKey) { mutableStateOf(publicKey) }
    var email by remember(userEmail) { mutableStateOf(userEmail) }
    var password by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = bottomPadding + 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Sincronización", style = MaterialTheme.typography.headlineMedium)
        Text("Guarda residuos sin Internet. Cuando tengas conexión, respáldalos en Supabase.")
        if (!connected) {
            Text("Conectar la nube", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(value = url, onValueChange = { url = it },
                label = { Text("URL del proyecto Supabase") }, singleLine = true,
                modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = key, onValueChange = { key = it },
                label = { Text("Clave pública (anon o publishable)") }, singleLine = true,
                modifier = Modifier.fillMaxWidth())
            Button(enabled = !loading, onClick = { onConfigure(url, key) }) {
                Text("Guardar conexión")
            }
            HorizontalDivider()
            OutlinedTextField(value = email, onValueChange = { email = it },
                label = { Text("Correo electrónico") }, singleLine = true,
                modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = password, onValueChange = { password = it },
                visualTransformation = PasswordVisualTransformation(),
                label = { Text("Contraseña") }, singleLine = true,
                modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onLogin(email, password) }, enabled = !loading) {
                    Text("Iniciar sesión")
                }
                OutlinedButton(onClick = { onSignUp(email, password) }, enabled = !loading) {
                    Text("Crear cuenta")
                }
            }
            Text("La tabla debe estar creada en Supabase. No uses la clave secreta service_role.",
                style = MaterialTheme.typography.bodySmall)
        } else {
            Text("Cuenta: " + userEmail, style = MaterialTheme.typography.titleSmall)
            Text("Registros pendientes: " + localPending)
            Text("Respaldos encontrados: " + items.size)
            Button(onClick = onSync, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
                Text("Sincronizar ahora")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onList, enabled = !loading) { Text("Ver respaldos") }
                OutlinedButton(onClick = onRestore, enabled = !loading) { Text("Recuperar registros") }
            }
            TextButton(onClick = onLogout) { Text("Cerrar sesión") }
            HorizontalDivider()
            Text("Registros de la nube", style = MaterialTheme.typography.titleMedium)
            items.forEach { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(item.source + " · " + item.weightKg + " kg",
                            style = MaterialTheme.typography.titleSmall)
                        Text(item.date + " · " + item.time)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { onDetail(item.id) }) { Text("Ver detalle") }
                            TextButton(onClick = { pendingDelete = item.id }) {
                                Text("Eliminar respaldo")
                            }
                        }
                    }
                }
            }
        }
        if (loading) CircularProgressIndicator()
        if (message.isNotBlank()) Card {
            Text(message, modifier = Modifier.padding(12.dp))
        }
    }
    if (pendingDelete != null) AlertDialog(
        onDismissRequest = { pendingDelete = null },
        title = { Text("¿Eliminar el respaldo?") },
        text = { Text("Se borrará de Internet, pero no del teléfono.") },
        confirmButton = {
            TextButton(onClick = {
                pendingDelete?.let(onDelete)
                pendingDelete = null
            }) { Text("Eliminar") }
        },
        dismissButton = {
            TextButton(onClick = { pendingDelete = null }) { Text("Cancelar") }
        }
    )
}
