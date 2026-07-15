package com.shanty.vault.di

import android.content.Context
import com.shanty.vault.data.local.*
import com.shanty.vault.data.repository.AuthRepositoryImpl
import com.shanty.vault.data.repository.VaultRepositoryImpl
import com.shanty.vault.domain.repository.AuthRepository
import com.shanty.vault.domain.repository.VaultRepository
import com.shanty.vault.security.EncryptionManager
import com.shanty.vault.security.TokenManager
import com.shanty.vault.util.Constants
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

class AppContainer(context: Context) {
    val encryptionManager = EncryptionManager()
    val tokenManager = TokenManager(context)
    val userPreferences = UserPreferences(context)

    val supabaseClient: SupabaseClient = createSupabaseClient(
        supabaseUrl = Constants.SUPABASE_URL,
        supabaseKey = Constants.SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Storage)
        install(Postgrest)
    }

    private val database = VaultDatabase.getInstance(context)
    private val vaultFileDao = database.vaultFileDao()
    private val folderDao = database.folderDao()
    private val noteDao = database.noteDao()
    private val activityDao = database.activityDao()

    val vaultRepository: VaultRepository = VaultRepositoryImpl(
        vaultFileDao, folderDao, noteDao, activityDao, encryptionManager, supabaseClient
    )
    val authRepository: AuthRepository = AuthRepositoryImpl(
        supabaseClient, tokenManager, userPreferences
    )
}
