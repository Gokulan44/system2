package com.systemmonitor.features.security.domain.model

enum class ScanStatus {
    IDLE,
    SCANNING,
    COMPLETED,
    FAILED
}

enum class ThreatSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

data class ThreatInfo(
    val id: String,
    val title: String,
    val description: String,
    val packageName: String? = null,
    val severity: ThreatSeverity,
    val category: String, // App, Permission, Configuration, Network, Storage, Accessibility
    val recommendedAction: String
)

data class ScanStepItem(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val isCurrent: Boolean = false
)

data class SecurityScore(
    val score: Int = 100, // 0 - 100
    val rating: String = "EXCELLENT", // EXCELLENT, GOOD, FAIR, POOR, DANGEROUS
    val issuesFoundCount: Int = 0,
    val criticalCount: Int = 0,
    val highCount: Int = 0,
    val mediumCount: Int = 0,
    val lowCount: Int = 0
)

data class ScanItem(
    val id: String,
    val name: String,
    val category: String,
    val isSafe: Boolean,
    val details: String
)

data class SecurityScan(
    val scanId: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis(),
    val score: SecurityScore = SecurityScore(),
    val threats: List<ThreatInfo> = emptyList(),
    val scannedItemsCount: Int = 0,
    val durationMs: Long = 0L
)
