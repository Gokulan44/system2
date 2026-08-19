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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.systemmonitor.applock.manager.AppLockManager
import com.systemmonitor.applock.settings.AppLockSettingsViewModel
import com.systemmonitor.features.intrusion.presentation.IntrusionCenterScreen
import com.systemmonitor.features.intrusion.presentation.IntrusionDetailsScreen
import com.systemmonitor.features.intrusion.presentation.IntrusionViewModel
import com.systemmonitor.applock.ui.ChooseLockMethodScreen
import com.systemmonitor.applock.ui.LockResultScreen
import com.systemmonitor.applock.ui.SetPinScreen
import com.systemmonitor.applock.ui.applock.AppLockDashboardScreen
import com.systemmonitor.applock.ui.applock.ProtectedAppsScreen
import com.systemmonitor.applock.ui.applock.AddAppsScreen
import com.systemmonitor.features.alerts.AlertsScreen
import com.systemmonitor.features.auth.LoginScreen
import com.systemmonitor.features.auth.RegisterScreen
import com.systemmonitor.features.dashboard.DashboardScreen
import com.systemmonitor.features.device.DeviceCenterScreen
import com.systemmonitor.features.device.DeviceInfoScreen
import com.systemmonitor.features.devices.DeviceListScreen
import com.systemmonitor.features.devices.PairLaptopScreen
import com.systemmonitor.features.devices.AddLaptopScreen
import com.systemmonitor.features.devices.LaptopNotFoundScreen
import com.systemmonitor.features.unlock.DeviceUnlockScreen
import com.systemmonitor.features.files.FileCenterScreen
import com.systemmonitor.features.remotepermission.presentation.ResourcePermissionScreen
import com.systemmonitor.features.remotepermission.presentation.PermissionHistoryScreen
import com.systemmonitor.features.remotepermission.presentation.PermissionViewModel
import com.systemmonitor.features.resources.ResourceCenterScreen
import com.systemmonitor.features.resources.DownloadStatusScreen
import com.systemmonitor.features.resources.ResourceViewModel
import com.systemmonitor.features.laptop.LaptopDetailsScreen
import com.systemmonitor.features.laptop.ProcessesScreen
import com.systemmonitor.features.laptop.LaptopUsageScreen
import com.systemmonitor.features.laptop.NetworkScreen as LaptopNetworkScreen
import com.systemmonitor.features.network.NetworkScreen
import com.systemmonitor.features.parental.ParentalControlScreen
import com.systemmonitor.features.profile.presentation.profile.ProfileScreen
import com.systemmonitor.features.profile.presentation.edit.EditProfileScreen
import com.systemmonitor.features.profile.presentation.security.AccountSecurityScreen
import com.systemmonitor.features.profile.presentation.devices.MyDevicesScreen
import com.systemmonitor.features.profile.presentation.login.LoginHistoryScreen
import com.systemmonitor.features.profile.presentation.activity.ActivityHistoryScreen
import com.systemmonitor.features.profile.presentation.HelpSupportScreen
import com.systemmonitor.features.profile.presentation.ProfileAboutScreen
import com.systemmonitor.features.profile.presentation.ProfileNotificationSettingsScreen
import com.systemmonitor.features.profile.presentation.ProfilePreferencesScreen
import com.systemmonitor.features.profile.presentation.ProfilePrivacyScreen
import com.systemmonitor.features.profile.ProfileUpdatedScreen
import com.systemmonitor.features.remote.RemoteControlScreen
import com.systemmonitor.features.reports.ReportsScreen
import com.systemmonitor.features.screen.ScreenViewerScreen
import com.systemmonitor.features.security.domain.model.SecurityScan
import com.systemmonitor.features.security.presentation.dashboard.SecurityDashboardScreen
import com.systemmonitor.features.security.presentation.history.ScanHistoryScreen
import com.systemmonitor.features.security.presentation.result.ResolveActionsScreen
import com.systemmonitor.features.security.presentation.result.ResolveActionsViewModel
import com.systemmonitor.features.security.presentation.result.ScanResultScreen
import com.systemmonitor.features.security.presentation.result.SecurityReportScreen
import com.systemmonitor.securityscan.presentation.SecurityScanScreen
import com.systemmonitor.features.usage.AppUsageScreen
import com.systemmonitor.features.usage.AppUsageViewModel
import com.systemmonitor.features.usage.UsageAnalyticsScreen
import com.systemmonitor.features.usage.UsageDetailsScreen
import com.systemmonitor.features.wellbeing.DigitalWellbeingScreen
import com.systemmonitor.viewmodel.AuthViewModel
import com.systemmonitor.viewmodel.LaptopViewModel
import com.systemmonitor.viewmodel.ScreenViewModel
import com.systemmonitor.vault.presentation.VaultScreen
import com.systemmonitor.features.settings.power.BatteryAnalysisScreen
import com.systemmonitor.domain.model.ConnectionMode

import com.systemmonitor.features.settings.AboutScreen
import com.systemmonitor.features.settings.AdvancedSettingsScreen
import com.systemmonitor.features.settings.BackupSettingsScreen
import com.systemmonitor.features.settings.DeviceSettingsScreen
import com.systemmonitor.features.settings.PrivacySettingsScreen
import com.systemmonitor.features.settings.privacy.PermissionManagerScreen
import com.systemmonitor.features.settings.privacy.DataCollectionScreen
import com.systemmonitor.features.settings.privacy.UsageAccessScreen
import com.systemmonitor.features.settings.privacy.AccessibilitySettingsScreen
import com.systemmonitor.features.settings.RemoteSettingsScreen
import com.systemmonitor.features.settings.ReportSettingsScreen
import com.systemmonitor.features.settings.ScreenSettingsScreen
import com.systemmonitor.features.settings.SettingsScreen
import com.systemmonitor.features.settings.SettingsViewModel
import com.systemmonitor.features.dashboard.DashboardViewModel
import com.systemmonitor.features.settings.monitoring.MonitoringSettingsScreen
import com.systemmonitor.features.settings.notifications.NotificationSettingsScreen
import com.systemmonitor.features.settings.power.PowerSettingsScreen
import com.systemmonitor.features.settings.security.SecuritySettingsScreen
import com.systemmonitor.features.settings.security.AppLockSettingsScreen

@Composable
fun MainScreenContainer(
    appLockManager: AppLockManager,
    authViewModel: AuthViewModel = hiltViewModel(),
    laptopViewModel: LaptopViewModel = hiltViewModel(),
    screenViewModel: ScreenViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    appUsageViewModel: AppUsageViewModel = hiltViewModel(),
    appLockSettingsViewModel: AppLockSettingsViewModel = hiltViewModel(),
    permissionViewModel: PermissionViewModel = hiltViewModel(),
    resourceViewModel: ResourceViewModel = hiltViewModel(),
    intrusionViewModel: IntrusionViewModel = hiltViewModel(),
    resolveActionsViewModel: ResolveActionsViewModel = hiltViewModel(),
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
    initialRoute: String? = null
) {
    var currentDestination by remember { mutableStateOf<NavDestination>(NavDestination.Home) }
    var lastScanResult by remember { mutableStateOf<SecurityScan?>(null) }
    var selectedThreat by remember { mutableStateOf<com.systemmonitor.features.security.domain.model.ThreatInfo?>(null) }
    var simulatedResourceName by remember { mutableStateOf("") }
    var selectedEventId by remember { mutableStateOf("") }
    var activeDownloadRequestId by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(initialRoute) {
        if (initialRoute == "resource_permission") {
            currentDestination = NavDestination.ResourcePermission
        } else if (initialRoute == "resource_center") {
            currentDestination = NavDestination.ResourceCenter
        } else if (initialRoute == "intrusion_center") {
            currentDestination = NavDestination.IntrusionCenter
        }
    }

    var userName by remember { mutableStateOf("Admin") }
    var userEmail by remember { mutableStateOf("admin@systemmonitor.com") }
    var userPhone by remember { mutableStateOf("+1 234 567 890") }
    var userCountry by remember { mutableStateOf("United States") }

    var selectedAppName by remember { mutableStateOf("") }
    var selectedAppDuration by remember { mutableStateOf("") }
    var selectedPackageName by remember { mutableStateOf("") }

    var selectedLockMethod by remember { mutableStateOf("PIN") }

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
                is NavDestination.DeviceCenter -> DeviceCenterScreen(
                    onNavigateToInfo = { currentDestination = NavDestination.DeviceInfo },
                    onBackClick = { currentDestination = NavDestination.Home }
                )
                is NavDestination.DeviceInfo -> DeviceInfoScreen(
                    onBackClick = { currentDestination = NavDestination.DeviceCenter }
                )
                is NavDestination.Laptops -> DeviceListScreen(
                    laptopViewModel = laptopViewModel,
                    onAddDeviceClick = { currentDestination = NavDestination.AddLaptop },
                    onSelectLaptop = { laptop ->
                        laptopViewModel.selectLaptop(laptop)
                        currentDestination = NavDestination.LaptopDetails
                    }
                )
                is NavDestination.AddLaptop -> AddLaptopScreen(
                    laptopViewModel = laptopViewModel,
                    onStatusOnline = { currentDestination = NavDestination.PairLaptop },
                    onStatusError = { currentDestination = NavDestination.LaptopNotFound },
                    onBackClick = { currentDestination = NavDestination.Laptops }
                )
                is NavDestination.PairLaptop -> PairLaptopScreen(
                    laptopViewModel = laptopViewModel,
                    onPairSuccess = { currentDestination = NavDestination.LaptopDetails },
                    onBackClick = { currentDestination = NavDestination.AddLaptop }
                )
                is NavDestination.LaptopNotFound -> LaptopNotFoundScreen(
                    laptopViewModel = laptopViewModel,
                    onTryAgain = { currentDestination = NavDestination.AddLaptop },
                    onSwitchToRemote = {
                        laptopViewModel.pendingConnectionMode = ConnectionMode.REMOTE
                        currentDestination = NavDestination.PairLaptop
                    },
                    onBackClick = { currentDestination = NavDestination.Laptops }
                )
                 is NavDestination.LaptopDetails -> LaptopDetailsScreen(
                    laptopViewModel = laptopViewModel,
                    onNavigateToRemote = { currentDestination = NavDestination.RemoteControl },
                    onNavigateToStream = { currentDestination = NavDestination.ScreenViewer },
                    onNavigateToProcesses = { currentDestination = NavDestination.Processes },
                    onNavigateToUsage = { currentDestination = NavDestination.LaptopUsage },
                    onNavigateToNetwork = { currentDestination = NavDestination.LaptopNetwork },
                    onNavigateToUnlock = { currentDestination = NavDestination.DeviceUnlock },
                    onNavigateToResources = { currentDestination = NavDestination.ResourceCenter },
                    onNavigateToPermissions = { currentDestination = NavDestination.ResourcePermission },
                    onBackClick = { currentDestination = NavDestination.Laptops }
                )
                is NavDestination.DeviceUnlock -> DeviceUnlockScreen(
                    laptopViewModel = laptopViewModel,
                    onBackClick = { currentDestination = NavDestination.LaptopDetails }
                )
                is NavDestination.ResourcePermission -> ResourcePermissionScreen(
                    viewModel = permissionViewModel,
                    onNavigateBack = { currentDestination = NavDestination.LaptopDetails },
                    onNavigateToHistory = { currentDestination = NavDestination.PermissionHistory }
                )
                is NavDestination.PermissionHistory -> PermissionHistoryScreen(
                    viewModel = permissionViewModel,
                    onBackClick = { currentDestination = NavDestination.ResourcePermission }
                )
                is NavDestination.ResourceCenter -> ResourceCenterScreen(
                    resourceViewModel = resourceViewModel,
                    permissionViewModel = permissionViewModel,
                    laptopViewModel = laptopViewModel,
                    onBackClick = { currentDestination = NavDestination.LaptopDetails },
                    onNavigateToStatus = { filename, reqId ->
                        simulatedResourceName = filename
                        activeDownloadRequestId = reqId
                        currentDestination = NavDestination.DownloadStatus
                    }
                )
                is NavDestination.DownloadStatus -> DownloadStatusScreen(
                    resourceViewModel = resourceViewModel,
                    resourceName = simulatedResourceName,
                    requestId = activeDownloadRequestId,
                    onBackClick = { currentDestination = NavDestination.ResourceCenter }
                )
                is NavDestination.IntrusionCenter -> IntrusionCenterScreen(
                    viewModel = intrusionViewModel,
                    onNavigateToDetails = { eventId ->
                        selectedEventId = eventId
                        currentDestination = NavDestination.IntrusionDetails
                    },
                    onBackClick = { currentDestination = NavDestination.Home }
                )
                is NavDestination.IntrusionDetails -> IntrusionDetailsScreen(
                    viewModel = intrusionViewModel,
                    eventId = selectedEventId,
                    onBackClick = { currentDestination = NavDestination.IntrusionCenter }
                )
                is NavDestination.LaptopUsage -> LaptopUsageScreen(
                    laptopViewModel = laptopViewModel,
                    onBackClick = { currentDestination = NavDestination.LaptopDetails }
                )
                is NavDestination.LaptopNetwork -> LaptopNetworkScreen(
                    laptopViewModel = laptopViewModel,
                    onBackClick = { currentDestination = NavDestination.LaptopDetails }
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
                is NavDestination.SecureVault -> VaultScreen(
                    onBackClick = { currentDestination = NavDestination.Home }
                )
                is NavDestination.BatteryAnalysis -> BatteryAnalysisScreen(
                    onBackClick = { currentDestination = NavDestination.Home }
                )
                is NavDestination.Processes -> ProcessesScreen(
                    laptopViewModel = laptopViewModel,
                    onBackClick = { currentDestination = NavDestination.LaptopDetails }
                )
                is NavDestination.SecurityCenter -> SecurityDashboardScreen(
                    onStartScan = { currentDestination = NavDestination.SecurityScanProgress },
                    onNavigateToHistory = { currentDestination = NavDestination.ScanHistory },
                    onNavigateToVault = { currentDestination = NavDestination.SecureVault },
                    onBackClick = { currentDestination = NavDestination.Home }
                )
                is NavDestination.SecurityScanProgress -> SecurityScanScreen(
                    onBackClick = { currentDestination = NavDestination.SecurityCenter }
                )
                is NavDestination.ScanResult -> {
                    val scan = lastScanResult
                    if (scan != null) {
                        ScanResultScreen(
                            scanResult = scan,
                            onViewThreatDetails = { threat ->
                                selectedThreat = threat
                                currentDestination = NavDestination.ResolveActions
                            },
                            onBackToDashboard = {
                                dashboardViewModel.loadRealSystemData()
                                currentDestination = NavDestination.SecurityCenter
                            }
                        )
                    } else {
                        // Fallback: no scan data, go to dashboard
                        currentDestination = NavDestination.SecurityCenter
                    }
                }
                is NavDestination.ScanHistory -> ScanHistoryScreen(
                    onBackClick = { currentDestination = NavDestination.SecurityCenter }
                )
                is NavDestination.ResolveActions -> {
                    val threat = selectedThreat
                    val scanId = lastScanResult?.scanId
                    if (threat != null && scanId != null) {
                        ResolveActionsScreen(
                            threat = threat,
                            onResolveAction = { action ->
                                resolveActionsViewModel.resolveThreat(threat.id, scanId, action) { updatedScan ->
                                    if (updatedScan != null) {
                                        lastScanResult = updatedScan
                                    }
                                    selectedThreat = null
                                    dashboardViewModel.loadRealSystemData()
                                    currentDestination = NavDestination.SecurityReport
                                }
                            },
                            onBackClick = { currentDestination = NavDestination.SecurityReport }
                        )
                    } else {
                        currentDestination = NavDestination.SecurityCenter
                    }
                }
                is NavDestination.SecurityReport -> {
                    val scan = lastScanResult ?: com.systemmonitor.features.security.domain.model.SecurityScan()
                    SecurityReportScreen(
                        scanResult = scan,
                        onBackToDashboard = {
                            lastScanResult = null
                            selectedThreat = null
                            currentDestination = NavDestination.SecurityCenter
                        }
                    )
                }
                is NavDestination.NetworkCenter -> NetworkScreen(
                    onBackClick = { currentDestination = NavDestination.Home }
                )
                is NavDestination.FileCenter -> FileCenterScreen(
                    onNavigateToVault = { currentDestination = NavDestination.SecureVault }
                )
                is NavDestination.AppLock -> AppLockDashboardScreen(
                    appLockManager = appLockManager,
                    onNavigateToSelectApps = { currentDestination = NavDestination.ProtectedApps },
                    onNavigateToSettings = { currentDestination = NavDestination.AppLockSettings },
                    onBackClick = { currentDestination = NavDestination.Home }
                )
                is NavDestination.AppLockSettings -> AppLockSettingsScreen(
                    viewModel = appLockSettingsViewModel,
                    onNavigateToChooseMethod = { currentDestination = NavDestination.ChooseLockMethod },
                    onBackClick = { currentDestination = NavDestination.AppLock }
                )
                is NavDestination.ProtectedApps -> ProtectedAppsScreen(
                    appLockManager = appLockManager,
                    onAddAppsClick = { currentDestination = NavDestination.AddApps },
                    onBackClick = { currentDestination = NavDestination.AppLock }
                )
                is NavDestination.AddApps -> AddAppsScreen(
                    appLockManager = appLockManager,
                    onBackClick = { currentDestination = NavDestination.ProtectedApps }
                )
                is NavDestination.ChooseLockMethod -> ChooseLockMethodScreen(
                    onSelectPin = { selectedLockMethod = "PIN"; currentDestination = NavDestination.SetPin },
                    onSelectOtherMethod = { method ->
                        selectedLockMethod = method
                        currentDestination = NavDestination.SetPin
                    },
                    onBackClick = { currentDestination = NavDestination.AppLock }
                )
                is NavDestination.SetPin -> SetPinScreen(
                    appLockManager = appLockManager,
                    lockMethodName = selectedLockMethod,
                    onPinSetSuccess = { currentDestination = NavDestination.LockResult },
                    onBackClick = { currentDestination = NavDestination.ChooseLockMethod }
                )
                is NavDestination.LockResult -> LockResultScreen(
                    lockMethodName = selectedLockMethod,
                    onDoneClick = { currentDestination = NavDestination.Home }
                )
                is NavDestination.AppUsage -> AppUsageScreen(
                    appUsageViewModel = appUsageViewModel,
                    onSelectApp = { pkgName, name, duration ->
                        selectedPackageName = pkgName
                        selectedAppName = name
                        selectedAppDuration = duration
                        currentDestination = NavDestination.UsageDetails
                    }
                )
                is NavDestination.UsageDetails -> UsageDetailsScreen(
                    appName = selectedAppName,
                    usageTime = selectedAppDuration,
                    packageName = selectedPackageName,
                    appUsageViewModel = appUsageViewModel,
                    onNavigateToAnalytics = { currentDestination = NavDestination.UsageAnalytics },
                    onBackClick = { currentDestination = NavDestination.AppUsage }
                )
                is NavDestination.UsageAnalytics -> UsageAnalyticsScreen(
                    appUsageViewModel = appUsageViewModel,
                    onBackClick = { currentDestination = NavDestination.UsageDetails }
                )
                is NavDestination.DigitalWellbeing -> DigitalWellbeingScreen()
                is NavDestination.ParentalControl -> ParentalControlScreen()
                is NavDestination.Reports -> ReportsScreen()
                is NavDestination.Alerts -> AlertsScreen()
                is NavDestination.Profile -> ProfileScreen(
                    onNavigateToEditProfile = { currentDestination = NavDestination.EditProfile },
                    onNavigateToSecurity = { currentDestination = NavDestination.ProfileSecurity },
                    onNavigateToDevices = { currentDestination = NavDestination.Laptops },
                    onNavigateToLoginHistory = { currentDestination = NavDestination.ProfileLoginHistory },
                    onNavigateToActivityHistory = { currentDestination = NavDestination.ProfileActivityHistory },
                    onNavigateToNotifications = { currentDestination = NavDestination.ProfileNotifications },
                    onNavigateToPrivacy = { currentDestination = NavDestination.ProfilePrivacy },
                    onNavigateToPreferences = { currentDestination = NavDestination.ProfilePreferences },
                    onNavigateToSupport = { currentDestination = NavDestination.ProfileSupport },
                    onNavigateToAbout = { currentDestination = NavDestination.ProfileAbout },
                    onSignOut = { currentDestination = NavDestination.Login },
                    onBackClick = { currentDestination = NavDestination.Home }
                )
                is NavDestination.EditProfile -> EditProfileScreen(
                    onSaveSuccess = { currentDestination = NavDestination.ProfileUpdated },
                    onBackClick = { currentDestination = NavDestination.Profile }
                )
                is NavDestination.ProfileUpdated -> ProfileUpdatedScreen(
                    onBackToProfile = { currentDestination = NavDestination.Profile }
                )
                is NavDestination.ProfileSecurity -> AccountSecurityScreen(
                    onBackClick = { currentDestination = NavDestination.Profile }
                )
                is NavDestination.ProfileDevices -> MyDevicesScreen(
                    onBackClick = { currentDestination = NavDestination.Profile }
                )
                is NavDestination.ProfileLoginHistory -> LoginHistoryScreen(
                    onBackClick = { currentDestination = NavDestination.Profile }
                )
                is NavDestination.ProfileActivityHistory -> ActivityHistoryScreen(
                    onBackClick = { currentDestination = NavDestination.Profile }
                )
                is NavDestination.ProfileNotifications -> ProfileNotificationSettingsScreen(
                    viewModel = settingsViewModel,
                    onBackClick = { currentDestination = NavDestination.Profile }
                )
                is NavDestination.ProfilePrivacy -> ProfilePrivacyScreen(
                    onBackClick = { currentDestination = NavDestination.Profile }
                )
                is NavDestination.ProfilePreferences -> ProfilePreferencesScreen(
                    onBackClick = { currentDestination = NavDestination.Profile }
                )
                is NavDestination.ProfileSupport -> HelpSupportScreen(
                    onBackClick = { currentDestination = NavDestination.Profile }
                )
                is NavDestination.ProfileAbout -> ProfileAboutScreen(
                    onBackClick = { currentDestination = NavDestination.Profile }
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
                    viewModel = settingsViewModel,
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
                    viewModel = settingsViewModel,
                    onBackClick = { currentDestination = NavDestination.Settings }
                )
                is NavDestination.SettingsPrivacy -> PrivacySettingsScreen(
                    viewModel = settingsViewModel,
                    onPermissionManagerClick = { currentDestination = NavDestination.PrivacyPermissionManager },
                    onDataCollectionClick = { currentDestination = NavDestination.PrivacyDataCollection },
                    onUsageAccessClick = { currentDestination = NavDestination.PrivacyUsageAccess },
                    onAccessibilitySettingsClick = { currentDestination = NavDestination.PrivacyAccessibility },
                    onBackClick = { currentDestination = NavDestination.Settings }
                )
                is NavDestination.PrivacyPermissionManager -> PermissionManagerScreen(
                    onBackClick = { currentDestination = NavDestination.SettingsPrivacy }
                )
                is NavDestination.PrivacyDataCollection -> DataCollectionScreen(
                    viewModel = settingsViewModel,
                    onBackClick = { currentDestination = NavDestination.SettingsPrivacy }
                )
                is NavDestination.PrivacyUsageAccess -> UsageAccessScreen(
                    onBackClick = { currentDestination = NavDestination.SettingsPrivacy }
                )
                is NavDestination.PrivacyAccessibility -> AccessibilitySettingsScreen(
                    onBackClick = { currentDestination = NavDestination.SettingsPrivacy }
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
