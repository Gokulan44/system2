package com.systemmonitor.features.laptop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemmonitor.data.network.NetworkResult
import com.systemmonitor.viewmodel.LaptopViewModel
import androidx.compose.runtime.DisposableEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkScreen(
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
                title = { Text("PC Network Telemetry", color = Color.White, fontWeight = FontWeight.Bold) },
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
                        Text("Error fetching network stats: ${res.message}", color = Color(0xFFEF4444), fontSize = 14.sp)
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
                        // Section 1: Broad Connection Stats
                        MetricSectionCard(
                            title = "Network Interfaces configuration",
                            icon = Icons.Default.NetworkCheck,
                            color = Color(0xFF8B5CF6)
                        ) {
                            TelemetryRow("Host System Name", u.network.hostname)
                            TelemetryRow("Primary IP Address", u.network.primaryIp)
                            val sentMB = u.network.bytesSent / (1024 * 1024f)
                            val recvMB = u.network.bytesRecv / (1024 * 1024f)
                            val sentPackets = u.network.packetsSent
                            val recvPackets = u.network.packetsRecv

                            TelemetryRow("Data Transmitted", String.format("%.2f MB (%s packets)", sentMB, sentPackets))
                            TelemetryRow("Data Received", String.format("%.2f MB (%s packets)", recvMB, recvPackets))
                        }

                        // Section 2: Active IP Bindings List
                        MetricSectionCard(
                            title = "Subnet Adapter Bindings",
                            icon = Icons.Default.SettingsInputAntenna,
                            color = Color(0xFF10B981)
                        ) {
                            if (u.network.interfaces.isEmpty()) {
                                Text("No interfaces detected", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            } else {
                                u.network.interfaces.forEachIndexed { index, iface ->
                                    if (index > 0) Spacer(modifier = Modifier.height(10.dp))
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                            .padding(10.dp)
                                    ) {
                                        Text(iface.interfaceName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        TelemetryRow("IPv4 Address", iface.ipAddress)
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
