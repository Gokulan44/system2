package com.systemmonitor.features.profile.data.dao

import androidx.room.*
import com.systemmonitor.features.profile.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile_table WHERE id = 'user_001'")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity)
}

@Dao
interface LoginHistoryDao {
    @Query("SELECT * FROM login_history_table ORDER BY loginTime DESC")
    fun getLoginHistory(): Flow<List<LoginHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoginSession(session: LoginHistoryEntity)
}

@Dao
interface ActivityHistoryDao {
    @Query("SELECT * FROM activity_history_table ORDER BY timestamp DESC")
    fun getActivityHistory(): Flow<List<ActivityHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityHistoryEntity)
}

@Dao
interface DeviceSessionDao {
    @Query("SELECT * FROM device_session_table")
    fun getDeviceSessions(): Flow<List<DeviceSessionEntity>>

    @Query("DELETE FROM device_session_table WHERE deviceId = :deviceId")
    suspend fun deleteDevice(deviceId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: DeviceSessionEntity)
}

@Dao
interface NotificationPreferenceDao {
    @Query("SELECT * FROM notification_preference_table WHERE id = 1")
    fun getPreferences(): Flow<NotificationPreferenceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreferences(prefs: NotificationPreferenceEntity)
}
