package com.shanty.vault.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shanty.vault.domain.model.Note
import com.shanty.vault.domain.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val pinnedNotes: List<Note> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val searchResults: List<Note> = emptyList()
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val vaultRepository: VaultRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    init {
        loadNotes()
    }

    private fun loadNotes() {
        viewModelScope.launch {
            vaultRepository.getAllNotes().collect { notes ->
                _uiState.update { it.copy(notes = notes, isLoading = false) }
            }
        }
        viewModelScope.launch {
            vaultRepository.getPinnedNotes().collect { pinned ->
                _uiState.update { it.copy(pinnedNotes = pinned) }
            }
        }
    }

    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList()) }
            return
        }
        viewModelScope.launch {
            vaultRepository.searchNotes(query).collect { results ->
                _uiState.update { it.copy(searchResults = results) }
            }
        }
    }

    fun createNote() {
        viewModelScope.launch {
            vaultRepository.createNote("Untitled Note", "")
            loadNotes()
        }
    }

    fun togglePin(noteId: String) {
        viewModelScope.launch { vaultRepository.toggleNotePinned(noteId) }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch { vaultRepository.deleteNote(noteId) }
    }
}
