package com.systemmonitor.features.security.data.repository

import com.systemmonitor.features.security.data.dao.SecurityScanDao
import com.systemmonitor.features.security.data.entity.SecurityScanEntity
import com.systemmonitor.features.security.data.entity.ThreatEntity
import com.systemmonitor.features.security.domain.model.SecurityScan
import com.systemmonitor.features.security.domain.model.SecurityScore
import com.systemmonitor.features.security.domain.model.ThreatInfo
import com.systemmonitor.features.security.domain.model.ThreatSeverity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityRepository @Inject constructor(
    private val securityScanDao: SecurityScanDao
) {
    fun getScanHistory(): Flow<List<SecurityScan>> {
        return securityScanDao.getAllScans().map { entities ->
            entities.map { entity ->
                SecurityScan(
                    scanId = entity.scanId,
                    timestamp = entity.timestamp,
                    score = SecurityScore(score = entity.score, rating = entity.rating, issuesFoundCount = entity.issuesFoundCount),
                    scannedItemsCount = entity.scannedItemsCount,
                    durationMs = entity.durationMs
                )
            }
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
                severity = try { ThreatSeverity.valueOf(entity.severity) } catch (e: Exception) { ThreatSeverity.MEDIUM },
                category = entity.category,
                recommendedAction = entity.recommendedAction
            )
        }
    }
}
