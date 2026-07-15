package com.shanty.vault.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shanty.vault.domain.model.Note
import com.shanty.vault.domain.repository.VaultRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class NoteDetailUiState(
    val note: Note? = null,
    val title: String = "",
    val content: String = "",
    val isPinned: Boolean = false,
    val colorHex: String? = null,
    val isSaving: Boolean = false,
    val isLoading: Boolean = true
)

class NoteDetailViewModel(
    private val vaultRepository: VaultRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteDetailUiState())
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()
    private var saveJob: Job? = null

    fun loadNote(noteId: String) {
        viewModelScope.launch {
            val note = vaultRepository.getNoteById(noteId)
            if (note != null) {
                _uiState.update {
                    it.copy(note = note, title = note.title, content = note.content,
                        isPinned = note.isPinned, colorHex = note.colorHex, isLoading = false)
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
        scheduleSave()
    }

    fun updateContent(content: String) {
        _uiState.update { it.copy(content = content) }
        scheduleSave()
    }

    fun togglePinned() {
        val noteId = _uiState.value.note?.id ?: return
        viewModelScope.launch { vaultRepository.toggleNotePinned(noteId) }
        _uiState.update { it.copy(isPinned = !it.isPinned) }
    }

    fun updateNoteColor(colorHex: String?) {
        _uiState.update { it.copy(colorHex = colorHex) }
        scheduleSave()
    }

    fun save() {
        val note = _uiState.value.note ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            vaultRepository.updateNote(
                note.id,
                _uiState.value.title,
                _uiState.value.content,
                _uiState.value.isPinned,
                _uiState.value.colorHex
            )
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    private fun scheduleSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(2000)
            save()
        }
    }
}
