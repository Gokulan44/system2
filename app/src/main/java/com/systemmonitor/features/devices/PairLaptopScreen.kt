package com.systemmonitor.features.devices

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Router
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemmonitor.data.network.NetworkResult
import com.systemmonitor.viewmodel.LaptopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairLaptopScreen(
    laptopViewModel: LaptopViewModel,
    onPairSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val pairingState by laptopViewModel.pairingState.collectAsState()

    var ipAddress by remember { mutableStateOf("192.168.1.50") }
    var port by remember { mutableStateOf("8765") }
    var pairingCode by remember { mutableStateOf("") }
    var deviceName by remember { mutableStateOf("My Windows Laptop") }

    LaunchedEffect(pairingState) {
        when (val res = pairingState) {
            is NetworkResult.Success -> {
                Toast.makeText(context, "Pairing Successful!", Toast.LENGTH_SHORT).show()
                laptopViewModel.clearPairingState()
                onPairSuccess()
            }
            is NetworkResult.Error -> {
                Toast.makeText(context, "Pairing Error: ${res.message}", Toast.LENGTH_LONG).show()
                laptopViewModel.clearPairingState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pair New Laptop", color = Color.White, fontWeight = FontWeight.Bold) },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Laptop,
                contentDescription = null,
                tint = Color(0xFF00E5FF),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Connect Windows Laptop",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Ensure the Windows Agent app is running on your laptop",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

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

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = ipAddress,
                    onValueChange = { ipAddress = it },
                    label = { Text("IP Address (e.g. 192.168.1.50)", color = Color(0xFF94A3B8)) },
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

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = pairingCode,
                onValueChange = { pairingCode = it },
                label = { Text("6-Digit Pairing Code", color = Color(0xFF94A3B8)) },
                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFF00E5FF)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF00E5FF),
                    unfocusedBorderColor = Color(0xFF475569)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val portNum = port.toIntOrNull() ?: 8765
                    laptopViewModel.pairLaptop(
                        ipAddress = ipAddress,
                        port = portNum,
                        pairingCode = pairingCode,
                        deviceName = deviceName,
                        deviceId = "laptop_${System.currentTimeMillis()}"
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                enabled = pairingState !is NetworkResult.Loading
            ) {
                if (pairingState is NetworkResult.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                } else {
                    Text("Verify & Pair Device", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
