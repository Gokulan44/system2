package com.systemmonitor.domain.model

data class ProcessInfo(
    val pid: Int,
    val name: String,
    val cpuPercent: Double,
    val memoryPercent: Double,
    val status: String,
    val username: String = ""
)
