package com.shanty.vault.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey val id: String,
    val type: String,
    val description: String,
    val itemId: String?,
    val itemName: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: String? = null
)
