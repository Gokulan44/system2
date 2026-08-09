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
