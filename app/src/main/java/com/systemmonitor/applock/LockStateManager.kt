package com.systemmonitor.applock

import com.systemmonitor.applock.model.LockState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LockStateManager @Inject constructor() {
    private val _lockState = MutableStateFlow<LockState>(LockState.LOCKED)
    val lockState: StateFlow<LockState> = _lockState

    fun setUnlocked() {
        _lockState.value = LockState.UNLOCKED
    }

    fun setLocked() {
        _lockState.value = LockState.LOCKED
    }
}
