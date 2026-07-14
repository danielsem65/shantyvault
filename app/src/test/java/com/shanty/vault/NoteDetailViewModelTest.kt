package com.shanty.vault

import app.cash.turbine.test
import com.shanty.vault.domain.model.Note
import com.shanty.vault.domain.repository.VaultRepository
import com.shanty.vault.presentation.notes.NoteDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class NoteDetailViewModelTest {

    private lateinit var vaultRepository: VaultRepository
    private lateinit var viewModel: NoteDetailViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        vaultRepository = mock()
        viewModel = NoteDetailViewModel(vaultRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assert(state.isLoading)
            assert(state.title.isEmpty())
            assert(state.content.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `load note updates state`() = runTest {
        val note = Note(
            id = "1", title = "Test Note", content = "Test content",
            isPinned = false, createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(), colorHex = "#FF0000",
            hasChecklist = false, checklistData = null
        )
        whenever(vaultRepository.getNoteById("1")).thenReturn(note)

        viewModel.loadNote("1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assert(!state.isLoading)
            assert(state.title == "Test Note")
            assert(state.content == "Test content")
            assert(state.colorHex == "#FF0000")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `update title updates state`() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            viewModel.updateTitle("New Title")
            val state = awaitItem()
            assert(state.title == "New Title")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `update content updates state`() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            viewModel.updateContent("New content")
            val state = awaitItem()
            assert(state.content == "New content")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggle pinned updates state`() = runTest {
        val note = Note(
            id = "1", title = "Test Note", content = "Test content",
            isPinned = false, createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(), colorHex = null,
            hasChecklist = false, checklistData = null
        )
        whenever(vaultRepository.getNoteById("1")).thenReturn(note)
        whenever(vaultRepository.toggleNotePinned("1")).thenReturn(Result.success(Unit))

        viewModel.loadNote("1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.togglePinned()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(vaultRepository).toggleNotePinned("1")
    }

    @Test
    fun `update note color updates state`() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            viewModel.updateNoteColor("#00FF00")
            val state = awaitItem()
            assert(state.colorHex == "#00FF00")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save without note does nothing`() = runTest {
        viewModel.save()
        testDispatcher.scheduler.advanceUntilIdle()
    }
}
