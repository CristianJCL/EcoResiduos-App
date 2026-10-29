package com.cristian.ecoresiduos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/** Base SQLite con las tablas de residuos, avisos y archivos. */
@Database(
    entities = [WasteRecord::class, NotificationEntity::class, MediaEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(WasteConverters::class)
abstract class EcoDatabase : RoomDatabase() {
    abstract fun wasteDao(): WasteDao
    abstract fun attachmentDao(): AttachmentDao

    companion object {
        @Volatile private var instance: EcoDatabase? = null
        fun getDatabase(context: Context): EcoDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext, EcoDatabase::class.java, "ecoresiduos.db"
            ).build().also { instance = it }
        }
    }
}
