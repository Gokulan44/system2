package com.systemmonitor.features.resources

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemmonitor.data.network.NetworkResult
import com.systemmonitor.domain.model.Laptop
import com.systemmonitor.domain.model.LaptopStatus
import com.systemmonitor.features.remotepermission.domain.model.ResourceRequest
import com.systemmonitor.features.remotepermission.presentation.PermissionViewModel
import com.systemmonitor.viewmodel.LaptopViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceCenterScreen(
    resourceViewModel: ResourceViewModel,
    permissionViewModel: PermissionViewModel,
    laptopViewModel: LaptopViewModel,
    onBackClick: () -> Unit,
    onNavigateToStatus: (String) -> Unit
) {
    val selectedLaptop by laptopViewModel.selectedLaptop.collectAsState()
    val catalogState by resourceViewModel.catalog.collectAsState()

    val laptop = selectedLaptop ?: Laptop(
        id = "LAPTOP-7F29A1",
        name = "My Paired Laptop",
        ipAddress = "192.168.1.50",
        port = 8765,
        status = LaptopStatus.ONLINE
    )

    LaunchedEffect(laptop) {
        resourceViewModel.loadResourcesForLaptop(laptop)
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
                title = { Text("Resource Catalog", color = Color.White, fontWeight = FontWeight.Bold) },
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
            when (val result = catalogState) {
                is NetworkResult.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF00E5FF)
                    )
                }
                is NetworkResult.Success -> {
                    val list = result.data
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Laptop, contentDescription = null, tint = Color(0xFF00E5FF))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(laptop.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("IP: ${laptop.ipAddress}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                }
                            }
                        }

                        Text(
                            text = "Available Files on Laptop",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(list) { item ->
                                ResourceItemRow(
                                    resource = item,
                                    onClick = {
                                        // 1. Simulate permission request popup on Android
                                        permissionViewModel.simulateRequest(
                                            laptopId = laptop.id,
                                            resourceName = item.name,
                                            fileSizeBytes = item.sizeBytes
                                        )
                                        // 2. Direct user to the status screen where progress is tracked
                                        onNavigateToStatus(item.name)
                                    }
                                )
                            }
                        }
                    }
                }
                is NetworkResult.Error -> {
                    Text(
                        text = "Failed to load catalog: ${result.message}",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun ResourceItemRow(resource: ResourceRequest, onClick: () -> Unit) {
    val sizeText = DecimalFormat("#.##").format(resource.sizeBytes.toDouble() / (1024 * 1024)) + " MB"
    val isDangerFile = resource.name == "Example.exe" || resource.name == "Setup.msi"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00E5FF).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.InsertDriveFile,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resource.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = sizeText,
                    color = Color(0xFF64748B),
                    fontSize = 11.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isDangerFile) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "RISKY",
                            color = Color(0xFFEF4444),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Request download",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
