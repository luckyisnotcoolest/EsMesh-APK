package com.example.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.EsMeshApplication
import com.example.data.model.EsMeshDevice
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ConfigScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DeviceDetailsScreen
import com.example.ui.screens.DevicesScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.FlasherScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.ConfigViewModel
import com.example.ui.viewmodel.DashboardViewModel
import com.example.ui.viewmodel.DevicesViewModel
import com.example.ui.viewmodel.DiagnosticsViewModel
import com.example.ui.viewmodel.FlasherViewModel
import com.example.ui.viewmodel.NetworkViewModel
import com.example.ui.viewmodel.SettingsViewModel

@Composable
fun EsMeshMainScreen(
    app: EsMeshApplication,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val dashboardViewModel = remember {
        DashboardViewModel(app.connectionRepository, app.deviceRepository, app.discoveryService)
    }
    val chatViewModel = remember {
        ChatViewModel(app.messageRepository, app.connectionRepository)
    }
    val devicesViewModel = remember {
        DevicesViewModel(app.deviceRepository, app.connectionRepository, app.discoveryService)
    }
    val networkViewModel = remember {
        NetworkViewModel(app.connectionRepository)
    }
    val configViewModel = remember {
        ConfigViewModel(app.connectionRepository)
    }
    val diagnosticsViewModel = remember {
        DiagnosticsViewModel(app.diagnosticsRepository, app.connectionRepository)
    }
    val flasherViewModel = remember {
        FlasherViewModel(app.flashBackend)
    }
    val settingsViewModel = remember {
        SettingsViewModel(app.settingsRepository)
    }

    var selectedDetailDevice by remember { mutableStateOf<EsMeshDevice?>(null) }

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            EsMeshBottomBar(
                currentRoute = currentRoute,
                onNavigate = { route -> navigateToTab(route) }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = EsMeshDestination.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(EsMeshDestination.Dashboard.route) {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToChat = { navigateToTab(EsMeshDestination.Chat.route) },
                    onNavigateToDevices = { navigateToTab(EsMeshDestination.Devices.route) },
                    onNavigateToNetwork = { navigateToTab(EsMeshDestination.Network.route) },
                    onNavigateToConfig = { navigateToTab(EsMeshDestination.Config.route) },
                    onNavigateToDiagnostics = { navController.navigate(EsMeshDestination.Diagnostics.route) }
                )
            }

            composable(EsMeshDestination.Chat.route) {
                ChatScreen(viewModel = chatViewModel)
            }

            composable(EsMeshDestination.Devices.route) {
                DevicesScreen(
                    viewModel = devicesViewModel,
                    onNavigateToDetails = { dev ->
                        selectedDetailDevice = dev
                        navController.navigate(EsMeshDestination.DeviceDetails.route)
                    }
                )
            }

            composable(EsMeshDestination.Network.route) {
                com.example.ui.screens.NetworkScreen(viewModel = networkViewModel)
            }

            composable(EsMeshDestination.Config.route) {
                ConfigScreen(viewModel = configViewModel)
            }

            composable(EsMeshDestination.Diagnostics.route) {
                DiagnosticsScreen(viewModel = diagnosticsViewModel)
            }

            composable(EsMeshDestination.Flasher.route) {
                FlasherScreen(viewModel = flasherViewModel)
            }

            composable(EsMeshDestination.Settings.route) {
                SettingsScreen(viewModel = settingsViewModel)
            }

            composable(EsMeshDestination.More.route) {
                MoreScreen()
            }

            composable(EsMeshDestination.DeviceDetails.route) {
                selectedDetailDevice?.let { dev ->
                    val isConnected = app.connectionRepository.connectedDevice.value?.id == dev.id
                    DeviceDetailsScreen(
                        device = dev,
                        isConnected = isConnected,
                        onBack = { navController.popBackStack() },
                        onConnectToggle = {
                            if (isConnected) {
                                app.connectionRepository.disconnect()
                            } else {
                                app.connectionRepository.connectDevice(dev)
                            }
                        },
                        onDelete = {
                            devicesViewModel.removeDevice(dev.id)
                            navController.popBackStack()
                        }
                    )
                } ?: run {
                    navController.popBackStack()
                }
            }
        }
    }
}
