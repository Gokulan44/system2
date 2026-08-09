package com.systemmonitor.features.settings.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemmonitor.features.settings.SettingsEvent
import com.systemmonitor.features.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val notif = state.settings.notifications

    var master by remember { mutableStateOf(notif.masterNotificationsEnabled) }
    var secAlerts by remember { mutableStateOf(notif.securityAlerts) }
    var devAlerts by remember { mutableStateOf(notif.deviceAlerts) }
    var battAlerts by remember { mutableStateOf(notif.batteryAlerts) }
    var storAlerts by remember { mutableStateOf(notif.storageAlerts) }
    var netAlerts by remember { mutableStateOf(notif.networkAlerts) }

    val bgGradient = Brush.verticalGradient(colors = listOf(Color(0xFF080C16), Color(0xFF0B132B), Color(0xFF070B18)))

    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Notification Settings", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(18.dp))

            NotifRow(
                icon = Icons.Default.Notifications,
                title = "Master Notifications",
                desc = "Enable or disable all app push notifications",
                checked = master,
                onCheckedChange = {
                    master = it
                    viewModel.onEvent(SettingsEvent.UpdateSettings(state.settings.copy(notifications = notif.copy(masterNotificationsEnabled = it))))
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            NotifRow(
                icon = Icons.Default.Security,
                title = "Security & Threat Alerts",
                desc = "Instant alerts on malware or suspicious logins",
                checked = secAlerts,
                onCheckedChange = {
                    secAlerts = it
                    viewModel.onEvent(SettingsEvent.UpdateSettings(state.settings.copy(notifications = notif.copy(securityAlerts = it))))
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            NotifRow(
                icon = Icons.Default.BatteryAlert,
                title = "Battery & Power Alerts",
                desc = "Alert when battery drops below 20% or 10%",
                checked = battAlerts,
                onCheckedChange = {
                    battAlerts = it
                    viewModel.onEvent(SettingsEvent.UpdateSettings(state.settings.copy(notifications = notif.copy(batteryAlerts = it))))
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            NotifRow(
                icon = Icons.Default.Storage,
                title = "Storage Threshold Alerts",
                desc = "Notify when disk storage exceeds 90% capacity",
                checked = storAlerts,
                onCheckedChange = {
                    storAlerts = it
                    viewModel.onEvent(SettingsEvent.UpdateSettings(state.settings.copy(notifications = notif.copy(storageAlerts = it))))
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            NotifRow(
                icon = Icons.Default.Wifi,
                title = "Network Change Alerts",
                desc = "Notify when Wi-Fi disconnects or changes",
                checked = netAlerts,
                onCheckedChange = {
                    netAlerts = it
                    viewModel.onEvent(SettingsEvent.UpdateSettings(state.settings.copy(notifications = notif.copy(networkAlerts = it))))
                }
            )
        }
    }
}

@Composable
private fun NotifRow(
    icon: ImageVector,
    title: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.85f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(icon, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(desc, color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF8B5CF6))
            )
        }
    }
}
