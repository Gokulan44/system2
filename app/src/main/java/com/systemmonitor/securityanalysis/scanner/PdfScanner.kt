package com.systemmonitor.securityanalysis.scanner

import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class PdfScanResult(
    val fileName: String,
    val sha256: String,
    val hasJavaScript: Boolean,
    val hasEmbeddedFiles: Boolean,
    val extractedUrlsCount: Int,
    val riskScore: Int,
    val verdict: String // Safe, Suspicious, Dangerous
)

@Singleton
class PdfScanner @Inject constructor(
    private val hashScanner: HashScanner
) {
    fun scanPdf(pdfFile: File): PdfScanResult {
        val sha256 = hashScanner.calculateSha256(pdfFile)
        val content = try { pdfFile.readText(Charsets.ISO_8859_1) } catch (e: Exception) { "" }

        val hasJS = content.contains("/JS") || content.contains("/JavaScript")
        val hasEmbedded = content.contains("/EmbeddedFiles")
        val urlMatches = Regex("http[s]?://").findAll(content).count()

        var score = 0
        if (hasJS) score += 50
        if (hasEmbedded) score += 30
        if (urlMatches > 5) score += 20
        score = score.coerceIn(0, 100)

        val verdict = when {
            score >= 60 -> "Dangerous"
            score >= 30 -> "Suspicious"
            else -> "Safe"
        }

        return PdfScanResult(
            fileName = pdfFile.name,
            sha256 = sha256,
            hasJavaScript = hasJS,
            hasEmbeddedFiles = hasEmbedded,
            extractedUrlsCount = urlMatches,
            riskScore = score,
            verdict = verdict
        )
    }
}
