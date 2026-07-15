package com.shanty.vault.domain.model

data class StorageUsage(
    val used: Long,
    val limit: Long,
    val percentUsed: Double
)
