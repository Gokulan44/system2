package com.systemmonitor.features.remotepermission.data.repository

import com.systemmonitor.features.remotepermission.data.dao.PermissionHistoryDao
import com.systemmonitor.features.remotepermission.data.dao.PermissionRequestDao
import com.systemmonitor.features.remotepermission.data.dao.ResourceRequestDao
import com.systemmonitor.features.remotepermission.data.entity.*
import com.systemmonitor.features.remotepermission.domain.model.*
import com.systemmonitor.features.remotepermission.domain.repository.PermissionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionRepositoryImpl @Inject constructor(
    private val requestDao: PermissionRequestDao,
    private val historyDao: PermissionHistoryDao,
    private val resourceDao: ResourceRequestDao
) : PermissionRepository {

    override fun getAllRequests(): Flow<List<PermissionRequest>> {
        return requestDao.getAllRequests().map { entities ->
            entities.mapNotNull { entity ->
                val resourceEntity = resourceDao.getResourceById(entity.resourceId) ?: return@mapNotNull null
                entity.toDomain(resourceEntity)
            }
        }
    }

    override suspend fun getRequestById(id: String): PermissionRequest? {
        val entity = requestDao.getRequestById(id) ?: return null
        val resourceEntity = resourceDao.getResourceById(entity.resourceId) ?: return null
        return entity.toDomain(resourceEntity)
    }

    override suspend fun saveRequest(request: PermissionRequest) {
        resourceDao.insertResource(request.resource.toEntity())
        requestDao.insertRequest(request.toEntity())
    }

    override suspend fun updateRequestStatus(requestId: String, status: PermissionStatus) {
        requestDao.updateRequestStatus(requestId, status.name)
    }

    override fun getHistoryFlow(): Flow<List<PermissionHistoryEntity>> {
        return historyDao.getHistoryFlow()
    }

    override suspend fun insertHistory(entry: PermissionHistoryEntity) {
        historyDao.insertHistory(entry)
    }

    override suspend fun insertVerificationEvent(event: VerificationEventEntity) {
        requestDao.insertVerificationEvent(event)
    }

    override suspend fun insertApprovalEvent(event: ApprovalEventEntity) {
        requestDao.insertApprovalEvent(event)
    }

    override suspend fun insertDownloadResult(result: DownloadResultEntity) {
        requestDao.insertDownloadResult(result)
    }

    override suspend fun insertSecurityScanResult(result: SecurityScanResultEntity) {
        requestDao.insertSecurityScanResult(result)
    }

    override suspend fun getDownloadResult(requestId: String): DownloadResultEntity? {
        return requestDao.getDownloadResult(requestId)
    }

    override suspend fun getSecurityScanResult(requestId: String): SecurityScanResultEntity? {
        return requestDao.getSecurityScanResult(requestId)
    }

    // Mappers
    private fun PermissionRequestEntity.toDomain(res: ResourceRequestEntity): PermissionRequest {
        return PermissionRequest(
            requestId = requestId,
            laptopId = laptopId,
            resource = ResourceRequest(
                resourceId = res.resourceId,
                name = res.name,
                type = runCatching { ResourceType.valueOf(res.type) }.getOrDefault(ResourceType.FILE),
                sizeBytes = res.sizeBytes,
                path = res.path
            ),
            requestedOperation = runCatching { PermissionType.valueOf(requestedOperation) }.getOrDefault(PermissionType.DOWNLOAD),
            createdAt = createdAt,
            expiresAt = expiresAt,
            requestNonce = requestNonce,
            status = runCatching { PermissionStatus.valueOf(status) }.getOrDefault(PermissionStatus.PENDING)
        )
    }

    private fun PermissionRequest.toEntity(): PermissionRequestEntity {
        return PermissionRequestEntity(
            requestId = requestId,
            laptopId = laptopId,
            resourceId = resource.resourceId,
            resourceName = resource.name,
            resourceType = resource.type.name,
            fileSize = resource.sizeBytes,
            requestedOperation = requestedOperation.name,
            createdAt = createdAt,
            expiresAt = expiresAt,
            requestNonce = requestNonce,
            status = status.name
        )
    }

    private fun ResourceRequest.toEntity(): ResourceRequestEntity {
        return ResourceRequestEntity(
            resourceId = resourceId,
            name = name,
            type = type.name,
            sizeBytes = sizeBytes,
            path = path
        )
    }
}
