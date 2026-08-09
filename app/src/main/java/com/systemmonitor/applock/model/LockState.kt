package com.systemmonitor.applock.model

enum class LockState {
    LOCKED,
    UNLOCKED,
    TIMED_OUT;

    val isLocked: Boolean
        get() = this == LOCKED || this == TIMED_OUT

    val isUnlocked: Boolean
        get() = this == UNLOCKED
}
