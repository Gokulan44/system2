package com.systemmonitor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemmonitor.data.network.NetworkResult
import com.systemmonitor.repository.AuthRepository
import com.systemmonitor.repository.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: StateFlow<UserSession?> = authRepository.currentUser

    private val _authState = MutableStateFlow<NetworkResult<UserSession>?>(null)
    val authState: StateFlow<NetworkResult<UserSession>?> = _authState.asStateFlow()

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = NetworkResult.Loading
            _authState.value = authRepository.login(email, pass)
        }
    }

    fun register(name: String, email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = NetworkResult.Loading
            _authState.value = authRepository.register(name, email, pass)
        }
    }

    fun logout() {
        authRepository.logout()
        _authState.value = null
    }

    fun clearState() {
        _authState.value = null
    }
}
