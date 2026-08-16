package com.systemmonitor.vault.authentication

import androidx.fragment.app.FragmentActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultAuthenticator @Inject constructor(
    private val pinAuthenticator: PinAuthenticator,
    private val passwordAuthenticator: PasswordAuthenticator,
    private val biometricAuthenticator: BiometricAuthenticator,
    private val sessionManager: VaultSessionManager
) {
    fun isSetup(): Boolean {
        return pinAuthenticator.isPinSetup() || passwordAuthenticator.isPasswordSetup()
    }

    fun setupPin(pin: String): Boolean {
        val success = pinAuthenticator.setupPin(pin)
        if (success) {
            sessionManager.startSession()
        }
        return success
    }

    fun authenticatePin(pin: String): AuthenticationResult {
        val result = pinAuthenticator.authenticate(pin)
        if (result is AuthenticationResult.Success) {
            sessionManager.startSession()
        }
        return result
    }

    fun authenticatePassword(password: String): AuthenticationResult {
        val result = passwordAuthenticator.authenticate(password)
        if (result is AuthenticationResult.Success) {
            sessionManager.startSession()
        }
        return result
    }

    fun authenticateBiometric(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        biometricAuthenticator.authenticate(
            activity = activity,
            onSuccess = {
                sessionManager.startSession()
                onSuccess()
            },
            onError = onError
        )
    }

    fun lockVault() {
        sessionManager.endSession()
    }

    fun isUnlocked(): Boolean {
        return sessionManager.checkSessionValid()
    }
}
