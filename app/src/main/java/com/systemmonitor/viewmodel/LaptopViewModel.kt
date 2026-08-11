package com.systemmonitor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemmonitor.data.network.NetworkResult
import com.systemmonitor.domain.model.CommandType
import com.systemmonitor.domain.model.ConnectionMode
import com.systemmonitor.domain.model.Laptop
import com.systemmonitor.domain.model.LaptopStatus
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
        telemetryJob?.cancel()
        telemetryJob = viewModelScope.launch {
            var failoverCheckCounter = 0
            while (true) {
                val currentLaptop = _selectedLaptop.value ?: break
                val result = laptopRepository.fetchTelemetry(currentLaptop)
                _telemetryState.value = result

                if (result is NetworkResult.Success) {
                    _connectionModeSuggestion.value = null
                    if (currentLaptop.status != LaptopStatus.ONLINE) {
                        laptopRepository.updateLaptopStatus(currentLaptop.id, LaptopStatus.ONLINE)
                        _selectedLaptop.value = currentLaptop.copy(status = LaptopStatus.ONLINE)
                    }
                } else if (result is NetworkResult.Error) {
                    if (currentLaptop.connectionMode == ConnectionMode.LOCAL) {
                        val remoteStatus = laptopRepository.checkStatusForLaptop(currentLaptop.copy(connectionMode = ConnectionMode.REMOTE))
                        if (remoteStatus is NetworkResult.Success && remoteStatus.data) {
                            laptopRepository.updateLaptopStatusAndMode(currentLaptop.id, LaptopStatus.ONLINE, ConnectionMode.REMOTE)
                            _selectedLaptop.value = currentLaptop.copy(connectionMode = ConnectionMode.REMOTE, status = LaptopStatus.ONLINE)
                            _connectionModeSuggestion.value = null
                            continue
                        } else {
                            laptopRepository.updateLaptopStatus(currentLaptop.id, LaptopStatus.OFFLINE)
                            _selectedLaptop.value = currentLaptop.copy(status = LaptopStatus.OFFLINE)
                            _connectionModeSuggestion.value = ConnectionMode.REMOTE
                        }
                    } else {
                        laptopRepository.updateLaptopStatus(currentLaptop.id, LaptopStatus.OFFLINE)
                        _selectedLaptop.value = currentLaptop.copy(status = LaptopStatus.OFFLINE)
                    }
                }

                if (currentLaptop.connectionMode == ConnectionMode.REMOTE) {
                    failoverCheckCounter++
                    if (failoverCheckCounter >= 5) {
                        failoverCheckCounter = 0
                        val localStatus = laptopRepository.checkStatusForLaptop(currentLaptop.copy(connectionMode = ConnectionMode.LOCAL))
                        if (localStatus is NetworkResult.Success && localStatus.data) {
                            laptopRepository.updateLaptopStatusAndMode(currentLaptop.id, LaptopStatus.ONLINE, ConnectionMode.LOCAL)
                            _selectedLaptop.value = currentLaptop.copy(connectionMode = ConnectionMode.LOCAL, status = LaptopStatus.ONLINE)
                            continue
                        }
                    }
                }

                val intervalMs = if (_selectedLaptop.value?.connectionMode == ConnectionMode.LOCAL) 3_000L else 10_000L
                kotlinx.coroutines.delay(intervalMs)
            }
        }
    }

    fun stopTelemetryPolling() {
        telemetryJob?.cancel()
        telemetryJob = null
    }

    fun startProcessesPolling() {
        processesJob?.cancel()
        processesJob = viewModelScope.launch {
            while (true) {
                val laptop = _selectedLaptop.value ?: break
                _processesState.value = laptopRepository.fetchProcesses(laptop)
                val intervalMs = if (laptop.connectionMode == ConnectionMode.LOCAL) 5_000L else 15_000L
                kotlinx.coroutines.delay(intervalMs)
            }
        }
    }

    fun stopProcessesPolling() {
        processesJob?.cancel()
        processesJob = null
    }

    fun unpairLaptop(laptopId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            laptopRepository.deleteLaptop(laptopId)
            if (_selectedLaptop.value?.id == laptopId) {
                _selectedLaptop.value = null
            }
            onComplete()
        }
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
