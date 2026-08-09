package com.systemmonitor.features.settings.domain

import com.systemmonitor.features.settings.SettingsRepository
import com.systemmonitor.features.settings.data.SettingsEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResetSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke() {
        repository.updateSettings(SettingsEntity())
    }
}
