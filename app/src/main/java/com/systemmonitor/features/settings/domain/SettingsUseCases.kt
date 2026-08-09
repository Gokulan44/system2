package com.systemmonitor.features.settings.domain

import com.systemmonitor.features.settings.SettingsRepository
import com.systemmonitor.features.settings.data.SettingsEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<SettingsEntity> = repository.settingsFlow
}

@Singleton
class UpdateSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(newSettings: SettingsEntity) {
        repository.updateSettings(newSettings)
    }
}

@Singleton
class ResetSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke() {
        repository.updateSettings(SettingsEntity())
    }
}

@Singleton
class ValidateSettingsUseCase @Inject constructor() {
    operator fun invoke(settings: SettingsEntity): Boolean {
        return settings.monitoring.refreshRateSeconds in 1..60 &&
                settings.monitoring.cpuWarningThreshold in 50..99
    }
}
