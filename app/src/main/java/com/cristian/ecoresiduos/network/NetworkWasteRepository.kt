package com.cristian.ecoresiduos.network

import android.content.Context
import com.cristian.ecoresiduos.BuildConfig
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cristian.ecoresiduos.data.WasteRecord
import com.cristian.ecoresiduos.data.WasteType
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Acceso remoto mediante Retrofit. La configuración es del desarrollador, no del usuario. */
class NetworkWasteRepository(context: Context) {
    private val preferences = context.getSharedPreferences("ecoresiduos_nube", Context.MODE_PRIVATE)

    // Valores públicos incluidos durante la compilación. Nunca se piden al usuario.
    private val server = BuildConfig.CLOUD_URL.trim().trimEnd('/')
    private val publicKey = BuildConfig.CLOUD_PUBLIC_KEY.trim()

    var email by mutableStateOf(preferences.getString("email", "") ?: "")
        private set
    var userId by mutableStateOf(preferences.getString("user_id", "") ?: "")
        private set

    private var accessToken = preferences.getString("access", "") ?: ""
    private var refreshToken = preferences.getString("refresh", "") ?: ""
    private var expiration = preferences.getLong("expiration", 0L)

    val configured get() = server.startsWith("https://") && publicKey.isNotBlank()
    val connected get() = configured && userId.isNotBlank() && refreshToken.isNotBlank()

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
        check(configured) { "Los respaldos en línea no están disponibles todavía." }
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
