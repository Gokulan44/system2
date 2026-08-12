package com.systemmonitor.features.remotepermission.domain.usecase

import com.systemmonitor.features.remotepermission.data.entity.PermissionHistoryEntity
import com.systemmonitor.features.remotepermission.domain.model.PermissionRequest
import com.systemmonitor.features.remotepermission.domain.model.PermissionStatus
import com.systemmonitor.features.remotepermission.domain.repository.PermissionRepository
import javax.inject.Inject

class DenyResourceUseCase @Inject constructor(
    private val repository: PermissionRepository
) {
    suspend operator fun invoke(request: PermissionRequest) {
        repository.updateRequestStatus(request.requestId, PermissionStatus.DENIED)
        
        val historyEntry = PermissionHistoryEntity(
            requestId = request.requestId,
            laptopId = request.laptopId,
            resourceName = request.resource.name,
            operation = request.requestedOperation.name,
            status = PermissionStatus.DENIED.name,
            timestamp = System.currentTimeMillis(),
            verificationMethod = null
        )
        repository.insertHistory(historyEntry)
    }
}
