package com.cristian.ecoresiduos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cristian.ecoresiduos.R
import com.cristian.ecoresiduos.data.WasteRecord
import com.cristian.ecoresiduos.data.WasteType

/**
 * Lista desplazable de registros capturados por el usuario.
 */
@Composable
fun RecordsScreen(
    records: List<WasteRecord>,
    onDelete: (Long) -> Unit,
    bottomPadding: Dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 20.dp)
    ) {
        Text(
            text = stringResource(R.string.records_title),
            style = MaterialTheme.typography.headlineMedium
        )

        if (records.isEmpty()) {
            Text(
                text = stringResource(R.string.records_empty),
                modifier = Modifier.padding(top = 24.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 12.dp,
                    bottom = bottomPadding + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = records,
                    key = { it.id }
                ) { record ->
                    RecordCard(
                        record = record,
                        onDelete = { onDelete(record.id) }
                    )
                }
            }
        }
    }
}

/** Tarjeta con la información principal de un registro. */
@Composable
private fun RecordCard(
    record: WasteRecord,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = wasteTypeLabel(record.type),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(stringResource(R.string.record_weight, record.weightKg))
                Text(stringResource(R.string.record_source, record.source))
                Text(stringResource(R.string.record_date_time, record.date, record.time))
                Text(
                    text = if (record.composted) {
                        stringResource(R.string.record_composted_yes)
                    } else {
                        stringResource(R.string.record_composted_no)
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.action_delete)
                )
            }
        }
    }
}

/** Convierte el valor de dominio a un texto traducible para la interfaz. */
@Composable
private fun wasteTypeLabel(type: WasteType): String = when (type) {
    WasteType.FOOD -> stringResource(R.string.type_food)
    WasteType.FRUIT_VEGETABLE -> stringResource(R.string.type_fruit_vegetable)
    WasteType.GARDEN -> stringResource(R.string.type_garden)
}
