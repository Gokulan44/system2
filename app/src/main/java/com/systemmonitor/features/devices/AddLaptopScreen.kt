package com.systemmonitor.features.devices

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemmonitor.data.network.NetworkResult
import com.systemmonitor.domain.model.ConnectionMode
import com.systemmonitor.viewmodel.LaptopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLaptopScreen(
    laptopViewModel: LaptopViewModel,
    onStatusOnline: () -> Unit,
    onStatusError: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val statusState by laptopViewModel.statusState.collectAsState()

    var selectedMode by remember { mutableStateOf(ConnectionMode.LOCAL) }
    var ipAddress by remember { mutableStateOf("192.168.1.50") }
    var port by remember { mutableStateOf("8765") }
    var deviceName by remember { mutableStateOf("My Windows Laptop") }

    // Sync mode to ViewModel
    LaunchedEffect(selectedMode) {
        laptopViewModel.pendingConnectionMode = selectedMode
    }

    LaunchedEffect(statusState) {
        when (val res = statusState) {
            is NetworkResult.Success -> {
                Toast.makeText(context, "Agent is Online! Proceeding to pair...", Toast.LENGTH_SHORT).show()
                laptopViewModel.clearStatusState()
                onStatusOnline()
            }
            is NetworkResult.Error -> {
                Toast.makeText(context, "Connection Failed: ${res.message}", Toast.LENGTH_LONG).show()
                laptopViewModel.clearStatusState()
                onStatusError()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Laptop", color = Color.White, fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Laptop,
                contentDescription = null,
                tint = Color(0xFF00E5FF),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Choose Connection Type",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "How will you connect to your Windows PC?",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Connection mode cards ──────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ConnectionModeCard(
                    mode = ConnectionMode.LOCAL,
                    selectedMode = selectedMode,
                    icon = Icons.Default.Wifi,
                    title = "Local Wi-Fi",
                    subtitle = "Same router\nFast & direct",
                    accentColor = Color(0xFF00E5FF),
                    modifier = Modifier.weight(1f),
                    onSelect = { selectedMode = ConnectionMode.LOCAL }
                )
                ConnectionModeCard(
                    mode = ConnectionMode.REMOTE,
                    selectedMode = selectedMode,
                    icon = Icons.Default.Cloud,
                    title = "Remote Cloud",
                    subtitle = "Different network\nFirebase relay",
                    accentColor = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f),
                    onSelect = { selectedMode = ConnectionMode.REMOTE }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Info banner
            val bannerText = if (selectedMode == ConnectionMode.LOCAL)
                "📡 Direct HTTP to 192.168.x.x:8765 — fastest, requires same router."
            else
                "☁️ Commands relay through Firebase — works anywhere, slight delay."

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (selectedMode == ConnectionMode.LOCAL)
                    Color(0xFF00E5FF).copy(alpha = 0.1f)
                else
                    Color(0xFF8B5CF6).copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = bannerText,
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Device Name ───────────────────────────────────────────────
            OutlinedTextField(
                value = deviceName,
                onValueChange = { deviceName = it },
                label = { Text("Laptop Name", color = Color(0xFF94A3B8)) },
                leadingIcon = { Icon(Icons.Default.Laptop, contentDescription = null, tint = Color(0xFF00E5FF)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF00E5FF),
                    unfocusedBorderColor = Color(0xFF475569)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // ── IP/Port (LOCAL only) ───────────────────────────────────────
            AnimatedVisibility(
                visible = selectedMode == ConnectionMode.LOCAL,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = ipAddress,
                            onValueChange = { ipAddress = it },
                            label = { Text("IP Address", color = Color(0xFF94A3B8)) },
                            leadingIcon = { Icon(Icons.Default.Router, contentDescription = null, tint = Color(0xFF00E5FF)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color(0xFF475569)
                            ),
                            modifier = Modifier.weight(2f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = port,
                            onValueChange = { port = it },
                            label = { Text("Port", color = Color(0xFF94A3B8)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color(0xFF475569)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── REMOTE info ───────────────────────────────────────────────
            AnimatedVisibility(
                visible = selectedMode == ConnectionMode.REMOTE,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E1B4B),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("How to pair remotely:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("1. Start the Windows Agent on your PC", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            Text("2. Agent registers in Firebase using your account", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            Text("3. Enter the pairing code shown on the agent", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            Text("4. Commands relay through Firebase cloud", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Connect button ─────────────────────────────────────────────
            val btnColor = if (selectedMode == ConnectionMode.LOCAL) Color(0xFF00E5FF) else Color(0xFF8B5CF6)
            Button(
                onClick = {
                    if (selectedMode == ConnectionMode.LOCAL) {
                        val portNum = port.toIntOrNull() ?: 8765
                        laptopViewModel.checkLaptopStatus(ipAddress, portNum, deviceName)
                    } else {
                        // For REMOTE, skip status check and go to pairing code entry
                        laptopViewModel.pendingIpAddress = ""
                        laptopViewModel.pendingPort = 8765
                        laptopViewModel.pendingDeviceName = deviceName
                        onStatusOnline()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                enabled = statusState !is NetworkResult.Loading
            ) {
                if (statusState is NetworkResult.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                } else {
                    val btnText = if (selectedMode == ConnectionMode.LOCAL) "Find & Connect" else "Proceed to Pairing"
                    Text(btnText, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun ConnectionModeCard(
    mode: ConnectionMode,
    selectedMode: ConnectionMode,
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit
) {
    val isSelected = selectedMode == mode
    val borderColor = if (isSelected) accentColor else Color(0xFF334155)
    val bgColor = if (isSelected) accentColor.copy(alpha = 0.12f) else Color(0xFF1E293B)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onSelect)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(accentColor.copy(alpha = if (isSelected) 0.25f else 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, color = Color(0xFF94A3B8), fontSize = 11.sp, textAlign = TextAlign.Center)
            if (isSelected) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = accentColor
                ) {
                    Text("Selected", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp))
                }
            }
        }
    }
}
