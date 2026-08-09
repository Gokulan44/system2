package com.systemmonitor.features.security.presentation.scan

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val completedScan: SecurityScan? = null
)

@HiltViewModel
class SecurityScanViewModel @Inject constructor(
    private val securityScanner: SecurityScanner,
    private val repository: SecurityRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    init {
        startScan()
    }

    fun startScan() {
        viewModelScope.launch {
            _uiState.update { it.copy(progress = 0, isScanning = true) }
            securityScanner.executeFullScan().collect { (prog, text) ->
                _uiState.update { it.copy(progress = prog, currentStepText = text) }
            }
            val result = securityScanner.performComprehensiveScan()
            repository.saveScanResult(result)
            _uiState.update { it.copy(progress = 100, isScanning = false, completedScan = result) }
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

            Spacer(modifier = Modifier.height(40.dp))

            // Progress Ring / Circular Shield
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E5FF).copy(alpha = 0.12f))
                        .border(3.dp, Color(0xFF00E5FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${state.progress}%", color = Color(0xFF00E5FF), fontSize = 36.sp, fontWeight = FontWeight.Bold)
                        Text(if (state.isScanning) "Scanning..." else "Complete", color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Step Progress Meter
            LinearProgressIndicator(
                progress = { state.progress / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF00E5FF),
                trackColor = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(state.currentStepText, color = Color(0xFF00E5FF), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(30.dp))

            // Scan Step Items List
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                scanSteps.forEachIndexed { index, step ->
                    val isDone = state.progress > ((index + 1) * 11)
                    val isCurrent = state.progress in (index * 11)..((index + 1) * 11)

                    Surface(
                        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.75f)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(step, color = if (isDone || isCurrent) Color.White else Color(0xFF64748B), fontSize = 13.sp, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal)
                            if (isDone) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                            } else if (isCurrent) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF00E5FF), strokeWidth = 2.dp)
                            }
                        }
                    }
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
