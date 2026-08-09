package com.systemmonitor.applock.authentication

import com.systemmonitor.applock.data.entity.AuthenticationLogEntity
import com.systemmonitor.applock.model.LockMethod

data class AuthenticationAttempt(
    val timestamp: Long = System.currentTimeMillis(),
    val packageName: String = "",
    val result: String = "SUCCESS", // "SUCCESS", "FAILED", "CANCELLED"
    val authenticationMethod: LockMethod = LockMethod.PIN,
    val attemptCount: Int = 1
) {
    fun isSuccess(): Boolean = result.equals("SUCCESS", ignoreCase = true)

    fun toEntity(): AuthenticationLogEntity {
        return AuthenticationLogEntity(
            packageName = packageName,
            timestamp = timestamp,
            result = result,
            authenticationMethod = authenticationMethod.name,
            attemptCount = attemptCount
        )
    }
}
