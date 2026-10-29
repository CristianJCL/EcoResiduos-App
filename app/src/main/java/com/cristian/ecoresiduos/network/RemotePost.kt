package com.cristian.ecoresiduos.network

/** Datos JSON de las solicitudes de ejemplo al servicio REST. */
data class RemotePost(
    val userId: Int = 1,
    val id: Int? = null,
    val title: String,
    val body: String
)
