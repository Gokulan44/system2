package com.systemmonitor.applock.data.database

import androidx.room.*
import com.systemmonitor.applock.data.entity.AuthenticationLogEntity
import com.systemmonitor.applock.data.entity.LockedAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LockedAppDao {
    @Query("SELECT * FROM locked_apps_table ORDER BY appName ASC")
    fun getAllLockedApps(): Flow<List<LockedAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLockedApp(app: LockedAppEntity)

    @Query("DELETE FROM locked_apps_table WHERE packageName = :packageName")
    suspend fun deleteLockedApp(packageName: String)

    @Query("SELECT EXISTS(SELECT 1 FROM locked_apps_table WHERE packageName = :packageName)")
    suspend fun isAppLocked(packageName: String): Boolean
}

@Dao
interface AuthenticationLogDao {
    @Query("SELECT * FROM authentication_logs_table ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AuthenticationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuthenticationLogEntity)

    @Query("DELETE FROM authentication_logs_table")
    suspend fun clearLogs()
}

@Dao
interface AppLockSettingsDao {
    @Query("SELECT * FROM applock_settings_table WHERE id = 1")
    fun getSettings(): Flow<com.systemmonitor.applock.data.entity.AppLockSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: com.systemmonitor.applock.data.entity.AppLockSettingsEntity)
}
