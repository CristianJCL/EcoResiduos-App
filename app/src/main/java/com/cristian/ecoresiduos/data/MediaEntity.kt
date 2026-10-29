package com.cristian.ecoresiduos.data
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Guarda la referencia local de una imagen asociada al residuo. */
@Entity(tableName = "media")
data class MediaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recordId: Long,
    val localUri: String,
    val mimeType: String
)
