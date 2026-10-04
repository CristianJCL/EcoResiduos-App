package com.cristian.ecoresiduos.data

/**
 * Contrato que separa la interfaz de usuario del origen de los datos.
 *
 * Gracias a esta interfaz, el ViewModel no necesita saber si los registros
 * provienen de memoria, Room o un servicio web.
 */
interface WasteRepository {
    fun getAll(): List<WasteRecord>
    fun add(record: WasteRecord)
    fun delete(id: Long)
}
