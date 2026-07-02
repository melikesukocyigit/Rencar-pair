package com.turkcell.rencar_pair.di

import com.turkcell.rencar_pair.data.repository.LicenseRepository
import com.turkcell.rencar_pair.data.repository.LicenseRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LicenseModule {

    @Binds
    @Singleton
    abstract fun bindLicenseRepository(
        impl: LicenseRepositoryImpl
    ): LicenseRepository
}
