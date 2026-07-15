package com.shanty.vault

import app.cash.turbine.test
import com.shanty.vault.domain.model.Note
import com.shanty.vault.domain.repository.VaultRepository
import com.shanty.vault.presentation.notes.NotesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.coVerify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    private lateinit var vaultRepository: VaultRepository

    @Before
    fun setup() {
        vaultRepository = mock()
        whenever(vaultRepository.getAllNotes()).thenReturn(flowOf(emptyList()))
        whenever(vaultRepository.getPinnedNotes()).thenReturn(flowOf(emptyList()))
    }

    @Test
    fun `create note calls repository`() = runTest {
        val viewModel = NotesViewModel(vaultRepository)
        whenever(vaultRepository.createNote("Untitled Note", "")).thenReturn(
            Result.success(Note("1", "Untitled Note", "", false, 0L, 0L, null, false, null))
        )
        viewModel.createNote()
        advanceUntilIdle()
        coVerify { vaultRepository.createNote("Untitled Note", "") }
    }

    @Test
    fun `delete note calls repository`() = runTest {
        val viewModel = NotesViewModel(vaultRepository)
        viewModel.deleteNote("note_1")
        advanceUntilIdle()
        coVerify { vaultRepository.deleteNote("note_1") }
    }

    @Test
    fun `toggle pin calls repository`() = runTest {
        val viewModel = NotesViewModel(vaultRepository)
        viewModel.togglePin("note_1")
        advanceUntilIdle()
        coVerify { vaultRepository.toggleNotePinned("note_1") }
    }

    @Test
    fun `search with query returns results`() = runTest {
        val viewModel = NotesViewModel(vaultRepository)
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
