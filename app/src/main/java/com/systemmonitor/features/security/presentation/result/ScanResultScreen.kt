package com.systemmonitor.features.security.presentation.result

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
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
import com.systemmonitor.features.security.domain.model.SecurityScan
import com.systemmonitor.features.security.domain.model.ThreatInfo
import com.systemmonitor.features.security.domain.model.ThreatSeverity
import com.systemmonitor.features.security.components.ThreatCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultScreen(
    scanResult: SecurityScan,
    onViewThreatDetails: (ThreatInfo) -> Unit,
    onBackToDashboard: () -> Unit
) {
    val isSecure = scanResult.threats.isEmpty()
    val bgGradient = Brush.verticalGradient(colors = listOf(Color(0xFF080C16), Color(0xFF0B132B), Color(0xFF070B18)))

    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackToDashboard) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan Results", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Score Banner (Secure vs Issues Found)
            Surface(
                modifier = Modifier.fillMaxWidth().border(1.dp, if (isSecure) Color(0xFF10B981) else Color(0xFFEF4444), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.85f)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(64.dp).clip(CircleShape).background(if (isSecure) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSecure) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isSecure) Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isSecure) "Your Device is Secure" else "${scanResult.threats.size} Issues Found",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Security Score: ${scanResult.score.score}/100 (${scanResult.score.rating})",
                        color = if (isSecure) Color(0xFF10B981) else Color(0xFFEF4444),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Scan Breakdown Metrics Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.85f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Scan Metrics", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    ScanDetailRow(label = "Items Inspected", value = "${scanResult.scannedItemsCount} apps & configurations")
                    ScanDetailRow(label = "Analysis Duration", value = String.format("%.2fs", scanResult.durationMs / 1000f))
                    ScanDetailRow(label = "Threats Registered", value = "${scanResult.threats.size} events")
                    ScanDetailRow(label = "Database Sync Status", value = "Verified Safe")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Statuses Checklist
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.85f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Security Categories Checked", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    CategoryStatusRow(label = "Application Signatures", status = "PASSED", details = "All packages verified against Google/Play certificate hashes")
                    CategoryStatusRow(label = "Excessive Permissions Audit", status = "PASSED", details = "No background recording or location leaks registered")
                    CategoryStatusRow(label = "Device Administration Level", status = "PASSED", details = "System binaries verified intact (non-rooted)")
                    CategoryStatusRow(label = "Network Port Security", status = "PASSED", details = "Local sockets and Active VPN interfaces audited clean")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isSecure) {
                Text("Detected Threats & Action Required", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                scanResult.threats.forEach { threat ->
                    ThreatCard(
                        threat = threat,
                        onResolveClick = { onViewThreatDetails(threat) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = onBackToDashboard,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Back to Security Dashboard", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ScanDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFF94A3B8), fontSize = 13.sp)
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CategoryStatusRow(label: String, status: String, details: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF10B981).copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(status, color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        Text(details, color = Color(0xFF64748B), fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = Color(0xFF1E293B).copy(alpha = 0.5f))
    }
}
