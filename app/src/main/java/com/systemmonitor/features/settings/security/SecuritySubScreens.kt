package com.systemmonitor.features.settings.security

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
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
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
import com.systemmonitor.applock.model.LockMethod
import com.systemmonitor.applock.model.LockTiming
import com.systemmonitor.applock.settings.AppLockSettingsViewModel

@Composable
fun AppLockSettingsScreen(
    viewModel: AppLockSettingsViewModel,
    onNavigateToChooseMethod: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val settings by viewModel.settings.collectAsState()
    val bgGradient = Brush.verticalGradient(colors = listOf(Color(0xFF080C16), Color(0xFF0B132B), Color(0xFF070B18)))

    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("App Lock Settings", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Security Configuration", color = Color(0xFF94A3B8), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            // 1. Lock Method Selection
            SettingsNavigationRow(
                title = "Lock Method",
                subtitle = "Current: ${settings.lockMethod.name}",
                icon = Icons.Default.Security,
                onClick = onNavigateToChooseMethod
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Lock Timing Selection
            SettingsDropdownRow(
                title = "Lock Timing",
                subtitle = when (settings.lockTiming) {
                    LockTiming.IMMEDIATELY -> "Lock Immediately"
                    LockTiming.AFTER_30_SECONDS -> "Lock after 30 seconds"
                    LockTiming.AFTER_1_MINUTE -> "Lock after 1 minute"
                    LockTiming.AFTER_SCREEN_OFF -> "Lock after screen off"
                },
                icon = Icons.Default.HourglassEmpty,
                onSelected = { method ->
                    viewModel.updateSettings(settings.copy(lockTiming = method))
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Biometric Toggle
            SettingsToggleRow(
                title = "Biometric Lock",
                subtitle = "Unlock apps using fingerprint or face recognition",
                icon = Icons.Default.Fingerprint,
                checked = settings.biometricEnabled,
                onCheckedChange = { checked ->
                    viewModel.updateSettings(settings.copy(biometricEnabled = checked))
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Lock on Screen Off
            SettingsToggleRow(
                title = "Lock on Screen Off",
                subtitle = "Force lock when the device screen turns off",
                icon = Icons.Default.Lock,
                checked = settings.lockOnScreenOff,
                onCheckedChange = { checked ->
                    viewModel.updateSettings(settings.copy(lockOnScreenOff = checked))
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("General Settings", color = Color(0xFF94A3B8), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            // 5. Start Service on Boot
            SettingsToggleRow(
                title = "Start Service After Reboot",
                subtitle = "Automatically run protection service on device startup",
                icon = Icons.Default.PowerSettingsNew,
                checked = settings.startAfterReboot,
                onCheckedChange = { checked ->
                    viewModel.updateSettings(settings.copy(startAfterReboot = checked))
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 6. Notifications Toggle
            SettingsToggleRow(
                title = "Show Alerts & Notifications",
                subtitle = "Display status alerts on intrusion attempts",
                icon = Icons.Default.Notifications,
                checked = settings.notificationsEnabled,
                onCheckedChange = { checked ->
                    viewModel.updateSettings(settings.copy(notificationsEnabled = checked))
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun BiometricSettingsScreen(onBackClick: () -> Unit = {}) {
    SecurityBaseScreen("Biometric Settings", onBackClick)
}

@Composable
fun VirusScanSettingsScreen(onBackClick: () -> Unit = {}) {
    SecurityBaseScreen("Virus Scan Settings", onBackClick)
}

@Composable
fun PrivacyCheckScreen(onBackClick: () -> Unit = {}) {
    SecurityBaseScreen("Privacy Check Settings", onBackClick)
}

@Composable
fun SecurityAlertSettingsScreen(onBackClick: () -> Unit = {}) {
    SecurityBaseScreen("Security Alert Settings", onBackClick)
}

@Composable
private fun SecurityBaseScreen(title: String, onBackClick: () -> Unit) {
    val bgGradient = Brush.verticalGradient(colors = listOf(Color(0xFF080C16), Color(0xFF0B132B), Color(0xFF070B18)))
    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
                Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SettingsNavigationRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.85f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(subtitle, color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF64748B))
        }
    }
}

@Composable
private fun SettingsDropdownRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onSelected: (LockTiming) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
            .clickable { expanded = true },
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.85f)
    ) {
        Box {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(subtitle, color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF64748B))
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF0F172A))
            ) {
                DropdownMenuItem(
                    text = { Text("Lock Immediately", color = Color.White) },
                    onClick = {
                        onSelected(LockTiming.IMMEDIATELY)
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Lock after 30 seconds", color = Color.White) },
                    onClick = {
                        onSelected(LockTiming.AFTER_30_SECONDS)
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Lock after 1 minute", color = Color.White) },
                    onClick = {
                        onSelected(LockTiming.AFTER_1_MINUTE)
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Lock after screen off", color = Color.White) },
                    onClick = {
                        onSelected(LockTiming.AFTER_SCREEN_OFF)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.85f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(icon, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(subtitle, color = Color(0xFF94A3B8), fontSize = 11.sp, lineHeight = 14.sp)
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF3B82F6),
                    uncheckedThumbColor = Color(0xFF94A3B8),
                    uncheckedTrackColor = Color(0xFF1E293B)
                )
            )
        }
    }
}
