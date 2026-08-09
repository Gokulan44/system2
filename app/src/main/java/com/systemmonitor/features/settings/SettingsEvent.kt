package com.systemmonitor.features.settings

import com.systemmonitor.features.settings.data.SettingsEntity

sealed class SettingsEvent {
    data class UpdateSettings(val settings: SettingsEntity) : SettingsEvent()
    data class SearchQueryChanged(val query: String) : SettingsEvent()
    object ResetToDefaults : SettingsEvent()
    object ClearMessage : SettingsEvent()
}
