package com.systemmonitor.features.network

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VpnScreen(
    state: NetworkState,
    viewModel: NetworkViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        // 1. Simulation Controls Card (Developer Mode)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.4f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Simulate VPN Tunnel Connection",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Audit network and location encryption behavior locally.",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = state.isVpnSimulationActive,
                    onCheckedChange = { viewModel.toggleVpnSimulation(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00E5FF),
                        checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.5f),
                        uncheckedThumbColor = Color(0xFF64748B),
                        uncheckedTrackColor = Color(0xFF334155)
                    )
                )
            }
        }

        // 2. VPN Status Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (state.isVpnActive) Color(0xFF10B981) else Color(0xFFEF4444).copy(alpha = 0.5f),
                    RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.85f)
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (state.isVpnActive) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (state.isVpnActive) Icons.Default.Lock else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (state.isVpnActive) Color(0xFF10B981) else Color(0xFFEF4444),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = if (state.isVpnActive) "VPN Status: Protected" else "VPN Status: Unprotected",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (state.isVpnActive) "Active tunnel encrypting network packets." else "Your IP and location details are public.",
                        color = if (state.isVpnActive) Color(0xFF10B981) else Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // 3. Simulated/Real VPN Telemetry Panel
        if (state.isVpnActive && state.isVpnSimulationActive) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.6f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Encrypted Tunnel Details",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TunnelDetailRow("Virtual IP Address", state.simulatedVpnIp)
                    TunnelDetailRow("VPN Protocol", state.simulatedVpnProtocol)
                    TunnelDetailRow("Gateway Node", state.simulatedVpnServer)
                    TunnelDetailRow("Encryption Standard", "AES-256-GCM / Poly1305")
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Security Analysis Recommendations
        Text(
            text = "VPN & Privacy Recommendations",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        RecommendationCard(
            title = "Always-on VPN Tunnel",
            desc = "Enable VPN auto-connect in system preferences to secure public Wi-Fi connections.",
            isGood = state.isVpnActive
        )
        Spacer(modifier = Modifier.height(8.dp))
        RecommendationCard(
            title = "Secure Encryption Protocols",
            desc = "WireGuard and OpenVPN protocols offer the fastest speeds with military-grade privacy.",
            isGood = true
        )
    }
}

@Composable
private fun TunnelDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color(0xFF64748B), fontSize = 12.sp)
        Text(text = value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
