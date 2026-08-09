package com.systemmonitor.features.security.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.systemmonitor.features.security.data.repository.SecurityRepository
import com.systemmonitor.features.security.domain.scanner.SecurityScanner
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SecurityScanWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val securityScanner: SecurityScanner,
    private val repository: SecurityRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val result = securityScanner.performComprehensiveScan()
            repository.saveScanResult(result)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
