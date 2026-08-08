package com.systemmonitor.domain.usecase

import com.systemmonitor.domain.model.Memory
import com.systemmonitor.repository.MemoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMemoryInfoUseCase @Inject constructor(
    private val repository: MemoryRepository
) {
    fun observe(): Flow<Memory?> = repository.observeLatest()
    suspend fun refreshNow(): Memory = repository.captureAndStore()
}
