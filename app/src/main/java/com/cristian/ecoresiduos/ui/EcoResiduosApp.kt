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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.cristian.ecoresiduos.R
import com.cristian.ecoresiduos.ui.screens.*

/** Pantallas disponibles desde el menú principal. */
private data class Destination(val route: String, @StringRes val title: Int, val icon: ImageVector)

private val destinations = listOf(
    Destination("home", R.string.nav_home, Icons.Default.Home),
    Destination("records", R.string.nav_records, Icons.Default.List),
    Destination("add", R.string.nav_add, Icons.Default.Add),
    Destination("network", R.string.nav_network, Icons.Default.List),
    Destination("settings", R.string.nav_settings, Icons.Default.Settings)
)

/** Muestra NavigationBar en teléfonos y NavigationRail en pantallas anchas. */
@Composable
fun EcoResiduosApp(viewModel: EcoResiduosViewModel) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: "home"

    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth >= 700.dp) {
            Row(Modifier.fillMaxSize()) {
                NavigationRail {
                    destinations.forEach { item ->
                        NavigationRailItem(
                            selected = route == item.route,
                            onClick = { goTo(nav, item.route) },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(stringResource(item.title)) }
                        )
                    }
                }
                Box(Modifier.weight(1f)) { Screens(nav, viewModel) }
            }
        } else {
            Scaffold(bottomBar = {
                NavigationBar {
                    destinations.forEach { item ->
                        NavigationBarItem(
                            selected = route == item.route,
                            onClick = { goTo(nav, item.route) },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(stringResource(item.title)) }
                        )
                    }
                }
            }) { padding ->
                Screens(nav, viewModel, padding.calculateBottomPadding())
            }
        }
    }
}

/** Cambia de pantalla conservando una sola instancia de cada destino. */
private fun goTo(nav: NavHostController, route: String) {
    nav.navigate(route) {
        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Conecta el NavHost con el ViewModel y los eventos de cada pantalla. */
@Composable
private fun Screens(nav: NavHostController, vm: EcoResiduosViewModel, bottom: Dp = 0.dp) {
    NavHost(navController = nav, startDestination = "home", modifier = Modifier.fillMaxSize()) {
        composable("home") {
            HomeScreen(vm.records,
                onAddClick = { vm.resetForm(); goTo(nav, "add") },
                bottomPadding = bottom)
        }
        composable("records") {
            RecordsScreen(vm.records, vm::deleteRecord,
                onEdit = { id -> vm.beginEdit(id); goTo(nav, "add") },
                bottomPadding = bottom)
        }
        composable("add") {
            AddRecordScreen(
                state = vm.formState,
                isEditing = vm.editingId != null,
                onWeightChange = vm::updateWeight,
                onSourceChange = vm::updateSource,
                onNotesChange = vm::updateNotes,
                onTimeChange = vm::updateTime,
                onTypeChange = vm::updateType,
                onCompostedChange = vm::updateComposted,
                onSave = { if (vm.saveRecord()) goTo(nav, "records") },
                onCancel = { vm.resetForm(); goTo(nav, "home") },
                bottomPadding = bottom
            )
        }
        composable("network") {
            NetworkScreen(vm.remoteItems, vm.networkMessage, vm.networkLoading,
                vm::loadRemote, vm::readRemoteDetail, vm::sendRemote,
                vm::updateRemote, vm::deleteRemote, bottom)
        }
        composable("settings") { SettingsScreen(bottomPadding = bottom) }
    }
}
