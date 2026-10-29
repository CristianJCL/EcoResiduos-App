package com.cristian.ecoresiduos.data
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Operaciones para las tablas de notificaciones y archivos adjuntos. */
@Dao
interface AttachmentDao {
    @Insert suspend fun insertNotification(item: NotificationEntity): Long
    @Query("SELECT * FROM notifications WHERE recordId = :recordId")
    fun notifications(recordId: Long): Flow<List<NotificationEntity>>
    @Insert suspend fun insertMedia(item: MediaEntity): Long
    @Query("SELECT * FROM media WHERE recordId = :recordId")
    fun media(recordId: Long): Flow<List<MediaEntity>>
}
