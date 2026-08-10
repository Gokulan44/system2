package com.systemmonitor.applock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemmonitor.applock.manager.AppLockManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetPinScreen(
    appLockManager: AppLockManager,
    lockMethodName: String = "PIN",
    onPinSetSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    var isConfirmed by remember { mutableStateOf(false) }
    var recoveryCode by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // PIN state
    var pin by remember { mutableStateOf("") }

    // Pattern state
    var isConfirmingPattern by remember { mutableStateOf(false) }
    val selectedDots = remember { mutableStateListOf<Int>() }
    val firstPattern = remember { mutableStateListOf<Int>() }

    // Password state
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isPattern = lockMethodName.contains("Pattern", ignoreCase = true)
    val isPassword = lockMethodName.contains("Password", ignoreCase = true)
    val isBiometric = lockMethodName.contains("Biometric", ignoreCase = true)

    fun onSetupComplete(passcode: String) {
        val saveType = if (isBiometric) "Biometric Lock" else lockMethodName
        appLockManager.setLockPasscode(passcode, saveType)
        recoveryCode = if (appLockManager.hasRecoveryCode()) null else appLockManager.generateRecoveryCode()
        isConfirmed = true
    }

    fun onPinDigit(digit: String) {
        if (pin.length < 4) {
            pin += digit
            if (pin.length == 4) {
                onSetupComplete(pin)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set Up $lockMethodName", color = Color.White, fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (isConfirmed) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("App Lock Enabled", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Lock Method: $lockMethodName • Auto Lock Enabled",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                    if (recoveryCode != null) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "YOUR RECOVERY CODE",
                                    color = Color(0xFFF59E0B),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    recoveryCode ?: "",
                                    color = Color.White,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 6.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Write this down. It is the only way to unlock apps if you forget your passcode.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onPinSetSuccess,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Finish Setup", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                if (isPattern) {
                    // Pattern setup layout
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.GridOn, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isConfirmingPattern) "Redraw Pattern to Confirm" else "Draw a Pattern (Min 4 dots)",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // 3x3 Grid
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            for (row in 0 until 3) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                                    modifier = Modifier.padding(vertical = 12.dp)
                                ) {
                                    for (col in 0 until 3) {
                                        val dotIndex = row * 3 + col
                                        val isSelected = selectedDots.contains(dotIndex)
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color(0xFF8B5CF6).copy(alpha = 0.25f) else Color(0xFF1E293B))
                                                .clickable {
                                                    if (!selectedDots.contains(dotIndex)) {
                                                        selectedDots.add(dotIndex)
                                                        errorMessage = null
                                                    }
                                                }
                                                .border(2.dp, if (isSelected) Color(0xFF8B5CF6) else Color(0xFF475569), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) Color(0xFF8B5CF6) else Color(0xFF94A3B8))
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        if (errorMessage != null) {
                            Text(errorMessage!!, color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(
                                onClick = {
                                    selectedDots.clear()
                                    firstPattern.clear()
                                    isConfirmingPattern = false
                                    errorMessage = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reset")
                            }

                            Button(
                                onClick = {
                                    if (!isConfirmingPattern) {
                                        if (selectedDots.size < 4) {
                                            errorMessage = "Pattern must be at least 4 dots"
                                        } else {
                                            firstPattern.addAll(selectedDots)
                                            selectedDots.clear()
                                            isConfirmingPattern = true
                                        }
                                    } else {
                                        if (selectedDots.toList() == firstPattern.toList()) {
                                            onSetupComplete(selectedDots.joinToString("-"))
                                        } else {
                                            errorMessage = "Patterns do not match. Restarting setup."
                                            selectedDots.clear()
                                            firstPattern.clear()
                                            isConfirmingPattern = false
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isConfirmingPattern) "Confirm" else "Next")
                            }
                        }
                    }
                } else if (isPassword) {
                    // Password setup layout
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Password, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Create a Password", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it; errorMessage = null },
                            label = { Text("Enter Password (Min 4 chars)", color = Color(0xFF94A3B8)) },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(icon, contentDescription = null, tint = Color(0xFF94A3B8))
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF3B82F6), unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = confirmPasswordInput,
                            onValueChange = { confirmPasswordInput = it; errorMessage = null },
                            label = { Text("Confirm Password", color = Color(0xFF94A3B8)) },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF3B82F6), unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        if (errorMessage != null) {
                            Text(errorMessage!!, color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Button(
                            onClick = {
                                if (passwordInput.length < 4) {
                                    errorMessage = "Password must be at least 4 characters"
                                } else if (passwordInput != confirmPasswordInput) {
                                    errorMessage = "Passwords do not match"
                                } else {
                                    onSetupComplete(passwordInput)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text("Confirm Password", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // PIN or Biometric setup layout
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isBiometric) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Biometric Authentication Selected", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Configure backup PIN below.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(18.dp))
                        } else {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Enter 4-Digit Security PIN", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // PIN Dots Indicator
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            for (i in 0 until 4) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(
                                            if (i < pin.length) Color(0xFF00E5FF) else Color(0xFF334155),
                                            shape = CircleShape
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        // Keypad Matrix
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val keypad = listOf(
                                listOf("1", "2", "3"),
                                listOf("4", "5", "6"),
                                listOf("7", "8", "9"),
                                listOf("", "0", "DEL")
                            )

                            keypad.forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    row.forEach { key ->
                                        if (key.isEmpty()) {
                                            Spacer(modifier = Modifier.size(70.dp))
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(70.dp)
                                                    .background(Color(0xFF1E293B), shape = CircleShape)
                                                    .clickable {
                                                        if (key == "DEL") {
                                                            if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                                        } else {
                                                            onPinDigit(key)
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (key == "DEL") {
                                                    Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = Color.White)
                                                } else {
                                                    Text(key, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
