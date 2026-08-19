package com.systemmonitor.securityscan.static

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionAnalyzer @Inject constructor() {
    fun analyze(manifest: ParsedManifest): List<String> {
        return manifest.permissions
    }
}
