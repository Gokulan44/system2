package com.systemmonitor.features.settings.security

import androidx.lifecycle.ViewModel
import com.systemmonitor.features.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SecuritySettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel()
