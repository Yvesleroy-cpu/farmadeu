package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.FarmViewModel

enum class AppFlowState {
    LOGIN,
    SIGNUP,
    SUBSCRIPTION,
    MAIN_APP
}

@Composable
fun MainScreen(viewModel: FarmViewModel) {
    var flowState by remember { mutableStateOf(AppFlowState.LOGIN) }
    var activeTab by remember { mutableStateOf(0) }

    when (flowState) {
        AppFlowState.LOGIN -> {
            LoginScreen(
                onLoginSuccess = { flowState = AppFlowState.SUBSCRIPTION },
                onNavigateToSignup = { flowState = AppFlowState.SIGNUP }
            )
        }
        AppFlowState.SIGNUP -> {
            SignupScreen(
                onSignupSuccess = { flowState = AppFlowState.SUBSCRIPTION },
                onNavigateToLogin = { flowState = AppFlowState.LOGIN }
            )
        }
        AppFlowState.SUBSCRIPTION -> {
            SubscriptionScreen(
                onPlanSelected = { plan ->
                    viewModel.updateSubscription(plan)
                    flowState = AppFlowState.MAIN_APP
                }
            )
        }
        AppFlowState.MAIN_APP -> {
            var showProfileScreen by remember { mutableStateOf(false) }
            var showReportsScreen by remember { mutableStateOf(false) }
            var showInventoryScreen by remember { mutableStateOf(false) }

            if (showProfileScreen) {
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigateBack = { showProfileScreen = false }
                )
            } else if (showReportsScreen) {
                ReportsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { showReportsScreen = false }
                )
            } else if (showInventoryScreen) {
                InventoryScreen(
                    viewModel = viewModel,
                    onNavigateBack = { showInventoryScreen = false }
                )
            } else {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.testTag("main_navigation_bar")
                        ) {
                            NavigationBarItem(
                                selected = activeTab == 0,
                                onClick = { activeTab = 0 },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                label = { Text("Accueil", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                                modifier = Modifier.testTag("nav_dashboard")
                            )
                            NavigationBarItem(
                                selected = activeTab == 1,
                                onClick = { activeTab = 1 },
                                icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Scanner IA") },
                                label = { Text("Scan IA", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                                modifier = Modifier.testTag("nav_scan")
                            )
                            NavigationBarItem(
                                selected = activeTab == 2,
                                onClick = { activeTab = 2 },
                                icon = { Icon(Icons.Default.Grass, contentDescription = "Cultures") },
                                label = { Text("Cultures", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                                modifier = Modifier.testTag("nav_crops")
                            )
                            NavigationBarItem(
                                selected = activeTab == 3,
                                onClick = { activeTab = 3 },
                                icon = { Icon(Icons.Default.Pets, contentDescription = "Élevage") },
                                label = { Text("Élevage", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                                modifier = Modifier.testTag("nav_livestock")
                            )
                            NavigationBarItem(
                                selected = activeTab == 4,
                                onClick = { activeTab = 4 },
                                icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Calendrier") },
                                label = { Text("Calendrier", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                                modifier = Modifier.testTag("nav_calendar")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (activeTab) {
                            0 -> DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToScan = { activeTab = 1 },
                                onNavigateToCrops = { activeTab = 2 },
                                onNavigateToLivestock = { activeTab = 3 },
                                onNavigateToProfile = { showProfileScreen = true },
                                onNavigateToReports = { showReportsScreen = true },
                                onNavigateToInventory = { showInventoryScreen = true }
                            )
                            1 -> ScanScreen(viewModel = viewModel)
                            2 -> CropsScreen(
                                viewModel = viewModel
                            )
                            3 -> LivestockScreen(
                                viewModel = viewModel
                            )
                            4 -> CalendarScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
