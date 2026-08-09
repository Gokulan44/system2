package com.systemmonitor.applock.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.systemmonitor.applock.manager.AppLockManager
import com.systemmonitor.features.dashboard.DashboardViewModel
import kotlinx.coroutines.launch

import androidx.compose.material.icons.filled.Settings

@Composable
fun SelectAppsScreen(
    onBackClick: () -> Unit,
    onNavigateToChooseMethod: () -> Unit = {},
    appLockManager: AppLockManager,
    dashboardViewModel: DashboardViewModel = hiltViewModel()
) {
    val dashboardState by dashboardViewModel.uiState.collectAsState()
    val lockedAppsList by appLockManager.getLockedApps().collectAsState(initial = emptyList())
    val lockedPackageNames = remember(lockedAppsList) { lockedAppsList.map { it.packageName }.toSet() }

    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val filteredApps = remember(dashboardState.installedApps, searchQuery) {
        if (searchQuery.isBlank()) dashboardState.installedApps
        else dashboardState.installedApps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
            it.packageName.contains(searchQuery, ignoreCase = true)
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
        ) {
            // Header
            com.systemmonitor.applock.ui.components.AppLockHeader(
                title = "App Lock Protection",
                subtitle = "${lockedPackageNames.size} apps locked",
                onBackClick = onBackClick,
                trailingContent = {
                    IconButton(onClick = onNavigateToChooseMethod) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Lock Settings",
                            tint = Color(0xFF00E5FF)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Permission Warning Banner
            val context = androidx.compose.ui.platform.LocalContext.current
            val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
            var hasUsage by remember { mutableStateOf(appLockManager.hasUsageStatsPermission()) }
            var hasOverlay by remember { mutableStateOf(appLockManager.hasOverlayPermission()) }

            androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                        hasUsage = appLockManager.hasUsageStatsPermission()
                        hasOverlay = appLockManager.hasOverlayPermission()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            com.systemmonitor.applock.ui.components.AppLockPermissionBanner(
                hasUsage = hasUsage,
                hasOverlay = hasOverlay,
                onGrantUsage = { context.startActivity(appLockManager.getUsageAccessIntent()) },
                onGrantOverlay = { context.startActivity(appLockManager.getOverlayPermissionIntent()) }
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search apps to lock...", color = Color(0xFF94A3B8)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8))
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF00E676),
                    unfocusedBorderColor = Color(0xFF1E293B)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // App Toggle List
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredApps) { app ->
                    val isProtected = lockedPackageNames.contains(app.packageName)
                    com.systemmonitor.applock.ui.components.AppListItem(
                        appName = app.appName,
                        packageName = app.packageName,
                        isLocked = isProtected,
                        onToggleLock = { check ->
                            scope.launch {
                                appLockManager.setAppLocked(app.packageName, app.appName, check)
                            }
                        }
                    )
                }
            }
        }
    }
}
