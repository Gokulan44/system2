package com.systemmonitor.di

import com.systemmonitor.features.profile.data.repository.ProfileRepositoryImpl
import com.systemmonitor.features.profile.domain.repository.ProfileRepository
import com.systemmonitor.features.remotepermission.domain.repository.PermissionRepository
import com.systemmonitor.features.remotepermission.data.repository.PermissionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindPermissionRepository(impl: PermissionRepositoryImpl): PermissionRepository
}
