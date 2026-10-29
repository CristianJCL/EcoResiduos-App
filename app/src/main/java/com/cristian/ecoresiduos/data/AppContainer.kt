package com.cristian.ecoresiduos.data

import android.content.Context
import com.cristian.ecoresiduos.network.EcoApiService
import com.cristian.ecoresiduos.network.NetworkWasteRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Crea y comparte las dependencias de base de datos y de conexión HTTP.
 * La Activity recibe el ViewModel sin construir servicios directamente.
 */
class AppContainer(context: Context) {
    private val database = EcoDatabase.getDatabase(context)

    val localRepository: WasteRepository by lazy {
        RoomWasteRepository(database.wasteDao())
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val networkRepository: NetworkWasteRepository by lazy {
        NetworkWasteRepository(retrofit.create(EcoApiService::class.java))
    }
}
