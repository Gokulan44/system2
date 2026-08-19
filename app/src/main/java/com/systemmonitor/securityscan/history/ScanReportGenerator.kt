package com.systemmonitor.securityscan.history

import com.systemmonitor.securityscan.analysis.ScanResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanReportGenerator @Inject constructor() {
    fun generateMarkdownReport(result: ScanResult): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateStr = sdf.format(Date(result.scanHistory.timestamp))

        val sb = StringBuilder()
        sb.append("# Security Scan Audit Report\n\n")
        sb.append("## General Information\n")
        sb.append("- **App Name**: ${result.scanHistory.targetName}\n")
        sb.append("- **Package**: ${result.scanHistory.scanTarget}\n")
        sb.append("- **Scan Date**: $dateStr\n")
        sb.append("- **Overall Rating**: **${result.scanHistory.score}/100**\n")
        sb.append("- **Verdict**: **${result.scanHistory.verdict}**\n\n")

        sb.append("## Findings Summary\n")
        if (result.findings.isEmpty()) {
            sb.append("No security anomalies or risky permissions were detected. The application appears clean.\n")
        } else {
            sb.append("| Category | Severity | Finding | Details |\n")
            sb.append("| --- | --- | --- | --- |\n")
            for (finding in result.findings) {
                sb.append("| ${finding.category} | **${finding.severity}** | ${finding.title} | ${finding.details} |\n")
            }
        }
        return sb.toString()
    }
}
