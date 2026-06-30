package com.shanty.vault.di

import com.shanty.vault.data.repository.AuthRepositoryImpl
import com.shanty.vault.data.repository.VaultRepositoryImpl
import com.shanty.vault.domain.repository.AuthRepository
import com.shanty.vault.domain.repository.VaultRepository
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
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindVaultRepository(impl: VaultRepositoryImpl): VaultRepository
}
