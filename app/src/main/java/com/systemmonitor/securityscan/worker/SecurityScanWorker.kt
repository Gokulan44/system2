package com.systemmonitor.securityscan.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.systemmonitor.securityscan.analysis.RiskAnalyzer
import com.systemmonitor.securityscan.history.ScanHistoryManager
import com.systemmonitor.securityscan.input.InstalledAppResolver
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class SecurityScanWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val appResolver: InstalledAppResolver,
    private val riskAnalyzer: RiskAnalyzer,
    private val scanHistoryManager: ScanHistoryManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val apps = appResolver.getInstalledApps()
            for (app in apps) {
                val scanResult = riskAnalyzer.analyze(app)
                scanHistoryManager.saveScanResult(scanResult)
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
