package com.systemmonitor.monitoring

import android.app.ActivityManager
import android.content.Context
import com.systemmonitor.domain.model.Memory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun readCurrent(): Memory {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)

        val totalMb = info.totalMem / (1024 * 1024)
        val availableMb = info.availMem / (1024 * 1024)
        val usedMb = totalMb - availableMb
        val thresholdMb = info.threshold / (1024 * 1024)

        return Memory(
            timestamp = System.currentTimeMillis(),
            totalMb = totalMb,
            availableMb = availableMb,
            usedMb = usedMb,
            thresholdMb = thresholdMb,
            isLowMemory = info.lowMemory
        )
    }
}
