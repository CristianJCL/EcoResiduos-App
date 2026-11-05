package com.cristian.ecoresiduos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.remember
import androidx.compose.foundation.isSystemInDarkTheme
import com.cristian.ecoresiduos.settings.AppSettings
import com.cristian.ecoresiduos.ui.EcoResiduosApp
import com.cristian.ecoresiduos.ui.EcoResiduosViewModel
import com.cristian.ecoresiduos.ui.theme.EcoResiduosTheme

/** Inicia Compose y proporciona los dos Repository al ViewModel. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings = remember { AppSettings(applicationContext) }
            val systemDark = isSystemInDarkTheme()
            val dark = when (settings.theme) {
                "dark" -> true
                "light" -> false
                else -> systemDark
            }
            EcoResiduosTheme(darkTheme = dark) {
                val container = (application as EcoResiduosApplication).container
                val viewModel: EcoResiduosViewModel = viewModel(
                    factory = EcoResiduosViewModel.factory(
                        container.localRepository,
                        container.networkRepository,
                        settings
                    )
                )
                viewModel.attachResolver(contentResolver)
                EcoResiduosApp(viewModel, settings)
            }
        }
    }
}
