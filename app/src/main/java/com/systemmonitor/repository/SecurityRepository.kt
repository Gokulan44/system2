package com.systemmonitor.repository

import com.systemmonitor.domain.mapper.toDomain
import com.systemmonitor.domain.mapper.toEntity
import com.systemmonitor.domain.model.InstalledApp
import com.systemmonitor.domain.model.SecurityResult
import com.systemmonitor.local.database.dao.InstalledAppDao
import com.systemmonitor.security.PermissionAnalyzer
import com.systemmonitor.security.SecurityScoreEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityRepository @Inject constructor(
    private val installedAppDao: InstalledAppDao,
    private val permissionAnalyzer: PermissionAnalyzer,
    private val securityScoreEngine: SecurityScoreEngine
) {
    fun observeInstalledApps(): Flow<List<InstalledApp>> =
        installedAppDao.observeAll().map { list -> list.map { it.toDomain() } }

    /** Re-scans installed apps via PackageManager, persists, and returns a fresh score. */
    suspend fun rescanAndScore(): SecurityResult {
        val now = System.currentTimeMillis()
        val apps = permissionAnalyzer.scanInstalledApps(includeSystemApps = false)
        installedAppDao.insertAll(apps.map { it.toEntity(scannedAt = now) })
        installedAppDao.deleteStale(beforeEpochMillis = now)
        return securityScoreEngine.score(apps)
    }
}
