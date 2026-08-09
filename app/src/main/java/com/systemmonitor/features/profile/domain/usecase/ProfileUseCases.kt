package com.systemmonitor.features.profile.domain.usecase

import com.systemmonitor.features.profile.domain.model.*
import com.systemmonitor.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetProfileUseCase @Inject constructor(private val repo: ProfileRepository) {
    operator fun invoke(): Flow<UserProfile> = repo.getUserProfile()
}

@Singleton
class UpdateProfileUseCase @Inject constructor(private val repo: ProfileRepository) {
    suspend operator fun invoke(profile: UserProfile): Boolean = repo.updateProfile(profile)
}

@Singleton
class UploadProfilePhotoUseCase @Inject constructor(private val repo: ProfileRepository) {
    suspend operator fun invoke(photoUri: String): Boolean = repo.uploadProfilePhoto(photoUri)
}

@Singleton
class GetConnectedDevicesUseCase @Inject constructor(private val repo: ProfileRepository) {
    operator fun invoke(): Flow<List<ConnectedDevice>> = repo.getConnectedDevices()
}

@Singleton
class RemoveDeviceUseCase @Inject constructor(private val repo: ProfileRepository) {
    suspend operator fun invoke(deviceId: String): Boolean = repo.removeDevice(deviceId)
}

@Singleton
class GetLoginHistoryUseCase @Inject constructor(private val repo: ProfileRepository) {
    operator fun invoke(): Flow<List<LoginSession>> = repo.getLoginHistory()
}

@Singleton
class GetActivityHistoryUseCase @Inject constructor(private val repo: ProfileRepository) {
    operator fun invoke(): Flow<List<ActivityRecord>> = repo.getActivityHistory()
}

@Singleton
class UpdateNotificationPreferencesUseCase @Inject constructor(private val repo: ProfileRepository) {
    suspend operator fun invoke(prefs: NotificationPreferences): Boolean = repo.updateNotificationPreferences(prefs)
}

@Singleton
class UpdatePrivacySettingsUseCase @Inject constructor(private val repo: ProfileRepository) {
    suspend operator fun invoke(privacy: PrivacySettings): Boolean = repo.updatePrivacySettings(privacy)
}

@Singleton
class UpdateUserPreferencesUseCase @Inject constructor(private val repo: ProfileRepository) {
    suspend operator fun invoke(prefs: UserPreferences): Boolean = repo.updateUserPreferences(prefs)
}

@Singleton
class SignOutUseCase @Inject constructor(private val repo: ProfileRepository) {
    suspend operator fun invoke(): Boolean = repo.signOut()
}

@Singleton
class DeleteAccountUseCase @Inject constructor(private val repo: ProfileRepository) {
    suspend operator fun invoke(): Boolean = repo.deleteAccount()
}
