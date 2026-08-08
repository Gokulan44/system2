package com.systemmonitor.domain.usecase

import com.systemmonitor.domain.model.InstalledApp
import com.systemmonitor.domain.model.SecurityResult
import com.systemmonitor.repository.SecurityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CalculateSecurityScoreUseCase @Inject constructor(
    private val repository: SecurityRepository
) {
    fun observeInstalledApps(): Flow<List<InstalledApp>> = repository.observeInstalledApps()
    suspend fun rescan(): SecurityResult = repository.rescanAndScore()
}
