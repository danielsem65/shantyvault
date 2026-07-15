package com.shanty.vault.domain.repository

import com.shanty.vault.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.io.File

interface VaultRepository {

    fun getAllFiles(): Flow<List<VaultFile>>
    fun getFilesByFolder(folderId: String?): Flow<List<VaultFile>>
    fun getRecentFiles(limit: Int): Flow<List<VaultFile>>
    fun getFavoriteFiles(): Flow<List<VaultFile>>
    fun searchFiles(query: String): Flow<List<VaultFile>>
    suspend fun getFileById(id: String): VaultFile?
    suspend fun uploadFile(file: File, folderId: String?): Result<VaultFile>
    suspend fun uploadFileWithEncryption(file: File, folderId: String?): Result<VaultFile>
    suspend fun downloadFile(fileId: String, destination: File): Result<String>
    suspend fun deleteFile(fileId: String): Result<Unit>
    suspend fun renameFile(fileId: String, newName: String): Result<Unit>
    suspend fun moveFile(fileId: String, newFolderId: String?): Result<Unit>
    suspend fun toggleFavorite(fileId: String): Result<Unit>

    fun getRootFolders(): Flow<List<Folder>>
    fun getSubFolders(parentId: String): Flow<List<Folder>>
    fun getFavoriteFolders(): Flow<List<Folder>>
    suspend fun getFolderById(id: String): Folder?
    suspend fun createFolder(name: String, parentId: String?): Result<Folder>
    suspend fun renameFolder(folderId: String, newName: String): Result<Unit>
    suspend fun deleteFolder(folderId: String): Result<Unit>
    suspend fun moveFolder(folderId: String, newParentId: String?): Result<Unit>
    suspend fun toggleFolderFavorite(folderId: String): Result<Unit>

    fun getAllNotes(): Flow<List<Note>>
    fun getPinnedNotes(): Flow<List<Note>>
    fun searchNotes(query: String): Flow<List<Note>>
    suspend fun getNoteById(id: String): Note?
    suspend fun createNote(title: String, content: String): Result<Note>
    suspend fun updateNote(id: String, title: String?, content: String?, isPinned: Boolean?, colorHex: String? = null): Result<Unit>
    suspend fun deleteNote(id: String): Result<Unit>
    suspend fun toggleNotePinned(id: String): Result<Unit>

    fun getRecentActivities(limit: Int): Flow<List<Activity>>
    suspend fun getStorageUsage(): Flow<StorageUsage>
    suspend fun syncAll(): Result<Unit>
}

