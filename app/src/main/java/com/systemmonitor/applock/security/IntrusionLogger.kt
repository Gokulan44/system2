package com.systemmonitor.applock.security

import com.systemmonitor.applock.data.database.AuthenticationLogDao
import com.systemmonitor.applock.data.entity.AuthenticationLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntrusionLogger @Inject constructor(
    private val authLogDao: AuthenticationLogDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun logFailedAttempt(packageName: String) {
        scope.launch {
            authLogDao.insertLog(
                AuthenticationLogEntity(
                    packageName = packageName,
                    timestamp = System.currentTimeMillis(),
                    result = "FAILED",
                    authenticationMethod = "INTRUSION",
                    attemptCount = 1
                )
            )
        }
    }

    fun getLogs(): Flow<List<String>> {
        return authLogDao.getAllLogs().map { entities ->
            entities.map { "[${it.timestamp}] Failed unlock attempt for ${it.packageName}" }
        }
    }
}
