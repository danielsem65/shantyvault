package com.shanty.vault.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.shanty.vault.domain.model.VaultFile

@Composable
fun FileTypeIcon(file: VaultFile, modifier: Modifier = Modifier) {
    val icon = when {
        file.isImage -> Icons.Filled.Image
        file.isVideo -> Icons.Filled.Videocam
        file.isDocument -> Icons.Filled.Description
        file.isAudio -> Icons.Filled.AudioFile
        file.isArchive -> Icons.Filled.FolderZip
        else -> Icons.Filled.InsertDriveFile
    }
    val tint = when {
        file.isImage -> MaterialTheme.colorScheme.tertiary
        file.isVideo -> MaterialTheme.colorScheme.secondary
        file.isDocument -> MaterialTheme.colorScheme.primary
        file.isAudio -> MaterialTheme.colorScheme.error
        file.isArchive -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    androidx.compose.material3.Icon(icon, contentDescription = null, modifier = modifier, tint = tint)
}

fun fileTypeIconVector(file: VaultFile): ImageVector = when {
    file.isImage -> Icons.Filled.Image
    file.isVideo -> Icons.Filled.Videocam
    file.isAudio -> Icons.Filled.AudioFile
    file.isDocument -> Icons.Filled.Description
    file.isArchive -> Icons.Filled.FolderZip
    else -> Icons.Filled.InsertDriveFile
}
