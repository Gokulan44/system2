package com.systemmonitor.features.remote

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemmonitor.data.network.NetworkResult
import com.systemmonitor.domain.model.CommandType
import com.systemmonitor.domain.model.Laptop
import com.systemmonitor.viewmodel.LaptopViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.systemmonitor.features.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteControlScreen(
    laptopViewModel: LaptopViewModel,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val settingsState by settingsViewModel.uiState.collectAsState()
    val powerSettings = settingsState.settings.power
    val selectedLaptop by laptopViewModel.selectedLaptop.collectAsState()
    val commandResult by laptopViewModel.commandResult.collectAsState()

    var activeDialogCommand by remember { mutableStateOf<CommandType?>(null) }

    LaunchedEffect(commandResult) {
        when (val res = commandResult) {
            is NetworkResult.Success -> {
                Toast.makeText(context, res.data, Toast.LENGTH_LONG).show()
                laptopViewModel.clearCommandResult()
            }
            is NetworkResult.Error -> {
                Toast.makeText(context, "Error: ${res.message}", Toast.LENGTH_LONG).show()
                laptopViewModel.clearCommandResult()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Remote Power Control", color = Color.White, fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            val laptop = selectedLaptop ?: Laptop(
                id = "laptop_1",
                name = "My Windows Laptop",
                ipAddress = "192.168.1.50",
                port = 8765
            )

            // Laptop Header Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF00E5FF).copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Laptop, contentDescription = null, tint = Color(0xFF00E5FF))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = laptop.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(text = "IP: ${laptop.ipAddress}:${laptop.port}", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            PowerControls(
                onCommandSelect = { cmd ->
                    val needsConfirmation = when (cmd) {
                        CommandType.SLEEP -> powerSettings.remoteSleepConfirmation
                        CommandType.RESTART -> powerSettings.remoteRestartConfirmation
                        CommandType.SHUTDOWN -> powerSettings.remoteShutdownConfirmation
                        else -> true
                    }
                    if (needsConfirmation) {
                        activeDialogCommand = cmd
                    } else {
                        laptopViewModel.sendPowerCommand(cmd, "1234")
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Security Notice Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Encrypted Authentication", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "All power execution commands are verified against your local Windows Agent security PIN to prevent unauthorized access.",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                }
            }

            if (activeDialogCommand != null) {
                CommandConfirmationDialog(
                    commandType = activeDialogCommand!!,
                    onConfirm = { pin ->
                        laptopViewModel.sendPowerCommand(activeDialogCommand!!, pin)
                        activeDialogCommand = null
                    },
                    onDismiss = { activeDialogCommand = null }
                )
            }
        }
    }
}
