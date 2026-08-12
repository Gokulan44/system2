package com.systemmonitor.features.remotepermission.verification

import com.systemmonitor.features.remotepermission.domain.usecase.VerifyUserUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserVerificationManager @Inject constructor(
    private val verifyUserUseCase: VerifyUserUseCase
) {
    suspend fun logVerificationAttempt(requestId: String, method: String, isSuccess: Boolean) {
        verifyUserUseCase(requestId, method, isSuccess)
    }
}
