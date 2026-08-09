package com.systemmonitor.features.profile.presentation.security

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhonelinkSetup
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AccountSecurityViewModel @Inject constructor() : ViewModel()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSecurityScreen(
    viewModel: AccountSecurityViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0: Overview, 1: Change Password, 2: 2FA & Biometrics

    // Passwords & Toggles
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordChangeSuccess by remember { mutableStateOf(false) }

    var biometricEnabled by remember { mutableStateOf(true) }
    var twoFactorEnabled by remember { mutableStateOf(false) }
    var loginAlertsEnabled by remember { mutableStateOf(true) }

    val bgGradient = Brush.verticalGradient(colors = listOf(Color(0xFF080C16), Color(0xFF0B132B), Color(0xFF070B18)))

    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Account Security", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Manage credentials, 2FA & biometric authentication", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.Transparent,
                contentColor = Color(0xFF00E5FF),
                edgePadding = 0.dp
            ) {
                Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) {
                    Text("Security Overview", modifier = Modifier.padding(12.dp), color = if (activeTab == 0) Color(0xFF00E5FF) else Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                }
                Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) {
                    Text("Change Password", modifier = Modifier.padding(12.dp), color = if (activeTab == 1) Color(0xFF00E5FF) else Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                }
                Tab(selected = activeTab == 2, onClick = { activeTab = 2 }) {
                    Text("2FA & Biometrics", modifier = Modifier.padding(12.dp), color = if (activeTab == 2) Color(0xFF00E5FF) else Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (activeTab) {
                0 -> {
                    // Security Overview
                    SecurityToggleTile("Biometric Authentication", "Use Fingerprint / Face ID to unlock app", biometricEnabled) { biometricEnabled = it }
                    Spacer(modifier = Modifier.height(10.dp))
                    SecurityToggleTile("Two-Factor Authentication (2FA)", "Require authenticator OTP code on login", twoFactorEnabled) { twoFactorEnabled = it }
                    Spacer(modifier = Modifier.height(10.dp))
                    SecurityToggleTile("New Login Alerts", "Receive instant security push alerts on new login", loginAlertsEnabled) { loginAlertsEnabled = it }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Active Security Sessions", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    ActiveSessionTile("This Device (Samsung Galaxy S24)", "IP: 192.168.1.42 • New York, USA", "Active Now", true)
                    Spacer(modifier = Modifier.height(8.dp))
                    ActiveSessionTile("Work Laptop (Dell XPS 15)", "IP: 192.168.1.108 • New York, USA", "2 hours ago", false)
                }
                1 -> {
                    // Change Password Form
                    Text("Change Account Password", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("Current Password", color = Color(0xFF94A3B8)) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFF00E5FF), unfocusedBorderColor = Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password", color = Color(0xFF94A3B8)) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFF00E5FF), unfocusedBorderColor = Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm New Password", color = Color(0xFF94A3B8)) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFF00E5FF), unfocusedBorderColor = Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (passwordChangeSuccess) {
                        Text("✅ Password updated successfully!", color = Color(0xFF10B981), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Button(
                        onClick = {
                            if (newPassword.isNotEmpty() && newPassword == confirmPassword) {
                                passwordChangeSuccess = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Update Password", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
                2 -> {
                    // 2FA & Biometric Setup
                    Text("Biometric Authentication Settings", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    SecurityToggleTile("Enable Biometric Lock", "Use Fingerprint / Face ID for App Lock & Login", biometricEnabled) { biometricEnabled = it }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Two-Factor Authentication (2FA)", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    SecurityToggleTile("Enable Authenticator App (TOTP)", "Scan QR Code with Google Authenticator", twoFactorEnabled) { twoFactorEnabled = it }

                    if (twoFactorEnabled) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF0F172A).copy(alpha = 0.85f)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Authenticator Secret Key", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("JBSWY3DPEHPK3PXP", color = Color(0xFF00E5FF), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("Enter this secret key in your Authenticator app", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityToggleTile(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.85f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun ActiveSessionTile(name: String, details: String, time: String, isCurrent: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.85f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    if (isCurrent) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("(Current)", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(details, color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
            Text(time, color = Color(0xFF64748B), fontSize = 11.sp)
        }
    }
}
