package com.systemmonitor.features.remotepermission.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.systemmonitor.features.remotepermission.data.dao.PermissionHistoryDao
import com.systemmonitor.features.remotepermission.data.dao.PermissionRequestDao
import com.systemmonitor.features.remotepermission.data.dao.ResourceRequestDao
import com.systemmonitor.features.remotepermission.data.entity.*

@Database(
    entities = [
        PermissionRequestEntity::class,
        PermissionHistoryEntity::class,
        ResourceRequestEntity::class,
        VerificationEventEntity::class,
        ApprovalEventEntity::class,
        DownloadResultEntity::class,
        SecurityScanResultEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RemotePermissionDatabase : RoomDatabase() {
    abstract fun permissionRequestDao(): PermissionRequestDao
    abstract fun permissionHistoryDao(): PermissionHistoryDao
    abstract fun resourceRequestDao(): ResourceRequestDao
}
