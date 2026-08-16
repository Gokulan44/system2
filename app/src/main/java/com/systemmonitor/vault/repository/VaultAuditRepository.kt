package com.systemmonitor.vault.repository

import com.systemmonitor.vault.database.VaultAuditDao
import com.systemmonitor.vault.database.VaultAuditEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultAuditRepository @Inject constructor(
    private val auditDao: VaultAuditDao
) {
    fun getAllAudits(): Flow<List<VaultAuditEntity>> {
        return auditDao.getAllAudits()
    }

    suspend fun logEvent(action: String, details: String) {
        val audit = VaultAuditEntity(
            id = UUID.randomUUID().toString(),
            action = action,
            details = details,
            timestamp = System.currentTimeMillis()
        )
        auditDao.insertAudit(audit)
    }

    suspend fun clearAllAudits() {
        auditDao.clearAllAudits()
    }
}
