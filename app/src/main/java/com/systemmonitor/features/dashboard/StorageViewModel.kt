package com.systemmonitor.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemmonitor.domain.model.Storage
import com.systemmonitor.domain.usecase.GetStorageInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface StorageUiState {
    data object Loading : StorageUiState
    data class Ready(val storage: Storage) : StorageUiState
}

@HiltViewModel
class StorageViewModel @Inject constructor(
    private val getStorageInfoUseCase: GetStorageInfoUseCase
) : ViewModel() {

    val uiState: StateFlow<StorageUiState> = getStorageInfoUseCase.observe()
        .map { it?.let(StorageUiState::Ready) ?: StorageUiState.Loading }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StorageUiState.Loading)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch { getStorageInfoUseCase.refreshNow() }
    }
}
