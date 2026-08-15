package com.systemmonitor.features.remotepermission.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemmonitor.core.security.AndroidKeyStoreManager
import com.systemmonitor.data.network.ConnectionManager
import com.systemmonitor.data.network.NetworkResult
import com.systemmonitor.domain.model.Laptop
import com.systemmonitor.features.remotepermission.data.entity.PermissionHistoryEntity
import com.systemmonitor.features.remotepermission.domain.model.*
import com.systemmonitor.features.remotepermission.domain.repository.PermissionRepository
import com.systemmonitor.features.remotepermission.domain.usecase.ApproveResourceUseCase
import com.systemmonitor.features.remotepermission.domain.usecase.DenyResourceUseCase
import com.systemmonitor.features.remotepermission.domain.usecase.GetPermissionHistoryUseCase
import com.systemmonitor.features.remotepermission.domain.usecase.ReceivePermissionRequestUseCase
import com.systemmonitor.features.remotepermission.verification.UserVerificationManager
import com.systemmonitor.repository.LaptopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.security.Signature
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val repository: PermissionRepository,
    private val approveResourceUseCase: ApproveResourceUseCase,
    private val denyResourceUseCase: DenyResourceUseCase,
    private val getPermissionHistoryUseCase: GetPermissionHistoryUseCase,
    private val receivePermissionRequestUseCase: ReceivePermissionRequestUseCase,
    private val keyStoreManager: AndroidKeyStoreManager,
    private val verificationManager: UserVerificationManager,
    private val connectionManager: ConnectionManager,
    private val laptopRepository: LaptopRepository
) : ViewModel() {

    private val _activeRequest = MutableStateFlow<PermissionRequest?>(null)
    val activeRequest: StateFlow<PermissionRequest?> = _activeRequest.asStateFlow()

    private val _history = MutableStateFlow<List<PermissionHistoryEntity>>(emptyList())
    val history: StateFlow<List<PermissionHistoryEntity>> = _history.asStateFlow()

    private val _actionState = MutableStateFlow<NetworkResult<Boolean>?>(null)
    val actionState: StateFlow<NetworkResult<Boolean>?> = _actionState.asStateFlow()

    init {
        // Observe pending requests automatically
        viewModelScope.launch {
            repository.getAllRequests().collectLatest { list ->
                val pending = list.firstOrNull { it.status == PermissionStatus.PENDING }
                _activeRequest.value = pending
            }
        }

        // Observe history
        viewModelScope.launch {
            getPermissionHistoryUseCase().collectLatest { list ->
                _history.value = list
            }
        }
    }

    fun initSignatureForLaptop(laptopId: String, useBiometric: Boolean = true): Signature {
        return keyStoreManager.initSignature(laptopId, useBiometric)
    }

    fun approveRequest(request: PermissionRequest, signatureInstance: Signature, verificationMethod: String) {
        viewModelScope.launch {
            _actionState.value = NetworkResult.Loading
            try {
                // Log user verification success
                verificationManager.logVerificationAttempt(request.requestId, verificationMethod, true)

                // Generate signed approval token
                val approvalTokenJson = approveResourceUseCase(request, signatureInstance, verificationMethod)

                // Load laptop details
                val laptop = laptopRepository.getAllLaptopsList().firstOrNull { it.id == request.laptopId }
                if (laptop != null) {
                    val result = connectionManager.approveResource(laptop, approvalTokenJson)
                    when (result) {
                        is NetworkResult.Success -> {
                            _actionState.value = NetworkResult.Success(true)
                            // Clear active request
                            if (_activeRequest.value?.requestId == request.requestId) {
                                _activeRequest.value = null
                            }
                        }
                        is NetworkResult.Error -> {
                            _actionState.value = NetworkResult.Error(result.message)
                        }
                        else -> {}
                    }
                } else {
                    _actionState.value = NetworkResult.Error("Requested laptop details could not be found.")
                }
            } catch (e: Exception) {
                _actionState.value = NetworkResult.Error(e.message ?: "Approval failed")
            }
        }
    }

    fun denyRequest(request: PermissionRequest) {
        viewModelScope.launch {
            _actionState.value = NetworkResult.Loading
            try {
                denyResourceUseCase(request)
                _actionState.value = NetworkResult.Success(true)
                if (_activeRequest.value?.requestId == request.requestId) {
                    _activeRequest.value = null
                }
            } catch (e: Exception) {
                _actionState.value = NetworkResult.Error(e.message ?: "Denial failed")
            }
        }
    }

    fun clearActionState() {
        _actionState.value = null
    }

    fun logVerificationFailure(requestId: String, method: String) {
        viewModelScope.launch {
            verificationManager.logVerificationAttempt(requestId, method, false)
        }
    }

    fun simulateRequest(
        laptopId: String,
        resourceName: String,
        resourceType: ResourceType = ResourceType.FILE,
        fileSizeBytes: Long = 12 * 1024 * 1024L
    ) {
        viewModelScope.launch {
            val reqId = "req_${UUID.randomUUID().toString().take(6)}"
            val dummyReq = PermissionRequest(
                requestId = reqId,
                laptopId = laptopId,
                resource = ResourceRequest(
                    resourceId = "res_${UUID.randomUUID().toString().take(6)}",
                    name = resourceName,
                    type = resourceType,
                    sizeBytes = fileSizeBytes,
                    path = "C:\\Downloads\\$resourceName"
                ),
                requestedOperation = PermissionType.DOWNLOAD,
                createdAt = System.currentTimeMillis(),
                expiresAt = System.currentTimeMillis() + 5 * 60 * 1000L, // 5 mins
                requestNonce = UUID.randomUUID().toString(),
                status = PermissionStatus.PENDING
            )
            receivePermissionRequestUseCase(dummyReq)
        }
    }
}
