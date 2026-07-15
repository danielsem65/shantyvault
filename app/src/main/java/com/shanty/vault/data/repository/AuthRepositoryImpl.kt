@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.shanty.vault.data.repository

import com.shanty.vault.data.local.UserPreferences
import com.shanty.vault.domain.model.User
import com.shanty.vault.domain.repository.AuthRepository
import com.shanty.vault.security.TokenManager
import com.shanty.vault.util.Constants
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put


class AuthRepositoryImpl(
    private val supabaseClient: SupabaseClient,
    private val tokenManager: TokenManager,
    private val userPreferences: UserPreferences
) : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: Flow<User?> = _currentUser

    private val _isAuthenticated = MutableStateFlow(false)
    override val isAuthenticated: Flow<Boolean> = _isAuthenticated

    private val _isSessionExpired = MutableStateFlow(false)
    override val isSessionExpired: Flow<Boolean> = _isSessionExpired

    private var loginAttempts = mutableMapOf<String, MutableList<Long>>()

    init {
        checkSession()
    }

    private fun checkSession() {
        kotlinx.coroutines.runBlocking {
            try {
                val session = supabaseClient.auth.currentSessionOrNull()
                if (session != null) {
                    val user = session.user!!
                    _currentUser.value = user.toDomainUser()
                    _isAuthenticated.value = true
                }
            } catch (_: Exception) {
                _isAuthenticated.value = false
            }
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            if (isRateLimited(email)) {
                return Result.failure(Exception("Too many login attempts. Please try again later."))
            }

            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val session = supabaseClient.auth.currentSessionOrNull()
                ?: throw Exception("Authentication failed")
            val user = session.user!!

            val accessToken = session.accessToken
            tokenManager.saveToken(Constants.KEY_ACCESS_TOKEN, accessToken)
            userPreferences.setUserId(user.id)
            userPreferences.updateLastActivity()
            userPreferences.setFirstLogin(false)

            val domainUser = user.toDomainUser()
            _currentUser.value = domainUser
            _isAuthenticated.value = true
            loginAttempts.remove(email.lowercase())
            Result.success(domainUser)
        } catch (e: Exception) {
            recordLoginAttempt(email)
            Result.failure(Exception(mapSupabaseError(e)))
        }
    }

    override suspend fun register(email: String, password: String, name: String): Result<User> {
        return try {
            supabaseClient.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("name", name)
                }
            }

            val user = supabaseClient.auth.currentUserOrNull()

            Result.success(User(
                id = user?.id ?: "",
                email = email,
                name = name,
                isEmailVerified = user?.emailConfirmedAt != null,
                isMfaEnabled = false,
                storageUsed = 0L,
                storageLimit = 5L * 1024 * 1024 * 1024,
                createdAt = System.currentTimeMillis()
            ))
        } catch (e: Exception) {
            Result.failure(Exception(mapSupabaseError(e)))
        }
    }

    override suspend fun logout() {
        try {
            supabaseClient.auth.signOut()
        } catch (_: Exception) { }
        tokenManager.clearAllTokens()
        _currentUser.value = null
        _isSessionExpired.value = false
        _isAuthenticated.value = false
        userPreferences.clearAll()
    }

    override suspend fun refreshToken(): Result<String> {
        return try {
            val session = supabaseClient.auth.currentSessionOrNull()
                ?: throw Exception("Not authenticated")
            val newSession = supabaseClient.auth.refreshSession(session.refreshToken)
            val token = newSession.accessToken
            tokenManager.saveToken(Constants.KEY_ACCESS_TOKEN, token)
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyEmail(code: String): Result<Unit> {
        return try {
            val user = supabaseClient.auth.currentUserOrNull()
            if (user?.emailConfirmedAt != null) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Email not yet verified. Please check your inbox."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            supabaseClient.auth.resetPasswordForEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            supabaseClient.auth.updateUser {
                password = newPassword
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setupMfa(): Result<String> {
        return Result.success("MFA_SETUP_PLACEHOLDER")
    }

    override suspend fun verifyMfa(code: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun disableMfa(code: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun getAccessToken(): String? {
        return tokenManager.getToken(Constants.KEY_ACCESS_TOKEN)
    }

    override suspend fun isTokenValid(): Boolean {
        val token = tokenManager.getToken(Constants.KEY_ACCESS_TOKEN) ?: return false
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return false
            val payload = String(
                android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE)
            )
            val expIndex = payload.indexOf("\"exp\":")
            if (expIndex == -1) return false
            val expEnd = payload.indexOf(",", expIndex)
            val expStr = if (expEnd == -1) payload.substring(expIndex + 6) else payload.substring(expIndex + 6, expEnd)
            val exp = expStr.trim().toLongOrNull() ?: return false
            (exp * 1000) > System.currentTimeMillis()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateLastActivity() {
        userPreferences.updateLastActivity()
    }

    override suspend fun checkSessionTimeout(): Boolean {
        val lastActivity = userPreferences.lastActivity.first()
        val timeout = userPreferences.sessionTimeout.first()
        return (System.currentTimeMillis() - lastActivity) > timeout
    }

    override suspend fun authenticateWithBiometric(): Result<Unit> {
        return try {
            val session = supabaseClient.auth.currentSessionOrNull()
            if (session != null) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("No active session. Please log in with your password."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Biometric authentication not available. Please log in with your password."))
        }
    }

    override suspend fun isTrustedDevice(): Boolean {
        return userPreferences.isTrustedDevice.first()
    }

    override suspend fun setTrustedDevice(trusted: Boolean) {
        userPreferences.setTrustedDevice(trusted)
    }

    private fun isRateLimited(email: String): Boolean {
        val attempts = loginAttempts[email.lowercase()] ?: return false
        val now = System.currentTimeMillis()
        attempts.removeAll { (now - it) > Constants.RATE_LIMIT_WINDOW_MS }
        return attempts.size >= Constants.MAX_LOGIN_ATTEMPTS
    }

    private fun recordLoginAttempt(email: String) {
        val key = email.lowercase()
        val attempts = loginAttempts.getOrDefault(key, mutableListOf())
        attempts.add(System.currentTimeMillis())
        loginAttempts[key] = attempts
    }

    private fun UserInfo.toDomainUser() = User(
        id = id,
        email = email ?: "",
        name = userMetadata?.get("name")?.toString()?.removeSurrounding("\"") ?: "",
        isEmailVerified = emailConfirmedAt != null,
        isMfaEnabled = false,
        storageUsed = 0L,
        storageLimit = 5L * 1024 * 1024 * 1024,
        createdAt = createdAt?.toEpochMilliseconds() ?: System.currentTimeMillis()
    )

    private fun mapSupabaseError(e: Exception): String {
        val message = e.message?.lowercase() ?: return "An error occurred"
        return when {
            "invalid login credentials" in message -> "Invalid email or password."
            "email already registered" in message || "user already registered" in message -> "An account with this email already exists."
            "password should be at least" in message -> "Password is too weak."
            "email not confirmed" in message -> "Please verify your email before logging in."
            "rate limit" in message -> "Too many attempts. Please try again later."
            else -> e.message ?: "An error occurred"
        }
    }
}
