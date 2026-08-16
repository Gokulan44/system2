package com.systemmonitor.vault.archive

import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArchiveSecurityValidator @Inject constructor() {
    fun validateZipSecurity(zipFile: File): ZipSecurityResult {
        return try {
            ZipInputStream(FileInputStream(zipFile)).use { zis ->
                var entry = zis.nextEntry
                var totalUncompressedSize = 0L
                var fileCount = 0
                while (entry != null) {
                    fileCount++
                    // Check Zip Slip path traversal vulnerability
                    val name = entry.name
                    if (name.contains("..") || name.startsWith("/") || name.contains(":\\")) {
                        return ZipSecurityResult.Insecure("Path traversal vulnerability detected in zip entry: $name")
                    }
                    totalUncompressedSize += entry.size
                    if (totalUncompressedSize > 500 * 1024 * 1024L) { // 500MB zip bomb threshold
                        return ZipSecurityResult.Insecure("Potential zip bomb detected: size exceeds limit")
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
                ZipSecurityResult.Secure(fileCount, totalUncompressedSize)
            }
        } catch (e: Exception) {
            ZipSecurityResult.Insecure("Failed to read archive: ${e.message}")
        }
    }

    sealed class ZipSecurityResult {
        data class Secure(val fileCount: Int, val totalSizeBytes: Long) : ZipSecurityResult()
        data class Insecure(val reason: String) : ZipSecurityResult()
    }
}

@Singleton
class ZipManager @Inject constructor(
    private val securityValidator: ArchiveSecurityValidator
) {
    fun listZipEntries(zipFile: File): List<String> {
        val validation = securityValidator.validateZipSecurity(zipFile)
        if (validation is ArchiveSecurityValidator.ZipSecurityResult.Insecure) {
            return emptyList()
        }

        val entries = mutableListOf<String>()
        try {
            ZipInputStream(FileInputStream(zipFile)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    entries.add(entry.name)
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        } catch (e: Exception) {
            // Return collected entries
        }
        return entries
    }
}

@Singleton
class ArchivePreviewManager @Inject constructor(
    val zipManager: ZipManager
) {
    fun getArchiveEntries(decryptedZipFile: File): List<String> {
        return zipManager.listZipEntries(decryptedZipFile)
    }
}

@Singleton
class ArchiveManager @Inject constructor(
    val securityValidator: ArchiveSecurityValidator,
    val zipManager: ZipManager,
    val previewManager: ArchivePreviewManager
)
