package com.systemmonitor.features.settings.account

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
fun AccountSettingsScreen(onBackClick: () -> Unit = {}) { AccBase("Account Settings Screen", onBackClick) }

@Composable
fun ProfileSettingsScreen(onBackClick: () -> Unit = {}) { AccBase("Profile Settings Screen", onBackClick) }

@Composable
fun LoginHistoryScreen(onBackClick: () -> Unit = {}) { AccBase("Login History Screen", onBackClick) }

@Composable
fun AccountSecurityScreen(onBackClick: () -> Unit = {}) { AccBase("Account Security Screen", onBackClick) }

@Composable
private fun AccBase(title: String, onBackClick: () -> Unit) {
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
