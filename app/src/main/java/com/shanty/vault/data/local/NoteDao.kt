package com.shanty.vault.data.local

import androidx.room.*
import com.shanty.vault.data.model.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: String): NoteEntity?

    @Query("SELECT * FROM notes WHERE isPinned = 1 AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getPinnedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')")
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("UPDATE notes SET isDeleted = 1, deletedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteNote(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET isPinned = CASE WHEN isPinned = 1 THEN 0 ELSE 1 END WHERE id = :id")
    suspend fun togglePinned(id: String)
}
