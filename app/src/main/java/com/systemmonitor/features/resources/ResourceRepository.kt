package com.systemmonitor.features.resources

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
    private val permissionRepository: PermissionRepository
) {

    fun getLaptopResources(laptop: Laptop): Flow<NetworkResult<List<ResourceRequest>>> = flow {
        emit(NetworkResult.Loading)
        // Simulated local catalog from the laptop
        val dummyCatalog = listOf(
            ResourceRequest("res_01", "Security-Lab.pdf", ResourceType.FILE, 12 * 1024 * 1024L, "C:\\Files\\Security-Lab.pdf"),
            ResourceRequest("res_02", "Setup.msi", ResourceType.FILE, 45 * 1024 * 1024L, "C:\\Files\\Setup.msi"),
            ResourceRequest("res_03", "Example.exe", ResourceType.FILE, 5 * 1024 * 1024L, "C:\\Files\\Example.exe"),
            ResourceRequest("res_04", "Report.docx", ResourceType.FILE, 2 * 1024 * 1024L, "C:\\Files\\Report.docx")
        )
        emit(NetworkResult.Success(dummyCatalog))
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
