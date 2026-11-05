package com.cristian.ecoresiduos.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.cristian.ecoresiduos.R
import com.cristian.ecoresiduos.data.WasteType
import com.cristian.ecoresiduos.settings.AppSettings
import com.cristian.ecoresiduos.settings.ReminderScheduler

/** Preferencias reales: tema, avisos, archivos y seguridad de registros. */
@Composable
fun SettingsScreen(
    settings: AppSettings,
    message: String,
    onExport: (android.net.Uri) -> Unit,
    onImport: (android.net.Uri) -> Unit,
    onClearAll: () -> Unit,
    bottomPadding: Dp
) {
    val context = LocalContext.current
    var firstWarning by remember { mutableStateOf(false) }
    var finalWarning by remember { mutableStateOf(false) }
    val saveFile = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri -> if (uri != null) onExport(uri) }
    val openFile = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) onImport(uri) }
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        settings.setReminder(granted)
        ReminderScheduler.update(context, granted, settings.reminderHour, settings.reminderMinute)
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = bottomPadding + 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium)

        Text("Apariencia", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("light" to "Claro", "dark" to "Oscuro", "system" to "Sistema").forEach { (value, label) ->
                FilterChip(selected = settings.theme == value, onClick = { settings.updateTheme(value) },
                    label = { Text(label) })
            }
        }

        HorizontalDivider()
        Text("Registros", style = MaterialTheme.typography.titleMedium)
        SettingToggle("Confirmar antes de eliminar", settings.confirmDelete) {
            settings.updateConfirmDelete(it)
        }
        Text("Tipo predeterminado al crear un registro")
        WasteType.entries.forEach { type ->
            val label = when (type) {
                WasteType.FOOD -> "Restos de comida"
                WasteType.FRUIT_VEGETABLE -> "Frutas y verduras"
                WasteType.GARDEN -> "Jardinería"
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = settings.defaultType == type,
                    onClick = { settings.updateDefaultType(type) })
                Text(label)
            }
        }

        HorizontalDivider()
        Text("Recordatorio diario", style = MaterialTheme.typography.titleMedium)
        SettingToggle("Recordarme registrar los residuos", settings.dailyReminder) { enable ->
            if (enable && Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            else {
                settings.setReminder(enable)
                ReminderScheduler.update(context, enable, settings.reminderHour, settings.reminderMinute)
            }
        }
        if (settings.dailyReminder) {
            OutlinedButton(onClick = {
                TimePickerDialog(context, { _, hour, minute ->
                    settings.setReminder(true, hour, minute)
                    ReminderScheduler.update(context, true, hour, minute)
                }, settings.reminderHour, settings.reminderMinute, true).show()
            }) {
                Text("Hora: %02d:%02d".format(settings.reminderHour, settings.reminderMinute))
            }
            Text("Android puede mostrar el aviso unos minutos después de la hora elegida.",
                style = MaterialTheme.typography.bodySmall)
        }

        HorizontalDivider()
        Text("Datos y almacenamiento", style = MaterialTheme.typography.titleMedium)
        Button(onClick = { saveFile.launch("EcoResiduos_registros.csv") }) {
            Text("Exportar registros a CSV")
        }
        OutlinedButton(onClick = {
            openFile.launch(arrayOf("text/*", "text/csv", "application/csv", "application/octet-stream"))
        }) { Text("Importar registros de CSV") }
        Text("Puedes abrir el CSV en Excel y recuperar tus datos cuando lo necesites.",
            style = MaterialTheme.typography.bodySmall)
        if (message.isNotBlank()) Text(message, color = MaterialTheme.colorScheme.primary)
        TextButton(onClick = { firstWarning = true }) { Text("Eliminar todos los registros") }

        HorizontalDivider()
        Text("Acerca de EcoResiduos", style = MaterialTheme.typography.titleMedium)
        Text("Versión 1.0")
        Text("Control de residuos orgánicos para escuelas y pequeños negocios.")
        Text("Los datos locales se almacenan en tu dispositivo.",
            style = MaterialTheme.typography.bodySmall)
    }

    if (firstWarning) AlertDialog(
        onDismissRequest = { firstWarning = false },
        title = { Text("¿Eliminar todos los registros?") },
        text = { Text("Se borrarán todos los residuos almacenados en este teléfono.") },
        confirmButton = { TextButton(onClick = {
            firstWarning = false
            finalWarning = true
        }) { Text("Continuar") } },
        dismissButton = { TextButton(onClick = { firstWarning = false }) { Text("Cancelar") } }
    )
    if (finalWarning) AlertDialog(
        onDismissRequest = { finalWarning = false },
        title = { Text("Confirmación final") },
        text = { Text("Esta acción no se puede deshacer. Puedes exportar un CSV antes de continuar.") },
        confirmButton = { TextButton(onClick = {
            finalWarning = false
            onClearAll()
        }) { Text("Borrar definitivamente") } },
        dismissButton = { TextButton(onClick = { finalWarning = false }) { Text("Cancelar") } }
    )
}

@Composable
private fun SettingToggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
