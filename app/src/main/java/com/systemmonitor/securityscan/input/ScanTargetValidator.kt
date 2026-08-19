package com.systemmonitor.securityscan.input

import com.systemmonitor.securityscan.validation.*
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanTargetValidator @Inject constructor() {
    private val validators = listOf(
        MimeTypeValidator(),
        ApkMagicValidator(),
        FileSizeValidator(),
        ApkStructureValidator()
    )

    fun validate(target: ScanTarget): ValidationResult {
        val file = File(target.apkPath)
        for (validator in validators) {
            val result = validator.validate(file)
            if (result is ValidationResult.Invalid) {
                return result
            }
        }
        return ValidationResult.Valid
    }
}
