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
import kotlinx.coroutines.flow.collect
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

    init {
        viewModelScope.launch {
            laptops.collect { list ->
                val current = _selectedLaptop.value
                if (current != null) {
                    val updated = list.find { it.id == current.id }
                    if (updated != null && updated != current) {
                        _selectedLaptop.value = updated
                    }
                }
            }
        }
    }


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
                    val telemetryLocked = result.data.isLocked
                    val statusChanged = currentLaptop.status != LaptopStatus.ONLINE
                    val lockChanged = currentLaptop.isLocked != telemetryLocked
                    
                    if (statusChanged || lockChanged) {
                        if (statusChanged) {
                            laptopRepository.updateLaptopStatus(currentLaptop.id, LaptopStatus.ONLINE)
                        }
                        if (lockChanged) {
                            laptopRepository.updateLaptopLockStatus(currentLaptop.id, telemetryLocked)
                            val method = "PHYSICAL"
                            val eventResult = if (telemetryLocked) "LOCKED" else "SUCCESS"
                            val reason = if (telemetryLocked) "Workstation locked physically" else "Workstation unlocked physically"
                            laptopRepository.logUnlockAttempt(currentLaptop.id, method, eventResult, reason)
                        }
                        _selectedLaptop.value = currentLaptop.copy(
                            status = LaptopStatus.ONLINE,
                            isLocked = telemetryLocked
                        )
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
        pendingIpAddress = ipAddress
        pendingPort = port
        pendingDeviceName = deviceName
        viewModelScope.launch {
            _statusState.value = NetworkResult.Loading
            val res = laptopRepository.checkStatus(ipAddress, port)
            _statusState.value = res
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

    private val _unlockState = MutableStateFlow<NetworkResult<Boolean>?>(null)
    val unlockState: StateFlow<NetworkResult<Boolean>?> = _unlockState.asStateFlow()

    private val _unlockHistory = MutableStateFlow<List<com.systemmonitor.local.database.entity.UnlockHistoryEntity>>(emptyList())
    val unlockHistory: StateFlow<List<com.systemmonitor.local.database.entity.UnlockHistoryEntity>> = _unlockHistory.asStateFlow()

    private var historyJob: kotlinx.coroutines.Job? = null

    private fun loadUnlockHistory(laptopId: String) {
        historyJob?.cancel()
        historyJob = viewModelScope.launch {
            laptopRepository.getUnlockHistory(laptopId).collect {
                _unlockHistory.value = it
            }
        }
    }

    fun selectLaptop(laptop: Laptop) {
        _selectedLaptop.value = laptop
        refreshTelemetry()
        refreshProcesses()
        loadUnlockHistory(laptop.id)
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

    fun clearUnlockState() {
        _unlockState.value = null
    }

    fun getPublicKeyBase64(laptopId: String): String {
        return com.systemmonitor.features.unlock.CryptoManager.getPublicKeyBase64(laptopId)
    }

    fun initSignature(laptopId: String): java.security.Signature {
        return com.systemmonitor.features.unlock.CryptoManager.initSignature(laptopId)
    }

    fun fetchUnlockChallenge(laptop: Laptop, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _unlockState.value = NetworkResult.Loading
            val res = laptopRepository.getUnlockChallenge(laptop)
            if (res is NetworkResult.Success) {
                onSuccess(res.data)
            } else {
                val errMsg = (res as? NetworkResult.Error)?.message ?: "Failed to get challenge"
                _unlockState.value = NetworkResult.Error(errMsg)
                laptopRepository.logUnlockAttempt(laptop.id, "UNKNOWN", "FAILED", errMsg)
            }
        }
    }

    fun unlockLaptopWithSignature(laptop: Laptop, signatureInstance: java.security.Signature, challenge: String, method: String) {
        viewModelScope.launch {
            _unlockState.value = NetworkResult.Loading
            try {
                val signatureBase64 = com.systemmonitor.features.unlock.CryptoManager.signChallenge(signatureInstance, challenge)
                val publicKeyBase64 = com.systemmonitor.features.unlock.CryptoManager.getPublicKeyBase64(laptop.id)
                val res = laptopRepository.submitUnlockSignature(laptop, challenge, signatureBase64, publicKeyBase64)
                if (res is NetworkResult.Success && res.data) {
                    _unlockState.value = NetworkResult.Success(true)
                    laptopRepository.updateLaptopLockStatus(laptop.id, false)
                    laptopRepository.logUnlockAttempt(laptop.id, method, "SUCCESS", "Unlocked via mobile app")
                    _selectedLaptop.value = laptop.copy(isLocked = false)
                } else {
                    val errMsg = (res as? NetworkResult.Error)?.message ?: "Verification rejected by Laptop"
                    _unlockState.value = NetworkResult.Error(errMsg)
                    laptopRepository.logUnlockAttempt(laptop.id, method, "FAILED", errMsg)
                }
            } catch (e: Exception) {
                _unlockState.value = NetworkResult.Error(e.message ?: "Signing error")
                laptopRepository.logUnlockAttempt(laptop.id, method, "FAILED", e.message)
            }
        }
    }

    fun unlockLaptopWithPIN(laptop: Laptop, pin: String) {
        viewModelScope.launch {
            _unlockState.value = NetworkResult.Loading
            val challengeRes = laptopRepository.getUnlockChallenge(laptop)
            if (challengeRes is NetworkResult.Success) {
                val challenge = challengeRes.data
                val dummySignature = android.util.Base64.encodeToString(pin.toByteArray(), android.util.Base64.NO_WRAP)
                val res = laptopRepository.submitUnlockSignature(laptop, challenge, dummySignature, "PIN_VERIFIED")
                if (res is NetworkResult.Success) {
                    _unlockState.value = NetworkResult.Success(true)
                    laptopRepository.updateLaptopLockStatus(laptop.id, false)
                    laptopRepository.logUnlockAttempt(laptop.id, "PIN", "SUCCESS", "Unlocked via mobile PIN")
                    _selectedLaptop.value = laptop.copy(isLocked = false)
                } else {
                    _unlockState.value = NetworkResult.Error("PIN verification rejected by laptop")
                    laptopRepository.logUnlockAttempt(laptop.id, "PIN", "FAILED", "Incorrect PIN signature")
                }
            } else {
                _unlockState.value = NetworkResult.Error("Failed to fetch challenge")
            }
        }
    }
}
