package com.cristian.ecoresiduos.data
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Almacena los datos necesarios para programar recordatorios. */
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recordId: Long,
    val message: String,
    val scheduledAt: Long,
    val enabled: Boolean = true
)
