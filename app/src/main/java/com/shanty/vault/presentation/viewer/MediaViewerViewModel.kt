package com.shanty.vault.presentation.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shanty.vault.domain.model.VaultFile
import com.shanty.vault.domain.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MediaViewerUiState(
    val file: VaultFile? = null,
    val isLoading: Boolean = true,
    val isPlaying: Boolean = false,
    val isFullscreen: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L
)

@HiltViewModel
class MediaViewerViewModel @Inject constructor(
    private val vaultRepository: VaultRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MediaViewerUiState())
    val uiState: StateFlow<MediaViewerUiState> = _uiState.asStateFlow()

    fun loadFile(fileId: String) {
        viewModelScope.launch {
            val file = vaultRepository.getFileById(fileId)
            _uiState.update { it.copy(file = file, isLoading = false) }
        }
    }

    fun togglePlayback() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun toggleFullscreen() {
        _uiState.update { it.copy(isFullscreen = !it.isFullscreen) }
    }

    fun updatePosition(position: Long) {
        _uiState.update { it.copy(currentPosition = position) }
    }

    fun updateDuration(duration: Long) {
        _uiState.update { it.copy(duration = duration) }
    }
}
