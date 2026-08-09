package com.systemmonitor.features.settings

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SettingsCategoryItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToCategory: (String) -> Unit,
    onBackClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    val categories = listOf(
        SettingsCategoryItem("Account", "Profile, Security, Login History", Icons.Default.Person, Color(0xFF3B82F6), "settings_account"),
        SettingsCategoryItem("Device", "Information, Name, Permissions", Icons.Default.PhoneAndroid, Color(0xFF00E5FF), "settings_device"),
        SettingsCategoryItem("Security", "Device Security, App Lock, Biometric, Malware", Icons.Default.Shield, Color(0xFF10B981), "settings_security"),
        SettingsCategoryItem("Monitoring", "CPU, RAM, Storage, Battery, Network, Process", Icons.Default.Speed, Color(0xFF8B5CF6), "settings_monitoring"),
        SettingsCategoryItem("Notifications", "Master, Security, Battery, Storage Alerts", Icons.Default.Notifications, Color(0xFFF59E0B), "settings_notifications"),
        SettingsCategoryItem("Power", "Battery Health, Power Saving, Remote Actions", Icons.Default.BatteryChargingFull, Color(0xFFEF4444), "settings_power"),
        SettingsCategoryItem("Screen", "Brightness, Dark Mode, Timeout", Icons.Default.Tv, Color(0xFFEC4899), "settings_screen"),
        SettingsCategoryItem("Remote Control", "Laptop Connection, Remote Actions", Icons.Default.Laptop, Color(0xFF00E5FF), "settings_remote"),
        SettingsCategoryItem("Privacy & Permissions", "App Permissions, Usage Access, Admin", Icons.Default.PrivacyTip, Color(0xFF10B981), "settings_privacy"),
        SettingsCategoryItem("Reports", "Security, Health, Export PDF/CSV", Icons.Default.Assessment, Color(0xFF8B5CF6), "settings_reports"),
        SettingsCategoryItem("Backup & Sync", "Local Backup, Cloud Sync, Restore", Icons.Default.CloudSync, Color(0xFF3B82F6), "settings_backup"),
        SettingsCategoryItem("Advanced & About", "Background Service, App Version, Licenses", Icons.Default.Settings, Color(0xFF94A3B8), "settings_advanced")
    )

    val filteredCategories = remember(searchQuery) {
        if (searchQuery.isBlank()) categories
        else categories.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
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
            .background(bgGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "System Settings",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Configure Security, Monitoring & Preferences",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search settings...", color = Color(0xFF94A3B8)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF00E5FF),
                    unfocusedBorderColor = Color(0xFF1E293B)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Category List
            filteredCategories.forEach { category ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
                        .clickable { onNavigateToCategory(category.route) },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.85f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(category.iconColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = category.title,
                                    tint = category.iconColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = category.title,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = category.description,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color(0xFF64748B)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
