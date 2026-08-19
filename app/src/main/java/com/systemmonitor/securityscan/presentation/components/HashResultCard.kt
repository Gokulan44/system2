package com.systemmonitor.securityscan.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemmonitor.securityscan.hash.HashResult

@Composable
fun HashResultCard(
    sha256: String,
    hashResult: HashResult,
    modifier: Modifier = Modifier
) {
    val (statusLabel, statusColor, statusDesc) = when (hashResult) {
        is HashResult.Clean -> Triple(
            "CLEAN SIGNATURE", 
            Color(0xFF10B981), 
            "Matched database record: Trusted Clean app (${hashResult.appName})."
        )
        is HashResult.Malware -> Triple(
            "KNOWN MALWARE MATCH", 
            Color(0xFFEF4444), 
            "Matches verified malware: ${hashResult.threatName}. Quarantine immediately."
        )
        is HashResult.Unknown -> Triple(
            "UNKNOWN SIGNATURE", 
            Color(0xFFF59E0B), 
            "Signature not in local lookup registry. Scanning local file structures..."
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.8f))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Signature Authentication",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "SHA-256",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
            )
            Text(
                text = sha256.take(8) + "..." + sha256.takeLast(8),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(statusColor.copy(alpha = 0.1f))
                .padding(10.dp)
        ) {
            Column {
                Text(
                    text = statusLabel,
                    color = statusColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = statusDesc,
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
        }
    }
}
