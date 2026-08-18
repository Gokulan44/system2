package com.systemmonitor.features.network

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WifiScreen(
    state: NetworkState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricMiniCard(
                icon = Icons.Default.Wifi,
                label = "Signal Strength",
                value = if (state.isConnected && state.ssid != null) "${state.signalPercent}%" else "Disconnected",
                subtitle = "${state.signalDbm} dBm",
                color = Color(0xFF00E5FF),
                modifier = Modifier.weight(1f)
            )
            MetricMiniCard(
                icon = Icons.Default.Speed,
                label = "Link Speed",
                value = if (state.isConnected && state.ssid != null) "${state.linkSpeedMbps} Mbps" else "N/A",
                subtitle = state.bandName,
                color = Color(0xFF8B5CF6),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Signal Quality Diagnostics Card
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
                    Text(
                        text = "Live Signal Stability Analysis",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        color = when {
                            state.stabilityScore >= 85 -> Color(0xFF10B981).copy(alpha = 0.2f)
                            state.stabilityScore >= 65 -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                            else -> Color(0xFFEF4444).copy(alpha = 0.2f)
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${state.stabilityScore}% • ${state.stabilityRating}",
                            color = when {
                                state.stabilityScore >= 85 -> Color(0xFF10B981)
                                state.stabilityScore >= 65 -> Color(0xFFF59E0B)
                                else -> Color(0xFFEF4444)
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                HealthDetailRow("Network Name (SSID)", state.ssid ?: "Not Connected", Color.White)
                Spacer(modifier = Modifier.height(10.dp))
                HealthDetailRow("Frequency Band", "${state.bandName} (${state.frequencyMhz} MHz)", Color(0xFF00E5FF))
                Spacer(modifier = Modifier.height(10.dp))
                HealthDetailRow("Real Ping Latency", "${state.latencyMs} ms", Color(0xFF10B981))
                Spacer(modifier = Modifier.height(10.dp))
                HealthDetailRow("Latency Jitter (Std Dev)", "${state.jitterMs} ms", Color(0xFFEC4899))
                Spacer(modifier = Modifier.height(10.dp))
                HealthDetailRow("Signal Level (RSSI)", "${state.signalPercent}% (${state.signalDbm} dBm)", Color(0xFF8B5CF6))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Traffic & Signal Stability Chart
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
                    Text(
                        text = "Signal Stability Waveform (Live)",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Jitter: ${state.jitterMs} ms",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val history = state.signalHistory
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                ) {
                    if (history.isNotEmpty()) {
                        val maxPoints = history.size
                        val stepX = size.width / (maxPoints - 1).coerceAtLeast(1)
                        val path = Path()
                        
                        history.forEachIndexed { i, value ->
                            val pct = value.coerceIn(0, 100) / 100f
                            val x = i * stepX
                            val y = size.height - (size.height * 0.8f * pct + size.height * 0.1f)
                            
                            if (i == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }
                        
                        drawPath(
                            path = path,
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF00E5FF), Color(0xFF8B5CF6))
                            ),
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}
