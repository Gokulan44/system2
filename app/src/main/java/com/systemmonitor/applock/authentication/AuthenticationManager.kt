package com.systemmonitor.applock.authentication

import com.systemmonitor.applock.model.LockMethod
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationManager @Inject constructor(
    val pinManager: PinManager,
    val patternManager: PatternManager,
    val passwordManager: PasswordManager,
    val biometricManager: BiometricManager
) {
    fun verify(method: LockMethod, input: String): AuthenticationResult {
        return when (method) {
            LockMethod.PIN -> pinManager.verifyPin(input)
            LockMethod.PASSWORD -> passwordManager.verifyPassword(input)
            LockMethod.PATTERN -> {
                val points = input.split("-").mapNotNull { it.toIntOrNull() }
                patternManager.verifyPattern(points)
            }
            LockMethod.BIOMETRIC -> {
                if (biometricManager.isBiometricAvailable()) AuthenticationResult.Success
                else AuthenticationResult.BiometricNotAvailable
            }
        }
    }

    fun isSecuritySet(): Boolean {
        return pinManager.isPinSet() || patternManager.isPatternSet() || passwordManager.isPasswordSet()
    }
}
