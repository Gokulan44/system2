package com.systemmonitor.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemmonitor.domain.model.NetworkInfo
import com.systemmonitor.domain.model.WifiInfo
import com.systemmonitor.domain.usecase.GetNetworkInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface NetworkUiState {
    data object Loading : NetworkUiState
    data class Ready(val network: NetworkInfo, val wifi: WifiInfo?) : NetworkUiState
}

@HiltViewModel
class NetworkViewModel @Inject constructor(
    private val getNetworkInfoUseCase: GetNetworkInfoUseCase
) : ViewModel() {

    val uiState: StateFlow<NetworkUiState> = combine(
        getNetworkInfoUseCase.observeNetwork(),
        getNetworkInfoUseCase.observeWifi()
    ) { network, wifi ->
        network?.let { NetworkUiState.Ready(it, wifi) } ?: NetworkUiState.Loading
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NetworkUiState.Loading)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch { getNetworkInfoUseCase.refreshNow() }
    }
}
