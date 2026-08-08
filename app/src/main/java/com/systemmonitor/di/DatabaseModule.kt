package com.systemmonitor.di

import android.content.Context
import androidx.room.Room
import com.systemmonitor.core.Constants
import com.systemmonitor.local.database.AppDatabase
import com.systemmonitor.local.database.DatabaseMigrations
import com.systemmonitor.local.database.dao.BatteryDao
import com.systemmonitor.local.database.dao.InstalledAppDao
import com.systemmonitor.local.database.dao.MemoryDao
import com.systemmonitor.local.database.dao.NetworkDao
import com.systemmonitor.local.database.dao.StorageDao
import com.systemmonitor.local.database.dao.WifiDao
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
            // Pre-release only: no shipped version has real user data yet, so a
            // destructive fallback is fine for schema churn. Remove this and add
            // a real Migration in DatabaseMigrations.kt before the first release.
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
}
