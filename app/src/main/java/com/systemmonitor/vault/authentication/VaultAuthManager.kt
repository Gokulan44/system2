package com.systemmonitor.vault.authentication

import androidx.fragment.app.FragmentActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultAuthManager @Inject constructor(
    private val pinManager: VaultPinManager,
    private val passwordManager: VaultPasswordManager,
    private val biometricManager: VaultBiometricManager,
    private val patternManager: VaultPatternManager,
    private val lockManager: VaultLockManager,
    private val wipeManager: VaultWipeManager
) {
    fun isSetup(): Boolean {
        return pinManager.isPinSetup() || passwordManager.isPasswordSetup() || patternManager.isPatternSetup()
    }

    fun setupPin(pin: String): Boolean {
        val success = pinManager.setupPin(pin)
        if (success) {
            lockManager.startSession()
        }
        return success
    }

    suspend fun authenticatePin(pin: String): AuthenticationResult {
        val result = pinManager.authenticate(pin)
        if (result is AuthenticationResult.Success) {
            lockManager.startSession()
        }
        return result
    }

    suspend fun authenticatePassword(password: String): AuthenticationResult {
        val result = passwordManager.authenticate(password)
        if (result is AuthenticationResult.Success) {
            lockManager.startSession()
        }
        return result
    }

    fun authenticateBiometric(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        biometricManager.authenticate(
            activity = activity,
            onSuccess = {
                lockManager.startSession()
                onSuccess()
            },
            onError = onError
        )
    }

    fun lockVault() {
        lockManager.endSession()
    }

    fun isUnlocked(): Boolean {
        return lockManager.checkSessionValid()
    }

    /**
     * Manual panic wipe — wire this to a clearly-labeled, double-confirmed
     * "Wipe Vault" button in Settings. This is the same irreversible
     * operation the auto-wipe-after-repeated-failures path triggers, just
     * invoked directly by the user instead of by LockoutManager.
     *
     * Always ends the current session afterward, since there is nothing
     * left to have a valid session over.
     */
    suspend fun wipeVault() {
        wipeManager.wipeVault()
        lockManager.endSession()
    }
}