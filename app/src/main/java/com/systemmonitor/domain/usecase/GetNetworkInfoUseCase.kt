package com.systemmonitor.domain.usecase

import com.systemmonitor.domain.model.NetworkInfo
import com.systemmonitor.domain.model.WifiInfo
import com.systemmonitor.repository.NetworkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNetworkInfoUseCase @Inject constructor(
    private val repository: NetworkRepository
) {
    fun observeNetwork(): Flow<NetworkInfo?> = repository.observeLatestNetwork()
    fun observeWifi(): Flow<WifiInfo?> = repository.observeLatestWifi()
    suspend fun refreshNow(): NetworkInfo = repository.captureAndStore()
}
