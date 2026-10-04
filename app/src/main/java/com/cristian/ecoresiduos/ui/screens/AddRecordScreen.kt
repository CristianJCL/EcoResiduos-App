package com.cristian.ecoresiduos.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cristian.ecoresiduos.R
import com.cristian.ecoresiduos.data.WasteType
import com.cristian.ecoresiduos.ui.WasteFormState
import java.util.Calendar

/**
 * Formulario funcional para capturar un nuevo residuo orgánico.
 *
 * Incluye cajas de texto, radios, checkbox, botones y selector de hora.
 */
@Composable
fun AddRecordScreen(
    state: WasteFormState,
    onWeightChange: (String) -> Unit,
    onSourceChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onTypeChange: (WasteType) -> Unit,
    onCompostedChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    bottomPadding: Dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = bottomPadding + 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = stringResource(R.string.add_title),
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = state.weight,
            onValueChange = onWeightChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.field_weight)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        OutlinedTextField(
            value = state.source,
            onValueChange = onSourceChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.field_source)) },
            singleLine = true
        )

        Text(
            text = stringResource(R.string.field_type),
            style = MaterialTheme.typography.titleMedium
        )

        WasteType.entries.forEach { type ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = state.type == type,
                    onClick = { onTypeChange(type) }
                )
                Text(text = wasteTypeLabel(type))
            }
        }

        OutlinedButton(
            onClick = {
                val calendar = Calendar.getInstance()
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        onTimeChange(String.format("%02d:%02d", hour, minute))
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            }
        ) {
            Text(stringResource(R.string.action_select_time, state.time))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = state.composted,
                onCheckedChange = onCompostedChange
            )
            Text(stringResource(R.string.field_composted))
        }

        OutlinedTextField(
            value = state.notes,
            onValueChange = onNotesChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.field_notes)) },
            minLines = 3
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(onClick = onCancel) {
                Text(stringResource(R.string.action_cancel))
            }

            Button(
                onClick = onSave,
                enabled = state.isValid,
                modifier = Modifier.padding(start = 10.dp)
            ) {
                Text(stringResource(R.string.action_save))
            }
        }
    }
}

/** Devuelve la etiqueta traducida de cada tipo de residuo. */
@Composable
private fun wasteTypeLabel(type: WasteType): String = when (type) {
    WasteType.FOOD -> stringResource(R.string.type_food)
    WasteType.FRUIT_VEGETABLE -> stringResource(R.string.type_fruit_vegetable)
    WasteType.GARDEN -> stringResource(R.string.type_garden)
}
