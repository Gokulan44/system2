package com.systemmonitor.securityscan.rules

enum class Severity {
    LOW, MEDIUM, HIGH, CRITICAL
}

data class ScanFinding(
    val category: String,
    val severity: Severity,
    val title: String,
    val details: String,
    val componentName: String? = null
)
