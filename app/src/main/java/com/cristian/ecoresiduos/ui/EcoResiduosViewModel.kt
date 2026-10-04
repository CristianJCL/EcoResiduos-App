package com.cristian.ecoresiduos.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cristian.ecoresiduos.data.WasteRecord
import com.cristian.ecoresiduos.data.WasteRepository
import com.cristian.ecoresiduos.data.WasteType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Estado editable del formulario de nuevo registro.
 */
data class WasteFormState(
    val weight: String = "",
    val source: String = "",
    val notes: String = "",
    val time: String = "12:00",
    val type: WasteType = WasteType.FOOD,
    val composted: Boolean = false
) {
    /** El formulario se considera válido cuando existe origen y peso positivo. */
    val isValid: Boolean
        get() = source.isNotBlank() && (weight.toDoubleOrNull() ?: 0.0) > 0.0
}

/**
 * ViewModel compartido por las pantallas de EcoResiduos.
 *
 * Centraliza el estado de los registros y del formulario, mientras el Repository
 * se encarga de las operaciones de datos.
 */
class EcoResiduosViewModel(
    private val repository: WasteRepository
) : ViewModel() {

    private val _records = mutableStateListOf<WasteRecord>()

    /** Lista observable que consume la UI. */
    val records: List<WasteRecord>
        get() = _records

    /** Estado actual del formulario. */
    var formState by mutableStateOf(WasteFormState())
        private set

    init {
        _records.addAll(repository.getAll())
    }

    fun updateWeight(value: String) {
        formState = formState.copy(weight = value)
    }

    fun updateSource(value: String) {
        formState = formState.copy(source = value)
    }

    fun updateNotes(value: String) {
        formState = formState.copy(notes = value)
    }

    fun updateTime(value: String) {
        formState = formState.copy(time = value)
    }

    fun updateType(value: WasteType) {
        formState = formState.copy(type = value)
    }

    fun updateComposted(value: Boolean) {
        formState = formState.copy(composted = value)
    }

    /**
     * Guarda un registro nuevo y reinicia el formulario.
     *
     * @return true si el registro era válido y se almacenó.
     */
    fun saveRecord(): Boolean {
        if (!formState.isValid) return false

        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val record = WasteRecord(
            id = System.currentTimeMillis(),
            type = formState.type,
            weightKg = formState.weight.toDouble(),
            source = formState.source.trim(),
            date = formatter.format(Date()),
            time = formState.time,
            composted = formState.composted,
            notes = formState.notes.trim()
        )

        repository.add(record)
        _records.add(0, record)
        formState = WasteFormState()
        return true
    }

    fun deleteRecord(id: Long) {
        repository.delete(id)
        _records.removeAll { it.id == id }
    }

    fun resetForm() {
        formState = WasteFormState()
    }

    companion object {
        /** Fábrica simple para entregar el Repository al ViewModel. */
        fun factory(repository: WasteRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return EcoResiduosViewModel(repository) as T
                }
            }
    }
}
