package com.systemmonitor.features.network

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DnsScreen(
    state: NetworkState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        // DNS Info Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.85f)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "DNS Configurations",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                HealthDetailRow("Primary DNS", state.primaryDns, Color(0xFF00E5FF))
                Spacer(modifier = Modifier.height(10.dp))
                HealthDetailRow("Secondary DNS", state.secondaryDns, Color(0xFF00E5FF))
                Spacer(modifier = Modifier.height(10.dp))
                HealthDetailRow("DNS over HTTPS (DoH)", if (state.isDohEnabled) "Active / Encrypted" else "Inactive", if (state.isDohEnabled) Color(0xFF10B981) else Color(0xFFF59E0B))
                Spacer(modifier = Modifier.height(10.dp))
                HealthDetailRow("DNS Latency", "${state.latencyMs} ms", Color(0xFF10B981))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Security Recommendations
        Text(
            text = "DNS Security recommendations",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        RecommendationCard(
            title = "Use Encrypted DNS servers",
            desc = "Configure AdGuard or Cloudflare Private DNS in Android Settings to bypass ISP tracking.",
            isGood = state.isDohEnabled
        )
        Spacer(modifier = Modifier.height(8.dp))
        RecommendationCard(
            title = "Primary DNS verified",
            desc = "DNS requests resolved successfully within secure latency ranges.",
            isGood = true
        )
    }
}
