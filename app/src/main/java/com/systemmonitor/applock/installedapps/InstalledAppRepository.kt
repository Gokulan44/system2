package com.systemmonitor.applock.installedapps

import com.systemmonitor.applock.installedapps.model.InstalledApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstalledAppRepository @Inject constructor(
    private val scanner: InstalledAppScanner
) {
    fun getInstalledApps(): Flow<List<InstalledApp>> = flow {
        emit(scanner.scanInstalledApps())
    }
}
