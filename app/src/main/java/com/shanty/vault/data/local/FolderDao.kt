package com.shanty.vault.data.local

import androidx.room.*
import com.shanty.vault.data.model.FolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    @Query("SELECT * FROM folders WHERE parentId IS NULL AND isDeleted = 0 ORDER BY name ASC")
    fun getRootFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE parentId = :parentId AND isDeleted = 0 ORDER BY name ASC")
    fun getSubFolders(parentId: String): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getFolderById(id: String): FolderEntity?

    @Query("SELECT * FROM folders WHERE isFavorite = 1 AND isDeleted = 0 ORDER BY name ASC")
    fun getFavoriteFolders(): Flow<List<FolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity)

    @Update
    suspend fun updateFolder(folder: FolderEntity)

    @Delete
    suspend fun deleteFolder(folder: FolderEntity)

    @Query("UPDATE folders SET isDeleted = 1, deletedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteFolder(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE folders SET isFavorite = CASE WHEN isFavorite = 1 THEN 0 ELSE 1 END WHERE id = :id")
    suspend fun toggleFavorite(id: String)

    @Query("UPDATE folders SET name = :name, updatedAt = :timestamp WHERE id = :id")
    suspend fun renameFolder(id: String, name: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE folders SET parentId = :newParentId, path = :newPath, updatedAt = :timestamp WHERE id = :id")
    suspend fun moveFolder(id: String, newParentId: String?, newPath: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM folders WHERE isDeleted = 0")
    fun getFolderCount(): Flow<Int>
}
