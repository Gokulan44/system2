package com.systemmonitor.repository

import com.systemmonitor.data.network.NetworkResult
import com.systemmonitor.firebase.FirebaseAuthManager
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
class AuthRepository @Inject constructor(
    private val firebaseAuthManager: FirebaseAuthManager
) {
    private val _currentUser = MutableStateFlow<UserSession?>(firebaseAuthManager.currentUser)
    val currentUser: StateFlow<UserSession?> = _currentUser

    suspend fun login(email: String, pass: String): NetworkResult<UserSession> {
        val result = firebaseAuthManager.login(email, pass)
        if (result is NetworkResult.Success) {
            _currentUser.value = result.data
        }
        return result
    }

    suspend fun register(name: String, email: String, pass: String): NetworkResult<UserSession> {
        val result = firebaseAuthManager.register(name, email, pass)
        if (result is NetworkResult.Success) {
            _currentUser.value = result.data
        }
        return result
    }

    fun logout() {
        firebaseAuthManager.logout()
        _currentUser.value = null
    }
}
