package com.systemmonitor.features.security.domain.scanner

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.systemmonitor.features.security.domain.model.ThreatInfo
import com.systemmonitor.features.security.domain.model.ThreatSeverity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scanApps(): List<ThreatInfo> {
        val threats = mutableListOf<ThreatInfo>()
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        for (app in apps) {
            val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            if (!isSystem) {
                val label = pm.getApplicationLabel(app).toString()
                if (label.lowercase().contains("fake") || label.lowercase().contains("hack")) {
                    threats.add(
                        ThreatInfo(
                            id = "app_${app.packageName}",
                            title = "Suspicious Application Detected",
                            description = "Application '$label' matches untrusted threat signatures.",
                            packageName = app.packageName,
                            severity = ThreatSeverity.HIGH,
                            category = "App Security",
                            recommendedAction = "Uninstall application immediately"
                        )
                    )
                }
            }
        }
        return threats
    }
}

@Singleton
class PermissionScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scanPermissions(): List<ThreatInfo> {
        val threats = mutableListOf<ThreatInfo>()
        val pm = context.packageManager
        val dangerousPermissions = listOf(
            android.Manifest.permission.CAMERA to "Camera",
            android.Manifest.permission.RECORD_AUDIO to "Microphone",
            android.Manifest.permission.ACCESS_FINE_LOCATION to "Location",
            android.Manifest.permission.READ_CONTACTS to "Contacts",
            android.Manifest.permission.READ_SMS to "SMS",
            android.Manifest.permission.READ_PHONE_STATE to "Phone"
        )

        val installedPackages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        for (pkg in installedPackages) {
            val isSystem = pkg.applicationInfo != null && (pkg.applicationInfo!!.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            if (!isSystem && pkg.requestedPermissions != null) {
                var sensitiveCount = 0
                for (perm in pkg.requestedPermissions) {
                    if (dangerousPermissions.any { it.first == perm }) {
                        sensitiveCount++
                    }
                }
                if (sensitiveCount >= 3) {
                    val appLabel = pm.getApplicationLabel(pkg.applicationInfo!!).toString()
                    threats.add(
                        ThreatInfo(
                            id = "perm_${pkg.packageName}",
                            title = "Excessive Sensitive Permissions",
                            description = "'$appLabel' requests $sensitiveCount sensitive permissions (Camera/Mic/Location/Contacts).",
                            packageName = pkg.packageName,
                            severity = ThreatSeverity.MEDIUM,
                            category = "Permission Security",
                            recommendedAction = "Review app permissions in Settings"
                        )
                    )
                }
            }
        }
        return threats
    }
}
