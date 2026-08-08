package com.systemmonitor.domain.usecase

import com.systemmonitor.domain.model.Battery
import com.systemmonitor.repository.BatteryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * One use case per user-facing intent, kept thin — the repository already
 * owns the "how", this just expresses the "what" for the ViewModel.
 */
class GetBatteryInfoUseCase @Inject constructor(
    private val repository: BatteryRepository
) {
    fun observe(): Flow<Battery?> = repository.observeLatest()

    suspend fun refreshNow(): Battery? = repository.captureAndStore()
}
