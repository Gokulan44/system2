package com.systemmonitor.securityscan.rules

import com.systemmonitor.securityscan.static.StaticAnalysisResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuleEngine @Inject constructor(
    private val permissionRules: PermissionRules,
    private val componentRules: ComponentRules,
    private val networkRules: NetworkRules,
    private val obfuscationRules: ObfuscationRules,
    private val knownMalwareRules: KnownMalwareRules
) {
    fun evaluate(result: StaticAnalysisResult): List<ScanFinding> {
        val findings = mutableListOf<ScanFinding>()
        findings.addAll(permissionRules.evaluate(result))
        findings.addAll(componentRules.evaluate(result))
        findings.addAll(networkRules.evaluate(result))
        findings.addAll(obfuscationRules.evaluate(result))
        findings.addAll(knownMalwareRules.evaluate(result))
        return findings.distinctBy { it.category + it.title + it.componentName }
    }
}
