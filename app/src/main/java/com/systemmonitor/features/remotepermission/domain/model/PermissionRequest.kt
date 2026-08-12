package com.systemmonitor.features.remotepermission.domain.model

data class PermissionRequest(
    val requestId: String,
    val laptopId: String,
    val resource: ResourceRequest,
    val requestedOperation: PermissionType,
    val createdAt: Long,
    val expiresAt: Long,
    val requestNonce: String,
    var status: PermissionStatus
)
