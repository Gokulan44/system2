package com.systemmonitor.applock.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.systemmonitor.applock.LockSessionManager
import com.systemmonitor.applock.manager.AppLockManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LockOverlayActivity : ComponentActivity() {

    @Inject lateinit var appLockManager: AppLockManager
    @Inject lateinit var sessionManager: LockSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val targetPackage = intent.getStringExtra("target_package") ?: ""

        setContent {
            LockOverlayScreen(
                packageName = targetPackage,
                appName = "Protected Application",
                appLockManager = appLockManager,
                onUnlockSuccess = {
                    sessionManager.grantTemporaryUnlock(targetPackage)
                    finish()
                }
            )
        }
    }

    override fun onBackPressed() {
        // Prevent back button bypass
        moveTaskToBack(true)
    }
}

@AndroidEntryPoint
class AppLockActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish()
    }
}
