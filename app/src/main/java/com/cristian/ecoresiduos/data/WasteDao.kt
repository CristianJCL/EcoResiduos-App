package com.cristian.ecoresiduos.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** Consultas y operaciones sobre residuos almacenados en SQLite. */
@Dao
interface WasteDao {
    @Query("SELECT * FROM waste_records ORDER BY id DESC")
    fun observeAll(): Flow<List<WasteRecord>>

    @Query("SELECT * FROM waste_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): WasteRecord?

    @Insert
    suspend fun insert(record: WasteRecord): Long

    @Update
    suspend fun update(record: WasteRecord)

    @Query("DELETE FROM waste_records WHERE id = :id")
    suspend fun deleteById(id: Long)
}
