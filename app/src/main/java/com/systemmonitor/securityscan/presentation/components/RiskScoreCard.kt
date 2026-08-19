package com.systemmonitor.securityscan.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
fun RiskScoreCard(
    score: Int,
    verdict: String,
    modifier: Modifier = Modifier
) {
    val color = when {
        score >= 80 -> Color(0xFF10B981) // Green
        score >= 45 -> Color(0xFFF59E0B) // Amber
        else -> Color(0xFFEF4444) // Red
    }

    val ratingText = when {
        score >= 80 -> "SAFE"
        score >= 45 -> "WARNING"
        else -> "DANGER"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.8f))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(24.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(140.dp)
        ) {
            CircularProgressIndicator(
                progress = { score.toFloat() / 100f },
                modifier = Modifier.fillMaxSize(),
                color = color,
                strokeWidth = 10.dp,
                trackColor = Color(0xFF1E293B)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$score%",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = ratingText,
                    color = color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "App Verdict: $verdict",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = when {
                score >= 80 -> "No critical structural issues detected. Safe to keep."
                score >= 45 -> "Suspicious elements or configurations found. Audit details."
                else -> "Malicious indicators detected. Quarantine recommended immediately."
            },
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
