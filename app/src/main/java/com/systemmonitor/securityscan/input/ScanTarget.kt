package com.systemmonitor.securityscan.input

data class ScanTarget(
    val packageName: String,
    val appName: String,
    val apkPath: String,
    val isSystemApp: Boolean,
    val versionName: String?,
    val versionCode: Long
)