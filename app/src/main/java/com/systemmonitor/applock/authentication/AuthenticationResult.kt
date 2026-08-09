package com.systemmonitor.applock.authentication

sealed class AuthenticationResult {
    object Success : AuthenticationResult()
    data class Failed(val attemptsRemaining: Int = 3, val isLockedOut: Boolean = false) : AuthenticationResult()
    data class Lockout(val lockoutSeconds: Int = 30) : AuthenticationResult()
    object ForgotPinTriggered : AuthenticationResult()
    object BiometricNotAvailable : AuthenticationResult()
    object Cancelled : AuthenticationResult()

    fun isSuccessful(): Boolean = this is Success

    fun getErrorMessage(): String? = when (this) {
        is Success -> null
        is Failed -> if (isLockedOut) "Too many failed attempts. Try again later." else "Incorrect passcode ($attemptsRemaining attempts left)"
        is Lockout -> "Device locked for $lockoutSeconds seconds due to failed attempts"
        is ForgotPinTriggered -> "Security recovery code required"
        is BiometricNotAvailable -> "Biometric hardware not available or enrolled"
        is Cancelled -> "Authentication cancelled"
    }
}
