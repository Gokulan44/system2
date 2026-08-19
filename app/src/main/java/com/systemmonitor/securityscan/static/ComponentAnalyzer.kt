package com.systemmonitor.securityscan.static

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComponentAnalyzer @Inject constructor() {
    fun analyze(manifest: ParsedManifest): List<ParsedComponent> {
        return manifest.activities
    }
}
