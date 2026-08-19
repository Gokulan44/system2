package com.systemmonitor.securityscan.staticscan

import android.content.Context
import android.content.pm.PackageManager
import com.systemmonitor.securityscan.input.ScanTarget
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.zip.ZipFile
import javax.inject.Inject
import javax.inject.Singleton

data class CertificateInfo(
    val subject: String,
    val issuer: String,
    val validFrom: String,
    val validTo: String,
    val serialNumber: String,
    val sigAlgName: String
)

@Singleton
class CertificateAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun analyze(target: ScanTarget): CertificateInfo? {
        return if (target.isSystemApp) {
            analyzeInstalled(target.packageName)
        } else {
            analyzeFile(File(target.apkPath))
        }
    }

    private fun analyzeInstalled(packageName: String): CertificateInfo? {
        val pm = context.packageManager
        return try {
            val flags = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                PackageManager.GET_SIGNING_CERTIFICATES
            } else {
                @Suppress("DEPRECATION")
                PackageManager.GET_SIGNATURES
            }
            val packageInfo = pm.getPackageInfo(packageName, flags)
            
            val signatures = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            val signature = signatures?.firstOrNull() ?: return null
            val certFactory = CertificateFactory.getInstance("X.509")
            val cert = certFactory.generateCertificate(signature.toByteArray().inputStream()) as X509Certificate
            mapToInfo(cert)
        } catch (e: Exception) {
            null
        }
    }

    private fun analyzeFile(file: File): CertificateInfo? {
        return try {
            ZipFile(file).use { zip ->
                val metaInfEntries = zip.entries().asSequence().filter {
                    val name = it.name.uppercase()
                    name.startsWith("META-INF/") && (name.endsWith(".RSA") || name.endsWith(".DSA") || name.endsWith(".EC"))
                }
                
                val entry = metaInfEntries.firstOrNull() ?: return null
                zip.getInputStream(entry).use { stream ->
                    val certFactory = CertificateFactory.getInstance("X.509")
                    val certs = certFactory.generateCertificates(stream)
                    val cert = certs.firstOrNull() as? X509Certificate ?: return null
                    mapToInfo(cert)
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun mapToInfo(cert: X509Certificate): CertificateInfo {
        return CertificateInfo(
            subject = cert.subjectDN.name,
            issuer = cert.issuerDN.name,
            validFrom = cert.notBefore.toString(),
            validTo = cert.notAfter.toString(),
            serialNumber = cert.serialNumber.toString(16),
            sigAlgName = cert.sigAlgName
        )
    }
}
