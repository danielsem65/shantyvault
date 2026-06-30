package com.shanty.vault.domain.model

data class User(
    val id: String,
    val email: String,
    val name: String,
    val isEmailVerified: Boolean,
    val isMfaEnabled: Boolean,
    val storageUsed: Long,
    val storageLimit: Long,
    val createdAt: Long
)
