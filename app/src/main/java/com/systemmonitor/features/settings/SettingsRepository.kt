package com.systemmonitor.features.settings

import android.content.Context
import android.content.SharedPreferences
import com.systemmonitor.features.settings.data.SettingsEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("system_monitor_settings_prefs", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(SettingsEntity())
    val settingsFlow: StateFlow<SettingsEntity> = _settingsFlow.asStateFlow()

    fun updateSettings(newSettings: SettingsEntity) {
        _settingsFlow.value = newSettings
    }
}
