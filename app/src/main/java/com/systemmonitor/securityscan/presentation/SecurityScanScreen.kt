package com.systemmonitor.securityscan.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.systemmonitor.securityscan.input.ScanTarget
import com.systemmonitor.securityscan.input.rememberApkPicker
import com.systemmonitor.securityscan.presentation.components.ScanButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScanScreen(
    onBackClick: () -> Unit,
    viewModel: SecurityScanViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val apkPickerLauncher = rememberApkPicker { file ->
        viewModel.scanApkFile(file)
    }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF080C16), Color(0xFF0B132B), Color(0xFF070B18))
    )

    when {
        state.isScanning -> {
            ScanProgressScreen(
                progress = state.progress,
                currentStepText = state.currentStepText,
                onCancel = { viewModel.resetScan() }
            )
        }
        state.selectedFinding != null -> {
            val result = state.scanResult
            if (result != null) {
                ThreatDetailsScreen(
                    finding = state.selectedFinding!!,
                    onBack = { viewModel.selectFinding(null) },
                    onQuarantine = {
                        viewModel.quarantineTarget(result.scanHistory.scanTarget, result.scanHistory.targetName, state.selectedFinding!!.title)
                        viewModel.selectFinding(null)
                    }
                )
            }
        }
        state.scanResult != null -> {
            val result = state.scanResult!!
            ScanResultScreen(
                result = result,
                onBack = { viewModel.resetScan() },
                onViewFinding = { finding -> viewModel.selectFinding(finding) },
                onQuarantine = {
                    viewModel.quarantineTarget(result.scanHistory.scanTarget, result.scanHistory.targetName, "Critical threats found during static scan.")
                },
                onShare = {
                    // Trigger simple system share sheet for audit report
                    val shareText = "Security audit report for ${result.scanHistory.targetName}: Verdict ${result.scanHistory.verdict}, score ${result.scanHistory.score}/100."
                    val sendIntent: android.content.Intent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                    shareIntent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(shareIntent)
                }
            )
        }
        else -> {
            // Main Scanner dashboard with picker + app list + history + quarantine
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Security Scan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF080C16))
                    )
                },
                containerColor = Color.Transparent,
                modifier = modifier.background(bgGradient)
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Scanner Shield Hero
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.85f)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00FFCC).copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = Color(0xFF00FFCC),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text("Deep Threat Scanner", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "Analyze package structures, decompiled bytecode, hashes, and isolated quarantine assets.",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            ScanButton(
                                text = "Scan External APK File",
                                onClick = { apkPickerLauncher() }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Error Box if present
                    if (state.error != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFEF4444))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(state.error!!, color = Color(0xFFEF4444), fontSize = 12.sp, modifier = Modifier.weight(1f))
                                IconButton(onClick = { viewModel.clearError() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFFEF4444))
                                }
                            }
                        }
                    }

                    // Quarantine Section
                    if (state.quarantineList.isNotEmpty()) {
                        Text("Quarantine Vault (${state.quarantineList.size})", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        state.quarantineList.forEach { qItem ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF0F172A).copy(alpha = 0.7f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(qItem.appName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text("Reason: ${qItem.reason}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    }
                                    IconButton(onClick = { viewModel.restoreQuarantineItem(qItem.id) }) {
                                        Icon(Icons.Default.Restore, contentDescription = "Restore", tint = Color(0xFF00FFCC))
                                    }
                                    IconButton(onClick = { viewModel.deleteQuarantineItemPermanently(qItem.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Installed Apps List
                    Text("Installed Application Audit", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    if (state.installedApps.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF00FFCC))
                        }
                    } else {
                        state.installedApps.forEach { app ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                                    .clickable { viewModel.scanInstalledApp(app.packageName) },
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF0F172A).copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF334155)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Apps, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(app.appName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(app.packageName, color = Color(0xFF64748B), fontSize = 11.sp)
                                    }
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Scan", tint = Color(0xFF00FFCC))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
