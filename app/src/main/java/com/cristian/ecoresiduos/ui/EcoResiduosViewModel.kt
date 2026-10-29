package com.cristian.ecoresiduos.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cristian.ecoresiduos.data.WasteRecord
import com.cristian.ecoresiduos.data.WasteRepository
import com.cristian.ecoresiduos.data.WasteType
import com.cristian.ecoresiduos.network.NetworkWasteRepository
import com.cristian.ecoresiduos.network.RemotePost
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Valores que edita el usuario antes de guardar un residuo. */
data class WasteFormState(
    val weight: String = "",
    val source: String = "",
    val notes: String = "",
    val time: String = "12:00",
    val type: WasteType = WasteType.FOOD,
    val composted: Boolean = false
) {
    val isValid: Boolean
        get() = source.isNotBlank() && (weight.toDoubleOrNull() ?: 0.0) > 0.0
}

/**
 * Administra el formulario, los registros persistidos y la conexión HTTP.
 * El ViewModel se comunica exclusivamente con los Repository.
 */
class EcoResiduosViewModel(
    private val local: WasteRepository,
    private val remote: NetworkWasteRepository
) : ViewModel() {

    private val _records = mutableStateListOf<WasteRecord>()
    val records: List<WasteRecord> get() = _records

    var formState by mutableStateOf(WasteFormState())
        private set
    var editingId by mutableStateOf<Long?>(null)
        private set

    var remoteItems by mutableStateOf<List<RemotePost>>(emptyList())
        private set
    var networkMessage by mutableStateOf("")
        private set
    var networkLoading by mutableStateOf(false)
        private set

    init {
        // Flow notifica los cambios de SQLite para actualizar la lista y el resumen.
        viewModelScope.launch {
            local.observeAll().collect { items ->
                _records.clear()
                _records.addAll(items)
            }
        }
    }

    fun updateWeight(value: String) { formState = formState.copy(weight = value) }
    fun updateSource(value: String) { formState = formState.copy(source = value) }
    fun updateNotes(value: String) { formState = formState.copy(notes = value) }
    fun updateTime(value: String) { formState = formState.copy(time = value) }
    fun updateType(value: WasteType) { formState = formState.copy(type = value) }
    fun updateComposted(value: Boolean) { formState = formState.copy(composted = value) }

    /** Carga un registro existente en el formulario para modificarlo. */
    fun beginEdit(id: Long) {
        val record = records.firstOrNull { it.id == id } ?: return
        editingId = record.id
        formState = WasteFormState(
            record.weightKg.toString(), record.source, record.notes, record.time,
            record.type, record.composted
        )
    }

    /** Envía el registro a Room sin bloquear el hilo de interfaz. */
    fun saveRecord(): Boolean {
        if (!formState.isValid) return false
        val state = formState
        val id = editingId
        val previous = records.firstOrNull { it.id == id }
        val date = previous?.date ?:
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        val record = WasteRecord(
            id = id ?: 0,
            type = state.type,
            weightKg = state.weight.toDouble(),
            source = state.source.trim(),
            date = date,
            time = state.time,
            composted = state.composted,
            notes = state.notes.trim()
        )
        viewModelScope.launch { local.save(record) }
        resetForm()
        return true
    }

    /** Elimina el registro en SQLite y actualiza la lista mediante Flow. */
    fun deleteRecord(id: Long) {
        viewModelScope.launch { local.delete(id) }
    }

    fun resetForm() {
        editingId = null
        formState = WasteFormState()
    }

    private fun request(action: suspend () -> String) {
        viewModelScope.launch {
            networkLoading = true
            networkMessage = ""
            networkMessage = try { action() }
            catch (e: Exception) { "Error de conexión: ${e.message ?: "Sin respuesta"}" }
            networkLoading = false
        }
    }

    /** Recupera publicaciones de ejemplo de la API REST. */
    fun loadRemote() = request {
        remoteItems = remote.list()
        "GET: ${remoteItems.size} resultados recibidos"
    }

    fun readRemoteDetail() = request {
        val item = remote.detail(1)
        "GET /1: ${item.title}"
    }

    /** Envía por POST el último residuo guardado localmente. */
    fun sendRemote() = request {
        val record = records.firstOrNull() ?: return@request "Registra primero un residuo"
        val response = remote.upload(record)
        "POST: respuesta recibida, ID ${response.id}"
    }

    fun updateRemote() = request {
        val record = records.firstOrNull() ?: return@request "Registra primero un residuo"
        val response = remote.update(1, record)
        "PUT /1: ${response.title}"
    }

    fun deleteRemote() = request {
        "DELETE /1: código HTTP ${remote.remove(1)}"
    }

    companion object {
        /** Construye el ViewModel con los Repository del contenedor de aplicación. */
        fun factory(
            local: WasteRepository,
            remote: NetworkWasteRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                EcoResiduosViewModel(local, remote) as T
        }
    }
}
