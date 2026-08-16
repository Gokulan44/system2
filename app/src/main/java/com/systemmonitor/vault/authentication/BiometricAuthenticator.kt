package com.systemmonitor.vault.authentication

import android.content.Context
import androidx.biometric.BiometricManager as AndroidBiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricAuthenticator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun isBiometricAvailable(): Boolean {
        val biometricManager = AndroidBiometricManager.from(context)
        return biometricManager.canAuthenticate(
            AndroidBiometricManager.Authenticators.BIOMETRIC_STRONG or
            AndroidBiometricManager.Authenticators.BIOMETRIC_WEAK
        ) == AndroidBiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isBiometricAvailable()) {
            onError("Biometric authentication is unavailable on this device")
            return
        }

        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError("Biometric verification failed")
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Secure Vault")
            .setSubtitle("Use your biometric credential to access encrypted files")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
