package com.systemmonitor.applock.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemmonitor.applock.model.LockSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppLockSettingsViewModel @Inject constructor(
    private val repository: AppLockSettingsRepository
) : ViewModel() {

    val settings: StateFlow<LockSettings> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LockSettings())

    fun updateSettings(settings: LockSettings) {
        viewModelScope.launch {
            repository.updateSettings(settings)
        }
    }
}
