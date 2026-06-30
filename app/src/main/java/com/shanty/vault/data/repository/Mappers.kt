package com.shanty.vault.data.repository

import com.shanty.vault.data.model.*
import com.shanty.vault.domain.model.*

fun VaultFileEntity.toDomain() = VaultFile(
    id = id, name = name, extension = extension, mimeType = mimeType,
    size = size, folderId = folderId, remotePath = remotePath,
    localPath = localPath, thumbnailPath = thumbnailPath,
    isFavorite = isFavorite, createdAt = createdAt, updatedAt = updatedAt,
    uploadedAt = uploadedAt, downloadedAt = downloadedAt, checksum = checksum
)

fun VaultFile.toEntity() = VaultFileEntity(
    id = id, name = name, extension = extension, mimeType = mimeType,
    size = size, folderId = folderId, isEncrypted = true,
    encryptionIv = null, remotePath = remotePath, localPath = localPath,
    thumbnailPath = thumbnailPath, isFavorite = isFavorite, createdAt = createdAt,
    updatedAt = updatedAt, uploadedAt = uploadedAt, downloadedAt = downloadedAt,
    checksum = checksum
)

fun FolderEntity.toDomain() = Folder(
    id = id, name = name, parentId = parentId, path = path,
    isFavorite = isFavorite, createdAt = createdAt, updatedAt = updatedAt,
    itemCount = itemCount, totalSize = totalSize
)

fun Folder.toEntity() = FolderEntity(
    id = id, name = name, parentId = parentId, path = path,
    isFavorite = isFavorite, createdAt = createdAt, updatedAt = updatedAt,
    itemCount = itemCount, totalSize = totalSize
)

fun NoteEntity.toDomain() = Note(
    id = id, title = title, content = content, isPinned = isPinned,
    createdAt = createdAt, updatedAt = updatedAt, colorHex = colorHex,
    hasChecklist = hasChecklist, checklistData = checklistData
)

fun Note.toEntity() = NoteEntity(
    id = id, title = title, content = content, isPinned = isPinned,
    createdAt = createdAt, updatedAt = updatedAt, colorHex = colorHex,
    hasChecklist = hasChecklist, checklistData = checklistData
)

fun ActivityEntity.toDomain() = Activity(
    id = id, type = try { Activity.ActivityType.valueOf(type) } catch (e: Exception) { Activity.ActivityType.UPLOAD },
    description = description, itemId = itemId, itemName = itemName, timestamp = timestamp
)

fun Activity.toEntity() = ActivityEntity(
    id = id, type = type.name, description = description,
    itemId = itemId, itemName = itemName, timestamp = timestamp
)
