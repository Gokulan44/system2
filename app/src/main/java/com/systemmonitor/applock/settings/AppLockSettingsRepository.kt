package com.systemmonitor.applock.settings

import com.systemmonitor.applock.model.LockSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLockSettingsRepository @Inject constructor(
    private val preferences: AppLockPreferences
) {
    private val _settingsFlow = MutableStateFlow(preferences.getSettings())
    val settingsFlow: Flow<LockSettings> = _settingsFlow.asStateFlow()

    fun updateSettings(settings: LockSettings) {
        preferences.saveSettings(settings)
        _settingsFlow.value = settings
    }
}
