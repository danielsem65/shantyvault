package com.shanty.vault

import app.cash.turbine.test
import com.shanty.vault.domain.model.VaultFile
import com.shanty.vault.domain.repository.VaultRepository
import com.shanty.vault.presentation.search.SearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private lateinit var vaultRepository: VaultRepository
    private lateinit var viewModel: SearchViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        vaultRepository = mock()
        viewModel = SearchViewModel(vaultRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assert(state.query.isEmpty())
            assert(state.results.isEmpty())
            assert(!state.hasSearched)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `update query updates state`() = runTest {
        viewModel.updateQuery("test")
        val state = viewModel.uiState.value
        assert(state.query == "test")
    }

    @Test
    fun `set filter updates state`() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            viewModel.setFilter("images")
            val state = awaitItem()
            assert(state.filterType == "images")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `empty query clears results`() = runTest {
        viewModel.updateQuery("")
        viewModel.uiState.test {
            val state = awaitItem()
            assert(state.results.isEmpty())
            assert(!state.hasSearched)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search with query calls repository`() = runTest {
        val mockFiles = listOf(
            VaultFile(
                id = "1", name = "test.jpg", extension = "jpg", mimeType = "image/jpeg",
                size = 1024L, folderId = null, remotePath = "", localPath = null,
                thumbnailPath = null, isFavorite = false, createdAt = 0L, updatedAt = 0L,
                uploadedAt = null, downloadedAt = null, checksum = null
            )
        )
        whenever(vaultRepository.searchFiles("test")).thenReturn(flowOf(mockFiles))

        viewModel.updateQuery("test")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assert(state.hasSearched)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
