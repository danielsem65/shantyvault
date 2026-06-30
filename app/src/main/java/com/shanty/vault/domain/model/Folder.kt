package com.shanty.vault.domain.model

data class Folder(
    val id: String,
    val name: String,
    val parentId: String?,
    val path: String,
    val isFavorite: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val itemCount: Int,
    val totalSize: Long
)
