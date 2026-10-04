package com.cristian.ecoresiduos.data

/**
 * Tipos de residuos orgánicos que maneja la aplicación.
 */
enum class WasteType {
    FOOD,
    FRUIT_VEGETABLE,
    GARDEN
}

/**
 * Modelo principal de EcoResiduos.
 *
 * @property id identificador único del registro.
 * @property type categoría del residuo.
 * @property weightKg peso aproximado en kilogramos.
 * @property source lugar donde se generó el residuo.
 * @property date fecha de registro.
 * @property time hora seleccionada por el usuario.
 * @property composted indica si el residuo fue destinado a composta.
 * @property notes observaciones opcionales.
 */
data class WasteRecord(
    val id: Long,
    val type: WasteType,
    val weightKg: Double,
    val source: String,
    val date: String,
    val time: String,
    val composted: Boolean,
    val notes: String = ""
)
