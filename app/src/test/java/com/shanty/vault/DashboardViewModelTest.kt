package com.shanty.vault

import app.cash.turbine.test
import com.shanty.vault.domain.model.StorageUsage
import com.shanty.vault.domain.model.VaultFile
import com.shanty.vault.domain.repository.VaultRepository
import com.shanty.vault.presentation.dashboard.DashboardViewModel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class DashboardViewModelTest {

    private lateinit var vaultRepository: VaultRepository
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setup() {
        vaultRepository = mock()
        whenever(vaultRepository.getRecentFiles(10)).thenReturn(flowOf(emptyList()))
        whenever(vaultRepository.getFavoriteFiles()).thenReturn(flowOf(emptyList()))
        whenever(vaultRepository.getRecentActivities(20)).thenReturn(flowOf(emptyList()))
        runTest {
            whenever(vaultRepository.getStorageUsage()).thenReturn(
                flowOf(StorageUsage(0L, 5L * 1024 * 1024 * 1024, 0.0))
            )
        }
        viewModel = DashboardViewModel(vaultRepository)
    }

    @Test
    fun `initial state loads data`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assert(!state.isLoading)
            assert(state.storageUsed == 0L)
            assert(state.recentFiles.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh updates state`() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            viewModel.refresh()
            val state = awaitItem()
            assert(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
