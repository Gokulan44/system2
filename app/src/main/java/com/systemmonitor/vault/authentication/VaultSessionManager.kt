package com.systemmonitor.vault.authentication

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultSessionManager @Inject constructor() {
    private val _isSessionActive = MutableStateFlow(false)
    val isSessionActive: StateFlow<Boolean> = _isSessionActive.asStateFlow()

    private var activeSessionToken: String? = null
    private var lastActivityTimestamp: Long = 0L
    
    companion object {
        const val SESSION_TIMEOUT_MS = 5 * 60 * 1000L // 5 minutes inactivity timeout
    }

    fun startSession(): String {
        val token = UUID.randomUUID().toString()
        activeSessionToken = token
        lastActivityTimestamp = System.currentTimeMillis()
        _isSessionActive.value = true
        return token
    }

    fun updateActivity() {
        if (_isSessionActive.value) {
            lastActivityTimestamp = System.currentTimeMillis()
        }
    }

    fun checkSessionValid(): Boolean {
        if (!_isSessionActive.value || activeSessionToken == null) return false
        val elapsed = System.currentTimeMillis() - lastActivityTimestamp
        if (elapsed > SESSION_TIMEOUT_MS) {
            endSession()
            return false
        }
        updateActivity()
        return true
    }

    fun endSession() {
        activeSessionToken = null
        lastActivityTimestamp = 0L
        _isSessionActive.value = false
    }
}
