package com.shanty.vault.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_files")
data class VaultFileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val extension: String,
    val mimeType: String,
    val size: Long,
    val folderId: String?,
    val isEncrypted: Boolean = true,
    val encryptionIv: String?,
    val remotePath: String,
    val localPath: String?,
    val thumbnailPath: String?,
    val isFavorite: Boolean = false,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null,
    val uploadedAt: Long?,
    val downloadedAt: Long?,
    val checksum: String?,
    val tags: String? = null
)
