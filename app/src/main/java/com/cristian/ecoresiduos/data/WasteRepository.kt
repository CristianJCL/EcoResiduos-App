package com.cristian.ecoresiduos.data

import kotlinx.coroutines.flow.Flow

/** Operaciones de persistencia que necesita el ViewModel. */
interface WasteRepository {
    fun observeAll(): Flow<List<WasteRecord>>
    suspend fun getById(id: Long): WasteRecord?
    suspend fun save(record: WasteRecord)
    suspend fun delete(id: Long)
    suspend fun clearAll()
    suspend fun allForSync(): List<WasteRecord>
    suspend fun markSynced(id: Long)
    suspend fun finishDeletion(id: Long)
    suspend fun restore(record: WasteRecord): Boolean
}
