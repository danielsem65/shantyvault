package com.shanty.vault.domain.model

data class Note(
    val id: String,
    val title: String,
    val content: String,
    val isPinned: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val colorHex: String?,
    val hasChecklist: Boolean,
    val checklistData: String?
)
