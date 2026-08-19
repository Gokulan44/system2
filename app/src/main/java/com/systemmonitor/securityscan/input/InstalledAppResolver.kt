package com.systemmonitor.securityscan.input

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstalledAppResolver @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val packageManager: PackageManager = context.packageManager

    suspend fun getInstalledApps(includeSystemApps: Boolean = false): List<ScanTarget> =
        withContext(Dispatchers.IO) {
            val flags = PackageManager.GET_META_DATA
            val packages = packageManager.getInstalledApplications(flags)

            packages
                .filter { includeSystemApps || !isSystemApp(it) }
                .mapNotNull { appInfo -> toScanTarget(appInfo) }
        }

    suspend fun resolveByPackageName(packageName: String): ScanTarget? =
        withContext(Dispatchers.IO) {
            try {
                val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                toScanTarget(appInfo)
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
        }

    private fun toScanTarget(appInfo: ApplicationInfo): ScanTarget? {
        return try {
            val packageInfo = packageManager.getPackageInfo(appInfo.packageName, 0)
            ScanTarget(
                packageName = appInfo.packageName,
                appName = packageManager.getApplicationLabel(appInfo).toString(),
                apkPath = appInfo.sourceDir,
                isSystemApp = isSystemApp(appInfo),
                versionName = packageInfo.versionName,
                versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode.toLong()
                }
            )
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    private fun isSystemApp(appInfo: ApplicationInfo): Boolean =
        (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
}