package com.systemmonitor.applock.model

enum class LockMethod {
    PIN,
    PATTERN,
    PASSWORD,
    BIOMETRIC;

    val displayName: String
        get() = when (this) {
            PIN -> "PIN Code"
            PATTERN -> "Pattern Lock"
            PASSWORD -> "Alphanumeric Password"
            BIOMETRIC -> "Fingerprint / Face Unlock"
        }
}

enum class LockTiming {
    IMMEDIATELY,
    AFTER_30_SECONDS,
    AFTER_1_MINUTE,
    AFTER_SCREEN_OFF;

    val displayName: String
        get() = when (this) {
            IMMEDIATELY -> "Immediately"
            AFTER_30_SECONDS -> "After 30 seconds"
            AFTER_1_MINUTE -> "After 1 minute"
            AFTER_SCREEN_OFF -> "After screen off"
        }
}
