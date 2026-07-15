package com.shanty.vault.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shanty.vault.domain.model.VaultFile
import com.shanty.vault.domain.repository.VaultRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val results: List<VaultFile> = emptyList(),
    val isSearching: Boolean = false,
    val filterType: String? = null,
    val hasSearched: Boolean = false
)

class SearchViewModel(
    private val vaultRepository: VaultRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            if (query.isNotBlank()) {
                _uiState.update { it.copy(isSearching = true) }
                vaultRepository.searchFiles(query).collect { results ->
                    _uiState.update { it.copy(results = results, isSearching = false, hasSearched = true) }
                }
            } else {
                _uiState.update { it.copy(results = emptyList(), isSearching = false, hasSearched = false) }
            }
        }
    }

    fun setFilter(type: String?) {
        _uiState.update { it.copy(filterType = type) }
    }
}
