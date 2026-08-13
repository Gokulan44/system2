package com.systemmonitor.features.wellbeing

import android.app.AppOpsManager
import android.app.NotificationManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemmonitor.features.dashboard.CircularGauge
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar
import javax.inject.Inject

data class WellbeingUiState(
    val hasUsagePermission: Boolean = false,
    val screenTimeMinutes: Int = 0,
    val screenTimeFormatted: String = "0h 0m",
    val focusModeActive: Boolean = false,
    val bedtimeActive: Boolean = false,
    val dailyLimitMinutes: Int = 180, // Default 3 hours
    val wellbeingScore: Int = 100,
    val showTipsDialog: Boolean = false,
    val showLimitDialog: Boolean = false
)

@HiltViewModel
class DigitalWellbeingViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(WellbeingUiState())
    val uiState: StateFlow<WellbeingUiState> = _uiState.asStateFlow()

    private val sharedPrefs = context.getSharedPreferences("wellbeing_settings", Context.MODE_PRIVATE)

    init {
        val savedLimit = sharedPrefs.getInt("daily_limit_minutes", 180)
        _uiState.update { it.copy(dailyLimitMinutes = savedLimit) }
        refreshWellbeingData()
    }

    fun refreshWellbeingData() {
        val hasPermission = checkUsageStatsPermission()
        val screenTimeMin = if (hasPermission) getTodayScreenTimeMinutes() else 0
        
        // Check Focus Mode (DND)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val filter = nm.currentInterruptionFilter
        val isFocusMode = filter != NotificationManager.INTERRUPTION_FILTER_ALL

        // Check Bedtime (Current hour is between 10 PM and 7 AM)
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val isBedtime = hour >= 22 || hour < 7

        // Calculate score
        val dailyLimit = _uiState.value.dailyLimitMinutes
        var score = 100
        if (screenTimeMin > dailyLimit) {
            val excess = screenTimeMin - dailyLimit
            score -= (excess / 10).coerceAtMost(40) // Deduct up to 40 points for excess usage
        }
        if (!isFocusMode) {
            score -= 10 // Deduct 10 points if focus mode is disabled during DND check
        }
        score = score.coerceIn(40, 100)

        val hours = screenTimeMin / 60
        val mins = screenTimeMin % 60
        val formatted = "${hours}h ${mins}m"

        _uiState.update {
            it.copy(
                hasUsagePermission = hasPermission,
                screenTimeMinutes = screenTimeMin,
                screenTimeFormatted = formatted,
                focusModeActive = isFocusMode,
                bedtimeActive = isBedtime,
                wellbeingScore = score
            )
        }
    }

    fun updateDailyLimit(minutes: Int) {
        sharedPrefs.edit().putInt("daily_limit_minutes", minutes).apply()
        _uiState.update { it.copy(dailyLimitMinutes = minutes, showLimitDialog = false) }
        refreshWellbeingData()
    }

    fun toggleLimitDialog(show: Boolean) {
        _uiState.update { it.copy(showLimitDialog = show) }
    }

    fun toggleTipsDialog(show: Boolean) {
        _uiState.update { it.copy(showTipsDialog = show) }
    }

    fun requestPermissionIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    private fun checkUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.noteOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun getTodayScreenTimeMinutes(): Int {
        try {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val calendar = Calendar.getInstance()
            val endTime = calendar.timeInMillis
            
            // Start of today (12:00 AM)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startTime = calendar.timeInMillis

            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            ) ?: return 0

            val totalMillis = stats.sumOf { it.totalTimeInForeground }
            return (totalMillis / (1000 * 60)).toInt()
        } catch (e: Exception) {
            return 0
        }
    }
}

@Composable
fun DigitalWellbeingScreen(
    viewModel: DigitalWellbeingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    // Refresh telemetry on screen focus
    LaunchedEffect(Unit) {
        viewModel.refreshWellbeingData()
    }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF080C16),
            Color(0xFF0B132B),
            Color(0xFF070B18)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Digital Wellbeing",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Healthy Digital Life",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E676).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SelfImprovement,
                        contentDescription = null,
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Permission Warning Card if not granted
            if (!state.hasUsagePermission) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.08f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFEF4444))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Usage Access Required", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "To monitor screen time, app limits, and calculate your wellbeing score, please grant usage stats permission in system settings.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { context.startActivity(viewModel.requestPermissionIntent()) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Grant Permission", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Score Header
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.85f)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularGauge(
                        percentage = state.wellbeingScore,
                        label = "Wellbeing Score",
                        primaryColor = Color(0xFF00E676),
                        secondaryColor = Color(0xFF00E5FF),
                        size = 100.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4 Wellbeing Status Tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WellbeingTile(
                    icon = Icons.Default.Psychology,
                    title = "Focus Mode",
                    sub = if (state.focusModeActive) "Active" else "Off",
                    color = if (state.focusModeActive) Color(0xFF00E676) else Color(0xFF64748B),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            try {
                                context.startActivity(Intent(Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS))
                            } catch (e: Exception) {
                                // Fallback
                            }
                        }
                )
                WellbeingTile(
                    icon = Icons.Default.Schedule,
                    title = "Screen Time",
                    sub = state.screenTimeFormatted,
                    color = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WellbeingTile(
                    icon = Icons.Default.Timer,
                    title = "Limit Goal",
                    sub = "${state.dailyLimitMinutes / 60} hours",
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
                WellbeingTile(
                    icon = Icons.Default.Bedtime,
                    title = "Bedtime",
                    sub = if (state.bedtimeActive) "Active (Resting)" else "Daytime",
                    color = if (state.bedtimeActive) Color(0xFF3B82F6) else Color(0xFF64748B),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Usage Goals & Tips
            Text(
                text = "Usage Goals",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            GoalRow(
                icon = Icons.Default.Timer,
                title = "Set Daily Limit",
                onClick = { viewModel.toggleLimitDialog(true) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            GoalRow(
                icon = Icons.Default.Lightbulb,
                title = "Wellbeing Tips",
                onClick = { viewModel.toggleTipsDialog(true) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Limit Dialog Composable
        if (state.showLimitDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.toggleLimitDialog(false) },
                title = { Text("Daily Screen Time Limit") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val limits = listOf(60, 120, 180, 240, 300)
                        limits.forEach { minutes ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.updateDailyLimit(minutes) }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${minutes / 60} hour(s)",
                                    fontWeight = if (state.dailyLimitMinutes == minutes) FontWeight.Bold else FontWeight.Normal,
                                    color = if (state.dailyLimitMinutes == minutes) Color(0xFF00E5FF) else Color.Unspecified
                                )
                                if (state.dailyLimitMinutes == minutes) {
                                    Text("Selected", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.toggleLimitDialog(false) }) {
                        Text("Close")
                    }
                }
            )
        }

        // Tips Dialog Composable
        if (state.showTipsDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.toggleTipsDialog(false) },
                title = { Text("Wellbeing Tips") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("• Take a 5-minute eye break for every 30 minutes of continuous screen use.", fontSize = 14.sp)
                        Text("• Activate Focus Mode (DND) during productive sessions to block notifications.", fontSize = 14.sp)
                        Text("• Sleep schedule: Put your phone away at least 1 hour before sleeping.", fontSize = 14.sp)
                        Text("• Configure bedtime rules to automatically silence calls after 10 PM.", fontSize = 14.sp)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.toggleTipsDialog(false) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                    ) {
                        Text("Got it")
                    }
                }
            )
        }
    }
}

@Composable
private fun WellbeingTile(
    icon: ImageVector,
    title: String,
    sub: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.75f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(text = title, color = Color(0xFF94A3B8), fontSize = 11.sp)
                Text(text = sub, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun GoalRow(icon: ImageVector, title: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.75f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E676).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(18.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF64748B))
        }
    }
}
