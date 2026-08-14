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

    suspend fun resolveThreat(threatId: String, scanId: Long, action: String) {
        val threats = securityScanDao.getThreatsForScan(scanId)
        val threat = threats.find { it.id == threatId }

        when (action.uppercase()) {
            "REMOVE" -> {
                // UI launches uninstallation, repository deletes from local DB
                securityScanDao.deleteThreat(threatId)
            }
            "QUARANTINE" -> {
                // Lock app to prevent execution (App Lock Quarantine)
                threat?.packageName?.let { pkg ->
                    appLockManager.setAppLocked(pkg, threat.title, true)
                }
                securityScanDao.deleteThreat(threatId)
            }
            "IGNORE" -> {
                // Add to persistent whitelist in SharedPreferences
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
            }
            else -> {
                securityScanDao.deleteThreat(threatId)
            }
        }

        val remainingThreatEntities = securityScanDao.getThreatsForScan(scanId)
        val remainingThreats = remainingThreatEntities.map { entity ->
            ThreatInfo(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                packageName = entity.packageName,
                severity = try {
                    ThreatSeverity.valueOf(entity.severity)
                } catch (e: IllegalArgumentException) {
                    ThreatSeverity.MEDIUM
                },
                category = entity.category,
                recommendedAction = entity.recommendedAction
            )
        }

        val existingScan = securityScanDao.getScanById(scanId) ?: return
        val score = ThreatAnalyzer().calculateScore(remainingThreats)

        securityScanDao.insertScan(
            existingScan.copy(
                score = score.score,
                rating = score.rating,
                issuesFoundCount = remainingThreats.size
            )
        )
    }
}
