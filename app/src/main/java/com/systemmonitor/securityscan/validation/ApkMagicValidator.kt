package com.systemmonitor.securityscan.validation

import java.io.File
import java.io.FileInputStream

class ApkMagicValidator : FileValidator {
    override fun validate(file: File): ValidationResult {
        if (!file.exists() || !file.isFile) {
            return ValidationResult.Invalid("File does not exist or is not a valid file.")
        }
        return try {
            FileInputStream(file).use { fis ->
                val header = ByteArray(4)
                val read = fis.read(header)
                if (read == 4 &&
                    header[0] == 0x50.toByte() && // P
                    header[1] == 0x4B.toByte() && // K
                    header[2] == 0x03.toByte() &&
                    header[3] == 0x04.toByte()
                ) {
                    ValidationResult.Valid
                } else {
                    ValidationResult.Invalid("Invalid file signature: Not a valid ZIP/APK archive.")
                }
            }
        } catch (e: Exception) {
            ValidationResult.Invalid("Error reading file header: ${e.localizedMessage}")
        }
    }
}
