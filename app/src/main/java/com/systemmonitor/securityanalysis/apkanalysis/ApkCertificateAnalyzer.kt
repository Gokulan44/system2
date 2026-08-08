package com.systemmonitor.securityanalysis.apkanalysis

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

data class CertificateAudit(
    val issuer: String,
    val isSelfSigned: Boolean,
    val certFingerprintSha256: String
)

@Singleton
class ApkCertificateAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun analyzeCertificate(packageName: String): CertificateAudit {
        return try {
            val pm = context.packageManager
            val info = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            val sigs = info.signatures
            if (sigs != null && sigs.isNotEmpty()) {
                val digest = MessageDigest.getInstance("SHA-256")
                val hashBytes = digest.digest(sigs[0].toByteArray())
                val fp = hashBytes.joinToString("") { "%02X".format(it) }
                CertificateAudit(
                    issuer = "CN=Android Debug / Release",
                    isSelfSigned = true,
                    certFingerprintSha256 = fp
                )
            } else {
                CertificateAudit("Unknown", false, "")
            }
        } catch (e: Exception) {
            CertificateAudit("Unknown", false, "")
        }
    }
}
