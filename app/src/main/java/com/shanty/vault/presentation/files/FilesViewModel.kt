package com.shanty.vault.presentation.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shanty.vault.domain.model.Folder
import com.shanty.vault.domain.model.VaultFile
import com.shanty.vault.domain.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FilesUiState(
    val files: List<VaultFile> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val isLoading: Boolean = true,
    val selectedFolderId: String? = null,
    val currentPath: String = "/",
    val isGridMode: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val fileToDelete: VaultFile? = null
)

@HiltViewModel
class FilesViewModel @Inject constructor(
    private val vaultRepository: VaultRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FilesUiState())
    val uiState: StateFlow<FilesUiState> = _uiState.asStateFlow()

    init {
        loadFiles()
        loadFolders()
    }

    private fun loadFiles() {
        viewModelScope.launch {
            vaultRepository.getFilesByFolder(null).collect { files ->
                _uiState.update { it.copy(files = files, isLoading = false) }
            }
        }
    }

    private fun loadFolders() {
        viewModelScope.launch {
            vaultRepository.getRootFolders().collect { folders ->
                _uiState.update { it.copy(folders = folders) }
            }
        }
    }

    fun navigateToFolder(folderId: String) {
        _uiState.update { it.copy(selectedFolderId = folderId, isLoading = true) }
        viewModelScope.launch {
            vaultRepository.getFilesByFolder(folderId).collect { files ->
                _uiState.update { it.copy(files = files, isLoading = false) }
            }
        }
        viewModelScope.launch {
            vaultRepository.getSubFolders(folderId).collect { folders ->
                _uiState.update { it.copy(folders = folders) }
            }
        }
    }

    fun navigateBack() {
        _uiState.update { it.copy(selectedFolderId = null, isLoading = true) }
        loadFiles()
        loadFolders()
    }

    fun toggleGridMode() {
        _uiState.update { it.copy(isGridMode = !it.isGridMode) }
    }

    fun requestDeleteFile(file: VaultFile) {
        _uiState.update { it.copy(fileToDelete = file, showDeleteConfirm = true) }
    }

    fun confirmDelete() {
        val file = _uiState.value.fileToDelete ?: return
        viewModelScope.launch {
            vaultRepository.deleteFile(file.id)
            _uiState.update { it.copy(showDeleteConfirm = false, fileToDelete = null) }
        }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(showDeleteConfirm = false, fileToDelete = null) }
    }

    fun toggleFavorite(fileId: String) {
        viewModelScope.launch { vaultRepository.toggleFavorite(fileId) }
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        if (_uiState.value.selectedFolderId != null) {
            navigateToFolder(_uiState.value.selectedFolderId!!)
        } else {
            loadFiles()
            loadFolders()
        }
    }
}
