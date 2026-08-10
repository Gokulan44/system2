package com.systemmonitor.applock.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemmonitor.applock.manager.AppLockManager
import com.systemmonitor.applock.security.IntrusionLogger
import com.systemmonitor.applock.security.SecurityPolicy
import kotlinx.coroutines.delay

@Composable
fun LockOverlayScreen(
    packageName: String,
    appName: String,
    appLockManager: AppLockManager,
    securityPolicy: SecurityPolicy,
    intrusionLogger: IntrusionLogger,
    onBiometricAuthenticate: () -> Unit = {},
    onUnlockSuccess: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var failedAttempts by remember { mutableStateOf(0) }
    var isLockedOut by remember { mutableStateOf(false) }
    var lockoutRemainingSeconds by remember { mutableStateOf(0) }

    fun onWrongPin() {
        failedAttempts++
        intrusionLogger.logFailedAttempt(packageName)
        if (failedAttempts >= securityPolicy.maxFailedAttempts) {
            isLockedOut = true
            lockoutRemainingSeconds = (securityPolicy.lockoutDurationMs / 1000).toInt()
            errorMessage = "Too many failed attempts. Locked for $lockoutRemainingSeconds seconds."
        } else {
            errorMessage = "Incorrect PIN. ${securityPolicy.maxFailedAttempts - failedAttempts} attempts remaining."
        }
    }

    LaunchedEffect(isLockedOut) {
        if (isLockedOut) {
            var remaining = securityPolicy.lockoutDurationMs
            while (remaining > 0) {
                delay(1000)
                remaining -= 1000
                lockoutRemainingSeconds = (remaining / 1000).toInt()
            }
            isLockedOut = false
            failedAttempts = 0
            errorMessage = null
        }
    }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF080C16),
            Color(0xFF0B132B),
            Color(0xFF070B18)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00E676).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "$appName is Protected",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Enter your PIN or use Biometric authentication to unlock",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // PIN Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..4) {
                    val isFilled = enteredPin.length >= i
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (isFilled) Color(0xFF00E676) else Color(0xFF1E293B))
                            .border(1.dp, Color(0xFF00E676), CircleShape)
                    )
                }
            }

            AnimatedVisibility(visible = errorMessage != null) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Keypad Grid
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("BIO", "0", "DEL")
            )

            keys.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    row.forEach { key ->
                        KeypadButton(
                            label = key,
                            onClick = {
                                when (key) {
                                    "DEL" -> if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                                    "BIO" -> {
                                        // Real biometric auth handled by the activity via BiometricPrompt
                                        if (!isLockedOut) onBiometricAuthenticate()
                                    }
                                    else -> {
                                        if (isLockedOut) return@KeypadButton
                                        if (enteredPin.length < 4) {
                                            enteredPin += key
                                            errorMessage = null
                                            if (enteredPin.length == 4) {
                                                if (appLockManager.verifyPasscode(enteredPin)) {
                                                    appLockManager.markSessionUnlocked(packageName)
                                                    onUnlockSuccess()
                                                } else {
                                                    enteredPin = ""
                                                    onWrongPin()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            var showForgotPinDialog by remember { mutableStateOf(false) }
            var recoveryCode by remember { mutableStateOf("") }
            var recoveryError by remember { mutableStateOf<String?>(null) }

            androidx.compose.material3.TextButton(onClick = { showForgotPinDialog = true }) {
                Text("Forgot PIN / Password?", color = Color(0xFF00E5FF), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            if (showForgotPinDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showForgotPinDialog = false },
                    containerColor = Color(0xFF0F172A),
                    title = { Text("Forgot PIN Recovery", color = Color.White, fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("Enter your 6-digit Security Recovery Code:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            androidx.compose.material3.OutlinedTextField(
                                value = recoveryCode,
                                onValueChange = { recoveryCode = it },
                                placeholder = { Text("6-digit Code (e.g. 123456)", color = Color(0xFF64748B)) },
                                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF00E5FF), unfocusedBorderColor = Color(0xFF1E293B)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (recoveryError != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(recoveryError!!, color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                // Real hashed recovery-code verification (was: any 6-digit code unlocked)
                                if (!appLockManager.hasRecoveryCode()) {
                                    recoveryError = "No recovery code has been set for this device."
                                } else if (appLockManager.verifyRecoveryCode(recoveryCode.trim())) {
                                    showForgotPinDialog = false
                                    appLockManager.markSessionUnlocked(packageName)
                                    onUnlockSuccess()
                                } else {
                                    recoveryError = "Invalid recovery code"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                        ) {
                            Text("Unlock App", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(onClick = { showForgotPinDialog = false }) {
                            Text("Cancel", color = Color(0xFF94A3B8))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun KeypadButton(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(Color(0xFF0F172A).copy(alpha = 0.85f))
            .border(1.dp, Color(0xFF1E293B), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when (label) {
            "DEL" -> Icon(imageVector = Icons.Default.Backspace, contentDescription = "Delete", tint = Color.White)
            "BIO" -> Icon(imageVector = Icons.Default.Fingerprint, contentDescription = "Biometric", tint = Color(0xFF00E676))
            else -> Text(text = label, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}
