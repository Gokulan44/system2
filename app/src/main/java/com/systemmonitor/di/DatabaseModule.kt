package com.systemmonitor.di

import android.content.Context
import androidx.room.Room
import com.systemmonitor.applock.data.database.AppDatabase as AppLockDataDatabase
import com.systemmonitor.applock.data.database.AppLockSettingsDao
import com.systemmonitor.applock.data.database.AuthenticationLogDao
import com.systemmonitor.applock.data.database.LockedAppDao
import com.systemmonitor.core.Constants
import com.systemmonitor.local.database.AppDatabase
import com.systemmonitor.local.database.DatabaseMigrations
import com.systemmonitor.local.database.dao.BatteryDao
import com.systemmonitor.local.database.dao.InstalledAppDao
import com.systemmonitor.local.database.dao.MemoryDao
import com.systemmonitor.local.database.dao.NetworkDao
import com.systemmonitor.local.database.dao.StorageDao
import com.systemmonitor.local.database.dao.WifiDao
import com.systemmonitor.securityanalysis.database.ScanDao
import com.systemmonitor.securityanalysis.database.SecurityDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import com.systemmonitor.local.database.dao.LaptopDao
import com.systemmonitor.features.remotepermission.data.RemotePermissionDatabase
import com.systemmonitor.features.remotepermission.data.dao.PermissionRequestDao
import com.systemmonitor.features.remotepermission.data.dao.PermissionHistoryDao
import com.systemmonitor.features.remotepermission.data.dao.ResourceRequestDao

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, Constants.DATABASE_NAME)
            .addMigrations(*DatabaseMigrations.ALL)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideBatteryDao(db: AppDatabase): BatteryDao = db.batteryDao()

    @Provides
    fun provideMemoryDao(db: AppDatabase): MemoryDao = db.memoryDao()

    @Provides
    fun provideStorageDao(db: AppDatabase): StorageDao = db.storageDao()

    @Provides
    fun provideInstalledAppDao(db: AppDatabase): InstalledAppDao = db.installedAppDao()

    @Provides
    fun provideNetworkDao(db: AppDatabase): NetworkDao = db.networkDao()

    @Provides
    fun provideWifiDao(db: AppDatabase): WifiDao = db.wifiDao()

    @Provides
    fun provideLaptopDao(db: AppDatabase): LaptopDao = db.laptopDao()

    @Provides
    fun provideUnlockHistoryDao(db: AppDatabase): com.systemmonitor.local.database.dao.UnlockHistoryDao = db.unlockHistoryDao()

    @Provides
    fun provideIntrusionEventDao(db: AppDatabase): com.systemmonitor.features.intrusion.data.dao.IntrusionEventDao = db.intrusionEventDao()

    @Provides
    fun provideVaultFileDao(db: AppDatabase): com.systemmonitor.vault.database.VaultFileDao = db.vaultFileDao()

    @Provides
    fun provideVaultFolderDao(db: AppDatabase): com.systemmonitor.vault.database.VaultFolderDao = db.vaultFolderDao()

    @Provides
    fun provideVaultAuditDao(db: AppDatabase): com.systemmonitor.vault.database.VaultAuditDao = db.vaultAuditDao()

    @Provides
    fun provideVaultSettingsDao(db: AppDatabase): com.systemmonitor.vault.database.VaultSettingsDao = db.vaultSettingsDao()

    @Provides
    @Singleton
    fun provideAppLockDatabase(@ApplicationContext context: Context): AppLockDataDatabase =
        Room.databaseBuilder(context, AppLockDataDatabase::class.java, "app_lock.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideLockedAppDao(db: AppLockDataDatabase): LockedAppDao = db.lockedAppDao()

    @Provides
    fun provideAuthenticationLogDao(db: AppLockDataDatabase): AuthenticationLogDao = db.authenticationLogDao()

    @Provides
    fun provideAppLockSettingsDao(db: AppLockDataDatabase): AppLockSettingsDao = db.appLockSettingsDao()

    @Provides
    @Singleton
    fun provideSecurityDatabase(@ApplicationContext context: Context): SecurityDatabase =
        Room.databaseBuilder(context, SecurityDatabase::class.java, "security_analysis.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideScanDao(db: SecurityDatabase): ScanDao = db.scanDao()

    @Provides
    @Singleton
    fun provideFeatureSecurityDatabase(@ApplicationContext context: Context): com.systemmonitor.features.security.data.FeatureSecurityDatabase =
        Room.databaseBuilder(context, com.systemmonitor.features.security.data.FeatureSecurityDatabase::class.java, "feature_security.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideSecurityScanDao(db: com.systemmonitor.features.security.data.FeatureSecurityDatabase): com.systemmonitor.features.security.data.dao.SecurityScanDao = db.securityScanDao()

    @Provides
    @Singleton
    fun provideProfileDatabase(@ApplicationContext context: Context): com.systemmonitor.features.profile.data.ProfileDatabase =
        Room.databaseBuilder(context, com.systemmonitor.features.profile.data.ProfileDatabase::class.java, "profile_db.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideUserProfileDao(db: com.systemmonitor.features.profile.data.ProfileDatabase): com.systemmonitor.features.profile.data.dao.UserProfileDao = db.userProfileDao()

    @Provides
    fun provideLoginHistoryDao(db: com.systemmonitor.features.profile.data.ProfileDatabase): com.systemmonitor.features.profile.data.dao.LoginHistoryDao = db.loginHistoryDao()

    @Provides
    fun provideActivityHistoryDao(db: com.systemmonitor.features.profile.data.ProfileDatabase): com.systemmonitor.features.profile.data.dao.ActivityHistoryDao = db.activityHistoryDao()

    @Provides
    fun provideDeviceSessionDao(db: com.systemmonitor.features.profile.data.ProfileDatabase): com.systemmonitor.features.profile.data.dao.DeviceSessionDao = db.deviceSessionDao()

    @Provides
    fun provideNotificationPreferenceDao(db: com.systemmonitor.features.profile.data.ProfileDatabase): com.systemmonitor.features.profile.data.dao.NotificationPreferenceDao = db.notificationPreferenceDao()

    @Provides
    @Singleton
    fun provideRemotePermissionDatabase(@ApplicationContext context: Context): RemotePermissionDatabase =
        Room.databaseBuilder(context, RemotePermissionDatabase::class.java, "remote_permission.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun providePermissionRequestDao(db: RemotePermissionDatabase): PermissionRequestDao = db.permissionRequestDao()

    @Provides
    fun providePermissionHistoryDao(db: RemotePermissionDatabase): PermissionHistoryDao = db.permissionHistoryDao()

    @Provides
    fun provideResourceRequestDao(db: RemotePermissionDatabase): ResourceRequestDao = db.resourceRequestDao()
}
