package com.cristian.ecoresiduos.network

import com.cristian.ecoresiduos.data.WasteRecord

/** Encapsula el servicio Retrofit para trabajar con datos HTTP. */
class NetworkWasteRepository(private val service: EcoApiService) {
    suspend fun list(): List<RemotePost> = service.getAll()
    suspend fun detail(id: Int): RemotePost = service.getById(id)
    suspend fun upload(record: WasteRecord): RemotePost = service.create(
        RemotePost(title = "Residuo: ${record.type.name}",
            body = "${record.source}: ${record.weightKg} kg. ${record.notes}")
    )
    suspend fun update(id: Int, record: WasteRecord): RemotePost = service.update(
        id, RemotePost(id = id, title = "Residuo: ${record.type.name}",
            body = "${record.source}: ${record.weightKg} kg. ${record.notes}")
    )
    suspend fun remove(id: Int): Int = service.delete(id).code()
}
