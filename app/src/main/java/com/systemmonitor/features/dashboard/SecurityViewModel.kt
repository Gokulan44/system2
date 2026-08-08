package com.systemmonitor.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemmonitor.domain.model.SecurityResult
import com.systemmonitor.domain.usecase.CalculateSecurityScoreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SecurityUiState {
    data object Loading : SecurityUiState
    data class Ready(val result: SecurityResult) : SecurityUiState
}

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val calculateSecurityScoreUseCase: CalculateSecurityScoreUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SecurityUiState>(SecurityUiState.Loading)
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    init {
        rescan()
    }

    fun rescan() {
        viewModelScope.launch {
            val result = calculateSecurityScoreUseCase.rescan()
            _uiState.value = SecurityUiState.Ready(result)
        }
    }
}
