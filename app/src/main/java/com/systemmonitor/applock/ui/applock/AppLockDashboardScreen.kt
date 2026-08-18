package com.systemmonitor.applock.ui.applock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
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
import com.systemmonitor.applock.manager.AppLockManager
import kotlinx.coroutines.delay
import com.systemmonitor.applock.ui.applock.components.AppIcon

@Composable
fun AppLockDashboardScreen(
    appLockManager: AppLockManager,
    onNavigateToSelectApps: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onBackClick: () -> Unit
) {
    val lockedApps by appLockManager.getLockedApps().collectAsState(initial = emptyList())
    
    var hasUsageStats by remember { mutableStateOf(appLockManager.hasUsageStatsPermission()) }
    var hasOverlay by remember { mutableStateOf(appLockManager.hasOverlayPermission()) }
    var isServiceRunning by remember { mutableStateOf(appLockManager.isServiceRunning()) }

    // Periodically poll permissions so they update immediately when returning from system settings
    LaunchedEffect(Unit) {
        while (true) {
            hasUsageStats = appLockManager.hasUsageStatsPermission()
            hasOverlay = appLockManager.hasOverlayPermission()
            isServiceRunning = appLockManager.isServiceRunning()
            delay(1500)
        }
    }

    val bgGradient = Brush.verticalGradient(colors = listOf(Color(0xFF080C16), Color(0xFF0B132B), Color(0xFF070B18)))

    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("App Lock Dashboard", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Dynamic State Header Card
            val isConfigured = hasUsageStats && hasOverlay
            val cardBorderColor = when {
                !isConfigured -> Color(0xFFEF4444).copy(alpha = 0.5f)
                lockedApps.isEmpty() -> Color(0xFF3B82F6).copy(alpha = 0.5f)
                else -> Color(0xFF10B981).copy(alpha = 0.5f)
            }
            val statusColor = when {
                !isConfigured -> Color(0xFFEF4444)
                lockedApps.isEmpty() -> Color(0xFF3B82F6)
                else -> Color(0xFF10B981)
            }
            val statusText = when {
                !isConfigured -> "Configuration Required"
                lockedApps.isEmpty() -> "Service Ready"
                else -> "App Lock Active"
            }
            val descriptionText = when {
                !isConfigured -> "System permissions are missing. Please grant them below to enable lock overlay."
                lockedApps.isEmpty() -> "No apps currently locked. Tap 'Manage Protected Apps' to secure apps."
                else -> "${lockedApps.size} Applications Protected and Monitored."
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, cardBorderColor, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.85f)
            ) {
                Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = if (!isConfigured) Icons.Default.Warning else Icons.Default.Lock
                        Icon(icon, contentDescription = null, tint = statusColor, modifier = Modifier.size(30.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(statusText, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(descriptionText, color = Color(0xFF94A3B8), fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }

            // Permissions setup section if missing
            if (!isConfigured) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Required Permissions", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                if (!hasUsageStats) {
                    Surface(
                        onClick = { appLockManager.launchUsageAccessSettings() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B).copy(alpha = 0.5f)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Usage Access Permission", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Required to detect when protected apps are opened.", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF64748B))
                        }
                    }
                }

                if (!hasOverlay) {
                    Surface(
                        onClick = { appLockManager.launchOverlaySettings() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B).copy(alpha = 0.5f)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Overlay Permission", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Required to display the PIN/Pattern lock screen overlay.", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF64748B))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Quick Actions", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                onClick = onNavigateToSelectApps,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.85f)
            ) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF3B82F6).copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Apps, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text("Manage Protected Apps", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF64748B))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.85f)
            ) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFA855F7).copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text("Lock Method & Security Settings", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF64748B))
                }
            }

            if (lockedApps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Currently Protected Apps (${lockedApps.size})", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                lockedApps.take(5).forEach { app ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.85f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AppIcon(
                                    packageName = app.packageName,
                                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(app.appName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(app.packageName, color = Color(0xFF64748B), fontSize = 10.sp)
                                }
                            }
                            Icon(Icons.Default.Lock, contentDescription = "Protected", tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                        }
                    }
                }

                if (lockedApps.size > 5) {
                    TextButton(
                        onClick = onNavigateToSelectApps,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("View all protected apps", color = Color(0xFF00E5FF), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
