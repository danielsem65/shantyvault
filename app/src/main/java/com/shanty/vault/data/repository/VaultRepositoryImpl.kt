package com.shanty.vault.data.repository

import com.shanty.vault.data.local.*
import com.shanty.vault.data.model.*
import com.shanty.vault.domain.model.*
import com.shanty.vault.domain.repository.VaultRepository
import com.shanty.vault.security.EncryptionManager
import com.shanty.vault.util.Constants
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID


class VaultRepositoryImpl(
    private val vaultFileDao: VaultFileDao,
    private val folderDao: FolderDao,
    private val noteDao: NoteDao,
    private val activityDao: ActivityDao,
    private val encryptionManager: EncryptionManager,
    private val supabaseClient: SupabaseClient
) : VaultRepository {

    override fun getAllFiles(): Flow<List<VaultFile>> =
        vaultFileDao.getAllFiles().map { entities -> entities.map { it.toDomain() } }

    override fun getFilesByFolder(folderId: String?): Flow<List<VaultFile>> =
        if (folderId == null) vaultFileDao.getRootFiles().map { it.map { e -> e.toDomain() } }
        else vaultFileDao.getFilesByFolder(folderId).map { it.map { e -> e.toDomain() } }

    override fun getRecentFiles(limit: Int): Flow<List<VaultFile>> =
        vaultFileDao.getRecentFiles(limit).map { it.map { e -> e.toDomain() } }

    override fun getFavoriteFiles(): Flow<List<VaultFile>> =
        vaultFileDao.getFavoriteFiles().map { it.map { e -> e.toDomain() } }

    override fun searchFiles(query: String): Flow<List<VaultFile>> =
        vaultFileDao.searchFiles(query).map { it.map { e -> e.toDomain() } }

    override suspend fun getFileById(id: String): VaultFile? =
        vaultFileDao.getFileById(id)?.toDomain()

    override suspend fun uploadFile(file: File, folderId: String?): Result<VaultFile> {
        return try {
            val userId = getUserId()
            val fileId = UUID.randomUUID().toString()
            val remotePath = "$userId/files/$fileId/${file.name}"
            val bucket = supabaseClient.storage.from(Constants.SUPABASE_STORAGE_BUCKET)

            val fileBytes = file.readBytes()
            bucket.upload(remotePath, fileBytes)

            val publicUrl = bucket.publicUrl(remotePath)

            val checksum = fileBytes.sha256()

            val vaultFile = VaultFile(
                id = fileId,
                name = file.name,
                extension = file.extension,
                mimeType = getMimeType(file.name),
                size = file.length(),
                folderId = folderId,
                remotePath = publicUrl,
                localPath = file.absolutePath,
                thumbnailPath = null,
                isFavorite = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                uploadedAt = System.currentTimeMillis(),
                downloadedAt = null,
                checksum = checksum
            )
            vaultFileDao.insertFile(vaultFile.toEntity())
            logActivity(Activity.ActivityType.UPLOAD, "Uploaded ${file.name}", fileId, file.name)
            Result.success(vaultFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadFileWithEncryption(file: File, folderId: String?): Result<VaultFile> {
        return try {
            val fileData = file.readBytes()
            val encryptedData = encryptionManager.encryptFile(fileData)
            val tempFile = File(file.parent, "enc_${file.name}")
            tempFile.writeBytes(encryptedData)

            val userId = getUserId()
            val fileId = UUID.randomUUID().toString()
            val remotePath = "$userId/files/$fileId/${file.name}.enc"
            val bucket = supabaseClient.storage.from(Constants.SUPABASE_STORAGE_BUCKET)

            val encryptedBytes = tempFile.readBytes()
            bucket.upload(remotePath, encryptedBytes)

            val publicUrl = bucket.publicUrl(remotePath)
            tempFile.delete()

            val vaultFile = VaultFile(
                id = fileId, name = file.name, extension = file.extension,
                mimeType = getMimeType(file.name), size = file.length(),
                folderId = folderId, remotePath = publicUrl,
                localPath = null, thumbnailPath = null,
                isFavorite = false, createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(), uploadedAt = System.currentTimeMillis(),
                downloadedAt = null, checksum = fileData.sha256()
            )
            vaultFileDao.insertFile(vaultFile.toEntity())
            logActivity(Activity.ActivityType.UPLOAD, "Uploaded (encrypted) ${file.name}", fileId, file.name)
            Result.success(vaultFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun downloadFile(fileId: String, destination: File): Result<String> {
        return try {
            val entity = vaultFileDao.getFileById(fileId)
                ?: return Result.failure(Exception("File not found"))

            val userId = getUserId()
            val remotePath = "$userId/files/$fileId/${entity.name}"
            val bucket = supabaseClient.storage.from(Constants.SUPABASE_STORAGE_BUCKET)

            val fileBytes = bucket.downloadPublic(remotePath)
            destination.parentFile?.mkdirs()
            destination.writeBytes(fileBytes)

            vaultFileDao.updateFile(entity.copy(
                localPath = destination.absolutePath,
                downloadedAt = System.currentTimeMillis()
            ))

            logActivity(Activity.ActivityType.DOWNLOAD, "Downloaded ${entity.name}", fileId, entity.name)
            Result.success(destination.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFile(fileId: String): Result<Unit> {
        return try {
            val entity = vaultFileDao.getFileById(fileId)
                ?: return Result.failure(Exception("File not found"))

            val userId = getUserId()
            val remotePath = "$userId/files/$fileId/${entity.name}"
            val bucket = supabaseClient.storage.from(Constants.SUPABASE_STORAGE_BUCKET)

            try {
                bucket.delete(listOf(remotePath))
            } catch (_: Exception) { }

            vaultFileDao.softDeleteFile(fileId)
            logActivity(Activity.ActivityType.DELETE, "Deleted ${entity.name}", fileId, entity.name)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun renameFile(fileId: String, newName: String): Result<Unit> {
        return try {
            vaultFileDao.renameFile(fileId, newName)
            logActivity(Activity.ActivityType.RENAME, "Renamed to $newName", fileId, newName)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun moveFile(fileId: String, newFolderId: String?): Result<Unit> {
        return try {
            vaultFileDao.moveFile(fileId, newFolderId)
            logActivity(Activity.ActivityType.MOVE, "Moved file", fileId, null)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleFavorite(fileId: String): Result<Unit> {
        return try {
            vaultFileDao.toggleFavorite(fileId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getRootFolders(): Flow<List<Folder>> =
        folderDao.getRootFolders().map { it.map { e -> e.toDomain() } }

    override fun getSubFolders(parentId: String): Flow<List<Folder>> =
        folderDao.getSubFolders(parentId).map { it.map { e -> e.toDomain() } }

    override fun getFavoriteFolders(): Flow<List<Folder>> =
        folderDao.getFavoriteFolders().map { it.map { e -> e.toDomain() } }

    override suspend fun getFolderById(id: String): Folder? =
        folderDao.getFolderById(id)?.toDomain()

    override suspend fun createFolder(name: String, parentId: String?): Result<Folder> {
        return try {
            val id = UUID.randomUUID().toString()
            val path = buildPath(parentId, name)
            val folder = Folder(id = id, name = name, parentId = parentId, path = path,
                isFavorite = false, createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(), itemCount = 0, totalSize = 0L)
            folderDao.insertFolder(folder.toEntity())
            logActivity(Activity.ActivityType.CREATE_FOLDER, "Created folder $name", id, name)
            Result.success(folder)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun renameFolder(folderId: String, newName: String): Result<Unit> {
        return try {
            folderDao.renameFolder(folderId, newName)
            logActivity(Activity.ActivityType.RENAME, "Renamed folder to $newName", folderId, newName)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFolder(folderId: String): Result<Unit> {
        return try {
            val entity = folderDao.getFolderById(folderId)
            folderDao.softDeleteFolder(folderId)
            logActivity(Activity.ActivityType.DELETE_FOLDER, "Deleted folder ${entity?.name}", folderId, entity?.name)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun moveFolder(folderId: String, newParentId: String?): Result<Unit> {
        return try {
            val folder = folderDao.getFolderById(folderId)
                ?: return Result.failure(Exception("Folder not found"))
            val newPath = buildPath(newParentId, folder.name)
            folderDao.moveFolder(folderId, newParentId, newPath)
            logActivity(Activity.ActivityType.MOVE, "Moved folder ${folder.name}", folderId, folder.name)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleFolderFavorite(folderId: String): Result<Unit> {
        return try {
            folderDao.toggleFavorite(folderId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllNotes(): Flow<List<Note>> =
        noteDao.getAllNotes().map { it.map { e -> e.toDomain() } }

    override fun getPinnedNotes(): Flow<List<Note>> =
        noteDao.getPinnedNotes().map { it.map { e -> e.toDomain() } }

    override fun searchNotes(query: String): Flow<List<Note>> =
        noteDao.searchNotes(query).map { it.map { e -> e.toDomain() } }

    override suspend fun getNoteById(id: String): Note? =
        noteDao.getNoteById(id)?.toDomain()

    override suspend fun createNote(title: String, content: String): Result<Note> {
        return try {
            val id = UUID.randomUUID().toString()
            val encryptedContent = encryptionManager.encryptString(content)
            val note = Note(id = id, title = title, content = encryptedContent,
                isPinned = false, createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(), colorHex = null,
                hasChecklist = false, checklistData = null)
            noteDao.insertNote(note.toEntity())
            logActivity(Activity.ActivityType.CREATE_NOTE, "Created note $title", id, title)
            Result.success(note)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateNote(id: String, title: String?, content: String?, isPinned: Boolean?, colorHex: String?): Result<Unit> {
        return try {
            val existing = noteDao.getNoteById(id)
                ?: return Result.failure(Exception("Note not found"))
            val encryptedContent = if (content != null) encryptionManager.encryptString(content) else existing.content
            noteDao.updateNote(existing.copy(
                title = title ?: existing.title,
                content = encryptedContent,
                isPinned = isPinned ?: existing.isPinned,
                colorHex = colorHex ?: existing.colorHex,
                updatedAt = System.currentTimeMillis()
            ))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNote(id: String): Result<Unit> {
        return try {
            noteDao.softDeleteNote(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleNotePinned(id: String): Result<Unit> {
        return try {
            noteDao.togglePinned(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getRecentActivities(limit: Int): Flow<List<Activity>> =
        activityDao.getRecentActivities(limit).map { it.map { e -> e.toDomain() } }

    override suspend fun getStorageUsage(): Flow<StorageUsage> {
        return vaultFileDao.getTotalStorageUsed().map { used ->
            StorageUsage(
                used = used,
                limit = 5L * 1024 * 1024 * 1024,
                percentUsed = if (used > 0) (used.toDouble() / (5L * 1024 * 1024 * 1024)) * 100 else 0.0
            )
        }
    }

    override suspend fun syncAll(): Result<Unit> {
        return try {
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun logActivity(
        type: Activity.ActivityType,
        description: String,
        itemId: String?,
        itemName: String?
    ) {
        val activity = Activity(
            id = UUID.randomUUID().toString(), type = type, description = description,
            itemId = itemId, itemName = itemName, timestamp = System.currentTimeMillis()
        )
        activityDao.insertActivity(activity.toEntity())
    }

    private suspend fun getUserId(): String {
        return try {
            supabaseClient.auth.currentUserOrNull()?.id ?: "unknown_user"
        } catch (_: Exception) {
            "unknown_user"
        }
    }

    private suspend fun buildPath(parentId: String?, folderName: String): String {
        if (parentId == null) return "/$folderName"
        val parent = folderDao.getFolderById(parentId) ?: return "/$folderName"
        return "${parent.path}/$folderName"
    }

    private fun getMimeType(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "bmp" -> "image/bmp"
            "svg" -> "image/svg+xml"
            "mp4" -> "video/mp4"
            "avi" -> "video/x-msvideo"
            "mkv" -> "video/x-matroska"
            "mov" -> "video/quicktime"
            "wmv" -> "video/x-ms-wmv"
            "flv" -> "video/x-flv"
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "txt" -> "text/plain"
            "rtf" -> "application/rtf"
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "aac" -> "audio/aac"
            "flac" -> "audio/flac"
            "ogg" -> "audio/ogg"
            "wma" -> "audio/x-ms-wma"
            "zip" -> "application/zip"
            "rar" -> "application/vnd.rar"
            "tar" -> "application/x-tar"
            "gz" -> "application/gzip"
            "7z" -> "application/x-7z-compressed"
            else -> "application/octet-stream"
        }
    }

    private fun ByteArray.sha256(): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        return digest.digest(this).joinToString("") { "%02x".format(it) }
    }
}

private val File.extension: String get() = name.substringAfterLast('.', "").lowercase()
