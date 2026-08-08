package com.systemmonitor.applock.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppLockDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun lockApp(app: LockedAppEntity)

    @Query("DELETE FROM locked_apps WHERE package_name = :packageName")
    suspend fun unlockApp(packageName: String)

    @Query("SELECT * FROM locked_apps WHERE enabled = 1")
    fun getLockedApps(): Flow<List<LockedAppEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM locked_apps WHERE package_name = :packageName AND enabled = 1)")
    suspend fun isAppLocked(packageName: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: LockSettingsEntity)

    @Query("SELECT * FROM lock_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): LockSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun logUnlockAttempt(history: UnlockHistoryEntity)

    @Query("SELECT * FROM unlock_history ORDER BY timestamp DESC LIMIT 50")
    fun getUnlockHistory(): Flow<List<UnlockHistoryEntity>>
}
