package com.systemmonitor.securityscan.update

import com.systemmonitor.securityscan.database.dao.KnownHashDao
import com.systemmonitor.securityscan.database.entity.KnownHashEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HashDatabaseUpdater @Inject constructor(
    private val knownHashDao: KnownHashDao,
    private val parser: HashDatabaseParser,
    private val verifier: HashDatabaseVerifier
) {
    suspend fun updateDatabase(feedUrl: String, expectedChecksum: String? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            val content = URL(feedUrl).readText()
            if (expectedChecksum != null && !verifier.verifyChecksum(content, expectedChecksum)) {
                return@withContext false
            }
            val entities = parser.parseJsonFeed(content)
            if (entities.isNotEmpty()) {
                knownHashDao.insertKnownHashes(entities)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun seedLocalDatabase(): Boolean = withContext(Dispatchers.IO) {
        // Pre-populate with mock threat hashes for demonstration
        val seeds = listOf(
            KnownHashEntity("5e883e29f023e9e359a356af7127040d1f1390b1ef3b63a921d798a3e7240f5a", "MALWARE", "Test Trojan App", "Trojan.Android.Generic"),
            KnownHashEntity("2012a64010b91e92d6e3c3ef270211ff130008b1ef3b63a921d798a3e7240f5b", "MALWARE", "Spyware Pro", "Spyware.Android.Keylogger"),
            KnownHashEntity("0a9b8c7d6e5f4a3b2c1d0e9f8a7b6c5d4e3f2a1b0c9d8e7f6a5b4c3d2e1f0a9b", "CLEAN", "System Monitor", null)
        )
        try {
            knownHashDao.insertKnownHashes(seeds)
            true
        } catch (e: Exception) {
            false
        }
    }
}
