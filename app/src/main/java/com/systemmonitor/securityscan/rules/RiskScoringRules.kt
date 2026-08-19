package com.systemmonitor.securityscan.rules

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RiskScoringRules @Inject constructor() {
    fun getDeduction(finding: ScanFinding): Int {
        return when (finding.severity) {
            Severity.LOW -> 2
            Severity.MEDIUM -> 6
            Severity.HIGH -> 16
            Severity.CRITICAL -> 36
        }
    }
}
