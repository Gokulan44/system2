package com.systemmonitor.vault.workers

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.systemmonitor.vault.importexport.VaultImportManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class VaultImportWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val importManager: VaultImportManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val uriString = inputData.getString(KEY_URI) ?: return Result.failure()
        val parentId = inputData.getString(KEY_PARENT_ID)
        val uri = Uri.parse(uriString)

        val result = importManager.importSingleFile(uri, parentId)
        return if (result.isSuccess) Result.success() else Result.failure()
    }

    companion object {
        const val KEY_URI = "key_uri"
        const val KEY_PARENT_ID = "key_parent_id"
    }
}
