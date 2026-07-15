package com.shanty.vault.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shanty.vault.data.local.UserPreferences
import com.shanty.vault.domain.repository.AuthRepository
import com.shanty.vault.domain.repository.VaultRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: String = "system",
    val biometricsEnabled: Boolean = false,
    val mfaEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val storageUsed: Long = 0L,
    val storageLimit: Long = 5L * 1024 * 1024 * 1024,
    val isLoading: Boolean = true,
    val showChangePasswordDialog: Boolean = false,
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val showAboutDialog: Boolean = false,
    val message: String? = null
)

class SettingsViewModel(
    private val userPreferences: UserPreferences,
    private val authRepository: AuthRepository,
    private val vaultRepository: VaultRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            userPreferences.themeMode.collect { mode ->
                _uiState.update { it.copy(themeMode = mode) }
            }
        }
        viewModelScope.launch {
            userPreferences.isBiometricEnabled.collect { enabled ->
                _uiState.update { it.copy(biometricsEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            userPreferences.isMfaEnabled.collect { enabled ->
                _uiState.update { it.copy(mfaEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            userPreferences.notificationsEnabled.collect { enabled ->
                _uiState.update { it.copy(notificationsEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            vaultRepository.getStorageUsage().collect { usage ->
                _uiState.update {
                    it.copy(
                        storageUsed = usage.used,
                        storageLimit = usage.limit,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            userPreferences.setThemeMode(mode)
            _uiState.update { it.copy(themeMode = mode) }
        }
    }

    fun toggleBiometrics() {
        viewModelScope.launch {
            val newState = !_uiState.value.biometricsEnabled
            userPreferences.setBiometricEnabled(newState)
            _uiState.update { it.copy(biometricsEnabled = newState) }
        }
    }

    fun toggleMfa() {
        viewModelScope.launch {
            val newState = !_uiState.value.mfaEnabled
            userPreferences.setMfaEnabled(newState)
            _uiState.update { it.copy(mfaEnabled = newState) }
        }
    }

    fun toggleNotifications() {
        viewModelScope.launch {
            val newState = !_uiState.value.notificationsEnabled
            _uiState.update { it.copy(notificationsEnabled = newState) }
        }
    }

    fun showChangePasswordDialog() {
        _uiState.update { it.copy(showChangePasswordDialog = true, currentPassword = "", newPassword = "", confirmPassword = "") }
    }

    fun hideChangePasswordDialog() {
        _uiState.update { it.copy(showChangePasswordDialog = false) }
    }

    fun updateCurrentPassword(password: String) {
        _uiState.update { it.copy(currentPassword = password) }
    }

    fun updateNewPassword(password: String) {
        _uiState.update { it.copy(newPassword = password) }
    }

    fun updateConfirmPassword(password: String) {
        _uiState.update { it.copy(confirmPassword = password) }
    }

    fun changePassword() {
        val current = _uiState.value.currentPassword
        val new = _uiState.value.newPassword
        val confirm = _uiState.value.confirmPassword

        if (current.isBlank() || new.isBlank()) {
            _uiState.update { it.copy(message = "Please fill in all fields") }
            return
        }
        if (new != confirm) {
            _uiState.update { it.copy(message = "New passwords do not match") }
            return
        }
        if (new.length < 12) {
            _uiState.update { it.copy(message = "Password must be at least 12 characters") }
            return
        }

        viewModelScope.launch {
            val result = authRepository.changePassword(current, new)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(showChangePasswordDialog = false, message = "Password changed successfully")
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(message = e.message ?: "Failed to change password") }
                }
            )
        }
    }

    fun showAboutDialog() {
        _uiState.update { it.copy(showAboutDialog = true) }
    }

    fun hideAboutDialog() {
        _uiState.update { it.copy(showAboutDialog = false) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
