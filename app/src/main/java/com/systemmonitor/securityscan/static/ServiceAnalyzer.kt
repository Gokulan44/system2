package com.systemmonitor.securityscan.static

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceAnalyzer @Inject constructor() {
    fun analyze(manifest: ParsedManifest): List<ParsedComponent> {
        return manifest.services
    }
}
