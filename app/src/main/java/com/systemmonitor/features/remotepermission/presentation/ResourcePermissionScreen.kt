package com.systemmonitor.features.remotepermission.presentation

import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.systemmonitor.data.network.NetworkResult
import com.systemmonitor.features.remotepermission.domain.model.PermissionRequest
import com.systemmonitor.features.remotepermission.domain.model.PermissionType
import java.security.Signature
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourcePermissionScreen(
    viewModel: PermissionViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val activeRequest by viewModel.activeRequest.collectAsState()
    val actionState by viewModel.actionState.collectAsState()

    var showPinPrompt by remember { mutableStateOf(false) }
    var pinValue by remember { mutableStateOf("") }
    
    val executor = remember(context) { ContextCompat.getMainExecutor(context) }
    var authMethod by remember { mutableStateOf("BIOMETRIC") } // BIOMETRIC or PIN
    
    // Set up BiometricPrompt
    val biometricPrompt = remember(executor, activity) {
        if (activity != null) {
            BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    Toast.makeText(context, "Verification error: $errString", Toast.LENGTH_SHORT).show()
                    activeRequest?.let { viewModel.logVerificationFailure(it.requestId, "BIOMETRIC") }
                    viewModel.clearActionState()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    val signature = result.cryptoObject?.signature
                    val request = activeRequest
                    if (signature != null && request != null) {
                        viewModel.approveRequest(request, signature, "BIOMETRIC")
                    } else {
                        Toast.makeText(context, "Signature initialization failed", Toast.LENGTH_SHORT).show()
                        viewModel.clearActionState()
                    }
                }

                override fun onAuthenticationFailed() {
                    Toast.makeText(context, "Biometric verification failed", Toast.LENGTH_SHORT).show()
                    activeRequest?.let { viewModel.logVerificationFailure(it.requestId, "BIOMETRIC") }
                    viewModel.clearActionState()
                }
            })
        } else null
    }

    LaunchedEffect(actionState) {
        when (actionState) {
            is NetworkResult.Success -> {
                Toast.makeText(context, "Permission approved and token sent!", Toast.LENGTH_LONG).show()
                viewModel.clearActionState()
            }
            is NetworkResult.Error -> {
                Toast.makeText(context, "Error: ${(actionState as NetworkResult.Error).message}", Toast.LENGTH_LONG).show()
                viewModel.clearActionState()
            }
            else -> {}
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
                title = { Text("Resource Permission", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "History", tint = Color.White)
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
            if (activeRequest == null) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Pending Requests",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "All resource and download access requests have been processed.",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            viewModel.simulateRequest(
                                laptopId = "LAPTOP-7F29A1",
                                resourceName = "Security-Lab.pdf"
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simulate Laptop Request", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Request card view
                val request = activeRequest!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.8f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE2E8F0).copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = Color(0xFF00E5FF)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Permission Request",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "Laptop demands verification",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(color = Color(0xFF1E293B))
                            Spacer(modifier = Modifier.height(16.dp))

                            // Detail Rows
                            DetailField(label = "Laptop", value = request.laptopId, icon = Icons.Default.Laptop)
                            Spacer(modifier = Modifier.height(12.dp))
                            DetailField(label = "Resource", value = request.resource.name, icon = Icons.Default.Description)
                            Spacer(modifier = Modifier.height(12.dp))
                            DetailField(
                                label = "Size",
                                value = formatSize(request.resource.sizeBytes),
                                icon = Icons.Default.SdCard
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            DetailField(
                                label = "Operation",
                                value = request.requestedOperation.name,
                                icon = Icons.Default.Build
                            )

                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Expires: " + SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date(request.expiresAt)),
                                color = Color(0xFFEF4444),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End
                            )
                        }
                    }

                    // Selector for verification method
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val methods = listOf("BIOMETRIC", "PIN")
                        methods.forEach { method ->
                            val selected = authMethod == method
                            val btnBg = if (selected) Color(0xFF00E5FF) else Color.Transparent
                            val btnFg = if (selected) Color.Black else Color.White
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(btnBg)
                                    .clickable { authMethod = method }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (method == "BIOMETRIC") "Biometric (Fingerprint)" else "App PIN",
                                    color = btnFg,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { viewModel.denyRequest(request) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Text("DENY", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (authMethod == "BIOMETRIC") {
                                    if (biometricPrompt == null) {
                                        Toast.makeText(context, "Biometrics not available on this device", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    try {
                                        val signature = viewModel.initSignatureForLaptop(request.laptopId)
                                        val cryptoObject = BiometricPrompt.CryptoObject(signature)
                                        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                            .setTitle("Verify Identity")
                                            .setSubtitle("Confirm auth to approve download request")
                                            .setNegativeButtonText("Cancel")
                                            .build()
                                        biometricPrompt.authenticate(promptInfo, cryptoObject)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Biometric error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    showPinPrompt = true
                                }
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (actionState is NetworkResult.Loading) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color.Black)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ALLOW", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Simulated PIN overlay dialog for testing
                    if (showPinPrompt) {
                        AlertDialog(
                            onDismissRequest = { showPinPrompt = false },
                            title = { Text("Enter App PIN") },
                            text = {
                                OutlinedTextField(
                                    value = pinValue,
                                    onValueChange = { pinValue = it },
                                    label = { Text("PIN") },
                                    singleLine = true
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showPinPrompt = false
                                        try {
                                            val signature = viewModel.initSignatureForLaptop(request.laptopId, useBiometric = false)
                                            viewModel.approveRequest(request, signature, "PIN")
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "PIN Auth Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Text("Approve")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showPinPrompt = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailField(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF64748B),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun formatSize(bytes: Long): String {
    val df = DecimalFormat("#.##")
    val sizeInMb = bytes.toDouble() / (1024 * 1024)
    return df.format(sizeInMb) + " MB"
}
