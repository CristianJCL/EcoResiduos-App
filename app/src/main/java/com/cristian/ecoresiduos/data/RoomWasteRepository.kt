package com.cristian.ecoresiduos.data

import kotlinx.coroutines.flow.Flow

/** Conecta el contrato del Repository con las consultas del DAO. */
class RoomWasteRepository(private val dao: WasteDao) : WasteRepository {
    override fun observeAll(): Flow<List<WasteRecord>> = dao.observeAll()
    override suspend fun getById(id: Long): WasteRecord? = dao.getById(id)
    override suspend fun save(record: WasteRecord) {
        if (record.id == 0L) dao.insert(record) else dao.update(record)
    }
    override suspend fun delete(id: Long) {
        dao.deleteById(id)
    }
}
