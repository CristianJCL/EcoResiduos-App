package com.cristian.ecoresiduos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cristian.ecoresiduos.R
import com.cristian.ecoresiduos.network.RemotePost

/** Permite ejecutar y ver las respuestas de la API de prueba. */
@Composable
fun NetworkScreen(
    items: List<RemotePost>,
    message: String,
    loading: Boolean,
    onList: () -> Unit,
    onDetail: () -> Unit,
    onSend: () -> Unit,
    onUpdate: () -> Unit,
    onDelete: () -> Unit,
    bottomPadding: Dp
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = bottomPadding + 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.network_title), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.network_notice), style = MaterialTheme.typography.bodyMedium)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onList, enabled = !loading) {
                Text(stringResource(R.string.network_list))
            }
            OutlinedButton(onClick = onDetail, enabled = !loading) {
                Text(stringResource(R.string.network_detail))
            }
        }
        Button(onClick = onSend, enabled = !loading) {
            Text(stringResource(R.string.network_send))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onUpdate, enabled = !loading) {
                Text(stringResource(R.string.network_update))
            }
            OutlinedButton(onClick = onDelete, enabled = !loading) {
                Text(stringResource(R.string.network_delete))
            }
        }

        if (loading) CircularProgressIndicator()
        if (message.isNotBlank()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(message, modifier = Modifier.padding(16.dp))
            }
        }
        items.forEach { post ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("ID ${post.id}", style = MaterialTheme.typography.titleSmall)
                    Text(post.title, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
