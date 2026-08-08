package com.systemmonitor.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemmonitor.domain.model.Memory
import com.systemmonitor.domain.usecase.GetMemoryInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MemoryUiState {
    data object Loading : MemoryUiState
    data class Ready(val memory: Memory) : MemoryUiState
}

@HiltViewModel
class MemoryViewModel @Inject constructor(
    private val getMemoryInfoUseCase: GetMemoryInfoUseCase
) : ViewModel() {

    val uiState: StateFlow<MemoryUiState> = getMemoryInfoUseCase.observe()
        .map { it?.let(MemoryUiState::Ready) ?: MemoryUiState.Loading }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MemoryUiState.Loading)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch { getMemoryInfoUseCase.refreshNow() }
    }
}
