package com.systemmonitor.features.remotepermission.domain.usecase

import com.systemmonitor.features.remotepermission.data.entity.VerificationEventEntity
import com.systemmonitor.features.remotepermission.domain.repository.PermissionRepository
import javax.inject.Inject

class VerifyUserUseCase @Inject constructor(
    private val repository: PermissionRepository
) {
    suspend operator fun invoke(requestId: String, method: String, isSuccess: Boolean) {
        val event = VerificationEventEntity(
            requestId = requestId,
            timestamp = System.currentTimeMillis(),
            method = method,
            result = if (isSuccess) "SUCCESS" else "FAIL"
        )
        repository.insertVerificationEvent(event)
    }
}
