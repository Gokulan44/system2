package com.systemmonitor.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.systemmonitor.applock.manager.AppLockManager
import com.systemmonitor.applock.ui.ChooseLockMethodScreen
import com.systemmonitor.applock.ui.LockResultScreen
import com.systemmonitor.applock.ui.SelectAppsScreen
import com.systemmonitor.applock.ui.SetPinScreen
import com.systemmonitor.features.alerts.AlertsScreen
import com.systemmonitor.features.auth.LoginScreen
import com.systemmonitor.features.auth.RegisterScreen
import com.systemmonitor.features.dashboard.DashboardScreen
import com.systemmonitor.features.device.DeviceCenterScreen
import com.systemmonitor.features.devices.DeviceListScreen
import com.systemmonitor.features.devices.PairLaptopScreen
import com.systemmonitor.features.files.FileCenterScreen
import com.systemmonitor.features.laptop.LaptopDetailsScreen
import com.systemmonitor.features.laptop.ProcessesScreen
import com.systemmonitor.features.network.NetworkCenterScreen
import com.systemmonitor.features.parental.ParentalControlScreen
import com.systemmonitor.features.profile.EditProfileScreen
import com.systemmonitor.features.profile.ProfileScreen
import com.systemmonitor.features.profile.ProfileUpdatedScreen
import com.systemmonitor.features.remote.RemoteControlScreen
import com.systemmonitor.features.reports.ReportsScreen
import com.systemmonitor.features.screen.ScreenViewerScreen
import com.systemmonitor.features.security.SecurityScreen
import com.systemmonitor.features.usage.AppUsageScreen
import com.systemmonitor.features.usage.UsageAnalyticsScreen
import com.systemmonitor.features.usage.UsageDetailsScreen
import com.systemmonitor.features.wellbeing.DigitalWellbeingScreen
import com.systemmonitor.viewmodel.AuthViewModel
import com.systemmonitor.viewmodel.LaptopViewModel
import com.systemmonitor.viewmodel.ScreenViewModel

import com.systemmonitor.features.settings.AboutScreen
import com.systemmonitor.features.settings.AdvancedSettingsScreen
import com.systemmonitor.features.settings.BackupSettingsScreen
import com.systemmonitor.features.settings.DeviceSettingsScreen
import com.systemmonitor.features.settings.PrivacySettingsScreen
import com.systemmonitor.features.settings.RemoteSettingsScreen
import com.systemmonitor.features.settings.ReportSettingsScreen
import com.systemmonitor.features.settings.ScreenSettingsScreen
import com.systemmonitor.features.settings.SettingsScreen
import com.systemmonitor.features.settings.SettingsViewModel
import com.systemmonitor.features.settings.monitoring.MonitoringSettingsScreen
import com.systemmonitor.features.settings.notifications.NotificationSettingsScreen
import com.systemmonitor.features.settings.power.PowerSettingsScreen
import com.systemmonitor.features.settings.security.SecuritySettingsScreen

@Composable
fun MainScreenContainer(
    appLockManager: AppLockManager,
    laptopViewModel: LaptopViewModel = hiltViewModel(),
    screenViewModel: ScreenViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    var currentDestination by remember { mutableStateOf<NavDestination>(NavDestination.Home) }

    var userName by remember { mutableStateOf("Admin") }
    var userEmail by remember { mutableStateOf("admin@systemmonitor.com") }
    var userPhone by remember { mutableStateOf("+1 234 567 890") }
    var userCountry by remember { mutableStateOf("United States") }

    var selectedAppName by remember { mutableStateOf("YouTube") }
    var selectedAppDuration by remember { mutableStateOf("1h 20m") }

    // Bottom Navigation Bar Items (matching image 6-tab navigation)
    val bottomNavItems = listOf(
        Triple(NavDestination.Home, "Home", Icons.Default.Home),
        Triple(NavDestination.DeviceCenter, "Device", Icons.Default.PhoneAndroid),
        Triple(NavDestination.AppUsage, "Apps", Icons.Default.Folder),
        Triple(NavDestination.SecurityCenter, "Security", Icons.Default.Security),
        Triple(NavDestination.Reports, "Reports", Icons.Default.Assessment),
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
                is NavDestination.DeviceCenter -> DeviceListScreen(
                    laptopViewModel = laptopViewModel,
                    onAddDeviceClick = { currentDestination = NavDestination.PairLaptop },
                    onSelectLaptop = { laptop ->
                        laptopViewModel.selectLaptop(laptop)
                        currentDestination = NavDestination.LaptopDetails
                    }
                )
                is NavDestination.PairLaptop -> PairLaptopScreen(
                    laptopViewModel = laptopViewModel,
                    onPairSuccess = { currentDestination = NavDestination.LaptopDetails },
                    onBackClick = { currentDestination = NavDestination.DeviceCenter }
                )
                is NavDestination.LaptopDetails -> LaptopDetailsScreen(
                    laptopViewModel = laptopViewModel,
                    onNavigateToRemote = { currentDestination = NavDestination.RemoteControl },
                    onNavigateToStream = { currentDestination = NavDestination.ScreenViewer },
                    onNavigateToProcesses = { currentDestination = NavDestination.Processes },
                    onBackClick = { currentDestination = NavDestination.DeviceCenter }
                )
                is NavDestination.RemoteControl -> RemoteControlScreen(
                    laptopViewModel = laptopViewModel,
                    onBackClick = { currentDestination = NavDestination.LaptopDetails }
                )
                is NavDestination.ScreenViewer -> ScreenViewerScreen(
                    laptopViewModel = laptopViewModel,
                    screenViewModel = screenViewModel,
                    onBackClick = { currentDestination = NavDestination.LaptopDetails }
                )
                is NavDestination.Processes -> ProcessesScreen(
                    onBackClick = { currentDestination = NavDestination.LaptopDetails }
                )
                is NavDestination.SecurityCenter -> SecurityScreen(
                    onNavigateToSelectApps = { currentDestination = NavDestination.AppLock }
                )
                is NavDestination.NetworkCenter -> NetworkCenterScreen(
                    onBackClick = { currentDestination = NavDestination.Home }
                )
                is NavDestination.FileCenter -> FileCenterScreen()
                is NavDestination.AppLock -> SelectAppsScreen(
                    onBackClick = { currentDestination = NavDestination.Home },
                    onNavigateToChooseMethod = { currentDestination = NavDestination.ChooseLockMethod },
                    appLockManager = appLockManager
                )
                is NavDestination.ChooseLockMethod -> ChooseLockMethodScreen(
                    onSelectPin = { currentDestination = NavDestination.SetPin },
                    onSelectOtherMethod = { currentDestination = NavDestination.LockResult },
                    onBackClick = { currentDestination = NavDestination.AppLock }
                )
                is NavDestination.SetPin -> SetPinScreen(
                    onPinSetSuccess = { currentDestination = NavDestination.LockResult },
                    onBackClick = { currentDestination = NavDestination.ChooseLockMethod }
                )
                is NavDestination.LockResult -> LockResultScreen(
                    onDoneClick = { currentDestination = NavDestination.Home }
                )
                is NavDestination.AppUsage -> AppUsageScreen(
                    onSelectApp = { name, duration ->
                        selectedAppName = name
                        selectedAppDuration = duration
                        currentDestination = NavDestination.UsageDetails
                    }
                )
                is NavDestination.UsageDetails -> UsageDetailsScreen(
                    appName = selectedAppName,
                    usageTime = selectedAppDuration,
                    onNavigateToAnalytics = { currentDestination = NavDestination.UsageAnalytics },
                    onBackClick = { currentDestination = NavDestination.AppUsage }
                )
                is NavDestination.UsageAnalytics -> UsageAnalyticsScreen(
                    onBackClick = { currentDestination = NavDestination.UsageDetails }
                )
                is NavDestination.DigitalWellbeing -> DigitalWellbeingScreen()
                is NavDestination.ParentalControl -> ParentalControlScreen()
                is NavDestination.Reports -> ReportsScreen()
                is NavDestination.Alerts -> AlertsScreen()
                is NavDestination.Profile -> ProfileScreen(
                    userName = userName,
                    userEmail = userEmail,
                    onNavigateToEditProfile = { currentDestination = NavDestination.EditProfile },
                    onNavigateToDevices = { currentDestination = NavDestination.DeviceCenter }
                )
                is NavDestination.EditProfile -> EditProfileScreen(
                    currentName = userName,
                    currentEmail = userEmail,
                    currentPhone = userPhone,
                    currentCountry = userCountry,
                    onSaveProfile = { name, email, phone, country ->
                        userName = name
                        userEmail = email
                        userPhone = phone
                        userCountry = country
                        currentDestination = NavDestination.ProfileUpdated
                    },
                    onBackClick = { currentDestination = NavDestination.Profile }
                )
                is NavDestination.ProfileUpdated -> ProfileUpdatedScreen(
                    onBackToProfile = { currentDestination = NavDestination.Profile }
                )
                is NavDestination.Login -> LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = { currentDestination = NavDestination.Home },
                    onNavigateToRegister = { currentDestination = NavDestination.Register }
                )
                is NavDestination.Register -> RegisterScreen(
                    authViewModel = authViewModel,
                    onRegisterSuccess = { currentDestination = NavDestination.Home },
                    onNavigateToLogin = { currentDestination = NavDestination.Login }
                )
                is NavDestination.Settings -> SettingsScreen(
                    onNavigateToCategory = { route ->
                        currentDestination = when (route) {
                            "settings_security" -> NavDestination.SettingsSecurity
                            "settings_notifications" -> NavDestination.SettingsNotifications
                            "settings_power" -> NavDestination.SettingsPower
                            "settings_screen" -> NavDestination.SettingsScreen
                            "settings_monitoring" -> NavDestination.SettingsMonitoring
                            "settings_device" -> NavDestination.SettingsDevice
                            "settings_remote" -> NavDestination.SettingsRemote
                            "settings_privacy" -> NavDestination.SettingsPrivacy
                            "settings_reports" -> NavDestination.SettingsReports
                            "settings_backup" -> NavDestination.SettingsBackup
                            "settings_advanced" -> NavDestination.SettingsAdvanced
                            "settings_account" -> NavDestination.Profile
                            else -> NavDestination.Settings
                        }
                    },
                    onBackClick = { currentDestination = NavDestination.Home }
                )
                is NavDestination.SettingsSecurity -> SecuritySettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateToAppLock = { currentDestination = NavDestination.AppLock },
                    onBackClick = { currentDestination = NavDestination.Settings }
                )
                is NavDestination.SettingsNotifications -> NotificationSettingsScreen(
                    viewModel = settingsViewModel,
                    onBackClick = { currentDestination = NavDestination.Settings }
                )
                is NavDestination.SettingsPower -> PowerSettingsScreen(
                    viewModel = settingsViewModel,
                    onBackClick = { currentDestination = NavDestination.Settings }
                )
                is NavDestination.SettingsScreen -> ScreenSettingsScreen(
                    onBackClick = { currentDestination = NavDestination.Settings }
                )
                is NavDestination.SettingsMonitoring -> MonitoringSettingsScreen(
                    viewModel = settingsViewModel,
                    onBackClick = { currentDestination = NavDestination.Settings }
                )
                is NavDestination.SettingsDevice -> DeviceSettingsScreen(
                    onBackClick = { currentDestination = NavDestination.Settings }
                )
                is NavDestination.SettingsRemote -> RemoteSettingsScreen(
                    onBackClick = { currentDestination = NavDestination.Settings }
                )
                is NavDestination.SettingsPrivacy -> PrivacySettingsScreen(
                    onBackClick = { currentDestination = NavDestination.Settings }
                )
                is NavDestination.SettingsReports -> ReportSettingsScreen(
                    onBackClick = { currentDestination = NavDestination.Settings }
                )
                is NavDestination.SettingsBackup -> BackupSettingsScreen(
                    onBackClick = { currentDestination = NavDestination.Settings }
                )
                is NavDestination.SettingsAdvanced -> AdvancedSettingsScreen(
                    onBackClick = { currentDestination = NavDestination.Settings }
                )
                is NavDestination.SettingsAbout -> AboutScreen(
                    onBackClick = { currentDestination = NavDestination.Settings }
                )
                else -> DashboardScreen(onNavigateTo = { currentDestination = it })
            }
        }
    }
}
