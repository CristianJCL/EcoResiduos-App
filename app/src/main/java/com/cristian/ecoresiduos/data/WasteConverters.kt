package com.cristian.ecoresiduos.data
import androidx.room.TypeConverter

/** Guarda los valores del enum como cadenas en SQLite. */
class WasteConverters {
    @TypeConverter fun toType(value: String): WasteType = WasteType.valueOf(value)
    @TypeConverter fun fromType(value: WasteType): String = value.name
}
