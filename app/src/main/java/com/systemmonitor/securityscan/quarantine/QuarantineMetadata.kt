package com.systemmonitor.securityscan.quarantine

data class QuarantineMetadata(
    val id: String,
    val appName: String,
    val packageName: String?,
    val originalPath: String,
    val quarantineTime: Long,
    val reason: String
)
