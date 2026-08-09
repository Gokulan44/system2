package com.systemmonitor.features.settings

import com.systemmonitor.features.settings.data.SettingsEntity

data class SettingsState(
    val settings: SettingsEntity = SettingsEntity(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val message: String? = null
)
