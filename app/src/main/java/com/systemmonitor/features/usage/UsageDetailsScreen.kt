package com.systemmonitor.features.usage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UsageDetailsScreen(
    appName: String = "App",
    usageTime: String = "0m",
    packageName: String = "",               // NEW – drives real data lookup
    appUsageViewModel: AppUsageViewModel,   // NEW – provides real stats
    onNavigateToAnalytics: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by appUsageViewModel.uiState.collectAsState()

    // Trigger real breakdown load when screen opens
    LaunchedEffect(packageName) {
        if (packageName.isNotBlank()) {
            appUsageViewModel.loadWeeklyBreakdown(packageName)
        }
    }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF080C16), Color(0xFF0B132B), Color(0xFF070B18))
    )

    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Usage Details", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Selected App Header Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.85f)
            ) {
                Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(appName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(usageTime, color = Color(0xFF00E5FF), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Today", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ✅ Real 7-day Bar Chart
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.85f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("7-Day Usage Breakdown", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        if (uiState.isLoadingDetail) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF3B82F6)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val breakdown = uiState.detailStats?.weeklyBreakdown
                    if (breakdown.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(130.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (uiState.isLoadingDetail) "Loading..." else "No data for this period",
                                color = Color(0xFF64748B),
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        // Real bar chart
                        Row(
                            modifier = Modifier.fillMaxWidth().height(130.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            breakdown.forEach { day ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Duration label on top of bar
                                    if (day.durationMs > 0) {
                                        val mins = (day.durationMs / 60_000L).toInt()
                                        val hrs = mins / 60
                                        Text(
                                            text = if (hrs > 0) "${hrs}h" else "${mins}m",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 8.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.6f)
                                            .height((110 * day.ratio).dp)
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                            .background(
                                                if (day.durationMs > 0)
                                                    Brush.verticalGradient(listOf(Color(0xFFA855F7), Color(0xFF3B82F6)))
                                                else
                                                    Brush.verticalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = day.dayLabel, color = Color(0xFF94A3B8), fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ✅ Real metrics
            val detail = uiState.detailStats
            val foregroundMs = detail?.foregroundMs ?: 0L
            val sessions = detail?.sessionsEstimate ?: 0
            val fgHours = foregroundMs / 3_600_000L
            val fgMins = (foregroundMs % 3_600_000L) / 60_000L
            val fgText = when {
                foregroundMs == 0L -> "—"
                fgHours > 0 -> "${fgHours}h ${fgMins}m"
                else -> "${fgMins}m"
            }

            DetailMetricRow("App Sessions (7 days)", if (sessions > 0) "$sessions" else "—")
            Spacer(modifier = Modifier.height(10.dp))
            DetailMetricRow("Foreground Usage (7 days)", fgText)
            Spacer(modifier = Modifier.height(10.dp))
            DetailMetricRow("Today's Usage", usageTime)

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onNavigateToAnalytics,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("View Full Analytics", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun DetailMetricRow(label: String, value: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.75f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color(0xFF94A3B8), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
