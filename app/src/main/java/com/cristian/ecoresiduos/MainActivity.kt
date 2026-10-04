package com.cristian.ecoresiduos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cristian.ecoresiduos.ui.EcoResiduosApp
import com.cristian.ecoresiduos.ui.EcoResiduosViewModel
import com.cristian.ecoresiduos.ui.theme.EcoResiduosTheme

/**
 * Activity principal del proyecto EcoResiduos.
 *
 * La Activity solo prepara Compose y obtiene el ViewModel. La lógica de pantalla
 * se mantiene fuera de esta clase para conservar una estructura sencilla.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EcoResiduosTheme {
                val application = application as EcoResiduosApplication
                val viewModel: EcoResiduosViewModel = viewModel(
                    factory = EcoResiduosViewModel.factory(application.repository)
                )

                EcoResiduosApp(viewModel = viewModel)
            }
        }
    }
}
