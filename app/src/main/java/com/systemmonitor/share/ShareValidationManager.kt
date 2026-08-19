package com.systemmonitor.share

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareValidationManager @Inject constructor() {
    fun validate(sharedFile: SharedFile): Result<Boolean> {
        if (sharedFile.size <= 0) {
            return Result.failure(Exception("File is empty or size could not be determined"))
        }
        if (sharedFile.name.isBlank()) {
            return Result.failure(Exception("File name is invalid"))
        }
        return Result.success(true)
    }
}
