package com.systemmonitor.securityscan.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PermissionRiskCard(
    permissions: List<String>,
    modifier: Modifier = Modifier
) {
    val highRiskPermissions = listOf(
        "android.permission.READ_SMS",
        "android.permission.SEND_SMS",
        "android.permission.RECEIVE_SMS",
        "android.permission.RECORD_AUDIO",
        "android.permission.CAMERA",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.SYSTEM_ALERT_WINDOW",
        "android.permission.READ_CALL_LOG"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.8f))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Requested Permissions (${permissions.size})",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        if (permissions.isEmpty()) {
            Text(
                text = "No permissions requested by this app.",
                color = Color(0xFF64748B),
                fontSize = 12.sp
            )
        } else {
            // Render list of items
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                permissions.take(10).forEach { perm ->
                    val isDangerous = highRiskPermissions.any { perm.endsWith(it.substringAfterLast(".")) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDangerous) Color(0xFFEF4444).copy(alpha = 0.1f) else Color(0xFF1E293B).copy(alpha = 0.4f))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = perm.substringAfterLast("."),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (isDangerous) "Dangerous" else "Normal",
                            color = if (isDangerous) Color(0xFFEF4444) else Color(0xFF10B981),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (permissions.size > 10) {
                    Text(
                        text = "+ ${permissions.size - 10} more permissions...",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
