package com.systemmonitor.securityscan.rules

import com.systemmonitor.securityscan.static.StaticAnalysisResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionRules @Inject constructor() {
    fun evaluate(result: StaticAnalysisResult): List<ScanFinding> {
        val findings = mutableListOf<ScanFinding>()
        for (permission in result.permissions) {
            when (permission) {
                "android.permission.READ_SMS",
                "android.permission.RECEIVE_SMS",
                "android.permission.SEND_SMS",
                "android.permission.RECEIVE_MMS" -> {
                    findings.add(
                        ScanFinding(
                            category = "Permission",
                            severity = Severity.CRITICAL,
                            title = "SMS Access Allowed",
                            details = "This app can read, write, or monitor incoming SMS/MMS messages. This can be abused to intercept 2FA codes.",
                            componentName = permission
                        )
                    )
                }
                "android.permission.RECORD_AUDIO" -> {
                    findings.add(
                        ScanFinding(
                            category = "Permission",
                            severity = Severity.HIGH,
                            title = "Microphone Access Allowed",
                            details = "This app can record audio at any time. Potential spy risk if unauthorized.",
                            componentName = permission
                        )
                    )
                }
                "android.permission.CAMERA" -> {
                    findings.add(
                        ScanFinding(
                            category = "Permission",
                            severity = Severity.HIGH,
                            title = "Camera Access Allowed",
                            details = "This app can capture photos and videos. Potential spy risk if unauthorized.",
                            componentName = permission
                        )
                    )
                }
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_COARSE_LOCATION",
                "android.permission.ACCESS_BACKGROUND_LOCATION" -> {
                    findings.add(
                        ScanFinding(
                            category = "Permission",
                            severity = Severity.HIGH,
                            title = "Location Tracking Allowed",
                            details = "This app can track the physical location of the device, even in the background.",
                            componentName = permission
                        )
                    )
                }
                "android.permission.READ_CONTACTS",
                "android.permission.WRITE_CONTACTS",
                "android.permission.GET_ACCOUNTS" -> {
                    findings.add(
                        ScanFinding(
                            category = "Permission",
                            severity = Severity.MEDIUM,
                            title = "Contacts and Account Access",
                            details = "This app can access your contacts or account database, presenting a potential privacy leak vector.",
                            componentName = permission
                        )
                    )
                }
                "android.permission.READ_CALL_LOG",
                "android.permission.WRITE_CALL_LOG",
                "android.permission.PROCESS_OUTGOING_CALLS" -> {
                    findings.add(
                        ScanFinding(
                            category = "Permission",
                            severity = Severity.CRITICAL,
                            title = "Call Log Access Allowed",
                            details = "This app can read/write detailed call histories or intercept phone calls.",
                            componentName = permission
                        )
                    )
                }
                "android.permission.SYSTEM_ALERT_WINDOW" -> {
                    findings.add(
                        ScanFinding(
                            category = "Permission",
                            severity = Severity.HIGH,
                            title = "Draw Over Other Apps (Overlay)",
                            details = "Allows overlays, which are commonly used in banking malware for clickjacking or phishing overlay attacks.",
                            componentName = permission
                        )
                    )
                }
            }
        }
        return findings
    }
}
