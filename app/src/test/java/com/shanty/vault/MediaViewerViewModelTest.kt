package com.shanty.vault

import app.cash.turbine.test
import com.shanty.vault.domain.model.VaultFile
import com.shanty.vault.domain.repository.VaultRepository
import com.shanty.vault.presentation.viewer.MediaViewerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class MediaViewerViewModelTest {

    private lateinit var vaultRepository: VaultRepository
    private lateinit var viewModel: MediaViewerViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        vaultRepository = mock()
        viewModel = MediaViewerViewModel(vaultRepository)
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
            assert(state.file == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `load file updates state`() = runTest {
        val file = VaultFile(
            id = "1", name = "photo.jpg", extension = "jpg", mimeType = "image/jpeg",
            size = 1024L, folderId = null, remotePath = "https://example.com/photo.jpg",
            localPath = null, thumbnailPath = null, isFavorite = false,
            createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis(),
            uploadedAt = System.currentTimeMillis(), downloadedAt = null, checksum = null
        )
        whenever(vaultRepository.getFileById("1")).thenReturn(file)

        viewModel.loadFile("1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assert(!state.isLoading)
            assert(state.file?.name == "photo.jpg")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggle playback updates state`() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            viewModel.togglePlayback()
            val state = awaitItem()
            assert(state.isPlaying)
            viewModel.togglePlayback()
            val state2 = awaitItem()
            assert(!state2.isPlaying)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggle fullscreen updates state`() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            viewModel.toggleFullscreen()
            val state = awaitItem()
            assert(state.isFullscreen)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clear error resets error message`() {
        viewModel.clearError()
        assert(viewModel.uiState.value.errorMessage == null)
    }

    @Test
    fun `clear success resets success message`() {
        viewModel.clearSuccess()
        assert(viewModel.uiState.value.successMessage == null)
    }
}
