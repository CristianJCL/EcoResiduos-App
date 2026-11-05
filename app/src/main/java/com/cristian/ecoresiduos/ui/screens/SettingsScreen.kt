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

        Text(stringResource(R.string.settings_appearance), style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("light" to "Claro", "dark" to "Oscuro", "system" to "Sistema").forEach { (value, label) ->
                FilterChip(selected = settings.theme == value, onClick = { settings.updateTheme(value) },
                    label = { Text(label) })
            }
        }

        HorizontalDivider()
        Text(stringResource(R.string.settings_records_section), style = MaterialTheme.typography.titleMedium)
        SettingToggle(stringResource(R.string.settings_confirm_delete), settings.confirmDelete) {
            settings.updateConfirmDelete(it)
        }
        Text(stringResource(R.string.settings_default_type))
        WasteType.entries.forEach { type ->
            val label = when (type) {
                WasteType.FOOD -> stringResource(R.string.type_food)
                WasteType.FRUIT_VEGETABLE -> stringResource(R.string.type_fruit_vegetable)
                WasteType.GARDEN -> stringResource(R.string.type_garden)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = settings.defaultType == type,
                    onClick = { settings.updateDefaultType(type) })
                Text(label)
            }
        }

        HorizontalDivider()
        Text(stringResource(R.string.settings_notifications), style = MaterialTheme.typography.titleMedium)
        SettingToggle(stringResource(R.string.settings_daily_reminder), settings.dailyReminder) { enable ->
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
            Text(stringResource(R.string.settings_approx_time),
                style = MaterialTheme.typography.bodySmall)
        }

        HorizontalDivider()
        Text(stringResource(R.string.settings_data_section), style = MaterialTheme.typography.titleMedium)
        Button(onClick = { saveFile.launch("EcoResiduos_registros.csv") }) {
            Text(stringResource(R.string.settings_export))
        }
        OutlinedButton(onClick = {
            openFile.launch(arrayOf("text/*", "text/csv", "application/csv", "application/octet-stream"))
        }) { Text(stringResource(R.string.settings_import)) }
        Text(stringResource(R.string.settings_csv_info),
            style = MaterialTheme.typography.bodySmall)
        if (message.isNotBlank()) Text(message, color = MaterialTheme.colorScheme.primary)
        TextButton(onClick = { firstWarning = true }) { Text(stringResource(R.string.settings_clear_all)) }

        HorizontalDivider()
        Text(stringResource(R.string.settings_about), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.settings_version))
        Text(stringResource(R.string.settings_about_body))
        Text(stringResource(R.string.settings_storage_info),
            style = MaterialTheme.typography.bodySmall)
    }

    if (firstWarning) AlertDialog(
        onDismissRequest = { firstWarning = false },
        title = { Text(stringResource(R.string.settings_clear_title)) },
        text = { Text(stringResource(R.string.settings_clear_body)) },
        confirmButton = { TextButton(onClick = {
            firstWarning = false
            finalWarning = true
        }) { Text(stringResource(R.string.settings_continue)) } },
        dismissButton = { TextButton(onClick = { firstWarning = false }) { Text(stringResource(R.string.action_cancel)) } }
    )
    if (finalWarning) AlertDialog(
        onDismissRequest = { finalWarning = false },
        title = { Text(stringResource(R.string.settings_final_title)) },
        text = { Text(stringResource(R.string.settings_final_body)) },
        confirmButton = { TextButton(onClick = {
            finalWarning = false
            onClearAll()
        }) { Text(stringResource(R.string.settings_clear_permanent)) } },
        dismissButton = { TextButton(onClick = { finalWarning = false }) { Text(stringResource(R.string.action_cancel)) } }
    )
}

@Composable
private fun SettingToggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
