package com.systemmonitor.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemmonitor.domain.model.Battery
import com.systemmonitor.domain.usecase.GetBatteryInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface BatteryUiState {
    data object Loading : BatteryUiState
    data class Ready(val battery: Battery) : BatteryUiState
    data object Unavailable : BatteryUiState
}

@HiltViewModel
class BatteryViewModel @Inject constructor(
    private val getBatteryInfoUseCase: GetBatteryInfoUseCase
) : ViewModel() {

    val uiState: StateFlow<BatteryUiState> = getBatteryInfoUseCase.observe()
        .map { it?.let(BatteryUiState::Ready) ?: BatteryUiState.Unavailable }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BatteryUiState.Loading)

    private val _history = MutableStateFlow<List<Battery>>(emptyList())
    val history: StateFlow<List<Battery>> = _history.asStateFlow()

    private val _avgLevel = MutableStateFlow<Double?>(null)
    val avgLevel: StateFlow<Double?> = _avgLevel.asStateFlow()

    private val _avgTemp = MutableStateFlow<Double?>(null)
    val avgTemp: StateFlow<Double?> = _avgTemp.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            getBatteryInfoUseCase.refreshNow()
            loadHistory()
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _history.value = getBatteryInfoUseCase.getHistory()
            _avgLevel.value = getBatteryInfoUseCase.getAverageLevel()
            _avgTemp.value = getBatteryInfoUseCase.getAverageTemp()
        }
    }
}
