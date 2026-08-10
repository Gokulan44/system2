package com.systemmonitor.applock.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.systemmonitor.applock.LockSessionManager
import com.systemmonitor.applock.manager.AppLockManager
import com.systemmonitor.applock.security.IntrusionLogger
import com.systemmonitor.applock.security.SecurityPolicy
import com.systemmonitor.applock.model.LockMethod
import com.systemmonitor.applock.ui.applock.PinScreen
import com.systemmonitor.applock.ui.applock.PatternScreen
import com.systemmonitor.applock.ui.applock.PasswordScreen
import com.systemmonitor.applock.ui.applock.BiometricScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.compose.runtime.remember

@AndroidEntryPoint
class LockOverlayActivity : FragmentActivity() {

    @Inject lateinit var appLockManager: AppLockManager
    @Inject lateinit var sessionManager: LockSessionManager
    @Inject lateinit var securityPolicy: SecurityPolicy
    @Inject lateinit var intrusionLogger: IntrusionLogger
    @Inject lateinit var preferences: com.systemmonitor.applock.settings.AppLockPreferences

    private lateinit var targetPackage: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        targetPackage = intent.getStringExtra("target_package") ?: ""
        val appName = resolveAppName(targetPackage)

        setContent {
            val lockMethod = remember {
                val method = appLockManager.getLockMethod()
                try {
                    LockMethod.valueOf(method.uppercase().replace(" LOCK", ""))
                } catch (e: Exception) {
                    LockMethod.PIN
                }
            }

            when (lockMethod) {
                LockMethod.PIN -> {
                    PinScreen(
                        pinManager = appLockManager.pinManager,
                        onPinSuccess = {
                            sessionManager.grantTemporaryUnlock(targetPackage)
                            finish()
                        },
                        onBackClick = { moveTaskToBack(true) }
                    )
                }
                LockMethod.PATTERN -> {
                    PatternScreen(
                        patternManager = appLockManager.patternManager,
                        onPatternSuccess = {
                            sessionManager.grantTemporaryUnlock(targetPackage)
                            finish()
                        },
                        onBackClick = { moveTaskToBack(true) }
                    )
                }
                LockMethod.PASSWORD -> {
                    PasswordScreen(
                        passwordManager = appLockManager.passwordManager,
                        onPasswordSuccess = {
                            sessionManager.grantTemporaryUnlock(targetPackage)
                            finish()
                        },
                        onBackClick = { moveTaskToBack(true) }
                    )
                }
                LockMethod.BIOMETRIC -> {
                    BiometricScreen(
                        onBiometricTrigger = { launchBiometricPrompt(appName) },
                        onBackClick = { moveTaskToBack(true) }
                    )
                }
            }
        }

        if (preferences.getSettings().biometricEnabled) {
            launchBiometricPrompt(appName)
        }
    }

    private fun resolveAppName(packageName: String): String {
        if (packageName.isEmpty()) return "Protected Application"
        return runCatching {
            val info = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(info).toString()
        }.getOrDefault("Protected Application")
    }

    private fun launchBiometricPrompt(appName: String) {
        val biometricManager = BiometricManager.from(this)
        val canAuth = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        )
        if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
            val prompt = BiometricPrompt(
                this,
                ContextCompat.getMainExecutor(this),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        appLockManager.markSessionUnlocked(targetPackage)
                        sessionManager.grantTemporaryUnlock(targetPackage)
                        finish()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        // User cancelled or hardware error — fall through to PIN entry.
                    }
                }
            )
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock $appName")
                .setSubtitle("Authenticate to open this protected app")
                .setNegativeButtonText("Cancel")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG
                )
                .build()
            prompt.authenticate(promptInfo)
        } else {
            // Biometric unavailable — the overlay screen must not silently unlock.
            // Nothing to do: the user continues with PIN entry.
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Prevent back button bypass
        moveTaskToBack(true)
    }
}