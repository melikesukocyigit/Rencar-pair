package com.turkcell.rencar_pair.di

import com.turkcell.rencar_pair.data.wallet.FakeWalletRepository
import com.turkcell.rencar_pair.data.wallet.WalletRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WalletModule {

    @Binds
    @Singleton
    abstract fun bindWalletRepository(
        impl: FakeWalletRepository
    ): WalletRepository
}
