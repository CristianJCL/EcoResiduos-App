package com.cristian.ecoresiduos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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

/**
 * Pantalla de inicio con un resumen rápido de la actividad registrada.
 */
@Composable
fun HomeScreen(
    records: List<WasteRecord>,
    onAddClick: () -> Unit,
    bottomPadding: Dp,
    modifier: Modifier = Modifier
) {
    val totalWeight = records.sumOf { it.weightKg }
    val compostedCount = records.count { it.composted }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = bottomPadding + 16.dp)
            .widthIn(max = 900.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = stringResource(R.string.home_subtitle),
            style = MaterialTheme.typography.bodyLarge
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                title = stringResource(R.string.summary_records),
                value = records.size.toString(),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = stringResource(R.string.summary_weight),
                value = stringResource(R.string.kg_value, totalWeight),
                modifier = Modifier.weight(1f)
            )
        }

        SummaryCard(
            title = stringResource(R.string.summary_composted),
            value = compostedCount.toString(),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onAddClick,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(stringResource(R.string.action_new_record))
        }
    }
}

/** Tarjeta reutilizable para mostrar una cifra del resumen. */
@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.labelLarge)
            Text(text = value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}
