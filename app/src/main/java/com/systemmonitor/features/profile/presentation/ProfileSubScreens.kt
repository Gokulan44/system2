package com.systemmonitor.features.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileNotificationSettingsScreen(onBackClick: () -> Unit = {}) {
    var securityAlerts by remember { mutableStateOf(true) }
    var scanCompleted by remember { mutableStateOf(true) }
    var deviceConnected by remember { mutableStateOf(false) }
    var batteryAlerts by remember { mutableStateOf(true) }
    var appLockAlerts by remember { mutableStateOf(true) }

    ProfileSubScreenBase("Notification Preferences", onBackClick) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            NotificationToggleItem("Security & Threat Alerts", "Get notified immediately when threats are found", securityAlerts) { securityAlerts = it }
            NotificationToggleItem("Scan Completed Alerts", "Notify when a full device scan completes", scanCompleted) { scanCompleted = it }
            NotificationToggleItem("New Device Pairings", "Get alerts when a laptop pairs with this device", deviceConnected) { deviceConnected = it }
            NotificationToggleItem("Battery & Power Alerts", "Notify on critical battery levels", batteryAlerts) { batteryAlerts = it }
            NotificationToggleItem("App Lock Notifications", "Show persistent status notifications", appLockAlerts) { appLockAlerts = it }
        }
    }
}

@Composable
fun ProfilePrivacyScreen(onBackClick: () -> Unit = {}) {
    var dataSync by remember { mutableStateOf(true) }
    var diagnostics by remember { mutableStateOf(false) }

    ProfileSubScreenBase("Privacy & Permissions Audit", onBackClick) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Data Collection & Synchronization", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            NotificationToggleItem("Cloud Data Sync", "Synchronize paired device history to secure cloud sync", dataSync) { dataSync = it }
            NotificationToggleItem("Diagnostics Sharing", "Help improve protection by sharing crash logs anonymously", diagnostics) { diagnostics = it }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFF1E293B))
            Text("Data Retention Actions", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)

            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Export Profile Data (JSON/CSV)", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Delete Account & Sync Cache", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfilePreferencesScreen(onBackClick: () -> Unit = {}) {
    var darkMode by remember { mutableStateOf(true) }
    var startOnBoot by remember { mutableStateOf(true) }
    var defaultDashboard by remember { mutableStateOf("Security Scan") }

    ProfileSubScreenBase("User Interface Preferences", onBackClick) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            NotificationToggleItem("Dynamic Dark Mode", "Enable AMOLED fluid dark styling", darkMode) { darkMode = it }
            NotificationToggleItem("Start-up Behavior", "Launch protection monitor automatically on boot", startOnBoot) { startOnBoot = it }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Default Dashboard View", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("Security Scan", "Device Center").forEach { view ->
                    val isSelected = defaultDashboard == view
                    Button(
                        onClick = { defaultDashboard = view },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color(0xFF00E5FF) else Color(0xFF1E293B)
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(view, color = if (isSelected) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun HelpSupportScreen(onBackClick: () -> Unit = {}) {
    ProfileSubScreenBase("Help & Technical Support", onBackClick) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Frequently Asked Questions", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            FAQItem("How do I pair my laptop?", "Launch the Windows Agent on your laptop, copy the generated 6-digit pairing code, and input it in the Device Center section of this application.")
            FAQItem("Why does App Lock require permissions?", "App Lock needs Usage Access to detect when protected apps are opened, and Overlay permissions to show the security PIN screen.")
            FAQItem("How do I reset my recovery code?", "Access Settings -> Account -> Change PIN and specify a new 6-digit recovery code for password reset fallback.")
        }
    }
}

@Composable
fun ProfileAboutScreen(onBackClick: () -> Unit = {}) {
    ProfileSubScreenBase("About System Monitor", onBackClick) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
        ) {
            Text("System Monitor", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Version 1.0.4 (Build 48)", color = Color(0xFF94A3B8), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Advanced protection utility for remote device monitoring, storage optimization, application locking, and network scanning.", color = Color(0xFF94A3B8), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp), lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(12.dp))
            Text("© 2026 System Monitor Inc. All rights reserved.", color = Color(0xFF64748B), fontSize = 10.sp)
        }
    }
}

@Composable
private fun NotificationToggleItem(title: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(description, color = Color(0xFF94A3B8), fontSize = 10.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF00E5FF),
                    uncheckedThumbColor = Color(0xFF64748B),
                    uncheckedTrackColor = Color(0xFF1E293B)
                )
            )
        }
    }
}

@Composable
private fun FAQItem(question: String, answer: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(question, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(answer, color = Color(0xFF94A3B8), fontSize = 11.sp, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun ProfileSubScreenBase(title: String, onBackClick: () -> Unit, content: @Composable () -> Unit) {
    val bgGradient = Brush.verticalGradient(colors = listOf(Color(0xFF080C16), Color(0xFF0B132B), Color(0xFF070B18)))
    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(18.dp))
            content()
        }
    }
}
