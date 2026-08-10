package com.systemmonitor.features.settings

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 1. Screen & Display Settings
@Composable
fun ScreenSettingsScreen(viewModel: SettingsViewModel, onBackClick: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val screen = state.settings.screen
    CategoryBaseScreen("Screen & Display Settings", onBackClick) {
        SimpleToggleTile("Dark Theme (Amoled)", "Optimized dark theme for OLED screens", screen.darkModeEnabled) {
            viewModel.onEvent(SettingsEvent.UpdateSettings(state.settings.copy(screen = screen.copy(darkModeEnabled = it))))
        }
        Spacer(modifier = Modifier.height(10.dp))
        SimpleToggleTile("Auto Brightness Adjust", "Dynamically adjust screen brightness", screen.autoBrightnessEnabled) {
            viewModel.onEvent(SettingsEvent.UpdateSettings(state.settings.copy(screen = screen.copy(autoBrightnessEnabled = it))))
        }
    }
}

// 2. Device Settings
@Composable
fun DeviceSettingsScreen(onBackClick: () -> Unit) {
    CategoryBaseScreen("Device Settings", onBackClick) {
        SimpleInfoTile("Device Name", "Android System Workstation")
        Spacer(modifier = Modifier.height(10.dp))
        SimpleInfoTile("OS Build", "Android 14 (API 34)")
        Spacer(modifier = Modifier.height(10.dp))
        SimpleInfoTile("Hardware Model", "Google Pixel Telemetry Node")
    }
}

// 3. Remote Control Settings
@Composable
fun RemoteSettingsScreen(viewModel: SettingsViewModel, onBackClick: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val remote = state.settings.remote
    CategoryBaseScreen("Remote Control Settings", onBackClick) {
        SimpleToggleTile("Enable Remote Control", "Allow paired Windows PC to trigger power actions", remote.remoteControlEnabled) {
            viewModel.onEvent(SettingsEvent.UpdateSettings(state.settings.copy(remote = remote.copy(remoteControlEnabled = it))))
        }
        Spacer(modifier = Modifier.height(10.dp))
        SimpleInfoTile("Paired Windows Agent", if (remote.laptopConnected) remote.laptopName else "No device paired")
        Spacer(modifier = Modifier.height(10.dp))
        SimpleInfoTile("Pairing Security Code", if (remote.pairingCode.isNotEmpty()) remote.pairingCode else "No active code")
    }
}

// 4. Privacy & Permissions Settings
@Composable
fun PrivacySettingsScreen(
    viewModel: SettingsViewModel,
    onPermissionManagerClick: () -> Unit,
    onDataCollectionClick: () -> Unit,
    onUsageAccessClick: () -> Unit,
    onAccessibilitySettingsClick: () -> Unit,
    onBackClick: () -> Unit
) {
    CategoryBaseScreen("Privacy & Permissions Settings", onBackClick) {
        SimpleNavigationTile(
            title = "Permission Manager",
            desc = "Configure Camera, Microphone, and Location permissions",
            onClick = onPermissionManagerClick
        )
        Spacer(modifier = Modifier.height(10.dp))
        SimpleNavigationTile(
            title = "Data Collection & Consent",
            desc = "Manage crash reporting & telemetry tracking settings",
            onClick = onDataCollectionClick
        )
        Spacer(modifier = Modifier.height(10.dp))
        SimpleNavigationTile(
            title = "Usage Access Permission",
            desc = "Grant access to device application usage telemetry",
            onClick = onUsageAccessClick
        )
        Spacer(modifier = Modifier.height(10.dp))
        SimpleNavigationTile(
            title = "Accessibility Settings",
            desc = "Grant accessibility service access for AppLock protection",
            onClick = onAccessibilitySettingsClick
        )
    }
}

@Composable
fun SimpleNavigationTile(title: String, desc: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.85f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(desc, color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF64748B)
            )
        }
    }
}

// 5. Reports & Export Settings
@Composable
fun ReportSettingsScreen(onBackClick: () -> Unit) {
    CategoryBaseScreen("Reports & Export Settings", onBackClick) {
        SimpleInfoTile("Automatic Report Schedule", "Weekly PDF Summary (Every Monday 09:00)")
        Spacer(modifier = Modifier.height(10.dp))
        SimpleInfoTile("Default Export Format", "PDF / CSV Data Log")
    }
}

// 6. Backup & Sync Settings
@Composable
fun BackupSettingsScreen(onBackClick: () -> Unit) {
    var autoBackup by remember { mutableStateOf(true) }
    CategoryBaseScreen("Backup & Sync Settings", onBackClick) {
        SimpleToggleTile("Automatic Settings Backup", "Backup AppLock configuration daily", autoBackup) { autoBackup = it }
        Spacer(modifier = Modifier.height(10.dp))
        SimpleInfoTile("Last Local Backup", "Today at 04:00 AM")
    }
}

// 7. Advanced & About Settings
@Composable
fun AdvancedSettingsScreen(onBackClick: () -> Unit) {
    var debugMode by remember { mutableStateOf(false) }
    CategoryBaseScreen("Advanced System Settings", onBackClick) {
        SimpleToggleTile("Developer Debug Mode", "Verbose logcat output for telemetry service", debugMode) { debugMode = it }
        Spacer(modifier = Modifier.height(10.dp))
        SimpleInfoTile("Database Vacuum & Clean", "Clear cached system monitoring data")
    }
}

@Composable
fun AboutScreen(onBackClick: () -> Unit) {
    CategoryBaseScreen("About System Monitor", onBackClick) {
        SimpleInfoTile("App Version", "v1.0.0 (Production Release)")
        Spacer(modifier = Modifier.height(10.dp))
        SimpleInfoTile("Build Number", "Build 2026.08.09-PROD")
        Spacer(modifier = Modifier.height(10.dp))
        SimpleInfoTile("Developer", "Google DeepMind Advanced Agentic Coding")
        Spacer(modifier = Modifier.height(10.dp))
        SimpleInfoTile("Open Source Licenses", "Apache 2.0 / MIT")
    }
}

@Composable
private fun CategoryBaseScreen(
    title: String,
    onBackClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val bgGradient = Brush.verticalGradient(colors = listOf(Color(0xFF080C16), Color(0xFF0B132B), Color(0xFF070B18)))
    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
private fun SimpleToggleTile(title: String, desc: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.85f)
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(desc, color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF00E5FF)))
        }
    }
}

@Composable
private fun SimpleInfoTile(title: String, desc: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.85f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = Color(0xFF94A3B8), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(desc, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}
