package com.systemmonitor.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemmonitor.applock.manager.AppLockManager
import com.systemmonitor.applock.ui.SelectAppsScreen
import com.systemmonitor.features.alerts.AlertsScreen
import com.systemmonitor.features.apps.AppManagerScreen
import com.systemmonitor.features.dashboard.DashboardScreen
import com.systemmonitor.features.device.DeviceCenterScreen
import com.systemmonitor.features.device.DeviceInfoScreen
import com.systemmonitor.features.files.FileCenterScreen
import com.systemmonitor.features.monitor.SystemMonitorScreen
import com.systemmonitor.features.network.NetworkCenterScreen
import com.systemmonitor.features.network.NetworkDetailsScreen
import com.systemmonitor.features.parental.ParentalControlScreen
import com.systemmonitor.features.profile.ProfileScreen
import com.systemmonitor.features.reports.ReportsScreen
import com.systemmonitor.features.security.SecurityScreen
import com.systemmonitor.features.usage.AppUsageScreen
import com.systemmonitor.features.wellbeing.DigitalWellbeingScreen

@Composable
fun MainScreenContainer(
    appLockManager: AppLockManager
) {
    var currentDestination by remember { mutableStateOf<NavDestination>(NavDestination.Home) }

    // Primary Bottom Navigation Items
    val bottomNavItems = listOf(
        Triple(NavDestination.Home, "Home", Icons.Default.Home),
        Triple(NavDestination.DeviceCenter, "Device", Icons.Default.PhoneAndroid),
        Triple(NavDestination.SecurityCenter, "Security", Icons.Default.Security),
        Triple(NavDestination.NetworkCenter, "Network", Icons.Default.Wifi),
        Triple(NavDestination.FileCenter, "Files", Icons.Default.Folder),
        Triple(NavDestination.AppLock, "App Lock", Icons.Default.Lock),
        Triple(NavDestination.Profile, "Profile", Icons.Default.Person)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0A0F1D),
                contentColor = Color.White
            ) {
                bottomNavItems.forEach { (destination, label, icon) ->
                    val selected = currentDestination == destination
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (selected) Color(0xFF00E5FF) else Color(0xFF64748B)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                color = if (selected) Color(0xFF00E5FF) else Color(0xFF64748B),
                                fontSize = 10.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentDestination) {
                is NavDestination.Home -> DashboardScreen(
                    onNavigateTo = { destination -> currentDestination = destination }
                )
                is NavDestination.DeviceCenter -> DeviceCenterScreen()
                is NavDestination.SecurityCenter -> SecurityScreen(
                    onNavigateToSelectApps = { currentDestination = NavDestination.AppLock }
                )
                is NavDestination.NetworkCenter -> NetworkCenterScreen()
                is NavDestination.FileCenter -> FileCenterScreen()
                is NavDestination.AppLock -> SelectAppsScreen(
                    onBackClick = { currentDestination = NavDestination.Home },
                    appLockManager = appLockManager
                )
                is NavDestination.AppUsage -> AppUsageScreen()
                is NavDestination.DigitalWellbeing -> DigitalWellbeingScreen()
                is NavDestination.ParentalControl -> ParentalControlScreen()
                is NavDestination.Reports -> ReportsScreen()
                is NavDestination.Alerts -> AlertsScreen()
                is NavDestination.Profile -> ProfileScreen()
            }
        }
    }
}
