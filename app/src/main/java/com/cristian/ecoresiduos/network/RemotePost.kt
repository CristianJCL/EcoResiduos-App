package com.cristian.ecoresiduos.network

import com.google.gson.annotations.SerializedName

data class LoginInput(val email: String, val password: String)
data class RefreshInput(@SerializedName("refresh_token") val refreshToken: String)
data class CloudUser(val id: String, val email: String?)
data class AuthSession(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("expires_in") val expiresIn: Long? = null,
    val user: CloudUser? = null
)

/** Campos de un residuo real guardado en Supabase. */
data class RemoteWaste(
    val id: String,
    @SerializedName("user_id") val userId: String,
    val type: String,
    @SerializedName("weight_kg") val weightKg: Double,
    val source: String,
    val date: String,
    val time: String,
    val composted: Boolean,
    val notes: String = ""
)
