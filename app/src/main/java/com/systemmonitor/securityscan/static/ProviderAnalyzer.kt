package com.systemmonitor.securityscan.static

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProviderAnalyzer @Inject constructor() {
    fun analyze(manifest: ParsedManifest): List<ParsedComponent> {
        return manifest.providers
    }
}
