package com.shanty.vault.domain.repository

import com.shanty.vault.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    val isAuthenticated: Flow<Boolean>
    val isSessionExpired: Flow<Boolean>

    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, name: String): Result<User>
    suspend fun logout()
    suspend fun refreshToken(): Result<String>
    suspend fun verifyEmail(code: String): Result<Unit>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>
    suspend fun setupMfa(): Result<String>
    suspend fun verifyMfa(code: String): Result<Unit>
    suspend fun disableMfa(code: String): Result<Unit>
    suspend fun getAccessToken(): String?
    suspend fun isTokenValid(): Boolean
    suspend fun updateLastActivity()
    suspend fun checkSessionTimeout(): Boolean
    suspend fun authenticateWithBiometric(): Result<Unit>
    suspend fun isTrustedDevice(): Boolean
    suspend fun setTrustedDevice(trusted: Boolean)
}
