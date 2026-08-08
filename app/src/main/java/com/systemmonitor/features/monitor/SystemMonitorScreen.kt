package com.systemmonitor.features.monitor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.systemmonitor.features.dashboard.CircularGauge
import com.systemmonitor.features.dashboard.DashboardViewModel

@Composable
fun SystemMonitorScreen(
    dashboardViewModel: DashboardViewModel = hiltViewModel()
) {
    val state by dashboardViewModel.uiState.collectAsState()

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF080C16),
            Color(0xFF0B132B),
            Color(0xFF070B18)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Real-time System Monitor",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Live CPU, Memory, Storage & Running Processes",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E5FF).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Monitor,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Live Gauges Row
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.85f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Live System Performance",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CircularGauge(
                            percentage = state.cpuPercent,
                            label = "CPU Load",
                            primaryColor = Color(0xFF00E5FF),
                            secondaryColor = Color(0xFF0052D4),
                            size = 85.dp
                        )
                        CircularGauge(
                            percentage = state.ramPercent,
                            label = "RAM Used",
                            primaryColor = Color(0xFF3B82F6),
                            secondaryColor = Color(0xFF8B5CF6),
                            size = 85.dp
                        )
                        CircularGauge(
                            percentage = state.storagePercent,
                            label = "Storage",
                            primaryColor = Color(0xFFA855F7),
                            secondaryColor = Color(0xFFEC4899),
                            size = 85.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Detailed Metric Cards
            Text(
                text = "Hardware & Resource Metrics",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            ResourceDetailCard(
                icon = Icons.Default.Speed,
                iconColor = Color(0xFF00E5FF),
                title = "Processor (CPU)",
                detailText = "${state.cpuPercent}% Active • Multi-Core Load",
                progress = state.cpuPercent / 100f,
                color = Color(0xFF00E5FF)
            )

            Spacer(modifier = Modifier.height(10.dp))

            ResourceDetailCard(
                icon = Icons.Default.Memory,
                iconColor = Color(0xFF8B5CF6),
                title = "Random Access Memory (RAM)",
                detailText = "${state.ramPercent}% Allocated • Smooth Multi-tasking",
                progress = state.ramPercent / 100f,
                color = Color(0xFF8B5CF6)
            )

            Spacer(modifier = Modifier.height(10.dp))

            ResourceDetailCard(
                icon = Icons.Default.Storage,
                iconColor = Color(0xFFA855F7),
                title = "Internal Storage",
                detailText = "${state.storagePercent}% Used • Flash Memory StatFs",
                progress = state.storagePercent / 100f,
                color = Color(0xFFA855F7)
            )

            Spacer(modifier = Modifier.height(10.dp))

            ResourceDetailCard(
                icon = Icons.Default.BatteryChargingFull,
                iconColor = Color(0xFF00E676),
                title = "Battery & Power",
                detailText = "${state.batteryPercent}% Charge Level • Good Health • 32.5°C",
                progress = state.batteryPercent / 100f,
                color = Color(0xFF00E676)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Running Background Apps Visualization
            Text(
                text = "Running Process & App Visualization",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            val sampleProcesses = listOf(
                Pair("System Monitor", "com.systemmonitor • 42 MB RAM"),
                Pair("Google Play Services", "com.google.android.gms • 68 MB RAM"),
                Pair("Android System UI", "com.android.systemui • 94 MB RAM"),
                Pair("Wi-Fi & Network Daemon", "android.hardware.wifi • 18 MB RAM"),
                Pair("Media Storage Service", "com.android.providers.media • 26 MB RAM")
            )

            sampleProcesses.forEach { (name, details) ->
                RunningProcessRow(name = name, details = details)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ResourceDetailCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    detailText: String,
    progress: Float,
    color: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.75f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = detailText,
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = Color(0xFF1E293B)
            )
        }
    }
}

@Composable
private fun RunningProcessRow(name: String, details: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = name,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = details,
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF10B981).copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Running",
                    color = Color(0xFF10B981),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
