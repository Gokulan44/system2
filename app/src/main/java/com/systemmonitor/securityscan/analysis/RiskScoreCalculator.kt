package com.systemmonitor.securityscan.analysis

import com.systemmonitor.securityscan.rules.RiskScoringRules
import com.systemmonitor.securityscan.rules.ScanFinding
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RiskScoreCalculator @Inject constructor(
    private val scoringRules: RiskScoringRules
) {
    fun calculateScore(findings: List<ScanFinding>): Int {
        var score = 100
        for (finding in findings) {
            score -= scoringRules.getDeduction(finding)
        }
        return score.coerceIn(0, 100)
    }

    fun getVerdict(score: Int): ScanVerdict {
        return ScanVerdict.fromScore(score)
    }
}
