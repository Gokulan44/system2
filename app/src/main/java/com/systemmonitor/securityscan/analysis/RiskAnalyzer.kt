package com.systemmonitor.securityscan.analysis

import android.content.Context
import android.content.pm.PackageManager
import com.systemmonitor.securityscan.database.entity.FindingEntity
import com.systemmonitor.securityscan.database.entity.ScanHistoryEntity
import com.systemmonitor.securityscan.input.ScanTarget
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

data class ScanResult(
    val scanHistory: ScanHistoryEntity,
    val findings: List<FindingEntity>
)

// Permissions considered dangerous/high-risk if requested together or by non-system apps
private val HIGH_RISK_PERMISSIONS = mapOf(
    "android.permission.READ_SMS" to 25,
    "android.permission.SEND_SMS" to 25,
    "android.permission.RECEIVE_SMS" to 20,
    "android.permission.READ_CONTACTS" to 10,
    "android.permission.CAMERA" to 8,
    "android.permission.RECORD_AUDIO" to 12,
    "android.permission.ACCESS_FINE_LOCATION" to 10,
    "android.permission.SYSTEM_ALERT_WINDOW" to 15,
    "android.permission.REQUEST_INSTALL_PACKAGES" to 20,
    "android.permission.BIND_ACCESSIBILITY_SERVICE" to 25,
    "android.permission.BIND_DEVICE_ADMIN" to 25,
    "android.permission.WRITE_SECURE_SETTINGS" to 30
)

@Singleton
class RiskAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val packageManager: PackageManager = context.packageManager

    suspend fun analyze(target: ScanTarget): ScanResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val findings = mutableListOf<FindingEntity>()

        val requestedPermissions = getRequestedPermissions(target.packageName)
        var score = 0

        // Permission-based findings
        for (permission in requestedPermissions) {
            val risk = HIGH_RISK_PERMISSIONS[permission]
            if (risk != null) {
                score += risk
                findings.add(
                    FindingEntity(
                        scanId = 0, // patched in after ScanHistoryEntity gets its id
                        category = "PERMISSION",
                        severity = severityForScore(risk),
                        title = "Sensitive permission requested",
                        details = "App requests $permission",
                        componentName = permission
                    )
                )
            }
        }

        // Non-system app requesting install-packages or accessibility is extra suspicious
        if (!target.isSystemApp &&
            requestedPermissions.contains("android.permission.BIND_ACCESSIBILITY_SERVICE")
        ) {
            score += 15
            findings.add(
                FindingEntity(
                    scanId = 0,
                    category = "COMPONENT",
                    severity = "HIGH",
                    title = "Non-system app uses Accessibility Service",
                    details = "Accessibility APIs are commonly abused for overlay/keylogging attacks",
                    componentName = target.packageName
                )
            )
        }

        val finalScore = score.coerceIn(0, 100)
        val verdict = ScanVerdict.fromScore(finalScore)
        val endTime = System.currentTimeMillis()

        val scanHistory = ScanHistoryEntity(
            scanTarget = target.packageName,
            targetName = target.appName,
            timestamp = startTime,
            score = finalScore,
            verdict = verdict.name,
            scannedItemsCount = requestedPermissions.size,
            durationMs = endTime - startTime
        )

        ScanResult(scanHistory = scanHistory, findings = findings)
    }

    private fun getRequestedPermissions(packageName: String): List<String> {
        return try {
            val packageInfo = packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_PERMISSIONS
            )
            packageInfo.requestedPermissions?.toList() ?: emptyList()
        } catch (e: PackageManager.NameNotFoundException) {
            emptyList()
        }
    }

    private fun severityForScore(score: Int): String = when {
        score >= 25 -> "HIGH"
        score >= 12 -> "MEDIUM"
        else -> "LOW"
    }

    @Suppress("unused")
    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}