package com.systemmonitor.applock.ui.applock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.systemmonitor.applock.manager.AppLockManager
import com.systemmonitor.features.dashboard.DashboardViewModel
import kotlinx.coroutines.launch

@Composable
fun AddAppsScreen(
    appLockManager: AppLockManager,
    onBackClick: () -> Unit,
    dashboardViewModel: DashboardViewModel = hiltViewModel()
) {
    val dashboardState by dashboardViewModel.uiState.collectAsState()
    val lockedAppsList by appLockManager.getLockedApps().collectAsState(initial = emptyList())
    val lockedPackageNames = remember(lockedAppsList) { lockedAppsList.map { it.packageName }.toSet() }

    val scope = rememberCoroutineScope()

    // Filter to show only apps that are NOT locked yet
    val availableApps = remember(dashboardState.installedApps, lockedPackageNames) {
        dashboardState.installedApps.filter { !lockedPackageNames.contains(it.packageName) }
    }

    val bgGradient = Brush.verticalGradient(colors = listOf(Color(0xFF080C16), Color(0xFF0B132B), Color(0xFF070B18)))
    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) { 
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) 
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Applications", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(18.dp))

            Text("Available Applications (${availableApps.size})", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            if (availableApps.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("All apps are locked", color = Color(0xFF64748B), fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(availableApps) { app ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0F172A).copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(app.appName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(app.packageName, color = Color(0xFF64748B), fontSize = 10.sp)
                                }
                                IconButton(onClick = {
                                    scope.launch {
                                        appLockManager.setAppLocked(app.packageName, app.appName, true)
                                    }
                                }) {
                                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFF00E5FF))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
