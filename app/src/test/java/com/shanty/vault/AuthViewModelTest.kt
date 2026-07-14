package com.shanty.vault

import app.cash.turbine.test
import com.shanty.vault.domain.model.User
import com.shanty.vault.domain.repository.AuthRepository
import com.shanty.vault.presentation.auth.AuthViewModel
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
class AuthViewModelTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: AuthViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mock()
        whenever(authRepository.currentUser).thenReturn(flowOf(null))
        whenever(authRepository.isAuthenticated).thenReturn(flowOf(false))
        whenever(authRepository.isSessionExpired).thenReturn(flowOf(false))
        viewModel = AuthViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login with valid credentials returns success`() = runTest {
        val user = User(
            id = "test_id",
            email = "test@example.com",
            name = "Test User",
            isEmailVerified = true,
            isMfaEnabled = false,
            storageUsed = 0L,
            storageLimit = 5L * 1024 * 1024 * 1024,
            createdAt = System.currentTimeMillis()
        )
        whenever(authRepository.login("test@example.com", "password123!")).thenReturn(Result.success(user))

        viewModel.login("test@example.com", "password123!")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.authState.test {
            val state = awaitItem()
            assert(state is com.shanty.vault.presentation.auth.AuthState.Authenticated)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login with invalid credentials returns error`() = runTest {
        whenever(authRepository.login("wrong@email.com", "wrongpass"))
            .thenReturn(Result.failure(Exception("Invalid email or password.")))

        viewModel.login("wrong@email.com", "wrongpass")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.authState.test {
            val state = awaitItem()
            assert(state is com.shanty.vault.presentation.auth.AuthState.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login with blank fields returns error`() = runTest {
        viewModel.login("", "")
        viewModel.authState.test {
            val state = awaitItem()
            assert(state is com.shanty.vault.presentation.auth.AuthState.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `register with valid data calls repository`() = runTest {
        val user = User(
            id = "test_id", email = "test@example.com", name = "Test User",
            isEmailVerified = false, isMfaEnabled = false, storageUsed = 0L,
            storageLimit = 5L * 1024 * 1024 * 1024, createdAt = System.currentTimeMillis()
        )
        whenever(authRepository.register("test@example.com", "Password123!", "Test User"))
            .thenReturn(Result.success(user))

        viewModel.register("test@example.com", "Password123!", "Test User")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.authState.test {
            val state = awaitItem()
            assert(state is com.shanty.vault.presentation.auth.AuthState.Registered)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `reset password calls repository`() = runTest {
        whenever(authRepository.resetPassword("test@example.com")).thenReturn(Result.success(Unit))

        viewModel.resetPassword("test@example.com")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.authState.test {
            val state = awaitItem()
            assert(state is com.shanty.vault.presentation.auth.AuthState.PasswordReset)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `logout sets unauthenticated`() = runTest {
        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.authState.test {
            val state = awaitItem()
            assert(state is com.shanty.vault.presentation.auth.AuthState.Unauthenticated)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `reset state returns idle`() = runTest {
        viewModel.resetState()
        viewModel.authState.test {
            val state = awaitItem()
            assert(state is com.shanty.vault.presentation.auth.AuthState.Idle)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
