package com.cristian.ecoresiduos.data

/**
 * Contrato de acceso a los registros de residuos.
 *
 * Separa el ViewModel del mecanismo concreto de almacenamiento, por lo que la
 * interfaz de usuario trabaja siempre con las mismas operaciones de datos.
 */
interface WasteRepository {
    fun getAll(): List<WasteRecord>
    fun add(record: WasteRecord)
    fun delete(id: Long)
}
