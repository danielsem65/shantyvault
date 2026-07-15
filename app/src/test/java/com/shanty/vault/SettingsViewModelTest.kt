package com.shanty.vault

import app.cash.turbine.test
import com.shanty.vault.data.local.UserPreferences
import com.shanty.vault.domain.model.StorageUsage
import com.shanty.vault.domain.repository.AuthRepository
import com.shanty.vault.domain.repository.VaultRepository
import com.shanty.vault.presentation.settings.SettingsViewModel
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
class SettingsViewModelTest {

    private lateinit var userPreferences: UserPreferences
    private lateinit var authRepository: AuthRepository
    private lateinit var vaultRepository: VaultRepository
    private lateinit var viewModel: SettingsViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userPreferences = mock()
        authRepository = mock()
        vaultRepository = mock()
        whenever(userPreferences.themeMode).thenReturn(flowOf("system"))
        whenever(userPreferences.isBiometricEnabled).thenReturn(flowOf(false))
        whenever(userPreferences.isMfaEnabled).thenReturn(flowOf(false))
        whenever(userPreferences.notificationsEnabled).thenReturn(flowOf(true))
        runTest {
            whenever(vaultRepository.getStorageUsage()).thenReturn(
                flowOf(StorageUsage(0L, 5L * 1024 * 1024 * 1024, 0.0))
            )
        }
        viewModel = SettingsViewModel(userPreferences, authRepository, vaultRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads from preferences`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assert(state.themeMode == "system")
            assert(!state.biometricsEnabled)
            assert(!state.mfaEnabled)
            assert(state.notificationsEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `set theme mode updates preferences`() = runTest {
        viewModel.setThemeMode("dark")
        testDispatcher.scheduler.advanceUntilIdle()
        verify(userPreferences).setThemeMode("dark")
    }

    @Test
    fun `toggle biometrics updates preference`() = runTest {
        viewModel.toggleBiometrics()
        testDispatcher.scheduler.advanceUntilIdle()
        verify(userPreferences).setBiometricEnabled(true)
    }

    @Test
    fun `toggle mfa updates preference`() = runTest {
        viewModel.toggleMfa()
        testDispatcher.scheduler.advanceUntilIdle()
        verify(userPreferences).setMfaEnabled(true)
    }

    @Test
    fun `show change password dialog updates state`() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            viewModel.showChangePasswordDialog()
            val state = awaitItem()
            assert(state.showChangePasswordDialog)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `change password with mismatched passwords shows error`() = runTest {
        viewModel.showChangePasswordDialog()
        viewModel.updateCurrentPassword("currentpass123")
        viewModel.updateNewPassword("newpass12345")
        viewModel.updateConfirmPassword("differentpass")
        viewModel.changePassword()
        viewModel.uiState.test {
            val state = awaitItem()
            assert(state.message?.contains("do not match") == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `change password with short password shows error`() = runTest {
        viewModel.showChangePasswordDialog()
        viewModel.updateCurrentPassword("currentpass123")
        viewModel.updateNewPassword("short")
        viewModel.updateConfirmPassword("short")
        viewModel.changePassword()
        viewModel.uiState.test {
            val state = awaitItem()
            assert(state.message?.contains("12 characters") == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `show about dialog updates state`() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            viewModel.showAboutDialog()
            val state = awaitItem()
            assert(state.showAboutDialog)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clear message resets message`() = runTest {
        viewModel.clearMessage()
        viewModel.uiState.test {
            val state = awaitItem()
            assert(state.message == null)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
