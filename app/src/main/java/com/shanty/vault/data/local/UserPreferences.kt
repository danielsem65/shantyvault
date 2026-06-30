package com.shanty.vault.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "shanty_vault_prefs")

class UserPreferences(private val context: Context) {

    private object Keys {
        val USER_ID = stringPreferencesKey("user_id")
        val FIRST_LOGIN = booleanPreferencesKey("first_login")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val MFA_ENABLED = booleanPreferencesKey("mfa_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val TRUSTED_DEVICE = booleanPreferencesKey("trusted_device")
        val LAST_ACTIVITY = longPreferencesKey("last_activity")
        val SESSION_TIMEOUT = longPreferencesKey("session_timeout")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val STORAGE_LIMIT = longPreferencesKey("storage_limit")
    }

    val userId: Flow<String?> = context.dataStore.data.map { it[Keys.USER_ID] }
    val isFirstLogin: Flow<Boolean> = context.dataStore.data.map { it[Keys.FIRST_LOGIN] ?: true }
    val isBiometricEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.BIOMETRIC_ENABLED] ?: false }
    val isMfaEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.MFA_ENABLED] ?: false }
    val themeMode: Flow<String> = context.dataStore.data.map { it[Keys.THEME_MODE] ?: "system" }
    val isTrustedDevice: Flow<Boolean> = context.dataStore.data.map { it[Keys.TRUSTED_DEVICE] ?: false }
    val lastActivity: Flow<Long> = context.dataStore.data.map { it[Keys.LAST_ACTIVITY] ?: 0L }
    val sessionTimeout: Flow<Long> = context.dataStore.data.map { it[Keys.SESSION_TIMEOUT] ?: 300000L }
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.NOTIFICATIONS_ENABLED] ?: true }
    val storageLimit: Flow<Long> = context.dataStore.data.map { it[Keys.STORAGE_LIMIT] ?: (5L * 1024 * 1024 * 1024) }

    suspend fun setUserId(id: String) {
        context.dataStore.edit { it[Keys.USER_ID] = id }
    }

    suspend fun setFirstLogin(value: Boolean) {
        context.dataStore.edit { it[Keys.FIRST_LOGIN] = value }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun setMfaEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MFA_ENABLED] = enabled }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun setTrustedDevice(trusted: Boolean) {
        context.dataStore.edit { it[Keys.TRUSTED_DEVICE] = trusted }
    }

    suspend fun updateLastActivity() {
        context.dataStore.edit { it[Keys.LAST_ACTIVITY] = System.currentTimeMillis() }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
