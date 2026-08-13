package com.systemmonitor.features.intrusion.presentation

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntrusionDetailsScreen(
    viewModel: IntrusionViewModel,
    eventId: String,
    onBackClick: () -> Unit
) {
    val events by viewModel.events.collectAsState()
    val event = events.find { it.eventId == eventId }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF060913),
            Color(0xFF0F172A),
            Color(0xFF0B0F19)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Logon Failure Details", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (event != null) {
                        IconButton(
                            onClick = {
                                viewModel.deleteEvent(event.eventId)
                                onBackClick()
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Event", tint = Color(0xFFEF4444))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B0F19))
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
            if (event == null) {
                // Not found fallback
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Intrusion Event Not Found", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                val isCritical = event.severity == "CRITICAL"
                val severityColor = if (isCritical) Color(0xFFEF4444) else Color(0xFFF59E0B)

                val date = Date(event.timestamp)
                val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
                val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
                val timeStr = timeFormat.format(date)
                val dateStr = dateFormat.format(date)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // ── Decrypted Photo Card ──────────────────────────────────
                    if (isCritical && !event.encryptedPhoto.isNullOrEmpty()) {
                        val decryptedBitmap = remember(event) {
                            viewModel.decryptIntruderPhoto(event.encryptedPhoto, event.photoHash)
                        }

                        if (decryptedBitmap != null) {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp)
                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                            ) {
                                Image(
                                    bitmap = decryptedBitmap.asImageBitmap(),
                                    contentDescription = "Intruder Capture",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            PhotoFallbackCard(
                                icon = Icons.Default.NoPhotography,
                                title = "Decryption Failed",
                                desc = "Integrity check failed. The payload has been tampered with or corrupted."
                            )
                        }
                    } else {
                        PhotoFallbackCard(
                            icon = Icons.Default.NoPhotography,
                            title = "No Camera Capture",
                            desc = "Security policy 'Alert-Only' was applied to this login event. Camera capture was disabled."
                        )
                    }

                    // ── Details Card ──────────────────────────────────
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(severityColor.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = severityColor, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Security Event Details", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("Logged via Windows security logs", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Color(0xFF1E293B))
                            Spacer(modifier = Modifier.height(16.dp))

                            DetailRow("Device ID", event.laptopId)
                            Spacer(modifier = Modifier.height(10.dp))
                            DetailRow("Event ID", event.eventId)
                            Spacer(modifier = Modifier.height(10.dp))
                            DetailRow("Severity", event.severity, color = severityColor)
                            Spacer(modifier = Modifier.height(10.dp))
                            DetailRow("Attempts", "${event.attemptCount} Failed Logon")
                            Spacer(modifier = Modifier.height(10.dp))
                            DetailRow("Time", timeStr)
                            Spacer(modifier = Modifier.height(10.dp))
                            DetailRow("Date", dateStr)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoFallbackCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFF334155).copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(desc, color = Color(0xFF64748B), fontSize = 12.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, color: Color = Color.White) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color(0xFF94A3B8), fontSize = 13.sp)
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
