package com.systemmonitor.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.systemmonitor.data.network.NetworkResult
import com.systemmonitor.repository.UserSession
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthManager @Inject constructor(
    private val auth: FirebaseAuth
) {
    val currentUser: UserSession?
        get() = auth.currentUser?.let { user ->
            UserSession(
                uid = user.uid,
                email = user.email ?: "",
                displayName = user.displayName ?: ""
            )
        }

    suspend fun login(email: String, pass: String): NetworkResult<UserSession> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val user = result.user
            if (user != null) {
                NetworkResult.Success(
                    UserSession(
                        uid = user.uid,
                        email = user.email ?: "",
                        displayName = user.displayName ?: ""
                    )
                )
            } else {
                NetworkResult.Error("Login failed: User is null")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Authentication error")
        }
    }

    suspend fun register(name: String, email: String, pass: String): NetworkResult<UserSession> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user
            if (user != null) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates).await()
                NetworkResult.Success(
                    UserSession(
                        uid = user.uid,
                        email = user.email ?: "",
                        displayName = name
                    )
                )
            } else {
                NetworkResult.Error("Registration failed: User is null")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Registration error")
        }
    }

    fun logout() {
        auth.signOut()
    }
}
