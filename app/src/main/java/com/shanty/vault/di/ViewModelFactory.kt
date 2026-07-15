package com.shanty.vault.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shanty.vault.presentation.auth.AuthViewModel
import com.shanty.vault.presentation.dashboard.DashboardViewModel
import com.shanty.vault.presentation.files.FilesViewModel
import com.shanty.vault.presentation.notes.NoteDetailViewModel
import com.shanty.vault.presentation.notes.NotesViewModel
import com.shanty.vault.presentation.search.SearchViewModel
import com.shanty.vault.presentation.settings.SettingsViewModel
import com.shanty.vault.presentation.viewer.MediaViewerViewModel

class ViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(container.authRepository)
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> DashboardViewModel(container.vaultRepository)
            modelClass.isAssignableFrom(FilesViewModel::class.java) -> FilesViewModel(container.vaultRepository)
            modelClass.isAssignableFrom(NotesViewModel::class.java) -> NotesViewModel(container.vaultRepository)
            modelClass.isAssignableFrom(NoteDetailViewModel::class.java) -> NoteDetailViewModel(container.vaultRepository)
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> SearchViewModel(container.vaultRepository)
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(container.userPreferences, container.authRepository, container.vaultRepository)
            modelClass.isAssignableFrom(MediaViewerViewModel::class.java) -> MediaViewerViewModel(container.vaultRepository)
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
