package com.shanty.vault.data.local

import androidx.room.*
import com.shanty.vault.data.model.ActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {

    @Query("SELECT * FROM activities ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentActivities(limit: Int = 50): Flow<List<ActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityEntity)

    @Query("DELETE FROM activities WHERE timestamp < :cutoffTime")
    suspend fun deleteOldActivities(cutoffTime: Long)

    @Query("DELETE FROM activities")
    suspend fun clearAll()
}
