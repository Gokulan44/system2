package com.systemmonitor.features.security.presentation.dashboard

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemmonitor.features.security.data.repository.SecurityRepository
import com.systemmonitor.features.security.domain.model.SecurityScan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SecurityDashboardViewModel @Inject constructor(
    private val repository: SecurityRepository
) : ViewModel() {
    private val _history = MutableStateFlow<List<SecurityScan>>(emptyList())
    val history: StateFlow<List<SecurityScan>> = _history.asStateFlow()

    init {
        repository.getScanHistory().onEach { _history.value = it }.launchIn(viewModelScope)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityDashboardScreen(
    viewModel: SecurityDashboardViewModel = hiltViewModel(),
    onStartScan: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToVault: () -> Unit = {},
    onBackClick: () -> Unit
) {
    val history by viewModel.history.collectAsState()
    val lastScan = history.firstOrNull()

    val isAmoled = com.systemmonitor.LocalDarkMode.current
    val bgGradient = if (isAmoled) {
        Brush.verticalGradient(colors = listOf(Color(0xFF000000), Color(0xFF000000)))
    } else {
        Brush.verticalGradient(colors = listOf(Color(0xFF080C16), Color(0xFF0B132B), Color(0xFF070B18)))
    }

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
                Column {
                    Text("Security Center", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Antivirus, Malware & Permission Analyzer", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Score Banner
            Surface(
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.85f)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(54.dp).clip(CircleShape).background(Color(0xFF10B981).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("System Protected", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(
                                if (lastScan != null) "Last Scan: ${lastScan.score.score}% (${lastScan.score.rating})" else "Deep Scan Recommended",
                                color = Color(0xFF10B981),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Text("${lastScan?.score?.score ?: 98}%", color = Color(0xFF10B981), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onStartScan,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Start Full Security Scan", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Tiles
            Text("Security Modules", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            SecurityModuleTile("Installed Apps Scan", "Audit 142 installed APK signatures & hash", Icons.Default.Apps, Color(0xFF3B82F6), onStartScan)
            Spacer(modifier = Modifier.height(10.dp))
            SecurityModuleTile("Permission Analyzer", "Check Camera, Mic, SMS, Location access", Icons.Default.PrivacyTip, Color(0xFF8B5CF6), onStartScan)
            Spacer(modifier = Modifier.height(10.dp))
            SecurityModuleTile("Secure Encrypted Vault", "Hardware AES-256 protected file vault & lock", Icons.Default.Lock, Color(0xFFEC4899), onNavigateToVault)
            Spacer(modifier = Modifier.height(10.dp))
            SecurityModuleTile("Network & Wi-Fi Security", "Analyze DNS, VPN & Wi-Fi security", Icons.Default.Wifi, Color(0xFF00E5FF), onStartScan)
            Spacer(modifier = Modifier.height(10.dp))
            SecurityModuleTile("Scan History & Audit Logs", "View past threat reports & history", Icons.Default.History, Color(0xFFF59E0B), onNavigateToHistory)
        }
    }
}

@Composable
private fun SecurityModuleTile(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp)).clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.85f)
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(desc, color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF64748B))
        }
    }
}
