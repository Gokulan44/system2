package com.systemmonitor.securityscan

import android.content.Context
import androidx.room.Room
import com.systemmonitor.securityscan.database.SecurityScanDatabase
import com.systemmonitor.securityscan.database.dao.FindingDao
import com.systemmonitor.securityscan.database.dao.KnownHashDao
import com.systemmonitor.securityscan.database.dao.ScanHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityScanModule {

    @Provides
    @Singleton
    fun provideSecurityScanDatabase(
        @ApplicationContext context: Context
    ): SecurityScanDatabase {
        return Room.databaseBuilder(
            context,
            SecurityScanDatabase::class.java,
            "security_scanner_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideScanHistoryDao(db: SecurityScanDatabase): ScanHistoryDao {
        return db.scanHistoryDao()
    }

    @Provides
    fun provideFindingDao(db: SecurityScanDatabase): FindingDao {
        return db.findingDao()
    }

    @Provides
    fun provideKnownHashDao(db: SecurityScanDatabase): KnownHashDao {
        return db.knownHashDao()
    }
}
