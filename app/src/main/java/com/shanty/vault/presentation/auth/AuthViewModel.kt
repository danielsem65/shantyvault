package com.shanty.vault.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shanty.vault.domain.repository.AuthRepository
import com.shanty.vault.presentation.auth.AuthState.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String? = null) : AuthState()
    data class Error(val message: String) : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Registered : AuthState()
    object EmailSent : AuthState()
    object PasswordReset : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = Error("Please fill in all fields")
            return
        }
        viewModelScope.launch {
            _authState.value = Loading
            val result = authRepository.login(email.trim(), password)
            _authState.value = result.fold(
                onSuccess = { Authenticated },
                onFailure = { Error(it.message ?: "Login failed") }
            )
        }
    }

    fun register(email: String, password: String, name: String) {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            _authState.value = Error("Please fill in all fields")
            return
        }
        viewModelScope.launch {
            _authState.value = Loading
            val result = authRepository.register(email.trim(), password, name.trim())
            _authState.value = result.fold(
                onSuccess = {
                    authRepository.logout()
                    Registered
                },
                onFailure = { Error(it.message ?: "Registration failed") }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = Unauthenticated
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _authState.value = Error("Please enter your email")
            return
        }
        viewModelScope.launch {
            _authState.value = Loading
            val result = authRepository.resetPassword(email.trim())
            _authState.value = result.fold(
                onSuccess = { PasswordReset },
                onFailure = { Error(it.message ?: "Failed to send reset email") }
            )
        }
    }

    fun verifyEmail() {
        viewModelScope.launch {
            _authState.value = Loading
            val user = firebaseAuth()
            if (user?.isEmailVerified == true) {
                _authState.value = Success("Email verified")
            } else {
                try {
                    user?.reload()
                    if (user?.isEmailVerified == true) {
                        _authState.value = Success("Email verified")
                    } else {
                        _authState.value = Error("Please verify your email and try again")
                    }
                } catch (e: Exception) {
                    _authState.value = Error("Failed to check verification status")
                }
            }
        }
    }

    fun authenticateWithBiometric() {
        viewModelScope.launch {
            _authState.value = Loading
            val result = authRepository.authenticateWithBiometric()
            _authState.value = result.fold(
                onSuccess = { Authenticated },
                onFailure = { Error(it.message ?: "Biometric authentication failed") }
            )
        }
    }

    fun resetState() {
        _authState.value = Idle
    }

    private fun firebaseAuth(): com.google.firebase.auth.FirebaseUser? {
        return com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    }
}
