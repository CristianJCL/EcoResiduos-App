package com.cristian.ecoresiduos.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

/** Usa el DAO para guardar y consultar residuos en la base del teléfono. */
class RoomWasteRepository(private val dao: WasteDao) : WasteRepository {

    // Flow envía los cambios de SQLite al ViewModel.
    override fun observeAll(): Flow<List<WasteRecord>> = dao.observeAll()
    override suspend fun getById(id: Long): WasteRecord? = dao.getById(id)

    override suspend fun save(record: WasteRecord) {
        val cloudId = record.cloudId.ifBlank { UUID.randomUUID().toString() }
        val state = if (record.syncState == "SYNCED" || record.syncState == "DIRTY") "DIRTY" else "PENDING"
        val pending = record.copy(cloudId = cloudId, syncState = state)
        if (record.id == 0L) dao.insert(pending) else dao.update(pending)
    }

    override suspend fun allForSync(): List<WasteRecord> = dao.getAllForSync()
    override suspend fun markSynced(id: Long) = dao.markSynced(id)
    override suspend fun finishDeletion(id: Long) = dao.deleteById(id)
    override suspend fun restore(record: WasteRecord): Boolean {
        if (dao.getByCloudId(record.cloudId) != null) return false
        dao.insert(record.copy(id = 0, syncState = "SYNCED"))
        return true
    }

    override suspend fun clearAll() = dao.clearAll()

    override suspend fun delete(id: Long) {
        val record = dao.getById(id) ?: return
        if (record.syncState == "PENDING") dao.deleteById(id)
        else dao.markDeleted(id)
    }
}
