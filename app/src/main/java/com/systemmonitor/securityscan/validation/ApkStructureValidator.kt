package com.systemmonitor.securityscan.validation

import java.io.File
import java.util.zip.ZipFile

class ApkStructureValidator : FileValidator {
    override fun validate(file: File): ValidationResult {
        return try {
            ZipFile(file).use { zip ->
                val manifestEntry = zip.getEntry("AndroidManifest.xml")
                if (manifestEntry != null) {
                    ValidationResult.Valid
                } else {
                    ValidationResult.Invalid("Invalid APK structure: AndroidManifest.xml is missing.")
                }
            }
        } catch (e: Exception) {
            ValidationResult.Invalid("Failed to open archive: ${e.localizedMessage}")
        }
    }
}
