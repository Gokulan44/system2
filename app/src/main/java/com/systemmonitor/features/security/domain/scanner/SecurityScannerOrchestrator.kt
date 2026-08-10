package com.systemmonitor.features.security.domain.scanner

import com.systemmonitor.features.security.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThreatAnalyzer @Inject constructor() {
    fun calculateScore(threats: List<ThreatInfo>): SecurityScore {
        var score = 100
        var critical = 0
        var high = 0
        var medium = 0
        var low = 0

        for (threat in threats) {
            when (threat.severity) {
                ThreatSeverity.CRITICAL -> {
                    score -= 30
                    critical++
                }
                ThreatSeverity.HIGH -> {
                    score -= 15
                    high++
                }
                ThreatSeverity.MEDIUM -> {
                    score -= 8
                    medium++
                }
                ThreatSeverity.LOW -> {
                    score -= 3
                    low++
                }
            }
        }

        score = score.coerceIn(0, 100)

        val rating = when {
            score >= 90 -> "EXCELLENT"
            score >= 75 -> "GOOD"
            score >= 60 -> "FAIR"
            score >= 40 -> "POOR"
            else -> "DANGEROUS"
        }

        return SecurityScore(
            score = score,
            rating = rating,
            issuesFoundCount = threats.size,
            criticalCount = critical,
            highCount = high,
            mediumCount = medium,
            lowCount = low
        )
    }
}

/**
 * ScanResult bundles progress + the collected threats at each step.
 * emitted by executeFullScan() so the ViewModel has both UI feedback
 * AND the final threat list from a single pass — no double-scanning.
 */
data class ScanProgress(
    val percent: Int,
    val stepText: String,
    val collectedThreats: List<ThreatInfo> = emptyList(),
    val isFinished: Boolean = false,
    val finalScan: SecurityScan? = null
)

@Singleton
class SecurityScanner @Inject constructor(
    private val appScanner: AppScanner,
    private val permissionScanner: PermissionScanner,
    private val configurationScanner: ConfigurationScanner,
    private val networkSecurityScanner: NetworkSecurityScanner,
    private val storageSecurityScanner: StorageSecurityScanner,
    private val accessibilityScanner: AccessibilityScanner,
    private val threatAnalyzer: ThreatAnalyzer
) {
    /**
     * Single-pass scan that emits UI progress AND collects all threats.
     * The final emission has isFinished=true and finalScan populated.
     * Callers should NOT call performComprehensiveScan() separately.
     */
    fun executeFullScan(): Flow<ScanProgress> = flow {
        val startTime = System.currentTimeMillis()
        val allThreats = mutableListOf<ThreatInfo>()

        emit(ScanProgress(10, "Checking installed apps..."))
        delay(400)
        val appThreats = withContext(Dispatchers.IO) { appScanner.scanApps() }
        allThreats.addAll(appThreats)

        emit(ScanProgress(25, "Checking dangerous permissions..."))
        delay(400)
        val permThreats = withContext(Dispatchers.IO) { permissionScanner.scanPermissions() }
        allThreats.addAll(permThreats)

        emit(ScanProgress(45, "Checking device security configuration..."))
        delay(400)
        val configThreats = withContext(Dispatchers.IO) { configurationScanner.scanConfiguration() }
        allThreats.addAll(configThreats)

        emit(ScanProgress(65, "Checking network security..."))
        delay(400)
        val netThreats = withContext(Dispatchers.IO) { networkSecurityScanner.scanNetwork() }
        allThreats.addAll(netThreats)

        emit(ScanProgress(80, "Checking accessibility services..."))
        delay(400)
        val accessThreats = withContext(Dispatchers.IO) { accessibilityScanner.scanAccessibility() }
        allThreats.addAll(accessThreats)

        emit(ScanProgress(90, "Checking storage/security configuration..."))
        delay(400)
        val storageThreats = withContext(Dispatchers.IO) { storageSecurityScanner.scanStorage() }
        allThreats.addAll(storageThreats)

        emit(ScanProgress(98, "Calculating security score & generating report..."))
        delay(300)

        val score = threatAnalyzer.calculateScore(allThreats)
        val durationMs = System.currentTimeMillis() - startTime

        val finalScan = SecurityScan(
            scanId = startTime,
            timestamp = startTime,
            score = score,
            threats = allThreats.toList(),
            scannedItemsCount = 142,
            durationMs = durationMs
        )

        emit(ScanProgress(100, "Scan complete!", isFinished = true, finalScan = finalScan))
    }

    /**
     * Legacy synchronous-style scan — kept for the background worker only.
     * Runs all scanners on the calling coroutine; callers must dispatch to IO.
     */
    suspend fun performComprehensiveScan(): SecurityScan = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val allThreats = mutableListOf<ThreatInfo>()

        allThreats.addAll(appScanner.scanApps())
        allThreats.addAll(permissionScanner.scanPermissions())
        allThreats.addAll(configurationScanner.scanConfiguration())
        allThreats.addAll(networkSecurityScanner.scanNetwork())
        allThreats.addAll(accessibilityScanner.scanAccessibility())
        allThreats.addAll(storageSecurityScanner.scanStorage())

        val score = threatAnalyzer.calculateScore(allThreats)
        val durationMs = System.currentTimeMillis() - startTime

        SecurityScan(
            scanId = startTime,
            timestamp = startTime,
            score = score,
            threats = allThreats,
            scannedItemsCount = 142,
            durationMs = durationMs
        )
    }
}
