package com.cristian.ecoresiduos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Base SQLite con las tablas de residuos, avisos y archivos. */
@Database(
    entities = [WasteRecord::class, NotificationEntity::class, MediaEntity::class],
    version = 2,
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
            ).addMigrations(object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE waste_records ADD COLUMN cloudId TEXT NOT NULL DEFAULT ''")
                    db.execSQL("ALTER TABLE waste_records ADD COLUMN syncState TEXT NOT NULL DEFAULT 'PENDING'")
                }
            }).build().also { instance = it }
        }
    }
}
