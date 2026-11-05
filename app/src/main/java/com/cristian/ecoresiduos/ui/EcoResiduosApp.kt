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
import com.cristian.ecoresiduos.settings.AppSettings
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
fun EcoResiduosApp(viewModel: EcoResiduosViewModel, settings: AppSettings) {
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
                Box(Modifier.weight(1f)) { AppScreens(nav, viewModel, settings) }
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
                AppScreens(nav, viewModel, settings, padding.calculateBottomPadding())
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

/** El NavHost indica qué pantalla mostrar para cada ruta del menú. */
@Composable
private fun AppScreens(
    nav: NavHostController,
    viewModel: EcoResiduosViewModel,
    settings: AppSettings,
    bottomPadding: Dp = 0.dp
) {
    NavHost(navController = nav, startDestination = "home", modifier = Modifier.fillMaxSize()) {
        composable("home") {
            HomeScreen(
                records = viewModel.records,
                onAddClick = {
                    viewModel.resetForm()
                    goTo(nav, "add")
                },
                bottomPadding = bottomPadding
            )
        }
        composable("records") {
            RecordsScreen(
                records = viewModel.records,
                onDelete = viewModel::deleteRecord,
                confirmDelete = settings.confirmDelete,
                onEdit = { id ->
                    viewModel.beginEdit(id)
                    goTo(nav, "add")
                },
                bottomPadding = bottomPadding
            )
        }
        composable("add") {
            AddRecordScreen(
                state = viewModel.formState,
                isEditing = viewModel.editingId != null,
                onWeightChange = viewModel::updateWeight,
                onSourceChange = viewModel::updateSource,
                onNotesChange = viewModel::updateNotes,
                onTimeChange = viewModel::updateTime,
                onTypeChange = viewModel::updateType,
                onCompostedChange = viewModel::updateComposted,
                onSave = {
                    if (viewModel.saveRecord()) goTo(nav, "records")
                },
                onCancel = {
                    viewModel.resetForm()
                    goTo(nav, "home")
                },
                bottomPadding = bottomPadding
            )
        }
        composable("network") {
            NetworkScreen(
                server = viewModel.cloudServer,
                publicKey = viewModel.cloudKey,
                userEmail = viewModel.cloudEmail,
                connected = viewModel.cloudConnected,
                localPending = viewModel.records.count { it.syncState != "SYNCED" },
                backupCount = if (viewModel.cloudLoaded) viewModel.remoteItems.size else null,
                items = viewModel.remoteItems,
                message = viewModel.networkMessage,
                loading = viewModel.networkLoading,
                onConfigure = viewModel::configureCloud,
                onSignUp = viewModel::signUpCloud,
                onLogin = viewModel::loginCloud,
                onLogout = viewModel::logoutCloud,
                onSync = viewModel::syncCloud,
                onList = viewModel::loadRemote,
                onDetail = viewModel::readRemoteDetail,
                onRestore = viewModel::restoreCloud,
                onDelete = viewModel::deleteRemote,
                bottomPadding = bottomPadding
            )
        }
        composable("settings") {
            SettingsScreen(
                settings = settings,
                message = viewModel.settingsMessage,
                onExport = viewModel::exportCsv,
                onImport = viewModel::importCsv,
                onClearAll = viewModel::clearAllRecords,
                bottomPadding = bottomPadding
            )
        }
    }
}
