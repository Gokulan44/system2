package com.systemmonitor.securityscan.validation

import java.io.File

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val reason: String) : ValidationResult()
}

interface FileValidator {
    fun validate(file: File): ValidationResult
}
