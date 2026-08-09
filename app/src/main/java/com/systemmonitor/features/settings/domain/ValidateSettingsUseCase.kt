package com.systemmonitor.features.settings.domain

import com.systemmonitor.features.settings.data.SettingsEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ValidateSettingsUseCase @Inject constructor() {
    operator fun invoke(settings: SettingsEntity): Boolean {
        return settings.monitoring.refreshRateSeconds in 1..60 &&
                settings.monitoring.cpuWarningThreshold in 50..99
    }
}
