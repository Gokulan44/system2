package com.systemmonitor.features.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemmonitor.features.settings.data.SettingsEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.settingsFlow.collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.UpdateSettings -> {
                repository.updateSettings(event.settings)
            }
            is SettingsEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
            }
            is SettingsEvent.ResetToDefaults -> {
                repository.updateSettings(SettingsEntity())
            }
            is SettingsEvent.ClearMessage -> {
                _uiState.update { it.copy(message = null) }
            }
        }
    }
}
