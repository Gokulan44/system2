package com.systemmonitor.securityscan.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemmonitor.securityscan.presentation.components.ScanProgressCard

@Composable
fun ScanProgressScreen(
    progress: Int,
    currentStepText: String,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val bgGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF080C16), Color(0xFF0F172A), Color(0xFF05070F))
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgGradient)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Security Scan In Progress",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Analyzing file package signatures & byte code",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Custom Radar Sweep Canvas
            Box(
                modifier = Modifier.size(220.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.minDimension / 2

                    // Concentric background circles
                    drawCircle(Color(0xFF00FFCC).copy(alpha = 0.05f * pulseScale), radius * 0.9f)
                    drawCircle(Color(0xFF1E293B), radius, style = Stroke(width = 2.dp.toPx()))
                    drawCircle(Color(0xFF1E293B), radius * 0.66f, style = Stroke(width = 1.5.dp.toPx()))
                    drawCircle(Color(0xFF1E293B), radius * 0.33f, style = Stroke(width = 1.dp.toPx()))

                    // Rotating sweep radar line
                    rotate(rotation, pivot = center) {
                        val sweepBrush = Brush.sweepGradient(
                            colors = listOf(
                                Color(0xFF00FFCC).copy(alpha = 0.5f),
                                Color.Transparent
                            ),
                            center = center
                        )
                        drawCircle(brush = sweepBrush, radius = radius)
                        drawLine(
                            color = Color(0xFF00FFCC),
                            start = center,
                            end = Offset(center.x, center.y - radius),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            ScanProgressCard(
                progress = progress,
                currentStepText = currentStepText
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = onCancel,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(Color.Transparent)
            ) {
                Text("Cancel Scan", fontWeight = FontWeight.Bold)
            }
        }
    }
}
