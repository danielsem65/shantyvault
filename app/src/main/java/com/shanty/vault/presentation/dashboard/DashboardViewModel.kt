package com.shanty.vault.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shanty.vault.domain.model.Activity
import com.shanty.vault.domain.model.VaultFile
import com.shanty.vault.domain.repository.VaultRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardUiState(
    val recentFiles: List<VaultFile> = emptyList(),
    val favoriteFiles: List<VaultFile> = emptyList(),
    val recentActivity: List<Activity> = emptyList(),
    val storageUsed: Long = 0L,
    val storageLimit: Long = 5L * 1024 * 1024 * 1024,
    val storagePercentUsed: Double = 0.0,
    val isLoading: Boolean = true
)

class DashboardViewModel(
    private val vaultRepository: VaultRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            vaultRepository.getRecentFiles(10).collect { files ->
                _uiState.update { it.copy(recentFiles = files) }
            }
        }
        viewModelScope.launch {
            vaultRepository.getFavoriteFiles().collect { files ->
                _uiState.update { it.copy(favoriteFiles = files) }
            }
        }
        viewModelScope.launch {
            vaultRepository.getRecentActivities(20).collect { activities ->
                _uiState.update { it.copy(recentActivity = activities) }
            }
        }
        viewModelScope.launch {
            vaultRepository.getStorageUsage().collect { usage ->
                _uiState.update {
                    it.copy(
                        storageUsed = usage.used,
                        storageLimit = usage.limit,
                        storagePercentUsed = usage.percentUsed,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadDashboard()
    }
}
