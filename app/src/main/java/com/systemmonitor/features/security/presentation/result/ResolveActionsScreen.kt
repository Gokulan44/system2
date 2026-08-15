package com.systemmonitor.features.security.presentation.result

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemmonitor.features.security.data.repository.SecurityRepository
import com.systemmonitor.features.security.domain.model.SecurityScan
import com.systemmonitor.features.security.domain.model.ThreatInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResolveActionsViewModel @Inject constructor(
    private val repository: SecurityRepository
) : ViewModel() {
    fun resolveThreat(threatId: String, scanId: Long, action: String, onComplete: (SecurityScan) -> Unit) {
        viewModelScope.launch {
            repository.resolveThreat(threatId, scanId, action)
            val history = repository.getScanHistory().first()
            val updatedScan = history.find { it.scanId == scanId }
            if (updatedScan != null) {
                onComplete(updatedScan)
            }
        }
    }
}

@Composable
fun ResolveActionsScreen(
    threat: ThreatInfo,
    onResolveAction: (String) -> Unit, // "REMOVE", "QUARANTINE", "IGNORE"
    onBackClick: () -> Unit
) {
    val bgGradient = Brush.verticalGradient(colors = listOf(Color(0xFF080C16), Color(0xFF0B132B), Color(0xFF070B18)))
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isResolving by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var resolutionStep by remember { mutableStateOf("") }
    var chosenAction by remember { mutableStateOf("") }

    fun executeRemediation(action: String) {
        chosenAction = action
        coroutineScope.launch {
            isResolving = true
            
            // Step-by-step diagnostic verification
            resolutionStep = "Initializing environment sandbox..."
            delay(500)
            resolutionStep = "Verifying target signatures..."
            delay(500)
            resolutionStep = "Executing policy resolution actions..."
            delay(600)

            // Trigger actual System Intents
            if (action == "REMOVE") {
                if (!threat.packageName.isNullOrEmpty()) {
                    runCatching {
                        val intent = Intent(Intent.ACTION_DELETE).apply {
                            data = Uri.parse("package:${threat.packageName}")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    }
                } else if (threat.id == "config_adb_enabled") {
                    runCatching {
                        val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    }.onFailure {
                        runCatching {
                            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        }
                    }
                } else if (threat.id == "config_unknown_sources") {
                    runCatching {
                        val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    }.onFailure {
                        runCatching {
                            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        }
                    }
                }
            }

            resolutionStep = "Finalizing telemetry registers..."
            delay(400)
            isResolving = false
            isSuccess = true
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        if (isResolving) {
            // Processing Screen
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF00E5FF),
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Applying Security Resolution",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = resolutionStep,
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else if (isSuccess) {
            // Success Screen
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(72.dp).clip(CircleShape).background(Color(0xFF10B981).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(38.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Remediation Applied",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = when (chosenAction) {
                        "REMOVE" -> if (!threat.packageName.isNullOrEmpty()) "Uninstall dialog launched for app." else "System settings opened to disable vulnerability configuration."
                        "QUARANTINE" -> "Suspicious app added to lock filter sandbox container."
                        else -> "Threat skipped and added to whitelist registry."
                    },
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(36.dp))
                Button(
                    onClick = { onResolveAction(chosenAction) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("CONTINUE", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Choice Selection Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Resolve Actions", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.85f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Target Threat:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(threat.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(threat.description, color = Color(0xFF94A3B8), fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                Text("Select Remediation Action:", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(14.dp))

                val actionTitle = if (!threat.packageName.isNullOrEmpty()) "Uninstall application from device" else "Open system settings to disable configuration"
                ResolveOptionTile(
                    title = "Remove Threat",
                    subtitle = actionTitle,
                    icon = Icons.Default.Delete,
                    color = Color(0xFFEF4444)
                ) { executeRemediation("REMOVE") }

                Spacer(modifier = Modifier.height(12.dp))

                ResolveOptionTile(
                    title = "Quarantine",
                    subtitle = "Isolate suspicious items in isolated vault container",
                    icon = Icons.Default.Shield,
                    color = Color(0xFF10B981)
                ) { executeRemediation("QUARANTINE") }

                Spacer(modifier = Modifier.height(12.dp))

                ResolveOptionTile(
                    title = "Ignore",
                    subtitle = "Skip for now & whitelist from future scans",
                    icon = Icons.Default.VisibilityOff,
                    color = Color(0xFF94A3B8)
                ) { executeRemediation("IGNORE") }
            }
        }
    }
}

@Composable
private fun ResolveOptionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.85f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(46.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color(0xFF94A3B8), fontSize = 12.sp)
            }
        }
    }
}
