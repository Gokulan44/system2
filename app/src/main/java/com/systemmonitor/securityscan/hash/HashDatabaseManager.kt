package com.systemmonitor.securityscan.hash

import com.systemmonitor.securityscan.database.dao.KnownHashDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HashDatabaseManager @Inject constructor(
    private val knownHashDao: KnownHashDao
) {
    suspend fun checkHash(sha256: String): HashResult {
        val entity = knownHashDao.getKnownHash(sha256.lowercase()) ?: return HashResult.Unknown
        return if (entity.type.uppercase() == "MALWARE") {
            HashResult.Malware(
                appName = entity.appName,
                threatName = entity.threatName ?: "Generic Malware"
            )
        } else {
            HashResult.Clean(appName = entity.appName)
        }
    }
}
