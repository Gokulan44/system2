package com.systemmonitor.securityscan.validation

import java.io.File

class MimeTypeValidator : FileValidator {
    override fun validate(file: File): ValidationResult {
        val extension = file.extension.lowercase()
        return if (extension == "apk") {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("Invalid file extension: Expected .apk, got .$extension")
        }
    }
}
