package com.systemmonitor.features.settings.power

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Warning
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
fun PowerSettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val power = state.settings.power

    val bgGradient = Brush.verticalGradient(colors = listOf(Color(0xFF080C16), Color(0xFF0B132B), Color(0xFF070B18)))

    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Power & Battery Settings", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(18.dp))

            PowerRow(
                icon = Icons.Default.BatteryChargingFull,
                title = "Continuous Battery Telemetry",
                desc = "Track voltage, temperature & health metrics",
                checked = power.batteryMonitoringEnabled,
                onCheckedChange = {
                    viewModel.onEvent(SettingsEvent.UpdateSettings(state.settings.copy(power = power.copy(batteryMonitoringEnabled = it))))
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.85f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Low Battery Alert Threshold", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Trigger alert when battery level reaches this limit", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                        Text("${power.batteryAlertThreshold}%", color = Color(0xFF10B981), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Slider(
                        value = power.batteryAlertThreshold.toFloat(),
                        onValueChange = {
                            viewModel.onEvent(SettingsEvent.UpdateSettings(state.settings.copy(power = power.copy(batteryAlertThreshold = it.toInt()))))
                        },
                        valueRange = 10f..50f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            activeTrackColor = Color(0xFF10B981),
                            inactiveTrackColor = Color(0xFF1E293B),
                            thumbColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            PowerRow(
                icon = Icons.Default.Power,
                title = "Auto Activate Power Saver",
                desc = "Reduce background telemetry when battery is low",
                checked = power.powerSavingAutoActivate,
                onCheckedChange = {
                    viewModel.onEvent(SettingsEvent.UpdateSettings(state.settings.copy(power = power.copy(powerSavingAutoActivate = it))))
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            PowerRow(
                icon = Icons.Default.Warning,
                title = "Remote Action Confirmation",
                desc = "Require dialog confirmation before Sleep / Shutdown",
                checked = power.remoteSleepConfirmation,
                onCheckedChange = {
                    viewModel.onEvent(SettingsEvent.UpdateSettings(state.settings.copy(power = power.copy(remoteSleepConfirmation = it))))
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            PowerRow(
                icon = Icons.Default.RestartAlt,
                title = "Background Screen-Off Monitoring",
                desc = "Keep monitoring active when screen is turned off",
                checked = power.backgroundMonitoringScreenOff,
                onCheckedChange = {
                    viewModel.onEvent(SettingsEvent.UpdateSettings(state.settings.copy(power = power.copy(backgroundMonitoringScreenOff = it))))
                }
            )
        }
    }
}

@Composable
private fun PowerRow(
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
                Icon(icon, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(desc, color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF10B981))
            )
        }
    }
}
