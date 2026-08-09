package com.systemmonitor.features.network

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
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
fun TrafficMonitorCard(
    downloadSpeed: Float,
    uploadSpeed: Float,
    totalDownloadedMB: Float,
    totalUploadedMB: Float,
    appUsageList: List<AppNetworkUsage>
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.85f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Live Network Traffic", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                // Download Speed Card
                Surface(
                    modifier = Modifier.weight(1f).border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    color = Color(0xFF080C16), shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(34.dp).clip(CircleShape).background(Color(0xFF00E5FF).copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Download", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text(String.format("%.1f Mbps", downloadSpeed), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(String.format("Total: %.1f MB", totalDownloadedMB), color = Color(0xFF64748B), fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Upload Speed Card
                Surface(
                    modifier = Modifier.weight(1f).border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    color = Color(0xFF080C16), shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(34.dp).clip(CircleShape).background(Color(0xFF8B5CF6).copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Upload", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text(String.format("%.1f Mbps", uploadSpeed), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(String.format("Total: %.1f MB", totalUploadedMB), color = Color(0xFF64748B), fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Per-App Network Consumption", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (appUsageList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No active application network traffic detected",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                appUsageList.forEach { app ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(app.iconColor))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(app.appName, color = Color.White, fontSize = 13.sp)
                        }
                        Text(String.format("↓ %.1f MB / ↑ %.1f MB", app.downloadMB, app.uploadMB), color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
