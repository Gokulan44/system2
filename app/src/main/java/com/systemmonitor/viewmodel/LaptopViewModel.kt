package com.systemmonitor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemmonitor.data.network.NetworkResult
import com.systemmonitor.domain.model.CommandType
import com.systemmonitor.domain.model.Laptop
import com.systemmonitor.domain.model.UsageInfo
import com.systemmonitor.repository.CommandRepository
import com.systemmonitor.repository.LaptopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LaptopViewModel @Inject constructor(
    private val laptopRepository: LaptopRepository,
    private val commandRepository: CommandRepository
) : ViewModel() {

    val laptops = laptopRepository.allLaptops

    private val _selectedLaptop = MutableStateFlow<Laptop?>(null)
    val selectedLaptop: StateFlow<Laptop?> = _selectedLaptop.asStateFlow()

    private val _telemetryState = MutableStateFlow<NetworkResult<UsageInfo>>(NetworkResult.Loading)
    val telemetryState: StateFlow<NetworkResult<UsageInfo>> = _telemetryState.asStateFlow()

    private val _pairingState = MutableStateFlow<NetworkResult<Laptop>?>(null)
    val pairingState: StateFlow<NetworkResult<Laptop>?> = _pairingState.asStateFlow()

    private val _commandResult = MutableStateFlow<NetworkResult<String>?>(null)
    val commandResult: StateFlow<NetworkResult<String>?> = _commandResult.asStateFlow()

    fun selectLaptop(laptop: Laptop) {
        _selectedLaptop.value = laptop
        refreshTelemetry()
    }

    fun refreshTelemetry() {
        val laptop = _selectedLaptop.value ?: return
        viewModelScope.launch {
            _telemetryState.value = NetworkResult.Loading
            _telemetryState.value = laptopRepository.fetchTelemetry(laptop)
        }
    }

    fun pairLaptop(ipAddress: String, port: Int, pairingCode: String, deviceName: String, deviceId: String) {
        viewModelScope.launch {
            _pairingState.value = NetworkResult.Loading
            val res = laptopRepository.pairLaptop(ipAddress, port, pairingCode, deviceName, deviceId)
            _pairingState.value = res
            if (res is NetworkResult.Success) {
                _selectedLaptop.value = res.data
            }
        }
    }

    fun sendPowerCommand(commandType: CommandType, pin: String?) {
        val laptop = _selectedLaptop.value ?: return
        viewModelScope.launch {
            _commandResult.value = NetworkResult.Loading
            _commandResult.value = commandRepository.sendPowerCommand(laptop, commandType, pin)
        }
    }

    fun clearPairingState() {
        _pairingState.value = null
    }

    fun clearCommandResult() {
        _commandResult.value = null
    }
}
