package com.cristian.ecoresiduos.data

import android.content.Context
import com.cristian.ecoresiduos.network.NetworkWasteRepository

/** Dependencias compartidas: Room y servicio REST de Supabase. */
class AppContainer(context: Context) {
    private val database = EcoDatabase.getDatabase(context)
    val localRepository: WasteRepository by lazy {
        RoomWasteRepository(database.wasteDao())
    }
    val networkRepository: NetworkWasteRepository by lazy {
        NetworkWasteRepository(context.applicationContext)
    }
}
