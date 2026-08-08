package com.systemmonitor.navigation

sealed class NavDestination(val route: String) {
    object Home : NavDestination("home")
    object DeviceCenter : NavDestination("device_center")
    object SecurityCenter : NavDestination("security_center")
    object NetworkCenter : NavDestination("network_center")
    object FileCenter : NavDestination("file_center")
    object AppLock : NavDestination("app_lock")
    object AppUsage : NavDestination("app_usage")
    object DigitalWellbeing : NavDestination("digital_wellbeing")
    object ParentalControl : NavDestination("parental_control")
    object Reports : NavDestination("reports")
    object Alerts : NavDestination("alerts")
    object Profile : NavDestination("profile")
}
