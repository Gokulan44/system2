package com.systemmonitor.features.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemmonitor.domain.model.Laptop
import com.systemmonitor.viewmodel.LaptopViewModel
import com.systemmonitor.viewmodel.ScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenViewerScreen(
    laptopViewModel: LaptopViewModel,
    screenViewModel: ScreenViewModel,
    onBackClick: () -> Unit
) {
    val selectedLaptop by laptopViewModel.selectedLaptop.collectAsState()
    val frameBitmap by screenViewModel.screenFrame.collectAsState()
    val isConnected by screenViewModel.isConnected.collectAsState()

    val laptop = selectedLaptop ?: Laptop(
        id = "laptop_1",
        name = "Windows Laptop",
        ipAddress = "192.168.1.50",
        port = 8765
    )

    DisposableEffect(laptop) {
        screenViewModel.startStreaming(laptop)
        onDispose {
            screenViewModel.stopStreaming()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Screen Viewer", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (isConnected) screenViewModel.stopStreaming()
                        else screenViewModel.startStreaming(laptop)
                    }) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = "Toggle Stream",
                            tint = if (isConnected) Color.Red else Color.Green
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A0F1D))
            )
        },
        containerColor = Color(0xFF0A0F1D)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Status Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444),
                                shape = RoundedCornerShape(5.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isConnected) "Streaming Live (${laptop.name})" else "Disconnected",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(text = "${laptop.ipAddress}:${laptop.port}", color = Color(0xFF94A3B8), fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Screen Stream Display Frame
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (frameBitmap != null) {
                        Image(
                            bitmap = frameBitmap!!.asImageBitmap(),
                            contentDescription = "Desktop Screen Stream",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Connecting to Windows Agent Stream...",
                                color = Color(0xFF94A3B8),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
