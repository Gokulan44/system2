package com.systemmonitor.vault.authentication

sealed class AuthenticationResult {
    object Success : AuthenticationResult()
    data class InvalidCredentials(val remainingAttempts: Int) : AuthenticationResult()
    data class LockedOut(val cooldownMs: Long) : AuthenticationResult()
    data class Error(val message: String) : AuthenticationResult()
}
