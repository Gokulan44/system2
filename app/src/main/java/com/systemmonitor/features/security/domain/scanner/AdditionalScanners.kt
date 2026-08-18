package com.systemmonitor.features.security.domain.scanner

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.systemmonitor.features.security.domain.model.ThreatInfo
import com.systemmonitor.features.security.domain.model.ThreatSeverity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import com.systemmonitor.securityanalysis.scanner.FileScanner

@Singleton
class ConfigurationScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scanConfiguration(): List<ThreatInfo> {
        val threats = mutableListOf<ThreatInfo>()

        // 1. Unknown Sources / Install Non-Market Apps
        val unknownSources = try {
            Settings.Secure.getInt(context.contentResolver, Settings.Secure.INSTALL_NON_MARKET_APPS, 0) == 1
        } catch (e: Exception) { false }

        if (unknownSources) {
            threats.add(
                ThreatInfo(
                    id = "config_unknown_sources",
                    title = "Unknown Sources Installation Enabled",
                    description = "Installing apps from unverified sources outside Play Store is allowed.",
                    severity = ThreatSeverity.HIGH,
                    category = "Device Configuration",
                    recommendedAction = "Disable Unknown Sources in Android Security Settings"
                )
            )
        }

        // 2. Developer Options / ADB Debugging
        val adbEnabled = try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        } catch (e: Exception) { false }

        if (adbEnabled) {
            threats.add(
                ThreatInfo(
                    id = "config_adb_enabled",
                    title = "USB / ADB Debugging Enabled",
                    description = "ADB debugging opens remote command shell vulnerabilities over USB.",
                    severity = ThreatSeverity.MEDIUM,
                    category = "Device Configuration",
                    recommendedAction = "Disable USB Debugging when not developing"
                )
            )
        }

        // 3. Rooted Device Check
        val isRooted = checkRootFiles()
        if (isRooted) {
            threats.add(
                ThreatInfo(
                    id = "config_rooted_device",
                    title = "Root Access Detected",
                    description = "Device system partition has superuser access, bypassing OS sandbox limits.",
                    severity = ThreatSeverity.CRITICAL,
                    category = "Device Configuration",
                    recommendedAction = "Remove su binaries to restore system integrity"
                )
            )
        }

        return threats
    }

    private fun checkRootFiles(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su"
        )
        return paths.any { File(it).exists() }
    }
}

@Singleton
class NetworkSecurityScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scanNetwork(): List<ThreatInfo> {
        val threats = mutableListOf<ThreatInfo>()
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return threats

        val activeNetwork = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(activeNetwork)

        if (caps != null) {
            val isVpn = caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
            if (!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                threats.add(
                    ThreatInfo(
                        id = "net_unvalidated",
                        title = "Unvalidated Network Connection",
                        description = "Current connection captive portal or internet access cannot be verified securely.",
                        severity = ThreatSeverity.MEDIUM,
                        category = "Network Security",
                        recommendedAction = "Verify Wi-Fi network credentials before transmitting sensitive data"
                    )
                )
            }
        }
        return threats
    }
}

@Singleton
class StorageSecurityScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileScanner: FileScanner
) {
    fun scanStorage(): List<ThreatInfo> {
        val threats = mutableListOf<ThreatInfo>()
        val downloadDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
        if (downloadDir != null && downloadDir.exists()) {
            scanDirectory(downloadDir, threats)
        }
        val appExternalDir = context.getExternalFilesDir(null)
        if (appExternalDir != null && appExternalDir.exists()) {
            scanDirectory(appExternalDir, threats)
        }
        return threats
    }

    private fun scanDirectory(dir: File, threats: MutableList<ThreatInfo>) {
        if (!dir.exists() || !dir.isDirectory) return
        
        dir.listFiles()?.forEach { file ->
            val path = file.absolutePath
            if (path.contains("/vault/") || file.name.equals("vault", ignoreCase = true) || path.contains("quarantine_vault") || path.contains("scan_workspace")) {
                return@forEach
            }
            if (file.isDirectory) {
                scanDirectory(file, threats)
            } else {
                if (file.name.endsWith(".apk", ignoreCase = true) || file.name.endsWith(".pdf", ignoreCase = true)) {
                    val result = fileScanner.scanFile(file)
                    if (result.verdict == "Malicious" || result.verdict == "Dangerous" || result.verdict == "Suspicious") {
                        val severity = when (result.verdict) {
                            "Malicious", "Dangerous" -> ThreatSeverity.HIGH
                            else -> ThreatSeverity.MEDIUM
                        }
                        threats.add(
                            ThreatInfo(
                                id = "file_${result.sha256}",
                                title = "Suspicious File Detected",
                                description = "File '${result.fileName}' contains potential threat signatures (verdict: ${result.verdict}).",
                                packageName = null,
                                filePath = result.filePath,
                                severity = severity,
                                category = "Storage Security",
                                recommendedAction = "Quarantine or delete this file from storage"
                            )
                        )
                    }
                }
            }
        }
    }
}

@Singleton
class AccessibilityScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scanAccessibility(): List<ThreatInfo> {
        val threats = mutableListOf<ThreatInfo>()
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            ?: return threats

        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        for (service in enabledServices) {
            val pkgName = service.resolveInfo?.serviceInfo?.packageName ?: ""
            if (pkgName.isNotEmpty() && !pkgName.startsWith("com.google") && !pkgName.startsWith("android")) {
                threats.add(
                    ThreatInfo(
                        id = "access_$pkgName",
                        title = "Active Accessibility Service",
                        description = "Third-party accessibility service running for '$pkgName'. Can read screen text and automate touches.",
                        packageName = pkgName,
                        severity = ThreatSeverity.HIGH,
                        category = "Accessibility Services",
                        recommendedAction = "Audit Accessibility permissions in Accessibility Settings"
                    )
                )
            }
        }
        return threats
    }
}
