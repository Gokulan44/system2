package com.systemmonitor.features.laptop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemmonitor.domain.model.ProcessInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessesScreen(
    onBackClick: () -> Unit
) {
    val sampleProcesses = remember {
        listOf(
            ProcessInfo(1044, "chrome.exe", 14.5, 8.2, "running", "User"),
            ProcessInfo(2392, "python.exe (Agent)", 2.1, 1.4, "running", "SYSTEM"),
            ProcessInfo(4810, "code.exe (VS Code)", 5.6, 6.1, "running", "User"),
            ProcessInfo(5920, "explorer.exe", 0.8, 2.3, "running", "User"),
            ProcessInfo(8124, "spotify.exe", 1.2, 3.5, "running", "User"),
            ProcessInfo(9012, "svchost.exe", 0.1, 0.9, "running", "SYSTEM")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Running Processes", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A0F1D))
            )
        },
        containerColor = Color(0xFF0A0F1D)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sampleProcesses) { proc ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(proc.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("PID: ${proc.pid} • User: ${proc.username}", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("CPU: ${proc.cpuPercent}%", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("RAM: ${proc.memoryPercent}%", color = Color(0xFF3B82F6), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
