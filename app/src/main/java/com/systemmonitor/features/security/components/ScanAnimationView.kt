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

/**
 * Represents what the scan engine is actually doing right now, so the radar
 * animation can plot blips tied to real work instead of decorative decoys.
 *
 * Wire this from whatever emits per-item scan progress (e.g. a
 * StateFlow<ScanProgress> updated inside your scan loop / worker) — see
 * usage note at the bottom of this file for what that loop needs to expose.
 */
data class ScanProgress(
    val currentItemId: String? = null,
    val recentlyScannedIds: List<String> = emptyList(),
    val itemsScanned: Int = 0,
    val totalItems: Int = 0
)

@Composable
fun ScanAnimationView(
    isScanning: Boolean,
    threats: List<com.systemmonitor.features.security.domain.model.ThreatInfo>,
    scanProgress: ScanProgress? = null,
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

    // Fade curve reused for both real-progress blips and real-threat blips
    val blipAlpha by infiniteTransition.animateFloat(
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
        label = "BlipAlpha"
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

                // 6a. Draw REAL in-progress blips: the actual items the scan
                // loop is currently touching. No decoys — if no progress data
                // was supplied, nothing is drawn here (honest empty state).
                scanProgress?.recentlyScannedIds?.forEach { id ->
                    val pos = blipPositionForId(id, center, baseRadius)
                    drawCircle(
                        color = Color(0xFF10B981).copy(alpha = blipAlpha * 0.6f),
                        radius = 4.dp.toPx(),
                        center = pos
                    )
                    drawCircle(
                        color = Color(0xFF10B981).copy(alpha = blipAlpha * 0.18f),
                        radius = 10.dp.toPx(),
                        center = pos
                    )
                }

                // 6b. Draw REAL threat blips detected so far, in bright red.
                threats.forEach { threat ->
                    val id = threat.packageName ?: threat.id
                    val pos = blipPositionForId(id, center, baseRadius)

                    drawCircle(
                        color = Color(0xFFEF4444).copy(alpha = blipAlpha),
                        radius = 5.dp.toPx(),
                        center = pos
                    )
                    drawCircle(
                        color = Color(0xFFEF4444).copy(alpha = blipAlpha * 0.3f),
                        radius = 12.dp.toPx(),
                        center = pos
                    )
                }
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

/**
 * Deterministically maps a real item id (file hash, package name, etc.) to a
 * position on the radar. Same hashing approach as the original threat-blip
 * placement, factored out so both progress blips and threat blips place
 * consistently and a given id always lands in the same spot while it's
 * visible (no jitter/flicker between recompositions).
 */
private fun blipPositionForId(id: String, center: Offset, baseRadius: Float): Offset {
    val hash = id.hashCode()
    val angleRad = Math.toRadians((hash % 360).toDouble().let { if (it < 0) it + 360 else it })
    val distanceFactor = 0.4f + (Math.abs(hash % 5) / 10f)
    val blipRadius = baseRadius * distanceFactor
    val x = center.x + (blipRadius * Math.cos(angleRad)).toFloat()
    val y = center.y + (blipRadius * Math.sin(angleRad)).toFloat()
    return Offset(x, y)
}

/*
 * USAGE NOTE — wiring real progress in:
 *
 * Your scan loop (wherever it iterates files/apps — VaultSecurityHandlers,
 * a WorkManager worker, etc.) needs to publish a ScanProgress as it works,
 * e.g.:
 *
 *   private val _scanProgress = MutableStateFlow(ScanProgress())
 *   val scanProgress: StateFlow<ScanProgress> = _scanProgress.asStateFlow()
 *
 *   suspend fun runScan(items: List<ScannableItem>) {
 *       val recent = ArrayDeque<String>(maxSize = 6)
 *       items.forEachIndexed { index, item ->
 *           recent.addLast(item.id)
 *           if (recent.size > 6) recent.removeFirst()
 *           _scanProgress.value = ScanProgress(
 *               currentItemId = item.id,
 *               recentlyScannedIds = recent.toList(),
 *               itemsScanned = index + 1,
 *               totalItems = items.size
 *           )
 *           // ... actual scan work on item ...
 *       }
 *   }
 *
 * Then in the screen:
 *   val progress by viewModel.scanProgress.collectAsState()
 *   ScanAnimationView(isScanning = ..., threats = ..., scanProgress = progress)
 *
 * If your scan engine doesn't currently expose per-item progress at all,
 * that's the piece to add first — share the scan loop file and I'll wire
 * this in directly.
 */