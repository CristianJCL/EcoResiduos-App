package com.cristian.ecoresiduos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cristian.ecoresiduos.R
import com.cristian.ecoresiduos.network.RemoteWaste
import java.util.Locale

/** Pantalla para consultar, guardar y recuperar los registros del usuario. */
@Composable
fun NetworkScreen(
    server: String, publicKey: String, userEmail: String, connected: Boolean,
    localPending: Int, backupCount: Int?, items: List<RemoteWaste>,
    message: String, loading: Boolean,
    onConfigure: (String, String) -> Unit, onSignUp: (String, String) -> Unit,
    onLogin: (String, String) -> Unit, onLogout: () -> Unit,
    onSync: () -> Unit, onList: () -> Unit, onDetail: (String) -> Unit,
    onRestore: () -> Unit, onDelete: (String) -> Unit, bottomPadding: Dp
) {
    var url by remember(server) { mutableStateOf(server) }
    var key by remember(publicKey) { mutableStateOf(publicKey) }
    var email by remember(userEmail) { mutableStateOf(userEmail) }
    var password by remember { mutableStateOf("") }
    var showAdvanced by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<String?>(null) }
    val serviceReady = server.startsWith("https://") && publicKey.isNotBlank()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = bottomPadding + 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.cloud_title), style = MaterialTheme.typography.headlineMedium)

        if (!connected) {
            Text(stringResource(R.string.cloud_account_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.cloud_account_description), style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text(stringResource(R.string.cloud_email)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text(stringResource(R.string.cloud_password)) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            val canSubmit = serviceReady && email.isNotBlank() && password.isNotBlank() && !loading
            Button(onClick = { onLogin(email, password) }, enabled = canSubmit,
                modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.cloud_login))
            }
            OutlinedButton(onClick = { onSignUp(email, password) }, enabled = canSubmit,
                modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.cloud_signup))
            }
            if (!serviceReady) {
                Text(stringResource(R.string.cloud_setup_needed),
                    style = MaterialTheme.typography.bodySmall)
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.cloud_connected), style = MaterialTheme.typography.titleMedium)
                    Text(userEmail, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                CloudCount(stringResource(R.string.cloud_pending), localPending.toString(), Modifier.weight(1f))
                CloudCount(stringResource(R.string.cloud_backups), backupCount?.toString() ?: "—", Modifier.weight(1f))
            }
            Button(onClick = onSync, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.cloud_sync))
            }
            OutlinedButton(onClick = onList, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.cloud_view))
            }
            OutlinedButton(onClick = onRestore, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.cloud_restore))
            }
            TextButton(onClick = onLogout, enabled = !loading) {
                Text(stringResource(R.string.cloud_logout))
            }
            if (items.isNotEmpty()) {
                HorizontalDivider()
                Text(stringResource(R.string.cloud_backups), style = MaterialTheme.typography.titleMedium)
                items.forEach { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                Text(stringResource(R.string.cloud_empty), style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (loading) CircularProgressIndicator()
        if (message.isNotBlank()) Text(message, style = MaterialTheme.typography.bodyMedium)

        HorizontalDivider()
        TextButton(onClick = { showAdvanced = !showAdvanced }) {
            Text(stringResource(if (showAdvanced) R.string.cloud_hide_options else R.string.cloud_advanced))
        }
        if (showAdvanced) {
            Text(stringResource(R.string.cloud_service_settings), style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(value = url, onValueChange = { url = it },
                label = { Text(stringResource(R.string.cloud_url)) }, singleLine = true,
                modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = key, onValueChange = { key = it },
                label = { Text(stringResource(R.string.cloud_key)) }, singleLine = true,
                modifier = Modifier.fillMaxWidth())
            OutlinedButton(onClick = { onConfigure(url, key) }, enabled = !loading) {
                Text(stringResource(R.string.cloud_save_settings))
            }
        }
    }

    if (pendingDelete != null) AlertDialog(
        onDismissRequest = { pendingDelete = null },
        title = { Text(stringResource(R.string.cloud_confirm_title)) },
        text = { Text(stringResource(R.string.cloud_confirm_body)) },
        confirmButton = {
            TextButton(onClick = {
                pendingDelete?.let(onDelete)
                pendingDelete = null
            }) { Text(stringResource(R.string.action_delete)) }
        },
        dismissButton = {
            TextButton(onClick = { pendingDelete = null }) { Text(stringResource(R.string.action_cancel)) }
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
