package com.systemmonitor.features.settings.privacy

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.os.Build
import androidx.compose.material.icons.filled.Notifications
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Process
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.systemmonitor.features.settings.SettingsEvent
import com.systemmonitor.features.settings.SettingsViewModel

@Composable
fun PermissionManagerScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var hasMicPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    var hasLocationPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasCameraPermission = it
    }
    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasMicPermission = it
    }
    val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasLocationPermission = it
    }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasNotificationPermission = it
    }

    PrivBase("Permission Manager", onBackClick) {
        Text("Device Runtime Permissions", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("Manage system hardware access permissions used by this application.", color = Color(0xFF94A3B8), fontSize = 12.sp)
        
        Spacer(modifier = Modifier.height(20.dp))

        PermissionItem(
            title = "Camera Access",
            desc = "Required for QR scanning and screenshot stream validation",
            isGranted = hasCameraPermission,
            icon = Icons.Default.CameraAlt,
            color = Color(0xFF3B82F6),
            onRequest = { cameraLauncher.launch(Manifest.permission.CAMERA) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        PermissionItem(
            title = "Microphone Access",
            desc = "Required for voice commands & remote telemetry analysis",
            isGranted = hasMicPermission,
            icon = Icons.Default.Mic,
            color = Color(0xFF8B5CF6),
            onRequest = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        PermissionItem(
            title = "Precise Location",
            desc = "Required to check secure Wi-Fi SSID network configurations",
            isGranted = hasLocationPermission,
            icon = Icons.Default.LocationOn,
            color = Color(0xFF00E5FF),
            onRequest = { locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Spacer(modifier = Modifier.height(10.dp))
            PermissionItem(
                title = "Push Notifications",
                desc = "Required to show incoming request alerts, intrusions, and scan results",
                isGranted = hasNotificationPermission,
                icon = Icons.Default.Notifications,
                color = Color(0xFFFF9800),
                onRequest = { notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
            )
        }
    }
}

@Composable
fun DataCollectionScreen(viewModel: SettingsViewModel, onBackClick: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val privacy = state.settings.privacy

    PrivBase("Data Collection & Consent", onBackClick) {
        Text("Anonymous Diagnostic Sharing", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("Help us improve System Monitor stability by sharing crash logs & performance telemetry anonymously.", color = Color(0xFF94A3B8), fontSize = 12.sp)
        
        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.85f)
        ) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Anonymous Diagnostics", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Automatically transmit error traces to Google Crashlytics", color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
                Switch(
                    checked = privacy.dataCollectionConsent,
                    onCheckedChange = {
                        viewModel.onEvent(SettingsEvent.UpdateSettings(state.settings.copy(privacy = privacy.copy(dataCollectionConsent = it))))
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF00E5FF))
                )
            }
        }
    }
}

@Composable
fun UsageAccessScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    var hasUsageAccess by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.noteOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        hasUsageAccess = mode == AppOpsManager.MODE_ALLOWED
    }

    PrivBase("Usage Access Permission", onBackClick) {
        Text("System App Usage Statistics", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("Required to analyze and protect your device by locks & parental restrictions.", color = Color(0xFF94A3B8), fontSize = 12.sp)

        Spacer(modifier = Modifier.height(24.dp))

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
                    Text("Permission Status", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Text(
                        text = if (hasUsageAccess) "Granted" else "Missing",
                        color = if (hasUsageAccess) Color(0xFF10B981) else Color(0xFFEF4444),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Grant Usage Access Settings", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AccessibilitySettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    var hasAccessibility by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        hasAccessibility = am.isEnabled
    }

    PrivBase("Accessibility Settings", onBackClick) {
        Text("Accessibility Services Binding", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("Required to prevent unauthorized app lock overlays and task terminations.", color = Color(0xFF94A3B8), fontSize = 12.sp)

        Spacer(modifier = Modifier.height(24.dp))

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
                    Text("Service Status", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Text(
                        text = if (hasAccessibility) "Enabled" else "Disabled",
                        color = if (hasAccessibility) Color(0xFF10B981) else Color(0xFFEF4444),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Configure Accessibility Settings", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PermissionItem(
    title: String,
    desc: String,
    isGranted: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onRequest: () -> Unit
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
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(desc, color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
            }
            if (isGranted) {
                Text("Granted", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            } else {
                Button(
                    onClick = onRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = color),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Allow", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PrivBase(
    title: String,
    onBackClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
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
                Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}
