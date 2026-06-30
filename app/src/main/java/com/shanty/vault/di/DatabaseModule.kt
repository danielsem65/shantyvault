package com.shanty.vault.di

import android.content.Context
import com.shanty.vault.data.local.*
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
    fun provideDatabase(@ApplicationContext context: Context): VaultDatabase {
        return VaultDatabase.getInstance(context)
    }

    @Provides
    fun provideVaultFileDao(database: VaultDatabase): VaultFileDao {
        return database.vaultFileDao()
    }

    @Provides
    fun provideFolderDao(database: VaultDatabase): FolderDao {
        return database.folderDao()
    }

    @Provides
    fun provideNoteDao(database: VaultDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    fun provideActivityDao(database: VaultDatabase): ActivityDao {
        return database.activityDao()
    }
}
