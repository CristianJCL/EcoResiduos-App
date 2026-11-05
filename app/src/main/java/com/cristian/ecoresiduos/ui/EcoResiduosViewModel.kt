package com.cristian.ecoresiduos.ui

import android.net.Uri
import android.content.ContentResolver
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cristian.ecoresiduos.data.CsvRecords
import com.cristian.ecoresiduos.settings.AppSettings
import com.cristian.ecoresiduos.data.WasteRecord
import com.cristian.ecoresiduos.data.WasteRepository
import com.cristian.ecoresiduos.data.WasteType
import com.cristian.ecoresiduos.network.NetworkWasteRepository
import com.cristian.ecoresiduos.network.RemoteWaste
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        get() {
            val kilos = weight.toDoubleOrNull()
            return source.isNotBlank() && kilos != null && kilos.isFinite() && kilos > 0.0
        }
}

/**
 * Administra el formulario, los registros persistidos y la conexión HTTP.
 * El ViewModel se comunica exclusivamente con los Repository.
 */
class EcoResiduosViewModel(
    private val localRepository: WasteRepository,
    private val networkRepository: NetworkWasteRepository,
    private val settings: AppSettings
) : ViewModel() {

    // 1. DATOS LOCALES: lista visible y formulario de captura.
    private val _records = mutableStateListOf<WasteRecord>()
    val records: List<WasteRecord> get() = _records

    var settingsMessage by mutableStateOf("")
        private set

    var formState by mutableStateOf(WasteFormState(type = settings.defaultType))
        private set
    var editingId by mutableStateOf<Long?>(null)
        private set

    // 2. RESPALDOS: solo muestra registros que realmente vienen del servidor.
    val cloudServer get() = networkRepository.server
    val cloudKey get() = networkRepository.publicKey
    val cloudEmail get() = networkRepository.email
    val cloudConnected get() = networkRepository.connected
    var remoteItems by mutableStateOf<List<RemoteWaste>>(emptyList())
        private set
    var cloudLoaded by mutableStateOf(false)
        private set
    var networkMessage by mutableStateOf("")
        private set
    var networkLoading by mutableStateOf(false)
        private set

    init {
        // Flow notifica los cambios de SQLite para actualizar la lista y el resumen.
        viewModelScope.launch {
            localRepository.observeAll().collect { items ->
                _records.clear()
                _records.addAll(items)
            }
        }
    }

    // 3. FORMULARIO: cada campo se actualiza desde Compose.
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
            notes = state.notes.trim(),
            cloudId = previous?.cloudId ?: "",
            syncState = previous?.syncState ?: "PENDING"
        )
        viewModelScope.launch { localRepository.save(record) }
        resetForm()
        return true
    }

    /** Elimina el registro en SQLite y actualiza la lista mediante Flow. */
    fun deleteRecord(id: Long) {
        viewModelScope.launch { localRepository.delete(id) }
    }

    fun resetForm() {
        editingId = null
        formState = WasteFormState(type = settings.defaultType)
    }

    /** Exporta un CSV real al archivo elegido en Android. */
    fun exportCsv(uri: Uri) {
        val copy = records.toList()
        viewModelScope.launch {
            settingsMessage = try {
                withContext(Dispatchers.IO) {
                    val resolver = appResolver ?: error("Abre el proyecto en Android Studio.")
                    resolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use {
                        it.write(CsvRecords.export(copy))
                    } ?: error("No se pudo abrir el archivo.")
                }
                "Se exportaron ${copy.size} registros."
            } catch (e: Exception) { "Error al exportar: ${e.message}" }
        }
    }

    fun importCsv(uri: Uri) {
        viewModelScope.launch {
            settingsMessage = try {
                val imported = withContext(Dispatchers.IO) {
                    val resolver = appResolver ?: error("No se encontró el archivo.")
                    val text = resolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use {
                        it.readText()
                    } ?: error("No se pudo leer el archivo.")
                    CsvRecords.import(text)
                }
                val current = records.toList().map(::signature).toMutableSet()
                var added = 0
                imported.forEach { record ->
                    if (current.add(signature(record))) {
                        localRepository.save(record)
                        added++
                    }
                }
                "Importación terminada: ${added} nuevos, ${imported.size - added} repetidos."
            } catch (e: Exception) { "No se pudo importar: ${e.message}" }
        }
    }

    private fun signature(item: WasteRecord): String = listOf(
        item.type, item.weightKg, item.source, item.date, item.time, item.composted, item.notes
    ).joinToString("|")

    fun clearAllRecords() {
        viewModelScope.launch {
            localRepository.clearAll()
            settingsMessage = "Se eliminaron los registros del teléfono."
        }
    }

    private var appResolver: ContentResolver? = null
    fun attachResolver(resolver: ContentResolver) { appResolver = resolver }

    // 4. RETROFIT: respaldo real con API REST de Supabase.
    fun configureCloud(url: String, key: String) {
        networkMessage = try {
            networkRepository.configure(url, key)
            remoteItems = emptyList()
            cloudLoaded = false
            "Conexión guardada. Inicia sesión para acceder a tus respaldos."
        } catch (e: Exception) { e.message ?: "Configuración incorrecta." }
    }
    private fun request(action: suspend () -> String) {
        viewModelScope.launch {
            networkLoading = true
            networkMessage = ""
            networkMessage = try { action() } catch (e: retrofit2.HttpException) {
                when (e.code()) {
                    400 -> "Datos incorrectos o servicio remoto sin configurar."
                    401, 403 -> "Acceso denegado. Revisa tu cuenta."
                    404 -> "No se puede consultar el respaldo. Comprueba la configuración del servicio."
                    else -> "Error HTTP " + e.code().toString()
                }
            } catch (e: Exception) { e.message ?: "No se pudo conectar a la nube." }
            networkLoading = false
        }
    }
    fun signUpCloud(email: String, password: String) = request {
        if (networkRepository.signUp(email.trim(), password)) "Cuenta creada; sesión iniciada."
        else "Cuenta creada. Confirma tu correo y después inicia sesión."
    }
    fun loginCloud(email: String, password: String) = request {
        networkRepository.login(email.trim(), password)
        remoteItems = networkRepository.list()
        cloudLoaded = true
        "Sesión iniciada: " + remoteItems.size.toString() + " registros respaldados."
    }
    fun logoutCloud() {
        networkRepository.logout()
        remoteItems = emptyList()
        cloudLoaded = false
        networkMessage = "Sesión cerrada."
    }
    /** POST/PATCH/DELETE reales para los cambios pendientes de Room. */
    fun syncCloud() = request {
        val savedIds = networkRepository.list().map { it.id }.toSet()
        var count = 0
        for (item in localRepository.allForSync()) {
            if (item.syncState == "DELETED") {
                if (item.cloudId in savedIds) networkRepository.remove(item.cloudId)
                localRepository.finishDeletion(item.id)
                count++
            } else if (item.syncState == "PENDING" || item.syncState == "DIRTY") {
                if (item.cloudId in savedIds) networkRepository.update(item)
                else networkRepository.create(item)
                localRepository.markSynced(item.id)
                count++
            }
        }
        remoteItems = networkRepository.list()
        cloudLoaded = true
        "Sincronizados " + count + " cambios. Total de respaldos: " + remoteItems.size
    }
    fun loadRemote() = request {
        remoteItems = networkRepository.list()
        cloudLoaded = true
        "Registros encontrados: " + remoteItems.size
    }
    fun readRemoteDetail(id: String) = request {
        val item = networkRepository.detail(id) ?: return@request "Ya no existe el respaldo."
        "Residuo " + item.type + " / " + item.weightKg + " kg / " + item.source
    }
    /** Recupera solo los registros que no existan ya en el teléfono. */
    fun restoreCloud() = request {
        remoteItems = networkRepository.list()
        cloudLoaded = true
        var count = 0
        for (item in remoteItems) {
            if (localRepository.restore(networkRepository.toLocal(item))) count++
        }
        "Se recuperaron " + count + " registros nuevos, sin duplicados."
    }
    fun deleteRemote(id: String) = request {
        networkRepository.remove(id)
        remoteItems = networkRepository.list()
        cloudLoaded = true
        "Respaldo eliminado. El registro del teléfono permanece."
    }

    companion object {
        /** Construye el ViewModel con los Repository del contenedor de aplicación. */
        fun factory(
            localRepository: WasteRepository,
            networkRepository: NetworkWasteRepository,
            settings: AppSettings
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                EcoResiduosViewModel(localRepository, networkRepository, settings) as T
        }
    }
}
