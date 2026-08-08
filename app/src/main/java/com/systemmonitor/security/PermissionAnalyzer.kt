package com.systemmonitor.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import com.systemmonitor.domain.model.InstalledApp
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads installed-app metadata and requested permissions entirely from the
 * OS via PackageManager. Classifies a permission as "dangerous" using
 * Android's own PermissionInfo.protectionLevel flag — the same signal the
 * system permission dialog uses — not a third-party threat database.
 */
@Singleton
class PermissionAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val packageManager: PackageManager get() = context.packageManager

    fun scanInstalledApps(includeSystemApps: Boolean = false): List<InstalledApp> {
        val flags = PackageManager.GET_PERMISSIONS
        val packages = packageManager.getInstalledPackages(flags)

        return packages
            .filter { includeSystemApps || !it.isSystemApp() }
            .map { it.toInstalledApp() }
    }

    private fun PackageInfo.isSystemApp(): Boolean =
        (applicationInfo?.flags ?: 0) and ApplicationInfo.FLAG_SYSTEM != 0

    private fun PackageInfo.toInstalledApp(): InstalledApp {
        val requested = requestedPermissions?.toList().orEmpty()
        val dangerousCount = requested.count { isDangerousPermission(it) }
        val appInfo = applicationInfo
        val appName = appInfo?.let { packageManager.getApplicationLabel(it).toString() } ?: packageName
        val installer = runCatching {
            packageManager.getInstallSourceInfo(packageName).installingPackageName
        }.getOrNull()

        return InstalledApp(
            packageName = packageName,
            appName = appName,
            versionName = versionName,
            installerPackageName = installer,
            isSystemApp = isSystemApp(),
            totalPermissions = requested.size,
            dangerousPermissions = dangerousCount,
            lastUpdatedTimestamp = lastUpdateTime
        )
    }

    private fun isDangerousPermission(permissionName: String): Boolean = runCatching {
        val info: PermissionInfo = packageManager.getPermissionInfo(permissionName, 0)
        (info.protectionLevel and PermissionInfo.PROTECTION_MASK_BASE) ==
            PermissionInfo.PROTECTION_DANGEROUS
    }.getOrDefault(false)
}
