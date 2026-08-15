package com.systemmonitor.features.security.presentation.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemmonitor.features.security.components.ScanAnimationView
import com.systemmonitor.features.security.components.ScanCategoryCard
import com.systemmonitor.features.security.components.ScanProgressCard
import com.systemmonitor.features.security.data.repository.SecurityRepository
import com.systemmonitor.features.security.domain.model.SecurityScan
import com.systemmonitor.features.security.domain.scanner.SecurityScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScanUiState(
    val progress: Int = 0,
    val currentStepText: String = "Initializing Scanner...",
    val isScanning: Boolean = true,
    val completedScan: SecurityScan? = null,
    val detectedThreats: List<com.systemmonitor.features.security.domain.model.ThreatInfo> = emptyList()
)

@HiltViewModel
class SecurityScanViewModel @Inject constructor(
    private val securityScanner: SecurityScanner,
    private val repository: SecurityRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    // Guard so init doesn't re-trigger if the composable is recomposed
    private var scanStarted = false

    init {
        startScan()
    }

    fun startScan() {
        // Idempotency guard — prevent duplicate concurrent scans
        if (scanStarted) return
        scanStarted = true

        viewModelScope.launch {
            _uiState.update { it.copy(progress = 0, isScanning = true, completedScan = null, detectedThreats = emptyList()) }

            // Single-pass scan — progress AND threats collected in one Flow
            securityScanner.executeFullScan().collect { scanProgress ->
                _uiState.update {
                    it.copy(
                        progress = scanProgress.percent,
                        currentStepText = scanProgress.stepText,
                        detectedThreats = scanProgress.collectedThreats
                    )
                }

                if (scanProgress.isFinished && scanProgress.finalScan != null) {
                    val result = scanProgress.finalScan
                    repository.saveScanResult(result)
                    _uiState.update {
                        it.copy(
                            progress = 100,
                            isScanning = false,
                            completedScan = result
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SecurityScanScreen(
    viewModel: SecurityScanViewModel = hiltViewModel(),
    onScanComplete: (SecurityScan) -> Unit,
    onCancelClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    val scanSteps = listOf(
        "Checking installed apps",
        "Checking APK information",
        "Checking dangerous permissions",
        "Checking device security configuration",
        "Checking network security",
        "Checking accessibility services",
        "Checking unknown-source configuration",
        "Checking storage/security configuration",
        "Calculating security score"
    )

    LaunchedEffect(state.completedScan) {
        state.completedScan?.let { onScanComplete(it) }
    }

    val bgGradient = Brush.verticalGradient(colors = listOf(Color(0xFF080C16), Color(0xFF0B132B), Color(0xFF070B18)))

    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text("Security Scan in Progress", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Scanning system files, memory & network", color = Color(0xFF94A3B8), fontSize = 13.sp)

            Spacer(modifier = Modifier.height(30.dp))

            // Animated Radar Scanner View
            ScanAnimationView(
                isScanning = state.isScanning,
                threats = state.detectedThreats
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Live Progress Meter Card
            ScanProgressCard(
                progress = state.progress,
                currentStepText = state.currentStepText
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Scan Step Items List
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                scanSteps.forEachIndexed { index, step ->
                    val isDone = state.progress > ((index + 1) * 11)
                    val isCurrent = state.progress in (index * 11)..((index + 1) * 11)
                    val icon = when (index) {
                        0, 1 -> Icons.Default.Apps
                        2, 5 -> Icons.Default.PrivacyTip
                        4 -> Icons.Default.Wifi
                        else -> Icons.Default.Security
                    }
                    val color = when (index) {
                        0, 1 -> Color(0xFF3B82F6)
                        2, 5 -> Color(0xFF8B5CF6)
                        4 -> Color(0xFF00E5FF)
                        else -> Color(0xFF10B981)
                    }

                    ScanCategoryCard(
                        title = step,
                        statusText = if (isDone) "Completed cleanly" else if (isCurrent) "Checking metrics..." else "Waiting to start...",
                        icon = icon,
                        color = color,
                        isDone = isDone,
                        isCurrent = isCurrent
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            OutlinedButton(
                onClick = onCancelClick,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Cancel Scan", fontWeight = FontWeight.Bold)
            }
        }
    }
}
