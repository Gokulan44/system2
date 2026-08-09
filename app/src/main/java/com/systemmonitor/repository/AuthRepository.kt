package com.systemmonitor.repository

import com.systemmonitor.data.network.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class UserSession(
    val uid: String,
    val email: String,
    val displayName: String
)

@Singleton
class AuthRepository @Inject constructor() {
    private val _currentUser = MutableStateFlow<UserSession?>(
        UserSession(
            uid = "user_demo_2026",
            email = "demo.user@systemmonitor.io",
            displayName = "Admin User"
        )
    )
    val currentUser: StateFlow<UserSession?> = _currentUser

    suspend fun login(email: String, pass: String): NetworkResult<UserSession> {
        if (email.contains("@") && pass.length >= 6) {
            val session = UserSession(
                uid = "uid_${email.hashCode()}",
                email = email,
                displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
            )
            _currentUser.value = session
            return NetworkResult.Success(session)
        }
        return NetworkResult.Error("Invalid credentials or password too short")
    }

    suspend fun register(name: String, email: String, pass: String): NetworkResult<UserSession> {
        if (email.contains("@") && pass.length >= 6) {
            val session = UserSession(
                uid = "uid_${email.hashCode()}",
                email = email,
                displayName = name.ifEmpty { "User" }
            )
            _currentUser.value = session
            return NetworkResult.Success(session)
        }
        return NetworkResult.Error("Registration failed. Check email format and password length.")
    }

    fun logout() {
        _currentUser.value = null
    }
}
