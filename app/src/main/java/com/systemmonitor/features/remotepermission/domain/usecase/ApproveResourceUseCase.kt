package com.systemmonitor.features.remotepermission.domain.usecase

import com.systemmonitor.features.remotepermission.data.entity.PermissionHistoryEntity
import com.systemmonitor.features.remotepermission.domain.model.PermissionRequest
import com.systemmonitor.features.remotepermission.domain.model.PermissionStatus
import com.systemmonitor.features.remotepermission.domain.repository.PermissionRepository
import java.security.Signature
import javax.inject.Inject

class ApproveResourceUseCase @Inject constructor(
    private val repository: PermissionRepository,
    private val createApprovalTokenUseCase: CreateApprovalTokenUseCase
) {
    suspend operator fun invoke(
        request: PermissionRequest,
        signatureInstance: Signature,
        verificationMethod: String
    ): String {
        repository.updateRequestStatus(request.requestId, PermissionStatus.APPROVED)
        
        val historyEntry = PermissionHistoryEntity(
            requestId = request.requestId,
            laptopId = request.laptopId,
            resourceName = request.resource.name,
            operation = request.requestedOperation.name,
            status = PermissionStatus.APPROVED.name,
            timestamp = System.currentTimeMillis(),
            verificationMethod = verificationMethod
        )
        repository.insertHistory(historyEntry)
        
        return createApprovalTokenUseCase(request, signatureInstance)
    }
}
