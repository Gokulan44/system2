package com.systemmonitor.features.laptop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemmonitor.data.network.NetworkResult
import com.systemmonitor.viewmodel.LaptopViewModel
import androidx.compose.runtime.DisposableEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaptopUsageScreen(
    laptopViewModel: LaptopViewModel,
    onBackClick: () -> Unit
) {
    val telemetryState by laptopViewModel.telemetryState.collectAsState()

    DisposableEffect(Unit) {
        laptopViewModel.startTelemetryPolling()
        onDispose {
            laptopViewModel.stopTelemetryPolling()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PC Hardware Diagnostics", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A0F1D))
            )
        },
        containerColor = Color(0xFF0A0F1D)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (val res = telemetryState) {
                is NetworkResult.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF00E5FF))
                    }
                }
                is NetworkResult.Error -> {
                    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("Error fetching diagnostics: ${res.message}", color = Color(0xFFEF4444), fontSize = 14.sp)
                    }
                }
                is NetworkResult.Success -> {
                    val u = res.data
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Section 1: CPU Detail Card
                        MetricSectionCard(
                            title = "Processor Information",
                            icon = Icons.Default.DeveloperBoard,
                            color = Color(0xFF00E5FF)
                        ) {
                            TelemetryRow("Processor name", u.cpu.processorName)
                            TelemetryRow("Logical Cores", "${u.cpu.logicalCores} threads")
                            TelemetryRow("Physical Cores", "${u.cpu.physicalCores} cores")
                            TelemetryRow("Clock Frequency", "${u.cpu.frequencyMhz.toInt()} MHz")
                            TelemetryRow("Peak Frequency", "${u.cpu.maxFrequencyMhz.toInt()} MHz")
                            TelemetryRow("Total CPU Load", "${u.cpu.usagePercent}%", isValueBold = true)
                        }

                        // Section 2: RAM Memory Card
                        MetricSectionCard(
                            title = "System Memory Usage",
                            icon = Icons.Default.Memory,
                            color = Color(0xFF3B82F6)
                        ) {
                            val totalGB = u.memory.totalBytes / (1024 * 1024 * 1024f)
                            val usedGB = u.memory.usedBytes / (1024 * 1024 * 1024f)
                            val freeGB = u.memory.freeBytes / (1024 * 1024 * 1024f)
                            val swapTotalGB = u.memory.swapTotalBytes / (1024 * 1024 * 1024f)
                            val swapUsedGB = u.memory.swapUsedBytes / (1024 * 1024 * 1024f)

                            TelemetryRow("Total Capacity", String.format("%.1f GB", totalGB))
                            TelemetryRow("Allocated RAM", String.format("%.1f GB", usedGB))
                            TelemetryRow("Available RAM", String.format("%.1f GB", freeGB))
                            TelemetryRow("Active Swap File", String.format("%.1f GB / %.1f GB", swapUsedGB, swapTotalGB))
                            TelemetryRow("RAM Load", "${u.memory.usagePercent}%", isValueBold = true)
                        }

                        // Section 3: Storage Partitions Card
                        MetricSectionCard(
                            title = "Disk Partition Diagnostics",
                            icon = Icons.Default.Storage,
                            color = Color(0xFFE2E8F0)
                        ) {
                            if (u.storage.partitions.isEmpty()) {
                                Text("No storage partitions detected", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            } else {
                                u.storage.partitions.forEachIndexed { index, part ->
                                    if (index > 0) Spacer(modifier = Modifier.height(10.dp))
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                            .padding(10.dp)
                                    ) {
                                        Text("${part.device} (${part.mountpoint})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        val totalGB = part.totalBytes / (1024 * 1024 * 1024f)
                                        val usedGB = part.usedBytes / (1024 * 1024 * 1024f)
                                        TelemetryRow("File system", part.fstype)
                                        TelemetryRow("Capacity", String.format("%.1f GB", totalGB))
                                        TelemetryRow("Disk space used", String.format("%.1f GB (%.1f%%)", usedGB, part.usagePercent))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricSectionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.85f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun TelemetryRow(label: String, value: String, isValueBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color(0xFF94A3B8), fontSize = 13.sp)
        Text(
            text = value,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = if (isValueBold) FontWeight.Bold else FontWeight.Medium
        )
    }
}
