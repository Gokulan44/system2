package com.systemmonitor.securityscan.rules

import com.systemmonitor.securityscan.static.StaticAnalysisResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkRules @Inject constructor() {
    fun evaluate(result: StaticAnalysisResult): List<ScanFinding> {
        val findings = mutableListOf<ScanFinding>()

        if (result.usesCleartextTraffic) {
            findings.add(
                ScanFinding(
                    category = "Network",
                    severity = Severity.MEDIUM,
                    title = "Cleartext Traffic Allowed",
                    details = "This application allows unencrypted HTTP network traffic. Man-in-the-middle attacks could intercept transferred user credentials and data.",
                    componentName = "usesCleartextTraffic"
                )
            )
        }

        val httpUrls = result.stringAnalysis?.httpUrls ?: emptyList()
        if (httpUrls.isNotEmpty()) {
            findings.add(
                ScanFinding(
                    category = "Network",
                    severity = Severity.LOW,
                    title = "Unencrypted HTTP Endpoints",
                    details = "Found ${httpUrls.size} hardcoded HTTP urls in binary. Connections to these addresses are not secured.",
                    componentName = httpUrls.take(3).joinToString(", ")
                )
            )
        }

        return findings
    }
}
