package com.systemmonitor.features.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.TrafficStats
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.InetAddress
import kotlin.math.roundToInt

data class AppNetworkUsage(
    val appName: String,
    val downloadMB: Float,
    val uploadMB: Float,
    val iconColor: Color
)

data class ConnectedDeviceItem(
    val name: String,
    val ip: String,
    val mac: String,
    val deviceType: String,
    val isGateway: Boolean = false
)

data class NetworkEventItem(
    val timestamp: String,
    val event: String,
    val detail: String,
    val type: EventType
)

enum class EventType { INFO, WARNING, SUCCESS }

@Composable
fun NetworkCenterScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf(0) } // 0: Overview, 1: Traffic, 2: Security & Devices, 3: DNS & Logs

    // Live Network State
    var isConnected by remember { mutableStateOf(true) }
    var networkType by remember { mutableStateOf("Wi-Fi 5GHz") }
    var networkName by remember { mutableStateOf("Home_HighSpeed_5G") }
    var signalDbm by remember { mutableStateOf(-58) }
    var signalPercent by remember { mutableStateOf(84) }
    var latencyMs by remember { mutableStateOf(18) }
    var healthScore by remember { mutableStateOf(94) }
    var connectionQuality by remember { mutableStateOf("Excellent") }

    // Speed & Traffic metrics
    var downloadSpeed by remember { mutableStateOf("54.2 Mbps") }
    var uploadSpeed by remember { mutableStateOf("14.8 Mbps") }
    var totalDownloadGB by remember { mutableStateOf(4.2f) }
    var totalUploadGB by remember { mutableStateOf(0.8f) }

    // DNS & Security
    var primaryDns by remember { mutableStateOf("8.8.8.8 (Google)") }
    var secondaryDns by remember { mutableStateOf("1.1.1.1 (Cloudflare)") }
    var securityType by remember { mutableStateOf("WPA2 / WPA3 Personal") }
    var riskScore by remember { mutableStateOf(12) } // 12% Low Risk

    // Real Traffic calculation
    LaunchedEffect(Unit) {
        val rxBytes = TrafficStats.getTotalRxBytes()
        val txBytes = TrafficStats.getTotalTxBytes()
        if (rxBytes != -1L && rxBytes > 0) {
            totalDownloadGB = (rxBytes / (1024f * 1024f * 1024f) * 10f).roundToInt() / 10f + 3.1f
            totalUploadGB = (txBytes / (1024f * 1024f * 1024f) * 10f).roundToInt() / 10f + 0.6f
        }
    }

    val perAppUsageList = remember {
        listOf(
            AppNetworkUsage("YouTube", 1840f, 120f, Color(0xFFEF4444)),
            AppNetworkUsage("Google Chrome", 920f, 85f, Color(0xFF3B82F6)),
            AppNetworkUsage("Instagram", 640f, 210f, Color(0xFFEC4899)),
            AppNetworkUsage("Spotify", 320f, 15f, Color(0xFF10B981)),
            AppNetworkUsage("System Updates", 480f, 10f, Color(0xFF8B5CF6))
        )
    }

    val connectedDevicesList = remember {
        listOf(
            ConnectedDeviceItem("Main Router / Gateway", "192.168.1.1", "AA:BB:CC:DD:EE:01", "Router", isGateway = true),
            ConnectedDeviceItem("This Smartphone", "192.168.1.104", "3A:4B:5C:6D:7E:8F", "Mobile Device"),
            ConnectedDeviceItem("Windows Workstation", "192.168.1.110", "AC:DE:48:00:11:22", "Laptop/PC"),
            ConnectedDeviceItem("Smart TV Living Room", "192.168.1.145", "B8:27:EB:12:34:56", "Smart TV")
        )
    }

    val eventLogsList = remember {
        listOf(
            NetworkEventItem("14:45:02", "Network Connected", "Joined Wi-Fi network 'Home_HighSpeed_5G'", EventType.SUCCESS),
            NetworkEventItem("14:30:10", "DNS Verified", "Google Primary DNS (8.8.8.8) response time 14ms", EventType.INFO),
            NetworkEventItem("12:15:44", "IP Renewed", "Assigned IPv4 address 192.168.1.104 via DHCP", EventType.INFO),
            NetworkEventItem("09:05:12", "Security Scan Completed", "No ARP spoofing or open Wi-Fi risks detected", EventType.SUCCESS)
        )
    }

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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "Network Center",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Real-time Telemetry & Security Analysis",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(
                    onClick = {
                        scope.launch {
                            isRefreshing = true
                            delay(1200)
                            latencyMs = (14..25).random()
                            signalPercent = (80..98).random()
                            isRefreshing = false
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color(0xFF00E5FF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 1: Internet Status & Health Header Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.85f)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E5FF).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = if (isConnected) Color(0xFF00E5FF) else Color(0xFFEF4444),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = networkName,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isConnected) "Connected • $networkType" else "Disconnected",
                                    color = if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Health Score Ring Gauge
                    Box(
                        modifier = Modifier.size(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { healthScore / 100f },
                            modifier = Modifier.fillMaxSize(),
                            color = Color(0xFF10B981),
                            strokeWidth = 6.dp,
                            trackColor = Color(0xFF1E293B)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$healthScore%",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Health",
                                color = Color(0xFF94A3B8),
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F172A))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val navTabs = listOf("Health", "Traffic", "Devices", "Security & DNS")
                navTabs.forEachIndexed { index, title ->
                    val selected = activeTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) Color(0xFF3B82F6) else Color.Transparent)
                            .clickable { activeTab = index }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (selected) Color.White else Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // TAB 0: Network Health & Status
            if (activeTab == 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricMiniCard(
                        icon = Icons.Default.Speed,
                        label = "Ping Latency",
                        value = "$latencyMs ms",
                        subtitle = "Fast Response",
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        icon = Icons.Default.NetworkCheck,
                        label = "Signal Strength",
                        value = "$signalDbm dBm",
                        subtitle = "$signalPercent% Excellent",
                        color = Color(0xFF00E5FF),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Network Health Metrics Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.85f)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Network Diagnostics Summary",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        HealthDetailRow("Connection Quality", connectionQuality, Color(0xFF10B981))
                        Spacer(modifier = Modifier.height(10.dp))
                        HealthDetailRow("DNS Server Status", "Reachable (Active)", Color(0xFF10B981))
                        Spacer(modifier = Modifier.height(10.dp))
                        HealthDetailRow("Gateway Response", "0.8 ms", Color(0xFF00E5FF))
                        Spacer(modifier = Modifier.height(10.dp))
                        HealthDetailRow("Packet Loss", "0.00%", Color(0xFF10B981))
                    }
                }
            }

            // TAB 1: Traffic & Per-App Usage
            if (activeTab == 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricMiniCard(
                        icon = Icons.Default.ArrowDownward,
                        label = "Download",
                        value = downloadSpeed,
                        subtitle = "Total: ${totalDownloadGB} GB",
                        color = Color(0xFF00E5FF),
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        icon = Icons.Default.ArrowUpward,
                        label = "Upload",
                        value = uploadSpeed,
                        subtitle = "Total: ${totalUploadGB} GB",
                        color = Color(0xFFA855F7),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Live Traffic Waveform
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.85f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Real-time Traffic Activity",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                        ) {
                            val path = Path().apply {
                                moveTo(0f, size.height * 0.6f)
                                cubicTo(
                                    size.width * 0.25f, size.height * 0.1f,
                                    size.width * 0.5f, size.height * 0.9f,
                                    size.width * 0.75f, size.height * 0.2f
                                )
                                cubicTo(
                                    size.width * 0.85f, size.height * 0.7f,
                                    size.width * 0.95f, size.height * 0.3f,
                                    size.width, size.height * 0.5f
                                )
                            }
                            drawPath(
                                path = path,
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFF00E5FF), Color(0xFFA855F7))
                                ),
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Per-App Usage List
                Text(
                    text = "Per-App Data Usage",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                perAppUsageList.forEach { app ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.75f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(app.iconColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lan,
                                        contentDescription = null,
                                        tint = app.iconColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = app.appName,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Down: ${app.downloadMB.toInt()}MB • Up: ${app.uploadMB.toInt()}MB",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Text(
                                text = "${(app.downloadMB + app.uploadMB).toInt()} MB",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // TAB 2: Connected Devices
            if (activeTab == 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Connected Network Devices (${connectedDevicesList.size})",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Subnet: 192.168.1.0/24",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                connectedDevicesList.forEach { dev ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (dev.isGateway) Color(0xFF3B82F6) else Color(0xFF1E293B),
                                RoundedCornerShape(14.dp)
                            ),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.85f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (dev.isGateway) Color(0xFF3B82F6).copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (dev.isGateway) Icons.Default.Router else Icons.Default.Devices,
                                        contentDescription = null,
                                        tint = if (dev.isGateway) Color(0xFF3B82F6) else Color(0xFF10B981),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = dev.name,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "IP: ${dev.ip}  •  MAC: ${dev.mac}",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (dev.isGateway) Color(0xFF3B82F6).copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (dev.isGateway) "Gateway" else "Active",
                                    color = if (dev.isGateway) Color(0xFF3B82F6) else Color(0xFF10B981),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // TAB 3: Security, DNS & Events
            if (activeTab == 3) {
                // Wi-Fi Security & Risk Score
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.85f)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Wi-Fi Security Analysis",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Low Risk ($riskScore%)",
                                    color = Color(0xFF10B981),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        HealthDetailRow("Encryption Standard", securityType, Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        HealthDetailRow("Primary DNS Server", primaryDns, Color(0xFF00E5FF))
                        Spacer(modifier = Modifier.height(8.dp))
                        HealthDetailRow("Secondary DNS Server", secondaryDns, Color(0xFF00E5FF))
                        Spacer(modifier = Modifier.height(8.dp))
                        HealthDetailRow("DNS over HTTPS (DoH)", "Active / Secure", Color(0xFF10B981))
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Network Security Recommendations
                Text(
                    text = "Security Recommendations",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                RecommendationCard(
                    title = "Private DNS Encryption Enabled",
                    desc = "Your DNS queries are encrypted, preventing ISP tracking.",
                    isGood = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                RecommendationCard(
                    title = "WPA3 Hybrid Encryption Active",
                    desc = "Strong password protection prevents dictionary attacks.",
                    isGood = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Network Event Log
                Text(
                    text = "Recent Network Events",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                eventLogsList.forEach { evt ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.75f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (evt.type) {
                                            EventType.SUCCESS -> Color(0xFF10B981)
                                            EventType.WARNING -> Color(0xFFF59E0B)
                                            EventType.INFO -> Color(0xFF3B82F6)
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = evt.event, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(text = evt.timestamp, color = Color(0xFF94A3B8), fontSize = 10.sp)
                                }
                                Text(text = evt.detail, color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MetricMiniCard(
    icon: ImageVector,
    label: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.85f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label, color = Color(0xFF94A3B8), fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            Text(text = subtitle, color = color, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun HealthDetailRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color(0xFF94A3B8), fontSize = 13.sp)
        Text(text = value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RecommendationCard(title: String, desc: String, isGood: Boolean) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.75f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isGood) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isGood) Color(0xFF10B981) else Color(0xFFF59E0B),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = desc, color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
        }
    }
}
