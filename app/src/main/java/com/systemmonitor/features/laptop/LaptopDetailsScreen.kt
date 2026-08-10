package com.systemmonitor.features.laptop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemmonitor.data.network.NetworkResult
import com.systemmonitor.domain.model.ConnectionMode
import com.systemmonitor.domain.model.Laptop
import com.systemmonitor.viewmodel.LaptopViewModel

import androidx.compose.runtime.DisposableEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaptopDetailsScreen(
    laptopViewModel: LaptopViewModel,
    onNavigateToRemote: () -> Unit,
    onNavigateToStream: () -> Unit,
    onNavigateToProcesses: () -> Unit,
    onNavigateToUsage: () -> Unit,
    onNavigateToNetwork: () -> Unit,
    onBackClick: () -> Unit
) {
    val selectedLaptop by laptopViewModel.selectedLaptop.collectAsState()
    val telemetryState by laptopViewModel.telemetryState.collectAsState()
    val modeSuggestion by laptopViewModel.connectionModeSuggestion.collectAsState()

    DisposableEffect(Unit) {
        laptopViewModel.startTelemetryPolling()
        onDispose {
            laptopViewModel.stopTelemetryPolling()
        }
    }

    val laptop = selectedLaptop ?: Laptop(
        id = "laptop_1",
        name = "My Windows Laptop",
        ipAddress = "192.168.1.50",
        port = 8765
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(laptop.name, color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { laptopViewModel.refreshTelemetry() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color(0xFF00E5FF))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A0F1D))
            )
        },
        containerColor = Color(0xFF0A0F1D)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Connection mode banner ──────────────────────────────────
            val isLocal = laptop.connectionMode == ConnectionMode.LOCAL
            val modeColor = if (isLocal) Color(0xFF00E5FF) else Color(0xFF8B5CF6)
            val modeLabel = if (isLocal) "Local Wi-Fi" else "Remote Cloud"
            val modeIcon = if (isLocal) Icons.Default.Wifi else Icons.Default.Cloud

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = modeColor.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(modeIcon, contentDescription = null, tint = modeColor, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(modeLabel, color = modeColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        if (isLocal) Text("Direct: ${laptop.ipAddress}:${laptop.port}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        else Text("Relay via Firebase Firestore", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    TextButton(
                        onClick = {
                            val nextMode = if (isLocal) ConnectionMode.REMOTE else ConnectionMode.LOCAL
                            laptopViewModel.setConnectionMode(nextMode)
                        }
                    ) {
                        Text("Switch", color = modeColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ── Auto-detect suggestion banner ──────────────────────────
            if (modeSuggestion == ConnectionMode.REMOTE) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF59E0B).copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Local connection failing", color = Color(0xFFF59E0B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Switch to Remote to connect via cloud", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                        TextButton(onClick = { laptopViewModel.setConnectionMode(ConnectionMode.REMOTE) }) {
                            Text("Switch", color = Color(0xFFF59E0B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { laptopViewModel.dismissModeSuggestion() }) {
                            Text("Dismiss", color = Color(0xFF64748B), fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickNavTile(
                    title = "Remote Control",
                    icon = Icons.Default.SettingsRemote,
                    color = Color(0xFF3B82F6),
                    onClick = onNavigateToRemote,
                    modifier = Modifier.weight(1f)
                )
                QuickNavTile(
                    title = "Screen Viewer",
                    icon = Icons.Default.PersonalVideo,
                    color = Color(0xFF10B981),
                    onClick = onNavigateToStream,
                    modifier = Modifier.weight(1f)
                )
                QuickNavTile(
                    title = "Processes",
                    icon = Icons.Default.List,
                    color = Color(0xFFF59E0B),
                    onClick = onNavigateToProcesses,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Live System Telemetry", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            when (val res = telemetryState) {
                is NetworkResult.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF00E5FF))
                    }
                }
                is NetworkResult.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Connection Error", color = Color.Red, fontWeight = FontWeight.Bold)
                            Text(res.message, color = Color(0xFF94A3B8), fontSize = 13.sp)
                        }
                    }
                }
                is NetworkResult.Success -> {
                    val u = res.data
                    TelemetryDetailCard("CPU Usage", "${u.cpu.usagePercent}%", "${u.cpu.processorName} (${u.cpu.logicalCores} Cores)", Color(0xFF00E5FF), onNavigateToUsage)
                    TelemetryDetailCard("RAM Memory", "${u.memory.usagePercent}%", "${u.memory.usedBytes / (1024*1024*1024)}GB / ${u.memory.totalBytes / (1024*1024*1024)}GB Used", Color(0xFF3B82F6), onNavigateToUsage)
                    TelemetryDetailCard("Battery Status", "${u.battery.percent}%", u.battery.status, Color(0xFF10B981))
                    TelemetryDetailCard("Network IO", "Online", "Host: ${u.network.hostname} (${u.network.primaryIp})", Color(0xFF8B5CF6), onNavigateToNetwork)
                }
            }
        }
    }
}

@Composable
fun QuickNavTile(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(85.dp).clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun TelemetryDetailCard(title: String, value: String, subtitle: String, color: Color, onClick: () -> Unit = {}) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, color = Color(0xFF94A3B8), fontSize = 13.sp)
                Text(subtitle, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}
