package com.shanty.vault.presentation.viewer

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shanty.vault.domain.model.VaultFile
import com.shanty.vault.domain.repository.VaultRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

data class MediaViewerUiState(
    val file: VaultFile? = null,
    val isLoading: Boolean = true,
    val isPlaying: Boolean = false,
    val isFullscreen: Boolean = false,
    val isDownloading: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class MediaViewerViewModel(
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

    fun downloadFile(context: Context) {
        val file = _uiState.value.file ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDownloading = true) }
            val destination = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                file.name
            )
            val result = vaultRepository.downloadFile(file.id, destination)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(isDownloading = false, successMessage = "Downloaded to ${destination.absolutePath}")
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isDownloading = false, errorMessage = "Download failed: ${e.message}")
                    }
                }
            )
        }
    }

    fun shareFile(context: Context) {
        val file = _uiState.value.file ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDownloading = true) }
            val destination = File(context.cacheDir, file.name)
            val result = vaultRepository.downloadFile(file.id, destination)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isDownloading = false) }
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        destination
                    )
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = file.mimeType
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share ${file.name}"))
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isDownloading = false, errorMessage = "Share failed: ${e.message}")
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
