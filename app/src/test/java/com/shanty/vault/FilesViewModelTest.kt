package com.shanty.vault

import app.cash.turbine.test
import com.shanty.vault.domain.model.Activity
import com.shanty.vault.domain.model.Folder
import com.shanty.vault.domain.model.Note
import com.shanty.vault.domain.model.VaultFile
import com.shanty.vault.domain.repository.VaultRepository
import com.shanty.vault.presentation.files.FilesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class FilesViewModelTest {

    private lateinit var vaultRepository: VaultRepository
    private lateinit var viewModel: FilesViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        vaultRepository = mock()
        whenever(vaultRepository.getFilesByFolder(null)).thenReturn(flowOf(emptyList()))
        whenever(vaultRepository.getRootFolders()).thenReturn(flowOf(emptyList()))
        viewModel = FilesViewModel(vaultRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading with empty lists`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assert(state.files.isEmpty())
            assert(state.folders.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggle grid mode updates state`() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            viewModel.toggleGridMode()
            val state = awaitItem()
            assert(state.isGridMode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `request delete shows confirm dialog`() = runTest {
        val file = VaultFile(
            id = "1", name = "test.txt", extension = "txt", mimeType = "text/plain",
            size = 100L, folderId = null, remotePath = "", localPath = null,
            thumbnailPath = null, isFavorite = false, createdAt = 0L, updatedAt = 0L,
            uploadedAt = null, downloadedAt = null, checksum = null
        )
        viewModel.uiState.test {
            skipItems(1)
            viewModel.requestDeleteFile(file)
            val state = awaitItem()
            assert(state.showDeleteConfirm)
            assert(state.fileToDelete?.id == "1")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cancel delete hides dialog`() = runTest {
        val file = VaultFile(
            id = "1", name = "test.txt", extension = "txt", mimeType = "text/plain",
            size = 100L, folderId = null, remotePath = "", localPath = null,
            thumbnailPath = null, isFavorite = false, createdAt = 0L, updatedAt = 0L,
            uploadedAt = null, downloadedAt = null, checksum = null
        )
        viewModel.requestDeleteFile(file)
        viewModel.uiState.test {
            skipItems(1)
            viewModel.cancelDelete()
            val state = awaitItem()
            assert(!state.showDeleteConfirm)
            assert(state.fileToDelete == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `confirm delete calls repository`() = runTest {
        val file = VaultFile(
            id = "1", name = "test.txt", extension = "txt", mimeType = "text/plain",
            size = 100L, folderId = null, remotePath = "", localPath = null,
            thumbnailPath = null, isFavorite = false, createdAt = 0L, updatedAt = 0L,
            uploadedAt = null, downloadedAt = null, checksum = null
        )
        viewModel.requestDeleteFile(file)
        viewModel.confirmDelete()
        testDispatcher.scheduler.advanceUntilIdle()
        verify(vaultRepository).deleteFile("1")
    }

    @Test
    fun `request rename shows dialog with file name`() = runTest {
        val file = VaultFile(
            id = "1", name = "test.txt", extension = "txt", mimeType = "text/plain",
            size = 100L, folderId = null, remotePath = "", localPath = null,
            thumbnailPath = null, isFavorite = false, createdAt = 0L, updatedAt = 0L,
            uploadedAt = null, downloadedAt = null, checksum = null
        )
        viewModel.uiState.test {
            skipItems(1)
            viewModel.requestRenameFile(file)
            val state = awaitItem()
            assert(state.showRenameDialog)
            assert(state.renameValue == "test.txt")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `confirm rename calls repository`() = runTest {
        val file = VaultFile(
            id = "1", name = "test.txt", extension = "txt", mimeType = "text/plain",
            size = 100L, folderId = null, remotePath = "", localPath = null,
            thumbnailPath = null, isFavorite = false, createdAt = 0L, updatedAt = 0L,
            uploadedAt = null, downloadedAt = null, checksum = null
        )
        viewModel.requestRenameFile(file)
        viewModel.updateRenameValue("renamed.txt")
        viewModel.confirmRename()
        testDispatcher.scheduler.advanceUntilIdle()
        verify(vaultRepository).renameFile("1", "renamed.txt")
    }

    @Test
    fun `create folder calls repository`() = runTest {
        viewModel.requestCreateFolder()
        viewModel.updateNewFolderName("My Folder")
        viewModel.confirmCreateFolder()
        testDispatcher.scheduler.advanceUntilIdle()
        verify(vaultRepository).createFolder("My Folder", null)
    }

    @Test
    fun `refresh updates loading state`() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            viewModel.refresh()
            val state = awaitItem()
            assert(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
