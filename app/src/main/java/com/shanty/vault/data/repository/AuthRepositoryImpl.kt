package com.shanty.vault.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.shanty.vault.data.local.UserPreferences
import com.shanty.vault.data.remote.ApiService
import com.shanty.vault.domain.model.User
import com.shanty.vault.domain.repository.AuthRepository
import com.shanty.vault.security.TokenManager
import com.shanty.vault.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val apiService: ApiService,
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
        firebaseAuth.addAuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                _isAuthenticated.value = true
            } else {
                _currentUser.value = null
                _isAuthenticated.value = false
            }
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            if (isRateLimited(email)) {
                return Result.failure(Exception("Too many login attempts. Please try again later."))
            }

            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Authentication failed")

            if (!firebaseUser.isEmailVerified) {
                firebaseAuth.signOut()
                return Result.failure(Exception("Please verify your email before logging in."))
            }

            val idToken = firebaseUser.getIdToken(true).await().token ?: ""
            tokenManager.saveToken(Constants.KEY_ACCESS_TOKEN, idToken)
            userPreferences.setUserId(firebaseUser.uid)
            userPreferences.updateLastActivity()
            userPreferences.setFirstLogin(false)

            val user = User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                name = firebaseUser.displayName ?: "",
                isEmailVerified = firebaseUser.isEmailVerified,
                isMfaEnabled = false,
                storageUsed = 0L,
                storageLimit = 5L * 1024 * 1024 * 1024,
                createdAt = firebaseUser.metadata?.creationTimestamp ?: System.currentTimeMillis()
            )
            _currentUser.value = user
            loginAttempts.remove(email.lowercase())
            Result.success(user)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            recordLoginAttempt(email)
            Result.failure(Exception("Invalid email or password."))
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(Exception("Password is too weak."))
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(Exception("An account with this email already exists."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String, name: String): Result<User> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Registration failed")

            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()
            firebaseUser.sendEmailVerification().await()

            firebaseAuth.signOut()

            Result.success(User(
                id = firebaseUser.uid,
                email = email,
                name = name,
                isEmailVerified = false,
                isMfaEnabled = false,
                storageUsed = 0L,
                storageLimit = 5L * 1024 * 1024 * 1024,
                createdAt = System.currentTimeMillis()
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
        tokenManager.clearAllTokens()
        _currentUser.value = null
        _isSessionExpired.value = false
        userPreferences.clearAll()
    }

    override suspend fun refreshToken(): Result<String> {
        return try {
            val user = firebaseAuth.currentUser ?: throw Exception("Not authenticated")
            val token = user.getIdToken(true).await().token ?: throw Exception("Failed to refresh token")
            tokenManager.saveToken(Constants.KEY_ACCESS_TOKEN, token)
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyEmail(code: String): Result<Unit> {
        return try {
            firebaseAuth.currentUser?.apply {
                if (isEmailVerified) return Result.success(Unit)
                reload().await()
                if (isEmailVerified) Result.success(Unit)
                else Result.failure(Exception("Email not yet verified."))
            } ?: Result.failure(Exception("No user logged in."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: throw Exception("Not authenticated")
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(
                user.email ?: "", currentPassword
            )
            user.reauthenticate(credential).await()
            user.updatePassword(newPassword).await()
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
        return Result.failure(Exception("Biometric not configured"))
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
}
