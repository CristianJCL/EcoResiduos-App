package com.cristian.ecoresiduos.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cristian.ecoresiduos.R
import com.cristian.ecoresiduos.ui.screens.AddRecordScreen
import com.cristian.ecoresiduos.ui.screens.HomeScreen
import com.cristian.ecoresiduos.ui.screens.RecordsScreen
import com.cristian.ecoresiduos.ui.screens.SettingsScreen

/**
 * Destinos disponibles en el menú principal.
 */
private data class AppDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
)

private val destinations = listOf(
    AppDestination("home", R.string.nav_home, Icons.Default.Home),
    AppDestination("records", R.string.nav_records, Icons.Default.List),
    AppDestination("add", R.string.nav_add, Icons.Default.Add),
    AppDestination("settings", R.string.nav_settings, Icons.Default.Settings)
)

/**
 * Contenedor principal de navegación.
 *
 * En pantallas compactas utiliza una barra inferior. En anchos de 700 dp o
 * más cambia a NavigationRail para aprovechar mejor el espacio disponible.
 */
@Composable
fun EcoResiduosApp(viewModel: EcoResiduosViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: "home"

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val wideScreen = maxWidth >= 700.dp

        if (wideScreen) {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail {
                    destinations.forEach { destination ->
                        NavigationRailItem(
                            selected = currentRoute == destination.route,
                            onClick = { navigateSingleTop(navController, destination.route) },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = stringResource(destination.labelRes)
                                )
                            },
                            label = { Text(stringResource(destination.labelRes)) }
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    EcoResiduosNavHost(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        } else {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        destinations.forEach { destination ->
                            NavigationBarItem(
                                selected = currentRoute == destination.route,
                                onClick = { navigateSingleTop(navController, destination.route) },
                                icon = {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = stringResource(destination.labelRes)
                                    )
                                },
                                label = { Text(stringResource(destination.labelRes)) }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                EcoResiduosNavHost(
                    navController = navController,
                    viewModel = viewModel,
                    bottomPadding = innerPadding.calculateBottomPadding()
                )
            }
        }
    }
}

/**
 * Navega sin crear copias repetidas del mismo destino y conserva el estado
 * básico de cada pantalla del menú.
 */
private fun navigateSingleTop(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * Grafo de navegación de las cuatro pantallas del proyecto.
 */
@Composable
private fun EcoResiduosNavHost(
    navController: NavHostController,
    viewModel: EcoResiduosViewModel,
    bottomPadding: Dp = 0.dp
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("home") {
            HomeScreen(
                records = viewModel.records,
                onAddClick = { navigateSingleTop(navController, "add") },
                bottomPadding = bottomPadding
            )
        }

        composable("records") {
            RecordsScreen(
                records = viewModel.records,
                onDelete = viewModel::deleteRecord,
                bottomPadding = bottomPadding
            )
        }

        composable("add") {
            AddRecordScreen(
                state = viewModel.formState,
                onWeightChange = viewModel::updateWeight,
                onSourceChange = viewModel::updateSource,
                onNotesChange = viewModel::updateNotes,
                onTimeChange = viewModel::updateTime,
                onTypeChange = viewModel::updateType,
                onCompostedChange = viewModel::updateComposted,
                onSave = {
                    if (viewModel.saveRecord()) {
                        navigateSingleTop(navController, "records")
                    }
                },
                onCancel = {
                    viewModel.resetForm()
                    navigateSingleTop(navController, "home")
                },
                bottomPadding = bottomPadding
            )
        }

        composable("settings") {
            SettingsScreen(bottomPadding = bottomPadding)
        }
    }
}
