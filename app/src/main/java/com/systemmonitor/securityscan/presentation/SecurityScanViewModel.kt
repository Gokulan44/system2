package com.systemmonitor.securityscan.presentation

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemmonitor.securityscan.analysis.RiskAnalyzer
import com.systemmonitor.securityscan.analysis.ScanResult
import com.systemmonitor.securityscan.database.entity.FindingEntity
import com.systemmonitor.securityscan.database.entity.ScanHistoryEntity
import com.systemmonitor.securityscan.history.ScanHistoryManager
import com.systemmonitor.securityscan.input.InstalledAppResolver
import com.systemmonitor.securityscan.input.ScanTarget
import com.systemmonitor.securityscan.input.ScanTargetValidator
import com.systemmonitor.securityscan.quarantine.QuarantineManager
import com.systemmonitor.securityscan.update.HashDatabaseUpdater
import com.systemmonitor.securityscan.validation.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class SecurityScanUiState(
    val isScanning: Boolean = false,
    val progress: Int = 0,
    val currentStepText: String = "",
    val scanResult: ScanResult? = null,
    val selectedFinding: FindingEntity? = null,
    val error: String? = null,
    val history: List<ScanHistoryEntity> = emptyList(),
    val installedApps: List<ScanTarget> = emptyList(),
    val quarantineList: List<com.systemmonitor.securityscan.quarantine.QuarantineMetadata> = emptyList()
)

@HiltViewModel
class SecurityScanViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appResolver: InstalledAppResolver,
    private val riskAnalyzer: RiskAnalyzer,
    private val scanHistoryManager: ScanHistoryManager,
    private val quarantineManager: QuarantineManager,
    private val hashDatabaseUpdater: HashDatabaseUpdater,
    private val scanTargetValidator: ScanTargetValidator
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityScanUiState())
    val uiState: StateFlow<SecurityScanUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            scanHistoryManager.getHistoryFlow().collect { list ->
                _uiState.update { it.copy(history = list) }
            }
        }
        loadInstalledApps()
        loadQuarantine()
        seedDatabase()
    }

    fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = appResolver.getInstalledApps()
            _uiState.update { it.copy(installedApps = apps) }
        }
    }

    fun loadQuarantine() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = quarantineManager.getQuarantinedApps()
            _uiState.update { it.copy(quarantineList = list) }
        }
    }

    private fun seedDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            hashDatabaseUpdater.seedLocalDatabase()
        }
    }

    fun scanApkFile(file: File) {
        val pm = context.packageManager
        val archiveInfo = pm.getPackageArchiveInfo(file.absolutePath, 0)
        val packageName = archiveInfo?.packageName ?: file.nameWithoutExtension
        val label = archiveInfo?.applicationInfo?.let { pm.getApplicationLabel(it).toString() } ?: file.name
        val target = ScanTarget(
            packageName = packageName,
            appName = label,
            apkPath = file.absolutePath,
            isSystemApp = false,
            versionName = archiveInfo?.versionName,
            versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                archiveInfo?.longVersionCode ?: 0L
            } else {
                @Suppress("DEPRECATION")
                (archiveInfo?.versionCode ?: 0).toLong()
            }
        )
        startScan(target)
    }

    fun scanInstalledApp(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val target = appResolver.resolveByPackageName(packageName)
            if (target != null) {
                startScan(target)
            } else {
                _uiState.update { it.copy(error = "Could not resolve installed app package.") }
            }
        }
    }

    private fun startScan(target: ScanTarget) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { 
                it.copy(
                    isScanning = true,
                    progress = 0,
                    currentStepText = "Validating target...",
                    scanResult = null,
                    error = null
                )
            }

            // 1. Validate Target
            val validation = scanTargetValidator.validate(target)
            if (validation is ValidationResult.Invalid) {
                _uiState.update { it.copy(isScanning = false, error = "Validation error: ${validation.reason}") }
                return@launch
            }

            // Simulated progress steps for scanning transitions
            val steps = listOf(
                15 to "Checking file integrity and metadata...",
                40 to "Verifying application package signature...",
                70 to "Auditing package requested permissions...",
                90 to "Checking for overlay or accessibility risks...",
                98 to "Calculating threat score and verdict..."
            )

            for ((prog, step) in steps) {
                delay(300)
                _uiState.update { it.copy(progress = prog, currentStepText = step) }
            }

            try {
                val result = riskAnalyzer.analyze(target)
                scanHistoryManager.saveScanResult(result)
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        progress = 100,
                        currentStepText = "Scan Complete!",
                        scanResult = result
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isScanning = false, error = "Scan failed: ${e.localizedMessage}") }
            }
        }
    }

    fun quarantineTarget(packageName: String, appName: String, reason: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val target = appResolver.resolveByPackageName(packageName) ?: ScanTarget(
                packageName = packageName,
                appName = appName,
                apkPath = "",
                isSystemApp = false,
                versionName = null,
                versionCode = 0
            )
            val success = quarantineManager.quarantine(target, reason)
            if (success) {
                _uiState.update { it.copy(scanResult = null, error = null) }
                loadQuarantine()
                loadInstalledApps()
            } else {
                _uiState.update { it.copy(error = "Failed to isolate file to quarantine vault.") }
            }
        }
    }

    fun restoreQuarantineItem(itemId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = quarantineManager.restore(itemId)
            if (success) {
                loadQuarantine()
                loadInstalledApps()
            }
        }
    }

    fun deleteQuarantineItemPermanently(itemId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            quarantineManager.deletePermanently(itemId)
            loadQuarantine()
        }
    }

    fun selectFinding(finding: FindingEntity?) {
        _uiState.update { it.copy(selectedFinding = finding) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetScan() {
        _uiState.update { it.copy(scanResult = null, isScanning = false, progress = 0) }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            scanHistoryManager.clearAllHistory()
        }
    }
}
