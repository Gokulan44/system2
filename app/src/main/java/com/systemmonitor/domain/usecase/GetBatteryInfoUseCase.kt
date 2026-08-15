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

    suspend fun getHistory(): List<Battery> =
        repository.getHistorySince(System.currentTimeMillis() - 24 * 60 * 60 * 1000)

    suspend fun getAverageLevel(): Double? =
        repository.getSummarySince(System.currentTimeMillis() - 24 * 60 * 60 * 1000).averageLevelPercent

    suspend fun getAverageTemp(): Double? =
        repository.getSummarySince(System.currentTimeMillis() - 24 * 60 * 60 * 1000).averageTemperatureCelsius
}
