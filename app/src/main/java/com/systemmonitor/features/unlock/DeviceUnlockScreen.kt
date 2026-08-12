package com.systemmonitor.features.unlock

import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.systemmonitor.data.network.NetworkResult
import com.systemmonitor.domain.model.Laptop
import com.systemmonitor.domain.model.LaptopStatus
import com.systemmonitor.local.database.entity.UnlockHistoryEntity
import com.systemmonitor.viewmodel.LaptopViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceUnlockScreen(
    laptopViewModel: LaptopViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val selectedLaptop by laptopViewModel.selectedLaptop.collectAsState()
    val unlockState by laptopViewModel.unlockState.collectAsState()
    val unlockHistory by laptopViewModel.unlockHistory.collectAsState()

    var authMethod by remember { mutableStateOf("FINGERPRINT") } // FINGERPRINT or PIN
    var showPinDialog by remember { mutableStateOf(false) }
    var pinValue by remember { mutableStateOf("") }

    val laptop = selectedLaptop ?: Laptop(
        id = "mock_laptop",
        name = "My Windows Laptop",
        ipAddress = "192.168.1.50",
        port = 8765,
        status = LaptopStatus.ONLINE
    )

    // Setup BiometricPrompt
    val executor = remember(context) { ContextCompat.getMainExecutor(context) }
    var latestChallenge by remember { mutableStateOf("") }

    val biometricPrompt = remember(executor, activity) {
        if (activity != null) {
            BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    Toast.makeText(context, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                    laptopViewModel.clearUnlockState()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    val signature = result.cryptoObject?.signature
                    if (signature != null && latestChallenge.isNotEmpty()) {
                        laptopViewModel.unlockLaptopWithSignature(laptop, signature, latestChallenge, "FINGERPRINT")
                    } else {
                        Toast.makeText(context, "Biometric Signature generation failed", Toast.LENGTH_SHORT).show()
                        laptopViewModel.clearUnlockState()
                    }
                }

                override fun onAuthenticationFailed() {
                    Toast.makeText(context, "Biometric Authentication failed", Toast.LENGTH_SHORT).show()
                    laptopViewModel.clearUnlockState()
                }
            })
        } else null
    }

    LaunchedEffect(unlockState) {
        when (val res = unlockState) {
            is NetworkResult.Success -> {
                Toast.makeText(context, "Unlock command verified and approved!", Toast.LENGTH_LONG).show()
                laptopViewModel.clearUnlockState()
            }
            is NetworkResult.Error -> {
                Toast.makeText(context, "Unlock failed: ${res.message}", Toast.LENGTH_LONG).show()
                laptopViewModel.clearUnlockState()
            }
            else -> {}
        }
    }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF080C16),
            Color(0xFF0F172A),
            Color(0xFF0A0F1D)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Unlock", color = Color.White, fontWeight = FontWeight.Bold) },
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header device status card
                item {
                    val isOnline = laptop.status == LaptopStatus.ONLINE
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Laptop,
                                contentDescription = null,
                                tint = if (isOnline) Color(0xFF00E5FF) else Color(0xFF64748B),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = laptop.name,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(if (isOnline) Color(0xFF10B981) else Color(0xFFEF4444), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isOnline) "CONNECTED • LOCKED" else "OFFLINE",
                                    color = if (isOnline) Color(0xFF10B981) else Color(0xFFEF4444),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Unlock Control Panel
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Authentication Setup",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            // Authentication Selection Options
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                AuthOptionButton(
                                    title = "Fingerprint",
                                    icon = Icons.Default.Fingerprint,
                                    selected = authMethod == "FINGERPRINT",
                                    modifier = Modifier.weight(1f),
                                    onSelect = { authMethod = "FINGERPRINT" }
                                )
                                AuthOptionButton(
                                    title = "PIN Code",
                                    icon = Icons.Default.Pin,
                                    selected = authMethod == "PIN",
                                    modifier = Modifier.weight(1f),
                                    onSelect = { authMethod = "PIN" }
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Action Unlock Button
                            Button(
                                onClick = {
                                    if (authMethod == "FINGERPRINT") {
                                        if (biometricPrompt == null) {
                                            Toast.makeText(context, "Biometrics not available on this device", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        laptopViewModel.fetchUnlockChallenge(laptop) { challenge ->
                                            latestChallenge = challenge
                                            try {
                                                val signature = laptopViewModel.initSignature(laptop.id)
                                                val cryptoObject = BiometricPrompt.CryptoObject(signature)
                                                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                                    .setTitle("Authorize Laptop Unlock")
                                                    .setSubtitle("Confirm fingerprint to unlock ${laptop.name}")
                                                    .setNegativeButtonText("Cancel")
                                                    .build()
                                                biometricPrompt.authenticate(promptInfo, cryptoObject)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error initializing key: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        pinValue = ""
                                        showPinDialog = true
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                shape = RoundedCornerShape(12.dp),
                                enabled = unlockState !is NetworkResult.Loading
                            ) {
                                if (unlockState is NetworkResult.Loading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("UNLOCK LAPTOP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Security Badges Status
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SecurityBadgeRow(title = "Securely paired", icon = Icons.Default.VerifiedUser)
                        SecurityBadgeRow(title = "Device trusted", icon = Icons.Default.Security)
                        SecurityBadgeRow(title = "Encrypted connection", icon = Icons.Default.VpnKey)
                    }
                }

                // Unlock History section title
                item {
                    Text(
                        text = "Unlock History",
                        color = Color(0xFF94A3B8),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                if (unlockHistory.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No unlock logs found for this device.",
                                color = Color(0xFF64748B),
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(unlockHistory) { log ->
                        UnlockHistoryCard(log = log)
                    }
                }
            }

            // PIN Dialog Fallback
            if (showPinDialog) {
                AlertDialog(
                    onDismissRequest = { showPinDialog = false },
                    containerColor = Color(0xFF0F172A),
                    title = { Text("Enter App Unlock PIN", color = Color.White, fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("Please confirm your App Access PIN to authorize the unlock command.", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = pinValue,
                                onValueChange = { pinValue = it },
                                label = { Text("Enter PIN", color = Color(0xFF94A3B8)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                visualTransformation = PasswordVisualTransformation(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF8B5CF6),
                                    unfocusedBorderColor = Color(0xFF334155)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (pinValue.isNotEmpty()) {
                                    showPinDialog = false
                                    laptopViewModel.unlockLaptopWithPIN(laptop, pinValue)
                                } else {
                                    Toast.makeText(context, "PIN cannot be empty", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                        ) {
                            Text("Confirm & Unlock", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPinDialog = false }) {
                            Text("Cancel", color = Color(0xFF94A3B8))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AuthOptionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit
) {
    val borderColor = if (selected) Color(0xFF8B5CF6) else Color(0xFF334155)
    val bgColor = if (selected) Color(0xFF8B5CF6).copy(alpha = 0.12f) else Color(0xFF1E293B)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onSelect)
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Color(0xFF8B5CF6) else Color(0xFF94A3B8),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SecurityBadgeRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF10B981),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = Color(0xFFCBD5E1),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun UnlockHistoryCard(log: UnlockHistoryEntity) {
    val date = Date(log.timestamp)
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val timeStr = timeFormat.format(date)
    val dateStr = dateFormat.format(date)

    val isSuccess = log.result == "SUCCESS"
    val statusColor = if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444)
    val titleText = if (isSuccess) "Laptop unlocked" else "Unlock rejected"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
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
                    .background(statusColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSuccess) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titleText,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Method: ${log.method}" + if (!log.reason.isNullOrEmpty()) " • ${log.reason}" else "",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = timeStr,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )
                Text(
                    text = dateStr,
                    color = Color(0xFF64748B),
                    fontSize = 10.sp
                )
            }
        }
    }
}
