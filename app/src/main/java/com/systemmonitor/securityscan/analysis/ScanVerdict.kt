package com.systemmonitor.securityscan.analysis

enum class ScanVerdict {
    CLEAN,
    LOW_RISK,
    SUSPICIOUS,
    MALICIOUS;

    companion object {
        fun fromScore(score: Int): ScanVerdict = when {
            score >= 80 -> MALICIOUS
            score >= 50 -> SUSPICIOUS
            score >= 20 -> LOW_RISK
            else -> CLEAN
        }
    }
}