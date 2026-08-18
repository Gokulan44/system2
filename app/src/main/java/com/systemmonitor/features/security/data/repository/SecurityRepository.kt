package com.systemmonitor.features.security.data.repository

import android.content.Context
import com.systemmonitor.applock.manager.AppLockManager
import com.systemmonitor.features.security.data.dao.SecurityScanDao
import com.systemmonitor.features.security.data.entity.SecurityScanEntity
import com.systemmonitor.features.security.data.entity.ThreatEntity
import com.systemmonitor.features.security.domain.model.SecurityScan
import com.systemmonitor.features.security.domain.model.SecurityScore
import com.systemmonitor.features.security.domain.model.ThreatInfo
import com.systemmonitor.features.security.domain.model.ThreatSeverity
import com.systemmonitor.features.security.domain.scanner.ThreatAnalyzer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityRepository @Inject constructor(
    private val securityScanDao: SecurityScanDao,
    private val appLockManager: AppLockManager,
    private val quarantineManager: com.systemmonitor.securityanalysis.isolation.QuarantineManager,
    @ApplicationContext private val context: Context
) {
    /**
     * Returns scan history with threats populated for each scan.
     * Uses a cold Flow<List<SecurityScan>> via getAllScans() and eagerly
     * loads threats for each scan row using the suspend DAO inside map{}.
     *
     * Since Room's getAllScans() returns a Flow, we use .map {} (which uses
     * the coroutine context of the collector — safe with Dispatchers.IO Room uses).
     * Calling a suspend function is allowed inside flow operators on a Room-backed Flow.
     */
    fun getScanHistory(): Flow<List<SecurityScan>> =
        securityScanDao.getAllScans().map { entities ->
            entities.map { entity ->
                val threatEntities = securityScanDao.getThreatsForScan(entity.scanId)
                val threats = threatEntities.map { t ->
                    ThreatInfo(
                        id = t.id,
                        title = t.title,
                        description = t.description,
                        packageName = t.packageName,
                        filePath = t.filePath,
                        severity = try {
                            ThreatSeverity.valueOf(t.severity)
                        } catch (e: IllegalArgumentException) {
                            ThreatSeverity.MEDIUM
                        },
                        category = t.category,
                        recommendedAction = t.recommendedAction
                    )
                }
                SecurityScan(
                    scanId = entity.scanId,
                    timestamp = entity.timestamp,
                    score = SecurityScore(
                        score = entity.score,
                        rating = entity.rating,
                        issuesFoundCount = entity.issuesFoundCount
                    ),
                    threats = threats,
                    scannedItemsCount = entity.scannedItemsCount,
                    durationMs = entity.durationMs
                )
            }
        }

    suspend fun saveScanResult(scan: SecurityScan) {
        securityScanDao.insertScan(
            SecurityScanEntity(
                scanId = scan.scanId,
                timestamp = scan.timestamp,
                score = scan.score.score,
                rating = scan.score.rating,
                issuesFoundCount = scan.score.issuesFoundCount,
                scannedItemsCount = scan.scannedItemsCount,
                durationMs = scan.durationMs
            )
        )

        val threatEntities = scan.threats.map { threat ->
            ThreatEntity(
                id = threat.id,
                scanId = scan.scanId,
                title = threat.title,
                description = threat.description,
                packageName = threat.packageName,
                filePath = threat.filePath,
                severity = threat.severity.name,
                category = threat.category,
                recommendedAction = threat.recommendedAction
            )
        }
        securityScanDao.insertThreats(threatEntities)
    }

    suspend fun getThreatsForScan(scanId: Long): List<ThreatInfo> {
        val entities = securityScanDao.getThreatsForScan(scanId)
        return entities.map { entity ->
            ThreatInfo(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                packageName = entity.packageName,
                filePath = entity.filePath,
                severity = try {
                    ThreatSeverity.valueOf(entity.severity)
                } catch (e: IllegalArgumentException) {
                    ThreatSeverity.MEDIUM
                },
                category = entity.category,
                recommendedAction = entity.recommendedAction
            )
        }
    }

    fun isThreatResolved(threat: ThreatInfo): Boolean {
        val pkg = threat.packageName
        if (!pkg.isNullOrEmpty()) {
            return try {
                context.packageManager.getPackageInfo(pkg, 0)
                false // Package still installed -> NOT resolved
            } catch (e: Exception) {
                true // Package uninstalled -> RESOLVED
            }
        }

        return when (threat.id) {
            "config_adb_enabled" -> {
                val adb = try {
                    android.provider.Settings.Global.getInt(context.contentResolver, android.provider.Settings.Global.ADB_ENABLED, 0) == 1
                } catch (e: Exception) { false }
                !adb
            }
            "config_unknown_sources" -> {
                val unknown = try {
                    android.provider.Settings.Secure.getInt(context.contentResolver, android.provider.Settings.Secure.INSTALL_NON_MARKET_APPS, 0) == 1
                } catch (e: Exception) { false }
                !unknown
            }
            else -> true
        }
    }

    suspend fun resolveThreat(threatId: String, scanId: Long, action: String): Boolean {
        val threats = securityScanDao.getThreatsForScan(scanId)
        val entity = threats.find { it.id == threatId }
        val threat = entity?.let {
            ThreatInfo(
                id = it.id,
                title = it.title,
                description = it.description,
                packageName = it.packageName,
                filePath = it.filePath,
                severity = try { ThreatSeverity.valueOf(it.severity) } catch (_: Exception) { ThreatSeverity.MEDIUM },
                category = it.category,
                recommendedAction = it.recommendedAction
            )
        }

        val isResolved = when (action.uppercase()) {
            "REMOVE" -> {
                if (threat != null) {
                    val resolved = isThreatResolved(threat)
                    if (resolved) {
                        securityScanDao.deleteThreat(threatId)
                    }
                    resolved
                } else {
                    securityScanDao.deleteThreat(threatId)
                    true
                }
            }
            "QUARANTINE" -> {
                if (threat != null && !threat.filePath.isNullOrEmpty()) {
                    val file = java.io.File(threat.filePath)
                    if (file.exists()) {
                        val sha256 = threat.id.removePrefix("file_")
                        quarantineManager.quarantineFile(file, sha256, threat.description)
                    }
                } else {
                    threat?.packageName?.let { pkg ->
                        appLockManager.setAppLocked(pkg, threat.title, true)
                    }
                }
                securityScanDao.deleteThreat(threatId)
                true
            }
            "IGNORE" -> {
                val prefs = context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
                val ignoredIds = prefs.getStringSet("ignored_threat_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
                ignoredIds.add(threatId)
                prefs.edit().putStringSet("ignored_threat_ids", ignoredIds).apply()

                threat?.packageName?.let { pkg ->
                    val ignoredPackages = prefs.getStringSet("ignored_packages", emptySet())?.toMutableSet() ?: mutableSetOf()
                    ignoredPackages.add(pkg)
                    prefs.edit().putStringSet("ignored_packages", ignoredPackages).apply()
                }
                securityScanDao.deleteThreat(threatId)
                true
            }
            else -> {
                securityScanDao.deleteThreat(threatId)
                true
            }
        }

        val remainingThreatEntities = securityScanDao.getThreatsForScan(scanId)
        val existingScan = securityScanDao.getScanById(scanId)
        if (existingScan != null) {
            securityScanDao.insertScan(
                existingScan.copy(
                    issuesFoundCount = remainingThreatEntities.size
                )
            )
        }
        return isResolved
    }
}
