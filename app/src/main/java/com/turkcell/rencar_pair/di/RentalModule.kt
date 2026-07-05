package com.turkcell.rencar_pair.di

import com.turkcell.rencar_pair.data.repository.RentalRepository
import com.turkcell.rencar_pair.data.repository.RentalRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RentalModule {
    @Binds
    @Singleton
    abstract fun bindRentalRepository(impl: RentalRepositoryImpl): RentalRepository
}
