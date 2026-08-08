package com.systemmonitor.domain.mapper

import com.systemmonitor.domain.model.InstalledApp
import com.systemmonitor.local.database.entity.InstalledAppEntity

fun InstalledAppEntity.toDomain(): InstalledApp = InstalledApp(
    packageName = packageName,
    appName = appName,
    versionName = versionName,
    installerPackageName = installerPackageName,
    isSystemApp = isSystemApp,
    totalPermissions = totalPermissions,
    dangerousPermissions = dangerousPermissions,
    lastUpdatedTimestamp = lastUpdatedTimestamp
)

fun InstalledApp.toEntity(scannedAt: Long): InstalledAppEntity = InstalledAppEntity(
    packageName = packageName,
    appName = appName,
    versionName = versionName,
    installerPackageName = installerPackageName,
    isSystemApp = isSystemApp,
    totalPermissions = totalPermissions,
    dangerousPermissions = dangerousPermissions,
    lastUpdatedTimestamp = lastUpdatedTimestamp,
    lastScannedTimestamp = scannedAt
)
