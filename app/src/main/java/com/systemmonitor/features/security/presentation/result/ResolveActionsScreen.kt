package com.systemmonitor.features.security.presentation.result

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemmonitor.features.security.data.repository.SecurityRepository
import com.systemmonitor.features.security.domain.model.SecurityScan
import com.systemmonitor.features.security.domain.model.ThreatInfo
import dagger.hilt.android.lifecycle.HiltViewModel
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

    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
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
                    Text(threat.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(threat.description, color = Color(0xFF94A3B8), fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text("Select Remediation Action:", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(14.dp))

            ResolveOptionTile(
                title = "Remove Threat",
                subtitle = "Delete malicious files/apps permanently from device",
                icon = Icons.Default.Delete,
                color = Color(0xFFEF4444)
            ) { onResolveAction("REMOVE") }

            Spacer(modifier = Modifier.height(12.dp))

            ResolveOptionTile(
                title = "Quarantine",
                subtitle = "Isolate suspicious items in isolated vault container",
                icon = Icons.Default.Shield,
                color = Color(0xFF10B981)
            ) { onResolveAction("QUARANTINE") }

            Spacer(modifier = Modifier.height(12.dp))

            ResolveOptionTile(
                title = "Ignore",
                subtitle = "Skip for now & whitelist from future scans",
                icon = Icons.Default.VisibilityOff,
                color = Color(0xFF94A3B8)
            ) { onResolveAction("IGNORE") }
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
            Column {
                Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color(0xFF94A3B8), fontSize = 12.sp)
            }
        }
    }
}
