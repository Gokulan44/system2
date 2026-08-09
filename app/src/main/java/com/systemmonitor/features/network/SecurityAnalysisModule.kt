package com.systemmonitor.features.network

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
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
fun SecurityAnalysisCard(
    riskScorePercent: Int,
    suspiciousAlertsCount: Int,
    recommendations: List<String>
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
                        modifier = Modifier.size(38.dp).clip(CircleShape).background(Color(0xFF10B981).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Network Security Analysis", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("Suspicious Connection Alerts: $suspiciousAlertsCount", color = if (suspiciousAlertsCount == 0) Color(0xFF10B981) else Color(0xFFEF4444), fontSize = 12.sp)
                    }
                }
                val riskColor = when {
                    riskScorePercent > 50 -> Color(0xFFEF4444)
                    riskScorePercent > 20 -> Color(0xFFF59E0B)
                    else -> Color(0xFF10B981)
                }
                Text("Risk: $riskScorePercent%", color = riskColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("Recommendations:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            if (recommendations.isEmpty()) {
                Text("• Network security parameters are fully optimized!", color = Color(0xFF10B981), fontSize = 11.sp)
            } else {
                recommendations.forEach { rec ->
                    Text("• $rec", color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
            }
        }
    }
}
