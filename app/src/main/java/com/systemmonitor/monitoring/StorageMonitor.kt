package com.systemmonitor.monitoring

import android.os.Environment
import android.os.StatFs
import com.systemmonitor.domain.model.Storage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageMonitor @Inject constructor() {

    fun readCurrent(): Storage {
        val stat = StatFs(Environment.getDataDirectory().path)
        val blockSize = stat.blockSizeLong
        val totalBytes = stat.blockCountLong * blockSize
        val freeBytes = stat.availableBlocksLong * blockSize
        val usedBytes = totalBytes - freeBytes

        return Storage(
            timestamp = System.currentTimeMillis(),
            totalBytes = totalBytes,
            freeBytes = freeBytes,
            usedBytes = usedBytes
        )
    }
}
