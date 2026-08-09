package com.systemmonitor.features.profile.domain.repository

import com.systemmonitor.features.profile.domain.model.*
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getUserProfile(): Flow<UserProfile>
    suspend fun updateProfile(profile: UserProfile): Boolean
    suspend fun uploadProfilePhoto(uriString: String): Boolean
    fun getConnectedDevices(): Flow<List<ConnectedDevice>>
    suspend fun removeDevice(deviceId: String): Boolean
    fun getLoginHistory(): Flow<List<LoginSession>>
    fun getActivityHistory(): Flow<List<ActivityRecord>>
    fun getNotificationPreferences(): Flow<NotificationPreferences>
    suspend fun updateNotificationPreferences(prefs: NotificationPreferences): Boolean
    fun getPrivacySettings(): Flow<PrivacySettings>
    suspend fun updatePrivacySettings(privacy: PrivacySettings): Boolean
    fun getUserPreferences(): Flow<UserPreferences>
    suspend fun updateUserPreferences(prefs: UserPreferences): Boolean
    suspend fun signOut(): Boolean
    suspend fun deleteAccount(): Boolean
}
