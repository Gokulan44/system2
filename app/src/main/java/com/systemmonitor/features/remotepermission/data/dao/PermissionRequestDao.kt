package com.systemmonitor.features.remotepermission.data.dao

import androidx.room.*
import com.systemmonitor.features.remotepermission.data.entity.PermissionRequestEntity
import com.systemmonitor.features.remotepermission.data.entity.VerificationEventEntity
import com.systemmonitor.features.remotepermission.data.entity.ApprovalEventEntity
import com.systemmonitor.features.remotepermission.data.entity.DownloadResultEntity
import com.systemmonitor.features.remotepermission.data.entity.SecurityScanResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PermissionRequestDao {
    @Query("SELECT * FROM permission_requests ORDER BY createdAt DESC")
    fun getAllRequests(): Flow<List<PermissionRequestEntity>>

    @Query("SELECT * FROM permission_requests WHERE requestId = :id LIMIT 1")
    suspend fun getRequestById(id: String): PermissionRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: PermissionRequestEntity)

    @Query("UPDATE permission_requests SET status = :status WHERE requestId = :requestId")
    suspend fun updateRequestStatus(requestId: String, status: String)

    @Delete
    suspend fun deleteRequest(request: PermissionRequestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerificationEvent(event: VerificationEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApprovalEvent(event: ApprovalEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadResult(result: DownloadResultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSecurityScanResult(result: SecurityScanResultEntity)

    @Query("SELECT * FROM verification_events ORDER BY timestamp DESC")
    fun getAllVerificationEvents(): Flow<List<VerificationEventEntity>>

    @Query("SELECT * FROM approval_events ORDER BY timestamp DESC")
    fun getAllApprovalEvents(): Flow<List<ApprovalEventEntity>>

    @Query("SELECT * FROM download_results WHERE requestId = :requestId LIMIT 1")
    suspend fun getDownloadResult(requestId: String): DownloadResultEntity?

    @Query("SELECT * FROM security_scan_results WHERE requestId = :requestId LIMIT 1")
    suspend fun getSecurityScanResult(requestId: String): SecurityScanResultEntity?
}
