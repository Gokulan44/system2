package com.systemmonitor.securityanalysis.scanner

import android.content.Context
import com.systemmonitor.securityanalysis.apkanalysis.ApkCertificateAnalyzer
import com.systemmonitor.securityanalysis.apkanalysis.ApkPermissionAnalyzer
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class ApkScanResult(
    val packageName: String,
    val versionName: String,
    val sha256: String,
    val permissionAudit: com.systemmonitor.securityanalysis.apkanalysis.PermissionAudit,
    val certAudit: com.systemmonitor.securityanalysis.apkanalysis.CertificateAudit,
    val overallRiskScore: Int,
    val verdict: String // Safe, Suspicious, Malicious
)

@Singleton
class ApkScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val hashScanner: HashScanner,
    private val permissionAnalyzer: ApkPermissionAnalyzer,
    private val certificateAnalyzer: ApkCertificateAnalyzer
) {
    fun scanApk(apkFile: File): ApkScanResult {
        val sha256 = hashScanner.calculateSha256(apkFile)
        val pm = context.packageManager
        val archiveInfo = pm.getPackageArchiveInfo(apkFile.absolutePath, 0)

        val packageName = archiveInfo?.packageName ?: apkFile.nameWithoutExtension
        val versionName = archiveInfo?.versionName ?: "1.0"

        val permAudit = permissionAnalyzer.analyzePackage(packageName)
        val certAudit = certificateAnalyzer.analyzeCertificate(packageName)

        val riskScore = permAudit.riskScore
        val verdict = when {
            riskScore >= 70 -> "Malicious"
            riskScore >= 30 -> "Suspicious"
            else -> "Safe"
        }

        return ApkScanResult(
            packageName = packageName,
            versionName = versionName,
            sha256 = sha256,
            permissionAudit = permAudit,
            certAudit = certAudit,
            overallRiskScore = riskScore,
            verdict = verdict
        )
    }
}
