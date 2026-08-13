package com.systemmonitor.features.resources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemmonitor.data.network.NetworkResult
import com.systemmonitor.domain.model.Laptop
import com.systemmonitor.features.remotepermission.data.entity.DownloadResultEntity
import com.systemmonitor.features.remotepermission.data.entity.SecurityScanResultEntity
import com.systemmonitor.features.remotepermission.domain.model.ResourceRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResourceViewModel @Inject constructor(
    private val repository: ResourceRepository
) : ViewModel() {

    private val _catalog = MutableStateFlow<NetworkResult<List<ResourceRequest>>>(NetworkResult.Loading)
    val catalog: StateFlow<NetworkResult<List<ResourceRequest>>> = _catalog.asStateFlow()

    private val _downloadResult = MutableStateFlow<DownloadResultEntity?>(null)
    val downloadResult: StateFlow<DownloadResultEntity?> = _downloadResult.asStateFlow()

    private val _securityScanResult = MutableStateFlow<SecurityScanResultEntity?>(null)
    val securityScanResult: StateFlow<SecurityScanResultEntity?> = _securityScanResult.asStateFlow()

    fun loadResourcesForLaptop(laptop: Laptop) {
        viewModelScope.launch {
            repository.getLaptopResources(laptop).collectLatest {
                _catalog.value = it
            }
        }
    }

    fun loadResultAndScan(requestId: String) {
        viewModelScope.launch {
            _downloadResult.value = repository.getDownloadResult(requestId)
            _securityScanResult.value = repository.getSecurityScanResult(requestId)
        }
    }

    fun simulateDownloadResult(requestId: String, status: String, filePath: String, scanStatus: String, hash: String, details: String) {
        viewModelScope.launch {
            val download = DownloadResultEntity(requestId, status, filePath, System.currentTimeMillis())
            val scan = SecurityScanResultEntity(requestId, scanStatus, hash, if (scanStatus == "SAFE") "LOW" else "HIGH", details)
            repository.saveDownloadResult(download)
            repository.saveSecurityScanResult(scan)
            _downloadResult.value = download
            _securityScanResult.value = scan
        }
    }

    private val _requestTriggerState = MutableStateFlow<NetworkResult<String>?>(null)
    val requestTriggerState: StateFlow<NetworkResult<String>?> = _requestTriggerState.asStateFlow()

    fun triggerResourceDownload(laptop: Laptop, resourceId: String, name: String, type: String, sizeBytes: Long) {
        viewModelScope.launch {
            _requestTriggerState.value = NetworkResult.Loading
            val result = repository.triggerResourceRequest(laptop, resourceId, name, type, sizeBytes)
            _requestTriggerState.value = result
        }
    }

    fun clearTriggerState() {
        _requestTriggerState.value = null
    }
}
