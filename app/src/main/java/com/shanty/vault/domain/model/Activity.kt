package com.shanty.vault.domain.model

data class Activity(
    val id: String,
    val type: ActivityType,
    val description: String,
    val itemId: String?,
    val itemName: String?,
    val timestamp: Long
) {
    enum class ActivityType {
        UPLOAD, DOWNLOAD, DELETE, RENAME, MOVE,
        LOGIN, LOGOUT, SHARE, CREATE_FOLDER,
        DELETE_FOLDER, CREATE_NOTE, UPDATE_NOTE,
        SECURITY_CHANGE, STORAGE_WARNING
    }
}
