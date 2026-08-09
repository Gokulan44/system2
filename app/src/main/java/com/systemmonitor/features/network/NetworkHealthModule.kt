package com.systemmonitor.features.network

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NetworkHealthCard(
    healthScore: Int,
    latencyMs: Int,
    dnsStatus: String,
    quality: String
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    val healthColor = when {
                        healthScore >= 80 -> Color(0xFF10B981)
                        healthScore >= 50 -> Color(0xFFF59E0B)
                        else -> Color(0xFFEF4444)
                    }
                    Column {
                        Text("Network Health", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("Overall Rating: $quality", color = healthColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                val healthColor = when {
                    healthScore >= 80 -> Color(0xFF10B981)
                    healthScore >= 50 -> Color(0xFFF59E0B)
                    else -> Color(0xFFEF4444)
                }
                Text("$healthScore%", color = healthColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                HealthMetricChip("Latency", "${latencyMs}ms", Color(0xFF00E5FF))
                HealthMetricChip("DNS Status", dnsStatus, Color(0xFF10B981))
                HealthMetricChip("Packet Loss", "0.0%", Color(0xFF8B5CF6))
            }
        }
    }
}

@Composable
private fun HealthMetricChip(label: String, value: String, accentColor: Color) {
    Box(
        modifier = Modifier
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
            .background(Color(0xFF080C16))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = Color(0xFF94A3B8), fontSize = 10.sp)
            Text(value, color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
