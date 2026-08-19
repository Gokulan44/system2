package com.systemmonitor.securityscan.validation

import java.io.File

class FileSizeValidator(private val maxSizeBytes: Long = 500 * 1024 * 1024) : FileValidator {
    override fun validate(file: File): ValidationResult {
        val size = file.length()
        return when {
            size <= 0 -> ValidationResult.Invalid("File is empty (0 bytes).")
            size > maxSizeBytes -> ValidationResult.Invalid("File size exceeds limit of ${maxSizeBytes / (1024 * 1024)}MB.")
            else -> ValidationResult.Valid
        }
    }
}
