package com.systemmonitor.features.settings.monitoring

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemmonitor.features.settings.SettingsEvent
import com.systemmonitor.features.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoringSettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val mon = state.settings.monitoring

    var realTime by remember { mutableStateOf(mon.realTimeMonitoringEnabled) }
    var cpuMon by remember { mutableStateOf(mon.cpuMonitoringEnabled) }
    var ramMon by remember { mutableStateOf(mon.ramMonitoringEnabled) }
    var storMon by remember { mutableStateOf(mon.storageMonitoringEnabled) }
    var netMon by remember { mutableStateOf(mon.networkMonitoringEnabled) }

    val bgGradient = Brush.verticalGradient(colors = listOf(Color(0xFF080C16), Color(0xFF0B132B), Color(0xFF070B18)))

    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Monitoring Settings", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(18.dp))

            MonRow(
                icon = Icons.Default.Speed,
                title = "Real-Time Telemetry Sampling",
                desc = "Refresh telemetry metrics every 2 seconds",
                checked = realTime,
                onCheckedChange = {
                    realTime = it
                    viewModel.onEvent(SettingsEvent.UpdateSettings(state.settings.copy(monitoring = mon.copy(realTimeMonitoringEnabled = it))))
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            MonRow(
                icon = Icons.Default.Memory,
                title = "CPU Core Monitoring",
                desc = "Track core frequencies & usage load",
                checked = cpuMon,
                onCheckedChange = {
                    cpuMon = it
                    viewModel.onEvent(SettingsEvent.UpdateSettings(state.settings.copy(monitoring = mon.copy(cpuMonitoringEnabled = it))))
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            MonRow(
                icon = Icons.Default.Memory,
                title = "RAM / Memory Monitoring",
                desc = "Track available & swap RAM utilization",
                checked = ramMon,
                onCheckedChange = {
                    ramMon = it
                    viewModel.onEvent(SettingsEvent.UpdateSettings(state.settings.copy(monitoring = mon.copy(ramMonitoringEnabled = it))))
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            MonRow(
                icon = Icons.Default.Storage,
                title = "Disk Storage Monitoring",
                desc = "Track internal flash & SD card usage",
                checked = storMon,
                onCheckedChange = {
                    storMon = it
                    viewModel.onEvent(SettingsEvent.UpdateSettings(state.settings.copy(monitoring = mon.copy(storageMonitoringEnabled = it))))
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            MonRow(
                icon = Icons.Default.SignalCellularAlt,
                title = "Network Bandwidth Telemetry",
                desc = "Track rx/tx byte counters continuously",
                checked = netMon,
                onCheckedChange = {
                    netMon = it
                    viewModel.onEvent(SettingsEvent.UpdateSettings(state.settings.copy(monitoring = mon.copy(networkMonitoringEnabled = it))))
                }
            )
        }
    }
}

@Composable
private fun MonRow(
    icon: ImageVector,
    title: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.85f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(icon, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(desc, color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF3B82F6))
            )
        }
    }
}
