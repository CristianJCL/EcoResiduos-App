package com.cristian.ecoresiduos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Categorías del residuo. */
enum class WasteType { FOOD, FRUIT_VEGETABLE, GARDEN }

/** Registro persistido en la tabla de residuos. */
@Entity(tableName = "waste_records")
data class WasteRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: WasteType,
    val weightKg: Double,
    val source: String,
    val date: String,
    val time: String,
    val composted: Boolean,
    val notes: String = ""
)
