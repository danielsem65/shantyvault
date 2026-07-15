package com.shanty.vault.presentation.files

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shanty.vault.domain.model.Folder
import com.shanty.vault.domain.model.VaultFile
import com.shanty.vault.domain.repository.VaultRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

data class FilesUiState(
    val files: List<VaultFile> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val isLoading: Boolean = true,
    val selectedFolderId: String? = null,
    val currentPath: String = "/",
    val isGridMode: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val fileToDelete: VaultFile? = null,
    val isUploading: Boolean = false,
    val uploadProgress: String = "",
    val showRenameDialog: Boolean = false,
    val fileToRename: VaultFile? = null,
    val renameValue: String = "",
    val showMoveDialog: Boolean = false,
    val fileToMove: VaultFile? = null,
    val selectedMoveFolderId: String? = null,
    val showCreateFolderDialog: Boolean = false,
    val newFolderName: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class FilesViewModel(
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

    fun uploadFile(file: File) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, uploadProgress = "Uploading ${file.name}...") }
            val result = vaultRepository.uploadFileWithEncryption(file, _uiState.value.selectedFolderId)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(isUploading = false, uploadProgress = "",
                            successMessage = "${file.name} uploaded successfully")
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isUploading = false, uploadProgress = "",
                            errorMessage = "Failed to upload ${file.name}: ${error.message}")
                    }
                }
            )
        }
    }

    fun uploadFileUri(context: android.content.Context, uri: Uri, fileName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, uploadProgress = "Uploading $fileName...") }
            try {
                val tempFile = File(context.cacheDir, "upload_$fileName")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    tempFile.outputStream().use { output -> input.copyTo(output) }
                }
                val result = vaultRepository.uploadFileWithEncryption(tempFile, _uiState.value.selectedFolderId)
                tempFile.delete()
                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(isUploading = false, uploadProgress = "",
                                successMessage = "$fileName uploaded successfully")
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(isUploading = false, uploadProgress = "",
                                errorMessage = "Failed to upload $fileName: ${e.message}")
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isUploading = false, uploadProgress = "",
                        errorMessage = "Failed to upload $fileName: ${e.message}")
                }
            }
        }
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

    fun requestRenameFile(file: VaultFile) {
        _uiState.update { it.copy(fileToRename = file, renameValue = file.name, showRenameDialog = true) }
    }

    fun updateRenameValue(value: String) {
        _uiState.update { it.copy(renameValue = value) }
    }

    fun confirmRename() {
        val file = _uiState.value.fileToRename ?: return
        val newName = _uiState.value.renameValue.trim()
        if (newName.isBlank()) return
        viewModelScope.launch {
            vaultRepository.renameFile(file.id, newName)
            _uiState.update { it.copy(showRenameDialog = false, fileToRename = null, renameValue = "") }
        }
    }

    fun cancelRename() {
        _uiState.update { it.copy(showRenameDialog = false, fileToRename = null, renameValue = "") }
    }

    fun requestMoveFile(file: VaultFile) {
        _uiState.update { it.copy(fileToMove = file, showMoveDialog = true) }
    }

    fun selectMoveFolder(folderId: String?) {
        _uiState.update { it.copy(selectedMoveFolderId = folderId) }
    }

    fun confirmMove() {
        val file = _uiState.value.fileToMove ?: return
        val folderId = _uiState.value.selectedMoveFolderId
        viewModelScope.launch {
            vaultRepository.moveFile(file.id, folderId)
            _uiState.update { it.copy(showMoveDialog = false, fileToMove = null, selectedMoveFolderId = null) }
        }
    }

    fun cancelMove() {
        _uiState.update { it.copy(showMoveDialog = false, fileToMove = null, selectedMoveFolderId = null) }
    }

    fun requestCreateFolder() {
        _uiState.update { it.copy(showCreateFolderDialog = true, newFolderName = "") }
    }

    fun updateNewFolderName(value: String) {
        _uiState.update { it.copy(newFolderName = value) }
    }

    fun confirmCreateFolder() {
        val name = _uiState.value.newFolderName.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            vaultRepository.createFolder(name, _uiState.value.selectedFolderId)
            _uiState.update { it.copy(showCreateFolderDialog = false, newFolderName = "") }
        }
    }

    fun cancelCreateFolder() {
        _uiState.update { it.copy(showCreateFolderDialog = false, newFolderName = "") }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
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
