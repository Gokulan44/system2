package com.systemmonitor.features.profile.presentation.profile

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemmonitor.features.profile.domain.model.UserProfile
import com.systemmonitor.features.profile.domain.usecase.GetProfileUseCase
import com.systemmonitor.features.profile.domain.usecase.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val userProfile: UserProfile = UserProfile(),
    val isLoading: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        getProfileUseCase().onEach { profile ->
            _uiState.value = _uiState.value.copy(userProfile = profile)
        }.launchIn(viewModelScope)
    }

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            signOutUseCase()
            onSignedOut()
        }
    }
}

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToEditProfile: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToDevices: () -> Unit,
    onNavigateToLoginHistory: () -> Unit,
    onNavigateToActivityHistory: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToPreferences: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onSignOut: () -> Unit,
    onBackClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val profile = state.userProfile

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
                Text("My Profile & Account", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // User Header Card
            Surface(
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.85f)
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(64.dp).clip(CircleShape).background(Color(0xFF00E5FF).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(profile.fullName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(profile.email, color = Color(0xFF94A3B8), fontSize = 13.sp)
                        Text("${profile.phone} • ${profile.country}", color = Color(0xFF64748B), fontSize = 11.sp)
                    }
                    IconButton(onClick = onNavigateToEditProfile) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = Color(0xFF00E5FF))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Account & Management", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            ProfileNavigationTile("Account Security", "Password, 2FA, Biometrics & Sessions", Icons.Default.Security, Color(0xFF10B981), onNavigateToSecurity)
            Spacer(modifier = Modifier.height(10.dp))
            ProfileNavigationTile("My Devices", "Connected laptops, mobile devices & sessions", Icons.Default.Devices, Color(0xFF3B82F6), onNavigateToDevices)
            Spacer(modifier = Modifier.height(10.dp))
            ProfileNavigationTile("Login History", "Review active sessions & past IPs", Icons.Default.History, Color(0xFFF59E0B), onNavigateToLoginHistory)
            Spacer(modifier = Modifier.height(10.dp))
            ProfileNavigationTile("Activity History", "Security scan logs & app lock events", Icons.Default.FormatListBulleted, Color(0xFF8B5CF6), onNavigateToActivityHistory)
            Spacer(modifier = Modifier.height(10.dp))
            ProfileNavigationTile("Notifications", "Manage security alerts & device push notifications", Icons.Default.Notifications, Color(0xFFEC4899), onNavigateToNotifications)
            Spacer(modifier = Modifier.height(10.dp))
            ProfileNavigationTile("Privacy & Permissions", "App permissions, data collection & export", Icons.Default.Lock, Color(0xFF00E5FF), onNavigateToPrivacy)
            Spacer(modifier = Modifier.height(10.dp))
            ProfileNavigationTile("Preferences", "Theme, language, startup behavior", Icons.Default.Settings, Color(0xFF6366F1), onNavigateToPreferences)
            Spacer(modifier = Modifier.height(10.dp))
            ProfileNavigationTile("Help & Support", "FAQ, Documentation, Contact Support", Icons.Default.Help, Color(0xFF14B8A6), onNavigateToSupport)
            Spacer(modifier = Modifier.height(10.dp))
            ProfileNavigationTile("About Application", "Version 2.4.0 • Terms & Privacy Policy", Icons.Default.Info, Color(0xFF94A3B8), onNavigateToAbout)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.signOut(onSignOut) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ProfileNavigationTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp)).clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.85f)
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(subtitle, color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF64748B))
        }
    }
}
