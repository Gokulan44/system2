package com.systemmonitor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemmonitor.data.network.NetworkResult
import com.systemmonitor.domain.model.CommandType
import com.systemmonitor.domain.model.ConnectionMode
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

    // Emits a suggested mode when auto-detect detects a failure on the current mode
    private val _connectionModeSuggestion = MutableStateFlow<ConnectionMode?>(null)
    val connectionModeSuggestion: StateFlow<ConnectionMode?> = _connectionModeSuggestion.asStateFlow()

    private var telemetryJob: kotlinx.coroutines.Job? = null
    private var processesJob: kotlinx.coroutines.Job? = null

    /** Mode chosen during the Add Laptop / pairing flow before the laptop is saved. */
    var pendingConnectionMode: ConnectionMode = ConnectionMode.LOCAL

    // --- Polling ---

    fun startTelemetryPolling() {
        val laptop = _selectedLaptop.value ?: return
        val intervalMs = if (laptop.connectionMode == ConnectionMode.LOCAL) 3_000L else 10_000L
        telemetryJob?.cancel()
        telemetryJob = viewModelScope.launch {
            while (true) {
                val result = laptopRepository.fetchTelemetry(laptop)
                _telemetryState.value = result
                // Auto-detect: if LOCAL fails, suggest switching to REMOTE
                if (result is NetworkResult.Error && laptop.connectionMode == ConnectionMode.LOCAL) {
                    _connectionModeSuggestion.value = ConnectionMode.REMOTE
                } else if (result is NetworkResult.Success) {
                    _connectionModeSuggestion.value = null
                }
                kotlinx.coroutines.delay(intervalMs)
            }
        }
    }

    fun stopTelemetryPolling() {
        telemetryJob?.cancel()
        telemetryJob = null
    }

    fun startProcessesPolling() {
        val laptop = _selectedLaptop.value ?: return
        val intervalMs = if (laptop.connectionMode == ConnectionMode.LOCAL) 5_000L else 15_000L
        processesJob?.cancel()
        processesJob = viewModelScope.launch {
            while (true) {
                _processesState.value = laptopRepository.fetchProcesses(laptop)
                kotlinx.coroutines.delay(intervalMs)
            }
        }
    }

    fun stopProcessesPolling() {
        processesJob?.cancel()
        processesJob = null
    }

    // --- Connection mode switching ---

    /** Switch the connection mode for the currently selected laptop and restart polling. */
    fun setConnectionMode(mode: ConnectionMode) {
        val laptop = _selectedLaptop.value ?: return
        viewModelScope.launch {
            laptopRepository.updateConnectionMode(laptop.id, mode)
            _selectedLaptop.value = laptop.copy(connectionMode = mode)
            _connectionModeSuggestion.value = null
            stopTelemetryPolling()
            stopProcessesPolling()
            startTelemetryPolling()
            startProcessesPolling()
        }
    }

    fun dismissModeSuggestion() {
        _connectionModeSuggestion.value = null
    }

    // --- Pairing ---

    private val _pairingState = MutableStateFlow<NetworkResult<Laptop>?>(null)
    val pairingState: StateFlow<NetworkResult<Laptop>?> = _pairingState.asStateFlow()

    private val _statusState = MutableStateFlow<NetworkResult<Boolean>?>(null)
    val statusState: StateFlow<NetworkResult<Boolean>?> = _statusState.asStateFlow()

    var pendingIpAddress = ""
    var pendingPort = 8765
    var pendingDeviceName = ""

    fun checkLaptopStatus(ipAddress: String, port: Int, deviceName: String) {
        viewModelScope.launch {
            _statusState.value = NetworkResult.Loading
            val res = laptopRepository.checkStatus(ipAddress, port)
            _statusState.value = res
            if (res is NetworkResult.Success) {
                pendingIpAddress = ipAddress
                pendingPort = port
                pendingDeviceName = deviceName
            }
        }
    }

    fun clearStatusState() {
        _statusState.value = null
    }

    // --- Command / Processes ---

    private val _commandResult = MutableStateFlow<NetworkResult<String>?>(null)
    val commandResult: StateFlow<NetworkResult<String>?> = _commandResult.asStateFlow()

    private val _processesState = MutableStateFlow<NetworkResult<List<com.systemmonitor.domain.model.ProcessInfo>>>(NetworkResult.Loading)
    val processesState: StateFlow<NetworkResult<List<com.systemmonitor.domain.model.ProcessInfo>>> = _processesState.asStateFlow()

    fun selectLaptop(laptop: Laptop) {
        _selectedLaptop.value = laptop
        refreshTelemetry()
        refreshProcesses()
    }

    fun refreshTelemetry() {
        val laptop = _selectedLaptop.value ?: return
        viewModelScope.launch {
            _telemetryState.value = NetworkResult.Loading
            _telemetryState.value = laptopRepository.fetchTelemetry(laptop)
        }
    }

    fun refreshProcesses() {
        val laptop = _selectedLaptop.value ?: return
        viewModelScope.launch {
            _processesState.value = NetworkResult.Loading
            _processesState.value = laptopRepository.fetchProcesses(laptop)
        }
    }

    fun pairLaptop(
        ipAddress: String,
        port: Int,
        pairingCode: String,
        deviceName: String,
        deviceId: String
    ) {
        viewModelScope.launch {
            _pairingState.value = NetworkResult.Loading
            val res = laptopRepository.pairLaptop(
                ipAddress, port, pairingCode, deviceName, deviceId,
                connectionMode = pendingConnectionMode
            )
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
