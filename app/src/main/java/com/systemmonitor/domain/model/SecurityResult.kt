package com.systemmonitor.domain.model

data class InstalledApp(
    val packageName: String,
    val appName: String,
    val versionName: String?,
    val installerPackageName: String?,
    val isSystemApp: Boolean,
    val totalPermissions: Int,
    val dangerousPermissions: Int,
    val lastUpdatedTimestamp: Long
) {
    /** Not from an app store the OS recognizes as trusted (e.g. Play Store). */
    val isSideloaded: Boolean get() = installerPackageName == null && !isSystemApp
    val hasExcessivePermissions: Boolean get() = dangerousPermissions >= 5
}

data class SecurityResult(
    val scanTimestamp: Long,
    val score: Int,               // 0-100, higher is better
    val totalAppsScanned: Int,
    val sideloadedAppCount: Int,
    val excessivePermissionAppCount: Int,
    val flaggedApps: List<InstalledApp>
)
