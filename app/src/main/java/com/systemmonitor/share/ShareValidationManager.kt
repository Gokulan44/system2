package com.systemmonitor.share

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareValidationManager @Inject constructor() {
    fun validate(sharedFile: SharedFile): Result<Boolean> {
        if (sharedFile.name.isBlank()) {
            return Result.failure(Exception("File name is invalid"))
        }
        return Result.success(true)
    }
}
