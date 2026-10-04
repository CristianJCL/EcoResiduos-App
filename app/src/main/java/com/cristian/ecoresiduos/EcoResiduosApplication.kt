package com.cristian.ecoresiduos

import android.app.Application
import com.cristian.ecoresiduos.data.InMemoryWasteRepository
import com.cristian.ecoresiduos.data.WasteRepository

/**
 * Clase Application utilizada como contenedor de dependencias.
 *
 * Crea una sola instancia de WasteRepository para compartir la misma fuente
 * de datos entre las pantallas y el ViewModel durante la ejecución de la app.
 */
class EcoResiduosApplication : Application() {

    /** Repository compartido por la aplicación. */
    val repository: WasteRepository by lazy {
        InMemoryWasteRepository()
    }
}
