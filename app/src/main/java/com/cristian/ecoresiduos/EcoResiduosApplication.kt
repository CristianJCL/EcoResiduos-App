package com.cristian.ecoresiduos

import android.app.Application
import com.cristian.ecoresiduos.data.InMemoryWasteRepository
import com.cristian.ecoresiduos.data.WasteRepository

/**
 * Contenedor mínimo de dependencias del proyecto.
 *
 * En esta primera fase el Repository utiliza memoria para validar la UI y la
 * arquitectura. En una etapa posterior la misma interfaz será implementada con
 * Room para almacenamiento local y con un servicio web para sincronización.
 */
class EcoResiduosApplication : Application() {

    /** Repository compartido por la aplicación. */
    val repository: WasteRepository by lazy {
        InMemoryWasteRepository()
    }
}
