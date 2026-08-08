package com.systemmonitor.security

import com.systemmonitor.domain.model.InstalledApp
import com.systemmonitor.domain.model.SecurityResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Turns a list of scanned apps into a 0-100 score using local heuristics
 * only: install source and requested-permission counts. Deliberately does
 * NOT call VirusTotal/AbuseIPDB/Safe Browsing/any external reputation API —
 * see AppConfig for why. Weights are simple and documented so they're easy
 * to tune later.
 */
@Singleton
class SecurityScoreEngine @Inject constructor() {

    fun score(apps: List<InstalledApp>): SecurityResult {
        val sideloaded = apps.filter { it.isSideloaded }
        val excessivePermissions = apps.filter { it.hasExcessivePermissions }
        val flagged = (sideloaded + excessivePermissions).distinctBy { it.packageName }

        var score = 100
        score -= sideloaded.size * SIDELOAD_PENALTY
        score -= excessivePermissions.size * EXCESSIVE_PERMISSION_PENALTY
        score = score.coerceIn(0, 100)

        return SecurityResult(
            scanTimestamp = System.currentTimeMillis(),
            score = score,
            totalAppsScanned = apps.size,
            sideloadedAppCount = sideloaded.size,
            excessivePermissionAppCount = excessivePermissions.size,
            flaggedApps = flagged.sortedByDescending { it.dangerousPermissions }
        )
    }

    private companion object {
        const val SIDELOAD_PENALTY = 8
        const val EXCESSIVE_PERMISSION_PENALTY = 4
    }
}
