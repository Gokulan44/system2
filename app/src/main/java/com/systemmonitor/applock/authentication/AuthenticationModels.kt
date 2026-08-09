package com.systemmonitor.applock.authentication

import com.systemmonitor.applock.model.LockMethod

sealed class AuthenticationResult {
    object Success : AuthenticationResult()
    data class Failed(val attemptsRemaining: Int, val isLockedOut: Boolean = false) : AuthenticationResult()
    object ForgotPinTriggered : AuthenticationResult()
    object BiometricNotAvailable : AuthenticationResult()
}

data class AuthenticationAttempt(
    val timestamp: Long = System.currentTimeMillis(),
    val packageName: String,
    val result: String,
    val authenticationMethod: LockMethod,
    val attemptCount: Int
)
