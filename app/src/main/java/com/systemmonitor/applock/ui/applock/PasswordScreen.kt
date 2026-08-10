package com.systemmonitor.applock.ui.applock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemmonitor.applock.authentication.PasswordManager
import kotlinx.coroutines.delay

@Composable
fun PasswordScreen(
    passwordManager: PasswordManager,
    onPasswordSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var lockoutSeconds by remember { mutableStateOf(0) }
    var isLockedOut by remember { mutableStateOf(false) }

    LaunchedEffect(isLockedOut) {
        val remainingMs = passwordManager.getLockoutTimeRemaining()
        if (remainingMs > 0) {
            isLockedOut = true
            var seconds = (remainingMs / 1000).toInt().coerceAtLeast(1)
            lockoutSeconds = seconds
            errorMessage = "Too many failed attempts. Locked for $lockoutSeconds seconds."
            while (seconds > 0) {
                delay(1000)
                seconds--
                lockoutSeconds = seconds
                if (seconds > 0) {
                    errorMessage = "Too many failed attempts. Locked for $lockoutSeconds seconds."
                } else {
                    errorMessage = null
                    isLockedOut = false
                }
            }
        }
    }

    val bgGradient = Brush.verticalGradient(colors = listOf(Color(0xFF080C16), Color(0xFF0B132B), Color(0xFF070B18)))
    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
                Text("Enter Password", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(40.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    errorMessage = null
                },
                enabled = !isLockedOut,
                label = { Text("Password", color = Color(0xFF94A3B8)) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(icon, contentDescription = null, tint = Color(0xFF94A3B8))
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = if (errorMessage != null) Color(0xFFEF4444) else Color(0xFF3B82F6),
                    unfocusedBorderColor = if (errorMessage != null) Color(0xFFEF4444) else Color(0xFF1E293B)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = Color(0xFFEF4444),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    when (val result = passwordManager.verifyPassword(password)) {
                        is com.systemmonitor.applock.authentication.AuthenticationResult.Success -> {
                            onPasswordSuccess()
                        }
                        is com.systemmonitor.applock.authentication.AuthenticationResult.Lockout -> {
                            isLockedOut = true
                            password = ""
                        }
                        else -> {
                            errorMessage = result.getErrorMessage() ?: "Incorrect Password"
                            password = ""
                        }
                    }
                },
                enabled = !isLockedOut,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Unlock App", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
