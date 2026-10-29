package com.cristian.ecoresiduos

import android.app.Application
import com.cristian.ecoresiduos.data.AppContainer

/** Mantiene las dependencias de Room y Retrofit compartidas por la aplicación. */
class EcoResiduosApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
