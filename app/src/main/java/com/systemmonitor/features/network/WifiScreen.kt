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
                subtitle = "${state.frequencyMhz} MHz",
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
                Text(
                    text = "Wi-Fi Signal Analysis",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                HealthDetailRow("SSID", state.ssid ?: "Not Connected", Color.White)
                Spacer(modifier = Modifier.height(10.dp))
                HealthDetailRow("Frequency band", if (state.frequencyMhz > 4900) "5 GHz" else "2.4 GHz", Color(0xFF00E5FF))
                Spacer(modifier = Modifier.height(10.dp))
                HealthDetailRow("Signal Rating", if (state.signalPercent >= 80) "Excellent" else if (state.signalPercent >= 50) "Good" else "Weak", Color(0xFF10B981))
                Spacer(modifier = Modifier.height(10.dp))
                HealthDetailRow("Frequency", "${state.frequencyMhz} MHz", Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Traffic chart
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.85f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Signal Stability (Live)",
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
                        moveTo(0f, size.height * 0.4f)
                        cubicTo(
                            size.width * 0.25f, size.height * 0.2f,
                            size.width * 0.5f, size.height * 0.7f,
                            size.width * 0.75f, size.height * 0.3f
                        )
                        cubicTo(
                            size.width * 0.85f, size.height * 0.5f,
                            size.width * 0.95f, size.height * 0.2f,
                            size.width, size.height * 0.4f
                        )
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
