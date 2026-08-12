package com.systemmonitor

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.systemmonitor.applock.manager.AppLockManager
import com.systemmonitor.features.settings.SettingsViewModel
import com.systemmonitor.navigation.MainScreenContainer
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import android.content.Intent

val LocalDarkMode = staticCompositionLocalOf { true }

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var appLockManager: AppLockManager

    private var initialRoute: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialRoute = intent?.getStringExtra("EXTRA_NAVIGATE_TO")
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val state by settingsViewModel.uiState.collectAsState()
            val screen = state.settings.screen
            val surfaceColor = if (screen.darkModeEnabled) Color(0xFF000000) else Color(0xFF080C16)

            CompositionLocalProvider(LocalDarkMode provides screen.darkModeEnabled) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = surfaceColor
                ) {
                    MainScreenContainer(appLockManager = appLockManager, initialRoute = initialRoute)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val navigateTo = intent.getStringExtra("EXTRA_NAVIGATE_TO")
        if (navigateTo != null) {
            initialRoute = navigateTo
            recreate() // Simple restart to apply new navigation route
        }
    }
}
