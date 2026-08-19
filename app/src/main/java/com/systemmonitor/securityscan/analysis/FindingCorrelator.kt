package com.systemmonitor.securityscan.analysis

import com.systemmonitor.securityscan.rules.ScanFinding
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FindingCorrelator @Inject constructor() {
    fun correlate(findings: List<ScanFinding>): List<ScanFinding> {
        // Group by category, deduplicate, and sort by severity (Critical first)
        return findings
            .distinctBy { it.category + it.title + it.componentName }
            .sortedWith(compareByDescending { it.severity.ordinal })
    }
}
