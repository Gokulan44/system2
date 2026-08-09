package com.systemmonitor.applock.installedapps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.systemmonitor.applock.installedapps.model.InstalledApp
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstalledAppScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scanInstalledApps(): List<InstalledApp> {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return packages
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
            .map { appInfo ->
                val appName = pm.getApplicationLabel(appInfo).toString()
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val icon = try { pm.getApplicationIcon(appInfo) } catch (e: Exception) { null }
                InstalledApp(
                    packageName = appInfo.packageName,
                    appName = appName,
                    icon = icon,
                    isSystemApp = isSystem,
                    isLocked = false
                )
            }
            .sortedBy { it.appName }
    }
}
