package com.systemmonitor.di

import android.content.Context
import androidx.room.Room
import com.systemmonitor.applock.database.AppLockDao
import com.systemmonitor.applock.database.AppLockDatabase
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
    @Singleton
    fun provideAppLockDatabase(@ApplicationContext context: Context): AppLockDatabase =
        Room.databaseBuilder(context, AppLockDatabase::class.java, "app_lock.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideAppLockDao(db: AppLockDatabase): AppLockDao = db.appLockDao()

    @Provides
    @Singleton
    fun provideSecurityDatabase(@ApplicationContext context: Context): SecurityDatabase =
        Room.databaseBuilder(context, SecurityDatabase::class.java, "security_analysis.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideScanDao(db: SecurityDatabase): ScanDao = db.scanDao()
}
