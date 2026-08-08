package com.systemmonitor.securityanalysis.scanner

import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class MasterScanResult(
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val sha256: String,
    val fileType: String, // APK, PDF, Generic
    val riskScore: Int,
    val verdict: String
)

@Singleton
class FileScanner @Inject constructor(
    private val apkScanner: ApkScanner,
    private val pdfScanner: PdfScanner,
    private val hashScanner: HashScanner
) {
    fun scanFile(file: File): MasterScanResult {
        return when {
            file.name.endsWith(".apk", ignoreCase = true) -> {
                val apkRes = apkScanner.scanApk(file)
                MasterScanResult(
                    fileName = file.name,
                    filePath = file.absolutePath,
                    fileSize = file.length(),
                    sha256 = apkRes.sha256,
                    fileType = "APK",
                    riskScore = apkRes.overallRiskScore,
                    verdict = apkRes.verdict
                )
            }
            file.name.endsWith(".pdf", ignoreCase = true) -> {
                val pdfRes = pdfScanner.scanPdf(file)
                MasterScanResult(
                    fileName = file.name,
                    filePath = file.absolutePath,
                    fileSize = file.length(),
                    sha256 = pdfRes.sha256,
                    fileType = "PDF",
                    riskScore = pdfRes.riskScore,
                    verdict = pdfRes.verdict
                )
            }
            else -> {
                val sha256 = hashScanner.calculateSha256(file)
                MasterScanResult(
                    fileName = file.name,
                    filePath = file.absolutePath,
                    fileSize = file.length(),
                    sha256 = sha256,
                    fileType = "Generic",
                    riskScore = 0,
                    verdict = "Safe"
                )
            }
        }
    }
}
