package com.systemmonitor.securityanalysis.apkanalysis

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class PermissionAudit(
    val totalPermissions: Int,
    val dangerousPermissions: List<String>,
    val riskScore: Int
)

@Singleton
class ApkPermissionAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dangerousList = setOf(
        "android.permission.READ_SMS",
        "android.permission.RECEIVE_SMS",
        "android.permission.SEND_SMS",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO",
        "android.permission.SYSTEM_ALERT_WINDOW",
        "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.REQUEST_INSTALL_PACKAGES"
    )

    fun analyzePackage(packageName: String): PermissionAudit {
        return try {
            val pm = context.packageManager
            val info = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            val requested = info.requestedPermissions ?: emptyArray()

            val dangerousFound = requested.filter { it in dangerousList }
            val score = (dangerousFound.size * 10).coerceAtMost(100)

            PermissionAudit(
                totalPermissions = requested.size,
                dangerousPermissions = dangerousFound,
                riskScore = score
            )
        } catch (e: Exception) {
            PermissionAudit(0, emptyList(), 0)
        }
    }
}
