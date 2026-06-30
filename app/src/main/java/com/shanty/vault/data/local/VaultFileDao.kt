package com.shanty.vault.data.local

import androidx.room.*
import com.shanty.vault.data.model.VaultFileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultFileDao {

    @Query("SELECT * FROM vault_files WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun getAllFiles(): Flow<List<VaultFileEntity>>

    @Query("SELECT * FROM vault_files WHERE folderId IS NULL AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getRootFiles(): Flow<List<VaultFileEntity>>

    @Query("SELECT * FROM vault_files WHERE folderId = :folderId AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getFilesByFolder(folderId: String): Flow<List<VaultFileEntity>>

    @Query("SELECT * FROM vault_files WHERE id = :id")
    suspend fun getFileById(id: String): VaultFileEntity?

    @Query("SELECT * FROM vault_files WHERE isFavorite = 1 AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getFavoriteFiles(): Flow<List<VaultFileEntity>>

    @Query("SELECT * FROM vault_files WHERE isDeleted = 0 ORDER BY updatedAt DESC LIMIT :limit")
    fun getRecentFiles(limit: Int = 20): Flow<List<VaultFileEntity>>

    @Query("SELECT * FROM vault_files WHERE isDeleted = 0 AND (name LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%')")
    fun searchFiles(query: String): Flow<List<VaultFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: VaultFileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<VaultFileEntity>)

    @Update
    suspend fun updateFile(file: VaultFileEntity)

    @Delete
    suspend fun deleteFile(file: VaultFileEntity)

    @Query("UPDATE vault_files SET isDeleted = 1, deletedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteFile(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE vault_files SET isFavorite = CASE WHEN isFavorite = 1 THEN 0 ELSE 1 END WHERE id = :id")
    suspend fun toggleFavorite(id: String)

    @Query("UPDATE vault_files SET name = :name, updatedAt = :timestamp WHERE id = :id")
    suspend fun renameFile(id: String, name: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE vault_files SET folderId = :newFolderId, updatedAt = :timestamp WHERE id = :id")
    suspend fun moveFile(id: String, newFolderId: String?, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM vault_files WHERE isDeleted = 0")
    fun getFileCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(size), 0) FROM vault_files WHERE isDeleted = 0")
    fun getTotalStorageUsed(): Flow<Long>

    @Query("SELECT * FROM vault_files WHERE extension IN (:extensions) AND isDeleted = 0")
    fun getFilesByExtensions(extensions: List<String>): Flow<List<VaultFileEntity>>

    @Query("SELECT * FROM vault_files WHERE isDeleted = 0 AND size BETWEEN :minSize AND :maxSize")
    fun getFilesBySize(minSize: Long, maxSize: Long): Flow<List<VaultFileEntity>>

    @Query("SELECT * FROM vault_files WHERE isDeleted = 0 AND createdAt BETWEEN :startDate AND :endDate")
    fun getFilesByDate(startDate: Long, endDate: Long): Flow<List<VaultFileEntity>>
}
