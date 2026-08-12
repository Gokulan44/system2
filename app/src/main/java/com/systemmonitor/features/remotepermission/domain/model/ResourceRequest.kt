package com.systemmonitor.features.remotepermission.domain.model

data class ResourceRequest(
    val resourceId: String,
    val name: String,
    val type: ResourceType,
    val sizeBytes: Long,
    val path: String?
)
