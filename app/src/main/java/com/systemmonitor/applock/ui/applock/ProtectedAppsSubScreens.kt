package com.systemmonitor.applock.ui.applock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
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

@Composable
fun ProtectedAppsScreen(onBackClick: () -> Unit = {}) {
    AppLockBase("Protected Applications", onBackClick) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Currently Locked Apps", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            MockAppItem("Gmail", "com.google.android.gm", true)
            MockAppItem("Google Photos", "com.google.android.apps.photos", true)
            MockAppItem("WhatsApp", "com.whatsapp", true)
        }
    }
}

@Composable
fun AddAppsScreen(onBackClick: () -> Unit = {}) {
    AppLockBase("Add Applications", onBackClick) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Available Applications", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            MockAppItem("Google Chrome", "com.android.chrome", false)
            MockAppItem("Settings", "com.android.settings", false)
            MockAppItem("Messages", "com.google.android.apps.messaging", false)
        }
    }
}

@Composable
fun LockScreen(onBackClick: () -> Unit = {}) {
    val bgGradient = Brush.verticalGradient(colors = listOf(Color(0xFF080C16), Color(0xFF0B132B), Color(0xFF070B18)))
    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        IconButton(onClick = onBackClick, modifier = Modifier.padding(16.dp).align(Alignment.TopStart)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Column(
            modifier = Modifier.align(Alignment.Center).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(100.dp).clip(CircleShape).background(Color(0xFF00E5FF).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(60.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("App Lock Active", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Your applications are protected under 6-digit military-grade encryption.", color = Color(0xFF94A3B8), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
private fun MockAppItem(appName: String, packageName: String, isLocked: Boolean) {
    var lockedState by remember { mutableStateOf(isLocked) }
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(appName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(packageName, color = Color(0xFF64748B), fontSize = 10.sp)
            }
            if (lockedState) {
                IconButton(onClick = { lockedState = false }) {
                    Icon(Icons.Default.Lock, contentDescription = "Lock", tint = Color(0xFFEF4444))
                }
            } else {
                IconButton(onClick = { lockedState = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFF00E5FF))
                }
            }
        }
    }
}

@Composable
private fun AppLockBase(title: String, onBackClick: () -> Unit, content: @Composable () -> Unit) {
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
