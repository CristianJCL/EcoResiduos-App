package com.cristian.ecoresiduos.network

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cristian.ecoresiduos.data.WasteRecord
import com.cristian.ecoresiduos.data.WasteType
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Conecta Room con Supabase sin utilizar claves secretas de servidor. */
class NetworkWasteRepository(context: Context) {
    private val preferences = context.getSharedPreferences("ecoresiduos_nube", Context.MODE_PRIVATE)

    var server by mutableStateOf(preferences.getString("servidor", "") ?: "")
        private set
    var publicKey by mutableStateOf(preferences.getString("public_key", "") ?: "")
        private set
    var email by mutableStateOf(preferences.getString("email", "") ?: "")
        private set
    var userId by mutableStateOf(preferences.getString("user_id", "") ?: "")
        private set

    private var accessToken = preferences.getString("access", "") ?: ""
    private var refreshToken = preferences.getString("refresh", "") ?: ""
    private var expiration = preferences.getLong("expiration", 0L)

    val configured get() = server.startsWith("https://") && publicKey.isNotBlank()
    val connected get() = userId.isNotBlank() && refreshToken.isNotBlank()

    fun configure(url: String, key: String) {
        val clean = url.trim().trimEnd('/')
        require(clean.startsWith("https://") && clean.contains(".supabase.co")) {
            "Introduce una URL válida de tu proyecto Supabase."
        }
        require(key.trim().isNotEmpty()) { "Falta la clave pública (anon/publishable)." }
        if (server != clean || publicKey != key.trim()) logout()
        server = clean
        publicKey = key.trim()
        preferences.edit().putString("servidor", server).putString("public_key", publicKey).apply()
    }

    fun logout() {
        accessToken = ""
        refreshToken = ""
        userId = ""
        email = ""
        expiration = 0
        preferences.edit().remove("access").remove("refresh").remove("expiration")
            .remove("user_id").remove("email").apply()
    }

    private fun service(): EcoApiService {
        require(configured) { "Configura primero la URL y clave pública de Supabase." }
        return Retrofit.Builder().baseUrl("$server/")
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(EcoApiService::class.java)
    }

    private fun saveSession(session: AuthSession, address: String? = null) {
        require(!session.accessToken.isNullOrBlank() && !session.refreshToken.isNullOrBlank()) {
            "Confirma tu correo electrónico y después inicia sesión."
        }
        accessToken = session.accessToken
        refreshToken = session.refreshToken
        if (!session.user?.id.isNullOrBlank()) userId = session.user!!.id
        if (address != null) email = address
        expiration = System.currentTimeMillis() / 1000 + (session.expiresIn ?: 3600) - 60
        preferences.edit().putString("access", accessToken)
            .putString("refresh", refreshToken).putString("user_id", userId)
            .putString("email", email).putLong("expiration", expiration).apply()
    }

    suspend fun login(address: String, password: String) {
        saveSession(service().login(key = publicKey, input = LoginInput(address, password)), address)
    }

    suspend fun signUp(address: String, password: String): Boolean {
        val result = service().signUp(publicKey, LoginInput(address, password))
        if (!result.accessToken.isNullOrBlank()) {
            saveSession(result, address)
            return true
        }
        return false // Supabase puede exigir confirmar primero el correo.
    }

    private suspend fun authorization(): String {
        require(connected) { "Inicia sesión para consultar tus respaldos." }
        if (System.currentTimeMillis() / 1000 >= expiration) {
            saveSession(service().refresh(key = publicKey, input = RefreshInput(refreshToken)))
        }
        return "Bearer $accessToken"
    }

    suspend fun list(): List<RemoteWaste> = service().list(publicKey, authorization())
    suspend fun detail(id: String): RemoteWaste? =
        service().detail(publicKey, authorization(), "eq.$id").firstOrNull()

    suspend fun create(record: WasteRecord) {
        service().create(publicKey, authorization(), record.toRemote(userId))
    }

    suspend fun update(record: WasteRecord) {
        service().update(publicKey, authorization(), "eq.${record.cloudId}", record.toRemote(userId))
    }

    suspend fun remove(cloudId: String) {
        val result = service().delete(publicKey, authorization(), "eq.$cloudId")
        require(result.isSuccessful) { "No fue posible eliminar el respaldo remoto." }
    }

    private fun WasteRecord.toRemote(owner: String): RemoteWaste = RemoteWaste(
        id = cloudId, userId = owner, type = type.name, weightKg = weightKg,
        source = source, date = date, time = time, composted = composted, notes = notes
    )

    fun toLocal(remote: RemoteWaste): WasteRecord = WasteRecord(
        type = WasteType.valueOf(remote.type),
        weightKg = remote.weightKg, source = remote.source, date = remote.date,
        time = remote.time, composted = remote.composted, notes = remote.notes,
        cloudId = remote.id, syncState = "SYNCED"
    )
}
