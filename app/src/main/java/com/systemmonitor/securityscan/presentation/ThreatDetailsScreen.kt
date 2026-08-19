package com.systemmonitor.securityscan.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemmonitor.securityscan.database.entity.FindingEntity
import com.systemmonitor.securityscan.presentation.components.ScanButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreatDetailsScreen(
    finding: FindingEntity,
    onBack: () -> Unit,
    onQuarantine: () -> Unit,
    modifier: Modifier = Modifier
) {
    val severityColor = when (finding.severity.uppercase()) {
        "CRITICAL" -> Color(0xFFEF4444)
        "HIGH" -> Color(0xFFF97316)
        "MEDIUM" -> Color(0xFFF59E0B)
        else -> Color(0xFF3B82F6)
    }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF080C16), Color(0xFF0B132B), Color(0xFF05070F))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Threat Auditing", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF080C16))
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier.background(bgGradient)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(severityColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = severityColor,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = finding.title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
            
            Text(
                text = "Severity: ${finding.severity}",
                color = severityColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Explanation Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.8f))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "Description",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = finding.details,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )

                if (!finding.componentName.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Vulnerable Target / Component",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = finding.componentName,
                        color = Color(0xFF00FFCC),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            ScanButton(
                text = "Isolate via Quarantine Vault",
                onClick = onQuarantine
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onBack,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Dismiss Warning", fontWeight = FontWeight.Bold)
            }
        }
    }
}
