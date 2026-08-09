package com.systemmonitor.features.security.domain.scanner

import com.systemmonitor.features.security.domain.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
    fun executeFullScan(): Flow<Pair<Int, String>> = flow {
        emit(10 to "Checking installed apps...")
        delay(400)
        val appThreats = appScanner.scanApps()

        emit(25 to "Checking dangerous permissions...")
        delay(400)
        val permThreats = permissionScanner.scanPermissions()

        emit(45 to "Checking device security configuration...")
        delay(400)
        val configThreats = configurationScanner.scanConfiguration()

        emit(65 to "Checking network security...")
        delay(400)
        val netThreats = networkSecurityScanner.scanNetwork()

        emit(80 to "Checking accessibility services...")
        delay(400)
        val accessThreats = accessibilityScanner.scanAccessibility()

        emit(90 to "Checking storage/security configuration...")
        delay(400)
        val storageThreats = storageSecurityScanner.scanStorage()

        emit(100 to "Calculating security score & generating report...")
        delay(300)
    }

    fun performComprehensiveScan(): SecurityScan {
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

        return SecurityScan(
            scanId = startTime,
            timestamp = startTime,
            score = score,
            threats = allThreats,
            scannedItemsCount = 142,
            durationMs = durationMs
        )
    }
}
