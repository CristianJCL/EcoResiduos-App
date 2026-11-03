package com.cristian.ecoresiduos.network

import com.cristian.ecoresiduos.data.WasteRecord

/** Peticiones al servicio web de demostración. */
class NetworkWasteRepository(private val service: EcoApiService) {

    // Consultar publicaciones de ejemplo.
    suspend fun list(): List<RemotePost> = service.getAll()
    suspend fun detail(id: Int): RemotePost = service.getById(id)

    // Enviar un registro local como JSON (la API no guarda cambios permanentes).
    suspend fun upload(record: WasteRecord): RemotePost =
        service.create(toRemotePost(record))

    suspend fun update(id: Int, record: WasteRecord): RemotePost =
        service.update(id, toRemotePost(record, id))

    suspend fun remove(id: Int): Int = service.delete(id).code()

    /** Prepara los datos HTTP que comparten las operaciones POST y PUT. */
    private fun toRemotePost(record: WasteRecord, id: Int? = null): RemotePost = RemotePost(
        id = id,
        title = "Residuo: ${record.type.name}",
        body = "${record.source}: ${record.weightKg} kg. ${record.notes}"
    )
}
