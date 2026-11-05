package com.cristian.ecoresiduos.data

import kotlinx.coroutines.flow.Flow

/** Usa el DAO para guardar y consultar residuos en la base del teléfono. */
class RoomWasteRepository(private val dao: WasteDao) : WasteRepository {

    // Flow envía los cambios de SQLite al ViewModel.
    override fun observeAll(): Flow<List<WasteRecord>> = dao.observeAll()
    override suspend fun getById(id: Long): WasteRecord? = dao.getById(id)

    override suspend fun save(record: WasteRecord) {
        if (record.id == 0L) {
            dao.insert(record)       // Nuevo registro.
        } else {
            dao.update(record)       // Registro existente.
        }
    }

    override suspend fun clearAll() = dao.clearAll()

    override suspend fun delete(id: Long) {
        dao.deleteById(id)
    }
}
