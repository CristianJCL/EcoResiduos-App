package com.cristian.ecoresiduos.data

import kotlinx.coroutines.flow.Flow

/** Operaciones de persistencia que necesita el ViewModel. */
interface WasteRepository {
    fun observeAll(): Flow<List<WasteRecord>>
    suspend fun getById(id: Long): WasteRecord?
    suspend fun save(record: WasteRecord)
    suspend fun delete(id: Long)
}
