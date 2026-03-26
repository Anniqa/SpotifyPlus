package com.lenerd.spotifyplus.manager.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lenerd.spotifyplus.manager.ui.screens.AboutScreen
import com.lenerd.spotifyplus.manager.ui.screens.HomeScreen
import com.lenerd.spotifyplus.manager.ui.screens.HooksScreen
import com.lenerd.spotifyplus.manager.ui.screens.SettingsScreen
import com.lenerd.spotifyplus.manager.ui.screens.UpdatesScreen
import com.lenerd.spotifyplus.manager.viewmodel.ManagerViewModel

sealed class AppRoute(val route: String, val label: String) {
    data object Home : AppRoute("home", "Home")
    data object Hooks : AppRoute("hooks", "Hooks")
    data object Updates : AppRoute("updates", "Updates")
    data object Settings : AppRoute("settings", "Settings")
    data object About : AppRoute("about", "Status")
}

@Composable
fun SpotifyPlusApp(viewModel: ManagerViewModel) {
    val navController = rememberNavController()

    val items = listOf(
        AppRoute.Home,
        AppRoute.Hooks,
        AppRoute.Updates,
        AppRoute.Settings,
        AppRoute.About
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    val icon = when (screen) {
                        AppRoute.Home -> Icons.Default.Home
                        AppRoute.Hooks -> Icons.Default.Add
                        AppRoute.Updates -> Icons.Default.Create
                        AppRoute.Settings -> Icons.Default.Settings
                        AppRoute.About -> Icons.Default.Info
                    }

                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(AppRoute.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(icon, contentDescription = screen.label) },
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppRoute.Home.route) {
                HomeScreen(
                    state = viewModel.uiState,
                    onRefreshStatus = viewModel::refreshStatus,
                    onCheckForUpdates = viewModel::checkForUpdates,
                    onEnableAllHooks = viewModel::enableAllHooks,
                    onDisableAllHooks = viewModel::disableAllHooks,
                    onOpenHooks = { navController.navigate(AppRoute.Hooks.route) },
                    onOpenUpdates = { navController.navigate(AppRoute.Updates.route) }
                )
            }

            composable(AppRoute.Hooks.route) {
                HooksScreen(
                    state = viewModel.uiState,
                    onToggleHook = viewModel::toggleHook,
                    onToggleScript = viewModel::toggleScript,
                    onEnableAllHooks = viewModel::enableAllHooks,
                    onDisableAllHooks = viewModel::disableAllHooks
                )
            }

            composable(AppRoute.Updates.route) {
                UpdatesScreen(
                    state = viewModel.uiState,
                    onCheckForUpdates = viewModel::checkForUpdates,
                    onInstallUpdate = viewModel::installUpdate
                )
            }

            composable(AppRoute.Settings.route) {
                SettingsScreen(
                    state = viewModel.uiState,
                    onSetAutoCheckUpdates = viewModel::setAutoCheckUpdates,
                    onSetDebugLogging = viewModel::setDebugLogging,
                    onSetStartupCheck = viewModel::setStartupCheck,
                    onSetTheme = viewModel::setTheme
                )
            }

            composable(AppRoute.About.route) {
                AboutScreen(
                    state = viewModel.uiState,
                    onRefreshStatus = viewModel::refreshStatus
                )
            }
        }
    }
}