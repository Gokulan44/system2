package com.systemmonitor.features.remotepermission.request

import com.systemmonitor.features.remotepermission.domain.model.PermissionRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestValidator @Inject constructor() {

    fun isValid(request: PermissionRequest): Boolean {
        // 1. Expiration check
        if (System.currentTimeMillis() > request.expiresAt) {
            return false
        }
        
        // 2. Validate fields
        if (request.requestId.isBlank() || request.laptopId.isBlank() || request.resource.resourceId.isBlank()) {
            return false
        }

        if (request.resource.sizeBytes < 0) {
            return false
        }

        return true
    }
}
