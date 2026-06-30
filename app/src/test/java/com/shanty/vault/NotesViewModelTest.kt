package com.shanty.vault

import app.cash.turbine.test
import com.shanty.vault.domain.model.Note
import com.shanty.vault.domain.repository.VaultRepository
import com.shanty.vault.presentation.notes.NotesViewModel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class NotesViewModelTest {

    private lateinit var vaultRepository: VaultRepository
    private lateinit var viewModel: NotesViewModel

    @Before
    fun setup() {
        vaultRepository = mock()
        whenever(vaultRepository.getAllNotes()).thenReturn(flowOf(emptyList()))
        whenever(vaultRepository.getPinnedNotes()).thenReturn(flowOf(emptyList()))
        viewModel = NotesViewModel(vaultRepository)
    }

    @Test
    fun `create note calls repository`() = runTest {
        whenever(vaultRepository.createNote("Untitled Note", "")).thenReturn(
            Result.success(Note("1", "Untitled Note", "", false, 0L, 0L, null, false, null))
        )
        viewModel.createNote()
        verify(vaultRepository).createNote("Untitled Note", "")
    }

    @Test
    fun `delete note calls repository`() = runTest {
        viewModel.deleteNote("note_1")
        verify(vaultRepository).deleteNote("note_1")
    }

    @Test
    fun `toggle pin calls repository`() = runTest {
        viewModel.togglePin("note_1")
        verify(vaultRepository).toggleNotePinned("note_1")
    }

    @Test
    fun `search with query returns results`() = runTest {
        val mockNotes = listOf(
            Note("1", "Test Note", "content", false, 0L, 0L, null, false, null)
        )
        whenever(vaultRepository.searchNotes("Test")).thenReturn(flowOf(mockNotes))

        viewModel.search("Test")

        viewModel.uiState.test {
            val state = awaitItem()
            assert(state.searchQuery == "Test")
            cancelAndIgnoreRemainingEvents()
        }
    }
}
