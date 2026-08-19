package com.systemmonitor.securityscan.history

import com.systemmonitor.securityscan.analysis.ScanResult
import com.systemmonitor.securityscan.database.dao.FindingDao
import com.systemmonitor.securityscan.database.dao.ScanHistoryDao
import com.systemmonitor.securityscan.database.entity.ScanHistoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanHistoryManager @Inject constructor(
    private val scanHistoryDao: ScanHistoryDao,
    private val findingDao: FindingDao
) {
    /** Persists a scan result: inserts the history row, then attaches findings to its generated id. */
    suspend fun saveScanResult(result: ScanResult): Long {
        val scanId = scanHistoryDao.insertScan(result.scanHistory)

        if (result.findings.isNotEmpty()) {
            val findingsWithScanId = result.findings.map { it.copy(scanId = scanId) }
            findingDao.insertFindings(findingsWithScanId)
        }

        return scanId
    }

    fun getHistoryFlow(): Flow<List<ScanHistoryEntity>> = scanHistoryDao.getAllHistoryFlow()

    suspend fun getScanWithFindings(scanId: Long) =
        scanHistoryDao.getScanById(scanId)?.let { scan ->
            scan to findingDao.getFindingsForScan(scanId)
        }

    suspend fun clearAllHistory() = scanHistoryDao.clearHistory()
}