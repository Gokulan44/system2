package com.systemmonitor.features.settings.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppLockSettingsScreen(onBackClick: () -> Unit = {}) {
    SecurityBaseScreen("App Lock Settings", onBackClick)
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
