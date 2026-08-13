package com.systemmonitor.features.remotepermission.domain.usecase

import com.systemmonitor.core.security.ApprovalTokenManager
import com.systemmonitor.features.remotepermission.data.entity.ApprovalEventEntity
import com.systemmonitor.features.remotepermission.domain.model.PermissionRequest
import com.systemmonitor.features.remotepermission.domain.repository.PermissionRepository
import java.security.Signature
import javax.inject.Inject

class CreateApprovalTokenUseCase @Inject constructor(
    private val tokenManager: ApprovalTokenManager,
    private val repository: PermissionRepository
) {
    suspend operator fun invoke(
        request: PermissionRequest,
        signatureInstance: Signature,
        verificationMethod: String = "BIOMETRIC"
    ): String {
        val now = System.currentTimeMillis()
        val expiresAt = request.expiresAt
        val tokenJson = tokenManager.createSignedTokenJson(
            requestId = request.requestId,
            laptopId = request.laptopId,
            resourceId = request.resource.resourceId,
            resourceName = request.resource.name,
            createdAt = now,
            expiresAt = expiresAt,
            nonce = request.requestNonce,
            signatureInstance = signatureInstance,
            verificationMethod = verificationMethod
        )

        // Parse signature for logging
        val obj = org.json.JSONObject(tokenJson)
        val signatureB64 = obj.optString("signature", "")

        val approvalEvent = ApprovalEventEntity(
            requestId = request.requestId,
            timestamp = now,
            token = tokenJson,
            signature = signatureB64
        )
        repository.insertApprovalEvent(approvalEvent)

        return tokenJson
    }
}
