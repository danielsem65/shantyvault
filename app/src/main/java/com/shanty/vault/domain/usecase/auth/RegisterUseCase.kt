package com.shanty.vault.domain.usecase.auth

import com.shanty.vault.domain.model.User
import com.shanty.vault.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, name: String): Result<User> {
        return authRepository.register(email, password, name)
    }
}
