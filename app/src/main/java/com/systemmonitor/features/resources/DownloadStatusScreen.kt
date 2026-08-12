package com.systemmonitor.features.resources

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadStatusScreen(
    resourceViewModel: ResourceViewModel,
    resourceName: String,
    onBackClick: () -> Unit
) {
    val downloadResult by resourceViewModel.downloadResult.collectAsState()
    val securityScanResult by resourceViewModel.securityScanResult.collectAsState()

    var progress by remember { mutableStateOf(0f) }
    var statusText by remember { mutableStateOf("Waiting for approval...") }
    var isSimulating by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Start simulation of download after user approvals
        resourceViewModel.simulateDownloadResult(
            requestId = "req_sim",
            status = "PENDING",
            filePath = "",
            scanStatus = "PENDING",
            hash = "",
            details = ""
        )

        // 1. Wait for simulated approval
        delay(2000)
        statusText = "Downloading file from laptop..."
        
        // 2. Animate progress bar
        while (progress < 1f) {
            progress += 0.2f
            delay(400)
        }
        
        // 3. Scan phase
        statusText = "Analyzing & Scanning file safety..."
        progress = 1.0f
        delay(1500)

        // 4. Save outcome
        isSimulating = false
        if (resourceName == "Example.exe" || resourceName == "Setup.msi") {
            resourceViewModel.simulateDownloadResult(
                requestId = "req_sim",
                status = "QUARANTINED",
                filePath = "C:\\quarantine\\$resourceName",
                scanStatus = "SUSPICIOUS",
                hash = "E92A8C103F808912A346C890FE1116B97C231450239A01B2E3F401E788F9651C",
                details = "Suspicious executable containing untrusted binary signatures matches security policy threat list."
            )
        } else {
            resourceViewModel.simulateDownloadResult(
                requestId = "req_sim",
                status = "COMPLETED",
                filePath = "C:\\downloads\\approved\\$resourceName",
                scanStatus = "SAFE",
                hash = "8A7C9241B26955C2F6E341B2E391F2618A7C9241B26955C2F6E341B2E391F261",
                details = "No threats found. SHA-256 hash match verified. File signature matches trusted author certificate."
            )
        }
    }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF070B18),
            Color(0xFF0F172A),
            Color(0xFF0A0F1D)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Download & Scan Status", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A0F1D))
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isSimulating) {
                    // Loading State
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF00E5FF),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = resourceName,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = statusText,
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF00E5FF),
                        trackColor = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${(progress * 100).toInt()}% Complete",
                        color = Color(0xFF00E5FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    // Result Panels matching user prompt requirements
                    val scan = securityScanResult
                    if (scan != null && scan.status == "SAFE") {
                        // SAFE Result Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.9f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981).copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Download Complete",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "✓ Resource downloaded safely",
                                    color = Color(0xFF10B981),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(20.dp))
                                HorizontalDivider(color = Color(0xFF1E293B))
                                Spacer(modifier = Modifier.height(16.dp))

                                FileScanDetail(label = "Resource", value = resourceName)
                                FileScanDetail(label = "Size", value = "12 MB")
                                FileScanDetail(label = "Security Scan", value = "✓ SAFE", valueColor = Color(0xFF10B981))
                                FileScanDetail(label = "SHA-256", value = scan.sha256.take(8) + "..." + scan.sha256.takeLast(4))

                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = onBackClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("VIEW DETAILS", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else if (scan != null) {
                        // SUSPICIOUS / QUARANTINED Result Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.9f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444).copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Security Alert",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "⚠ Resource quarantined",
                                    color = Color(0xFFEF4444),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(20.dp))
                                HorizontalDivider(color = Color(0xFF1E293B))
                                Spacer(modifier = Modifier.height(16.dp))

                                FileScanDetail(label = "Resource", value = resourceName)
                                FileScanDetail(label = "Reason", value = "Security policy threat matches")
                                FileScanDetail(label = "Status", value = "File was NOT executed.", valueColor = Color(0xFFEF4444))

                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = onBackClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("VIEW SCAN DETAILS", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FileScanDetail(label: String, value: String, valueColor: Color = Color.White) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color(0xFF64748B), fontSize = 13.sp)
        Text(text = value, color = valueColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}
