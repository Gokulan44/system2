package com.systemmonitor.features.remotepermission.domain.usecase

import com.systemmonitor.features.remotepermission.data.entity.PermissionHistoryEntity
import com.systemmonitor.features.remotepermission.domain.repository.PermissionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPermissionHistoryUseCase @Inject constructor(
    private val repository: PermissionRepository
) {
    operator fun invoke(): Flow<List<PermissionHistoryEntity>> {
        return repository.getHistoryFlow()
    }
}
