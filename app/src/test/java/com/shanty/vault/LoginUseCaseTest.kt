package com.shanty.vault

import com.shanty.vault.domain.model.User
import com.shanty.vault.domain.repository.AuthRepository
import com.shanty.vault.domain.usecase.auth.LoginUseCase
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class LoginUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var loginUseCase: LoginUseCase

    @Before
    fun setup() {
        authRepository = mock()
        loginUseCase = LoginUseCase(authRepository)
        whenever(authRepository.currentUser).thenReturn(flowOf(null))
        whenever(authRepository.isAuthenticated).thenReturn(flowOf(false))
        whenever(authRepository.isSessionExpired).thenReturn(flowOf(false))
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

        val result = loginUseCase("test@example.com", "password123!")

        assert(result.isSuccess)
        assert(result.getOrNull()?.email == "test@example.com")
    }

    @Test
    fun `login with invalid credentials returns failure`() = runTest {
        whenever(authRepository.login("wrong@email.com", "wrongpass"))
            .thenReturn(Result.failure(Exception("Invalid email or password.")))

        val result = loginUseCase("wrong@email.com", "wrongpass")

        assert(result.isFailure)
    }
}
