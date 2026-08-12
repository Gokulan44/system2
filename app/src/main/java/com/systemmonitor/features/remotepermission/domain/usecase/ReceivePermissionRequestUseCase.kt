package com.systemmonitor.features.remotepermission.domain.usecase

import com.systemmonitor.features.remotepermission.domain.model.PermissionRequest
import com.systemmonitor.features.remotepermission.domain.repository.PermissionRepository
import javax.inject.Inject

class ReceivePermissionRequestUseCase @Inject constructor(
    private val repository: PermissionRepository
) {
    suspend operator fun invoke(request: PermissionRequest) {
        repository.saveRequest(request)
    }
}
