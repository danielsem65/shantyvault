package com.shanty.vault.domain.model

import kotlin.math.log10
import kotlin.math.pow

data class VaultFile(
    val id: String,
    val name: String,
    val extension: String,
    val mimeType: String,
    val size: Long,
    val folderId: String?,
    val remotePath: String,
    val localPath: String?,
    val thumbnailPath: String?,
    val isFavorite: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val uploadedAt: Long?,
    val downloadedAt: Long?,
    val checksum: String?
) {
    val formattedSize: String get() = size.toFormattedFileSize()
    val isImage: Boolean get() = extension in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "svg")
    val isVideo: Boolean get() = extension in listOf("mp4", "avi", "mkv", "mov", "wmv", "flv")
    val isDocument: Boolean get() = extension in listOf("pdf", "doc", "docx", "xls", "xlsx", "txt", "rtf")
    val isAudio: Boolean get() = extension in listOf("mp3", "wav", "aac", "flac", "ogg", "wma")
    val isArchive: Boolean get() = extension in listOf("zip", "rar", "tar", "gz", "7z")

    companion object {
        private fun Long.toFormattedFileSize(): String {
            if (this <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (log10(this.toDouble()) / log10(1024.0)).toInt()
            val size = this.toDouble() / 1024.0.pow(digitGroups)
            return "%.1f %s".format(size, units[digitGroups])
        }
    }
}
