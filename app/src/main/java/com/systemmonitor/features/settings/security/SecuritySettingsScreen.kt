package com.systemmonitor.features.settings.security

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
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
fun SecuritySettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToAppLock: () -> Unit,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val security = state.settings.security

    var deviceSec by remember { mutableStateOf(security.deviceSecurityEnabled) }
    var appLock by remember { mutableStateOf(security.appLockEnabled) }
    var biometric by remember { mutableStateOf(security.biometricEnabled) }
    var virusScan by remember { mutableStateOf(security.virusScanEnabled) }
    var privacyCheck by remember { mutableStateOf(security.privacyCheckEnabled) }
    var alerts by remember { mutableStateOf(security.securityAlertsEnabled) }

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
                Text("Security Settings", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(18.dp))

            SecurityToggleRow(
                icon = Icons.Default.Shield,
                title = "Device Security Protection",
                desc = "Real-time threat monitoring & memory scan",
                checked = deviceSec,
                onCheckedChange = {
                    deviceSec = it
                    viewModel.onEvent(SettingsEvent.UpdateSettings(state.settings.copy(security = security.copy(deviceSecurityEnabled = it))))
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecurityToggleRow(
                icon = Icons.Default.Lock,
                title = "App Lock Protection",
                desc = "Require passcode/biometric for selected apps",
                checked = appLock,
                onCheckedChange = {
                    appLock = it
                    viewModel.onEvent(SettingsEvent.UpdateSettings(state.settings.copy(security = security.copy(appLockEnabled = it))))
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecurityToggleRow(
                icon = Icons.Default.Fingerprint,
                title = "Biometric Unlock",
                desc = "Use Fingerprint / Face authentication",
                checked = biometric,
                onCheckedChange = {
                    biometric = it
                    viewModel.onEvent(SettingsEvent.UpdateSettings(state.settings.copy(security = security.copy(biometricEnabled = it))))
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecurityToggleRow(
                icon = Icons.Default.Security,
                title = "Automated Virus & Malware Scan",
                desc = "Daily background scanning for malware signatures",
                checked = virusScan,
                onCheckedChange = {
                    virusScan = it
                    viewModel.onEvent(SettingsEvent.UpdateSettings(state.settings.copy(security = security.copy(virusScanEnabled = it))))
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecurityToggleRow(
                icon = Icons.Default.PrivacyTip,
                title = "Privacy & Permission Check",
                desc = "Alert on excessive background permission usage",
                checked = privacyCheck,
                onCheckedChange = {
                    privacyCheck = it
                    viewModel.onEvent(SettingsEvent.UpdateSettings(state.settings.copy(security = security.copy(privacyCheckEnabled = it))))
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onNavigateToAppLock,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Configure App Lock Settings", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SecurityToggleRow(
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
                Icon(icon, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(desc, color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF00E5FF))
            )
        }
    }
}
