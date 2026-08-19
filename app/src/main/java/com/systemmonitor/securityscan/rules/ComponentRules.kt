package com.systemmonitor.securityscan.rules

import com.systemmonitor.securityscan.staticscan.StaticAnalysisResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComponentRules @Inject constructor() {
    fun evaluate(result: StaticAnalysisResult): List<ScanFinding> {
        val findings = mutableListOf<ScanFinding>()

        // Analyze Receivers (high risk if exported and unguarded because anyone can send broadcasts)
        for (receiver in result.receivers) {
            if (receiver.exported && receiver.permission.isNullOrEmpty()) {
                findings.add(
                    ScanFinding(
                        category = "Component",
                        severity = Severity.HIGH,
                        title = "Unguarded Exported Receiver",
                        details = "Broadcast Receiver is exported but has no permission guard. Threat actors can send malicious intents to trigger this receiver.",
                        componentName = receiver.name
                    )
                )
            }
        }

        // Analyze Providers (high risk if exported and unguarded because anyone can query/write app data)
        for (provider in result.providers) {
            if (provider.exported && provider.permission.isNullOrEmpty()) {
                findings.add(
                    ScanFinding(
                        category = "Component",
                        severity = Severity.CRITICAL,
                        title = "Unguarded Exported Provider",
                        details = "Content Provider is exported but has no permission guard. Database records or files could be read/written by other apps.",
                        componentName = provider.name
                    )
                )
            }
        }

        // Analyze Services (medium risk if exported and unguarded because other apps can start/bind to them)
        for (service in result.services) {
            if (service.exported && service.permission.isNullOrEmpty()) {
                findings.add(
                    ScanFinding(
                        category = "Component",
                        severity = Severity.MEDIUM,
                        title = "Unguarded Exported Service",
                        details = "Service is exported without a guarding permission. Background procedures can be bound or hijacked by other apps.",
                        componentName = service.name
                    )
                )
            }
        }

        return findings
    }
}
