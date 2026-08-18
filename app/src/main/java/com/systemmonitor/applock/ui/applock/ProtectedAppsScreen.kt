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
import androidx.compose.material.icons.filled.Lock
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
import com.systemmonitor.applock.manager.AppLockManager
import kotlinx.coroutines.launch
import com.systemmonitor.applock.ui.applock.components.AppIcon

@Composable
fun ProtectedAppsScreen(
    appLockManager: AppLockManager,
    onAddAppsClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val lockedApps by appLockManager.getLockedApps().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    val bgGradient = Brush.verticalGradient(colors = listOf(Color(0xFF080C16), Color(0xFF0B132B), Color(0xFF070B18)))
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAppsClick,
                containerColor = Color(0xFF00E5FF),
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Apps")
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
                .padding(innerPadding)
        ) {
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
                    Text("Protected Applications", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(18.dp))
                
                Text("Currently Locked Apps (${lockedApps.size})", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                if (lockedApps.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No apps currently locked", color = Color(0xFF64748B), fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(lockedApps) { app ->
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
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AppIcon(
                                            packageName = app.packageName,
                                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp))
                                        )
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column {
                                            Text(app.appName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text(app.packageName, color = Color(0xFF64748B), fontSize = 10.sp)
                                        }
                                    }
                                    IconButton(onClick = {
                                        scope.launch {
                                            appLockManager.setAppLocked(app.packageName, app.appName, false)
                                        }
                                    }) {
                                        Icon(Icons.Default.Lock, contentDescription = "Unlock", tint = Color(0xFFEF4444))
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
