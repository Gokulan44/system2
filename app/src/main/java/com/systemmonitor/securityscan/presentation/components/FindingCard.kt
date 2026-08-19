package com.systemmonitor.securityscan.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemmonitor.securityscan.database.entity.FindingEntity

@Composable
fun FindingCard(
    finding: FindingEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val severityColor = when (finding.severity.uppercase()) {
        "CRITICAL" -> Color(0xFFEF4444)
        "HIGH" -> Color(0xFFF97316)
        "MEDIUM" -> Color(0xFFF59E0B)
        else -> Color(0xFF3B82F6) // LOW or fallback
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.6f))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(4.dp, 40.dp)
                .background(severityColor, RoundedCornerShape(2.dp))
        )
        
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = finding.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = finding.severity,
                    color = severityColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = finding.details,
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                maxLines = 2
            )
        }
    }
}
