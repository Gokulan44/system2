package com.systemmonitor.features.security.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

@Composable
fun ScanAnimationView(
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    
    // Core radar rotation angle
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RotationAngle"
    )

    // Outer HUD ring counter-rotation angle
    val outerHUDLockAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OuterHUDLockAngle"
    )
    
    // Expanding glowing wave pulses
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseScale"
    )
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha"
    )

    // Threat detection target blips (fading/pulsing)
    val blip1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                0.1f at 0
                0.8f at 800
                0.2f at 1600
                0.1f at 3000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "Blip1Alpha"
    )

    val blip2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                0.1f at 0
                0.2f at 1000
                0.9f at 1800
                0.1f at 3000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "Blip2Alpha"
    )

    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val baseRadius = size.minDimension / 3.2f
            val maxSweepRadius = baseRadius * 1.5f

            // 1. Draw Concentric Grid Rings
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.25f),
                radius = baseRadius * 0.5f,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                radius = baseRadius,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.15f),
                radius = baseRadius * 1.5f,
                style = Stroke(width = 1.dp.toPx())
            )

            // 2. Draw Crosshair Grid lines
            drawLine(
                color = Color(0xFF00E5FF).copy(alpha = 0.15f),
                start = Offset(center.x - maxSweepRadius, center.y),
                end = Offset(center.x + maxSweepRadius, center.y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
            drawLine(
                color = Color(0xFF00E5FF).copy(alpha = 0.15f),
                start = Offset(center.x, center.y - maxSweepRadius),
                end = Offset(center.x, center.y + maxSweepRadius),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )

            if (isScanning) {
                // 3. Draw Rotating Sweep Gradient Sector (Radar Scan Effect)
                val sweepBrush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFF00E5FF).copy(alpha = 0.4f),
                        Color(0xFF00E5FF).copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = center
                )
                rotate(rotationAngle, center) {
                    drawArc(
                        brush = sweepBrush,
                        startAngle = 0f,
                        sweepAngle = -90f,
                        useCenter = true,
                        size = Size(maxSweepRadius * 2, maxSweepRadius * 2),
                        topLeft = Offset(center.x - maxSweepRadius, center.y - maxSweepRadius)
                    )
                }

                // 4. Draw Glowing Waves Expanding Outward
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = pulseAlpha),
                    radius = baseRadius * pulseScale,
                    style = Stroke(width = 2.dp.toPx())
                )

                // 5. Draw Sci-Fi Outer Segments HUD Ring (Dashed, Rotating opposite)
                rotate(outerHUDLockAngle, center) {
                    drawCircle(
                        color = Color(0xFF00E5FF).copy(alpha = 0.35f),
                        radius = maxSweepRadius + 12.dp.toPx(),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 15f), 0f)
                        )
                    )
                }

                // 6. Draw Threat Blips (Faintly detected active items)
                // Blip 1: Top Right Sector
                val blip1Pos = Offset(center.x + baseRadius * 0.8f, center.y - baseRadius * 0.7f)
                drawCircle(
                    color = Color(0xFFEF4444).copy(alpha = blip1Alpha),
                    radius = 4.dp.toPx(),
                    center = blip1Pos
                )
                drawCircle(
                    color = Color(0xFFEF4444).copy(alpha = blip1Alpha * 0.3f),
                    radius = 10.dp.toPx(),
                    center = blip1Pos
                )

                // Blip 2: Bottom Left Sector
                val blip2Pos = Offset(center.x - baseRadius * 0.9f, center.y + baseRadius * 0.5f)
                drawCircle(
                    color = Color(0xFFF59E0B).copy(alpha = blip2Alpha),
                    radius = 4.dp.toPx(),
                    center = blip2Pos
                )
                drawCircle(
                    color = Color(0xFFF59E0B).copy(alpha = blip2Alpha * 0.3f),
                    radius = 10.dp.toPx(),
                    center = blip2Pos
                )
            }
        }

        // 7. Center Glowing Shield Emblem
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = if (isScanning) Color(0xFF00E5FF) else Color(0xFF10B981),
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
