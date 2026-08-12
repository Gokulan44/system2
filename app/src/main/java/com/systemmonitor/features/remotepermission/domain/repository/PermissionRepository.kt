package com.systemmonitor.features.remotepermission.domain.repository

import com.systemmonitor.features.remotepermission.data.entity.PermissionHistoryEntity
import com.systemmonitor.features.remotepermission.data.entity.VerificationEventEntity
import com.systemmonitor.features.remotepermission.data.entity.ApprovalEventEntity
import com.systemmonitor.features.remotepermission.data.entity.DownloadResultEntity
import com.systemmonitor.features.remotepermission.data.entity.SecurityScanResultEntity
import com.systemmonitor.features.remotepermission.domain.model.PermissionRequest
import com.systemmonitor.features.remotepermission.domain.model.PermissionStatus
import kotlinx.coroutines.flow.Flow

interface PermissionRepository {
    fun getAllRequests(): Flow<List<PermissionRequest>>
    suspend fun getRequestById(id: String): PermissionRequest?
    suspend fun saveRequest(request: PermissionRequest)
    suspend fun updateRequestStatus(requestId: String, status: PermissionStatus)
    
    fun getHistoryFlow(): Flow<List<PermissionHistoryEntity>>
    suspend fun insertHistory(entry: PermissionHistoryEntity)
    
    suspend fun insertVerificationEvent(event: VerificationEventEntity)
    suspend fun insertApprovalEvent(event: ApprovalEventEntity)
    suspend fun insertDownloadResult(result: DownloadResultEntity)
    suspend fun insertSecurityScanResult(result: SecurityScanResultEntity)

    suspend fun getDownloadResult(requestId: String): DownloadResultEntity?
    suspend fun getSecurityScanResult(requestId: String): SecurityScanResultEntity?
}
