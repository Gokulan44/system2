package com.systemmonitor.applock.ui.applock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GridOn
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
import com.systemmonitor.applock.authentication.PatternManager
import kotlinx.coroutines.delay
import com.systemmonitor.applock.security.IntrusionLogger

@Composable
fun PatternScreen(
    patternManager: PatternManager,
    onPatternSuccess: () -> Unit,
    onBackClick: () -> Unit,
    intrusionLogger: IntrusionLogger? = null,
    packageName: String = ""
) {
    val selectedDots = remember { mutableStateListOf<Int>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var lockoutSeconds by remember { mutableStateOf(0) }
    var isLockedOut by remember { mutableStateOf(false) }

    LaunchedEffect(isLockedOut) {
        val remainingMs = patternManager.getLockoutTimeRemaining()
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
                Text("Draw Pattern", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            // 3x3 Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                                    .background(if (isSelected) Color(0xFF8B5CF6).copy(alpha = 0.25f) else Color(0xFF0F172A))
                                    .clickable(enabled = !isLockedOut) {
                                        if (!selectedDots.contains(dotIndex)) {
                                            selectedDots.add(dotIndex)
                                            errorMessage = null
                                        }
                                    }
                                    .border(2.dp, if (isSelected) Color(0xFF8B5CF6) else Color(0xFF1E293B), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color(0xFF8B5CF6) else Color(0xFF64748B))
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Sequence: ${selectedDots.joinToString(" → ")}", color = Color(0xFF94A3B8), fontSize = 12.sp)
            
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMessage!!, color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { selectedDots.clear(); errorMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text("Clear", color = Color.White)
                }

                Button(
                    onClick = {
                        when (val result = patternManager.verifyPattern(selectedDots.toList())) {
                            is com.systemmonitor.applock.authentication.AuthenticationResult.Success -> {
                                onPatternSuccess()
                            }
                            is com.systemmonitor.applock.authentication.AuthenticationResult.Lockout -> {
                                isLockedOut = true
                                selectedDots.clear()
                                intrusionLogger?.logFailedAttempt(packageName)
                            }
                            else -> {
                                errorMessage = result.getErrorMessage() ?: "Incorrect Pattern"
                                selectedDots.clear()
                                intrusionLogger?.logFailedAttempt(packageName)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text("Unlock", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
