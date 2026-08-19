package com.systemmonitor.securityscan.rules

import com.systemmonitor.securityscan.static.StaticAnalysisResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObfuscationRules @Inject constructor() {
    fun evaluate(result: StaticAnalysisResult): List<ScanFinding> {
        val findings = mutableListOf<ScanFinding>()
        val dexResult = result.dexAnalysis ?: return findings

        if (dexResult.hasDynamicLoading) {
            findings.add(
                ScanFinding(
                    category = "Obfuscation",
                    severity = Severity.HIGH,
                    title = "Dynamic Code Loading",
                    details = "This application uses dynamic loaders (DexClassLoader/PathClassLoader) to execute external code. This is a common method for downloading malware payloads at runtime.",
                    componentName = "DexClassLoader"
                )
            )
        }

        if (dexResult.hasReflection) {
            findings.add(
                ScanFinding(
                    category = "Obfuscation",
                    severity = Severity.LOW,
                    title = "Reflection Utilized",
                    details = "Reflection APIs are used to call class methods dynamically. This reduces code audit visibility.",
                    componentName = "Reflection"
                )
            )
        }

        if (dexResult.hasAntiDebugging) {
            findings.add(
                ScanFinding(
                    category = "Obfuscation",
                    severity = Severity.MEDIUM,
                    title = "Anti-Debugging Detection",
                    details = "Attempts to check if a debugger is attached. While it protects app logic, malware also uses this to bypass automated analysis sandboxes.",
                    componentName = "isDebuggerConnected"
                )
            )
        }

        return findings
    }
}
