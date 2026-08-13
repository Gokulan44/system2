package com.systemmonitor.features.resources

import com.systemmonitor.data.network.ConnectionManager
import com.systemmonitor.data.network.NetworkResult
import com.systemmonitor.domain.model.Laptop
import com.systemmonitor.features.remotepermission.data.entity.DownloadResultEntity
import com.systemmonitor.features.remotepermission.data.entity.SecurityScanResultEntity
import com.systemmonitor.features.remotepermission.domain.model.ResourceRequest
import com.systemmonitor.features.remotepermission.domain.model.ResourceType
import com.systemmonitor.features.remotepermission.domain.repository.PermissionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourceRepository @Inject constructor(
    private val permissionRepository: PermissionRepository,
    private val connectionManager: ConnectionManager
) {

    fun getLaptopResources(laptop: Laptop): Flow<NetworkResult<List<ResourceRequest>>> = flow {
        emit(NetworkResult.Loading)
        val result = connectionManager.fetchResourceCatalog(laptop)
        emit(result)
    }

    suspend fun triggerResourceRequest(
        laptop: Laptop,
        resourceId: String,
        resourceName: String,
        resourceType: String,
        fileSize: Long
    ): NetworkResult<String> {
        return connectionManager.triggerResourceRequest(laptop, resourceId, resourceName, resourceType, fileSize)
    }

    suspend fun getDownloadResult(requestId: String): DownloadResultEntity? {
        return permissionRepository.getDownloadResult(requestId)
    }

    suspend fun getSecurityScanResult(requestId: String): SecurityScanResultEntity? {
        return permissionRepository.getSecurityScanResult(requestId)
    }

    suspend fun saveDownloadResult(result: DownloadResultEntity) {
        permissionRepository.insertDownloadResult(result)
    }

    suspend fun saveSecurityScanResult(result: SecurityScanResultEntity) {
        permissionRepository.insertSecurityScanResult(result)
    }
}
