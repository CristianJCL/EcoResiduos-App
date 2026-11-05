package com.cristian.ecoresiduos.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cristian.ecoresiduos.R
import com.cristian.ecoresiduos.network.RemoteWaste
import java.util.Locale

/** Acciones de respaldo para cualquier usuario, sin pedir configuración técnica. */
@Composable
fun NetworkScreen(
    onlineAvailable: Boolean, userEmail: String, connected: Boolean,
    localPending: Int, backupCount: Int?, items: List<RemoteWaste>,
    message: String, fileMessage: String, loading: Boolean,
    onExport: (Uri) -> Unit, onImport: (Uri) -> Unit,
    onSignUp: (String, String) -> Unit, onLogin: (String, String) -> Unit,
    onLogout: () -> Unit, onSync: () -> Unit, onList: () -> Unit,
    onDetail: (String) -> Unit, onRestore: () -> Unit,
    onDelete: (String) -> Unit, bottomPadding: Dp
) {
    var email by remember(userEmail) { mutableStateOf(userEmail) }
    var password by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<String?>(null) }

    val saveFile = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri -> if (uri != null) onExport(uri) }
    val openFile = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) onImport(uri) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = bottomPadding + 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.cloud_title), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.backup_intro), style = MaterialTheme.typography.bodyMedium)

        Text(stringResource(R.string.backup_file_section), style = MaterialTheme.typography.titleMedium)
        Button(onClick = { saveFile.launch("Mis_residuos.csv") }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.backup_save_file))
        }
        OutlinedButton(onClick = {
            openFile.launch(arrayOf("text/*", "text/csv", "application/csv", "application/octet-stream"))
        }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.backup_restore_file))
        }
        if (fileMessage.isNotBlank()) Text(fileMessage, color = MaterialTheme.colorScheme.primary)

        // Cuando hay un servidor configurado en el APK, aparece la cuenta.
        if (onlineAvailable) {
            HorizontalDivider()
            if (!connected) {
                Text(stringResource(R.string.cloud_account_title), style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(value = email, onValueChange = { email = it },
                    label = { Text(stringResource(R.string.cloud_email)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = password, onValueChange = { password = it },
                    label = { Text(stringResource(R.string.cloud_password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                val valid = !loading && email.contains('@') && email.contains('.') && password.length >= 6
                Button(onClick = { onLogin(email, password) }, enabled = valid,
                    modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.cloud_login))
                }
                OutlinedButton(onClick = { onSignUp(email, password) }, enabled = valid,
                    modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.cloud_signup))
                }
            } else {
                Text(stringResource(R.string.cloud_connected), style = MaterialTheme.typography.titleMedium)
                Text(userEmail)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    CloudCount(stringResource(R.string.cloud_pending), localPending.toString(), Modifier.weight(1f))
                    CloudCount(stringResource(R.string.cloud_backups), backupCount?.toString() ?: "—", Modifier.weight(1f))
                }
                Button(onClick = onSync, enabled = !loading,
                    modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.cloud_sync)) }
                OutlinedButton(onClick = onList, enabled = !loading,
                    modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.cloud_view)) }
                OutlinedButton(onClick = onRestore, enabled = !loading,
                    modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.cloud_restore)) }
                TextButton(onClick = onLogout, enabled = !loading) {
                    Text(stringResource(R.string.cloud_logout))
                }
                if (items.isNotEmpty()) {
                    Text(stringResource(R.string.cloud_backups), style = MaterialTheme.typography.titleMedium)
                    items.forEach { item ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(item.source, style = MaterialTheme.typography.titleSmall)
                                Text(String.format(Locale.getDefault(), "%.1f kg · %s · %s",
                                    item.weightKg, item.date, item.time))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(onClick = { onDetail(item.id) }, enabled = !loading) {
                                        Text(stringResource(R.string.cloud_detail))
                                    }
                                    TextButton(onClick = { pendingDelete = item.id }, enabled = !loading) {
                                        Text(stringResource(R.string.cloud_delete))
                                    }
                                }
                            }
                        }
                    }
                } else if (backupCount == 0) {
                    Text(stringResource(R.string.cloud_empty))
                }
            }
            if (loading) CircularProgressIndicator()
            if (message.isNotBlank()) Text(message, style = MaterialTheme.typography.bodyMedium)
        }
    }

    if (pendingDelete != null) AlertDialog(
        onDismissRequest = { pendingDelete = null },
        title = { Text(stringResource(R.string.cloud_confirm_title)) },
        text = { Text(stringResource(R.string.cloud_confirm_body)) },
        confirmButton = {
            TextButton(onClick = { pendingDelete?.let(onDelete); pendingDelete = null }) {
                Text(stringResource(R.string.action_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = { pendingDelete = null }) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun CloudCount(title: String, value: String, modifier: Modifier) {
    Card(modifier) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}
