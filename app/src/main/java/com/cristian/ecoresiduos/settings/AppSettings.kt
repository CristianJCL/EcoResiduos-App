package com.cristian.ecoresiduos.settings

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cristian.ecoresiduos.data.WasteType

/** Opciones sencillas que se conservan después de cerrar EcoResiduos. */
class AppSettings(context: Context) {
    private val prefs = context.getSharedPreferences("ecoresiduos_ajustes", Context.MODE_PRIVATE)

    var theme by mutableStateOf(prefs.getString("tema", "system") ?: "system")
        private set
    var confirmDelete by mutableStateOf(prefs.getBoolean("confirmar", true))
        private set
    var defaultType by mutableStateOf(
        runCatching { WasteType.valueOf(prefs.getString("tipo", "FOOD") ?: "FOOD") }
            .getOrDefault(WasteType.FOOD)
    )
        private set
    var dailyReminder by mutableStateOf(prefs.getBoolean("aviso", false))
        private set
    var reminderHour by mutableStateOf(prefs.getInt("hora", 18))
        private set
    var reminderMinute by mutableStateOf(prefs.getInt("minuto", 0))
        private set

    fun updateTheme(value: String) {
        if (value !in listOf("system", "light", "dark")) return
        theme = value
        prefs.edit().putString("tema", value).apply()
    }
    fun updateConfirmDelete(value: Boolean) {
        confirmDelete = value
        prefs.edit().putBoolean("confirmar", value).apply()
    }
    fun updateDefaultType(value: WasteType) {
        defaultType = value
        prefs.edit().putString("tipo", value.name).apply()
    }
    fun setReminder(value: Boolean, hour: Int = reminderHour, minute: Int = reminderMinute) {
        dailyReminder = value
        reminderHour = hour
        reminderMinute = minute
        prefs.edit().putBoolean("aviso", value).putInt("hora", hour)
            .putInt("minuto", minute).apply()
    }
}
