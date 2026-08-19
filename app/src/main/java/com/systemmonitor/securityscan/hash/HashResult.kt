package com.systemmonitor.securityscan.hash

sealed class HashResult {
    data class Clean(val appName: String) : HashResult()
    data class Malware(val appName: String, val threatName: String) : HashResult()
    object Unknown : HashResult()
}
