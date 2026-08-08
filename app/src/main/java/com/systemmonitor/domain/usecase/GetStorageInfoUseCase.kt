package com.systemmonitor.domain.usecase

import com.systemmonitor.domain.model.Storage
import com.systemmonitor.repository.StorageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStorageInfoUseCase @Inject constructor(
    private val repository: StorageRepository
) {
    fun observe(): Flow<Storage?> = repository.observeLatest()
    suspend fun refreshNow(): Storage = repository.captureAndStore()
}
