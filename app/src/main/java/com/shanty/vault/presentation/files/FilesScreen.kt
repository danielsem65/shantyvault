package com.shanty.vault.presentation.files

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.shanty.vault.domain.model.Folder
import com.shanty.vault.domain.model.VaultFile
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FilesScreen(
    viewModel: FilesViewModel = hiltViewModel(),
    onNavigateToFolder: (String) -> Unit,
    onNavigateToFile: (String) -> Unit,
    onNavigateToSearch: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Files") },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { viewModel.toggleGridMode() }) {
                        Icon(
                            if (uiState.isGridMode) Icons.Filled.ViewList else Icons.Filled.GridView,
                            contentDescription = if (uiState.isGridMode) "List view" else "Grid view"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { }) {
                Icon(Icons.Filled.Upload, contentDescription = "Upload")
            }
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(uiState.isLoading),
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading && uiState.files.isEmpty() && uiState.folders.isEmpty()) {
                ShimmerLoadingState()
            } else if (uiState.folders.isEmpty() && uiState.files.isEmpty()) {
                EmptyState()
            } else {
                if (uiState.isGridMode) {
                    GridContent(
                        folders = uiState.folders,
                        files = uiState.files,
                        onFolderClick = { folder ->
                            viewModel.navigateToFolder(folder.id)
                            onNavigateToFolder(folder.id)
                        },
                        onFileClick = { file -> onNavigateToFile(file.id) },
                        onFileLongClick = { viewModel.requestDeleteFile(it) },
                        onToggleFavorite = { viewModel.toggleFavorite(it.id) },
                        viewModel = viewModel
                    )
                } else {
                    ListContent(
                        folders = uiState.folders,
                        files = uiState.files,
                        onFolderClick = { folder ->
                            viewModel.navigateToFolder(folder.id)
                            onNavigateToFolder(folder.id)
                        },
                        onFileClick = { file -> onNavigateToFile(file.id) },
                        onFileLongClick = { viewModel.requestDeleteFile(it) },
                        onToggleFavorite = { viewModel.toggleFavorite(it.id) },
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    if (uiState.showDeleteConfirm && uiState.fileToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            title = { Text("Delete File") },
            text = { Text("Are you sure you want to delete \"${uiState.fileToDelete!!.name}\"?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun GridContent(
    folders: List<Folder>,
    files: List<VaultFile>,
    onFolderClick: (Folder) -> Unit,
    onFileClick: (VaultFile) -> Unit,
    onFileLongClick: (VaultFile) -> Unit,
    onToggleFavorite: (VaultFile) -> Unit,
    viewModel: FilesViewModel
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 140.dp),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(folders, key = { it.id }) { folder ->
            FolderGridCard(folder = folder, onClick = { onFolderClick(folder) })
        }
        items(files, key = { it.id }) { file ->
            FileGridCard(
                file = file,
                onClick = { onFileClick(file) },
                onLongClick = { onFileLongClick(file) },
                onToggleFavorite = { onToggleFavorite(file) }
            )
        }
    }
}

@Composable
private fun ListContent(
    folders: List<Folder>,
    files: List<VaultFile>,
    onFolderClick: (Folder) -> Unit,
    onFileClick: (VaultFile) -> Unit,
    onFileLongClick: (VaultFile) -> Unit,
    onToggleFavorite: (VaultFile) -> Unit,
    viewModel: FilesViewModel
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (folders.isNotEmpty()) {
            item {
                Text(
                    "Folders",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )
            }
            items(folders, key = { it.id }) { folder ->
                FolderListRow(folder = folder, onClick = { onFolderClick(folder) })
            }
        }
        if (files.isNotEmpty()) {
            item {
                Text(
                    "Files",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )
            }
            items(files, key = { it.id }) { file ->
                FileListRow(
                    file = file,
                    onClick = { onFileClick(file) },
                    onLongClick = { onFileLongClick(file) },
                    onToggleFavorite = { onToggleFavorite(file) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FolderGridCard(folder: Folder, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.Folder,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                folder.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${folder.itemCount} items",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileGridCard(
    file: VaultFile,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                FileTypeIcon(file, modifier = Modifier.size(48.dp))
                if (file.isFavorite) {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = "Favorite",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(16.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                file.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                file.formattedSize,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                formatDate(file.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
        DropdownMenuItem(
            text = { Text("Rename") },
            onClick = { showMenu = false },
            leadingIcon = { Icon(Icons.Filled.DriveFileRenameOutline, null) }
        )
        DropdownMenuItem(
            text = { Text("Move") },
            onClick = { showMenu = false },
            leadingIcon = { Icon(Icons.Filled.DriveFileMove, null) }
        )
        DropdownMenuItem(
            text = { Text(if (file.isFavorite) "Unfavorite" else "Favorite") },
            onClick = {
                onToggleFavorite()
                showMenu = false
            },
            leadingIcon = {
                Icon(
                    if (file.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    null
                )
            }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
            onClick = {
                onLongClick()
                showMenu = false
            },
            leadingIcon = {
                Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error)
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FolderListRow(folder: Folder, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    folder.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${folder.itemCount} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileListRow(
    file: VaultFile,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FileTypeIcon(file, modifier = Modifier.size(40.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    file.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        file.formattedSize,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        " \u00B7 ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatDate(file.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (file.isFavorite) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = "Favorite",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
        DropdownMenuItem(
            text = { Text("Rename") },
            onClick = { showMenu = false },
            leadingIcon = { Icon(Icons.Filled.DriveFileRenameOutline, null) }
        )
        DropdownMenuItem(
            text = { Text("Move") },
            onClick = { showMenu = false },
            leadingIcon = { Icon(Icons.Filled.DriveFileMove, null) }
        )
        DropdownMenuItem(
            text = { Text(if (file.isFavorite) "Unfavorite" else "Favorite") },
            onClick = {
                onToggleFavorite()
                showMenu = false
            },
            leadingIcon = {
                Icon(
                    if (file.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    null
                )
            }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
            onClick = {
                onLongClick()
                showMenu = false
            },
            leadingIcon = {
                Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error)
            }
        )
    }
}

@Composable
private fun FileTypeIcon(file: VaultFile, modifier: Modifier = Modifier) {
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
    Icon(icon, contentDescription = null, modifier = modifier, tint = tint)
}

@Composable
private fun ShimmerLoadingState() {
    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(8) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .padding(4.dp)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surface
                        ) {}
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(12.dp),
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surface
                        ) {}
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth(0.4f)
                                .height(10.dp),
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surface
                        ) {}
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.FolderOpen,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "No files yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Tap the upload button to add files",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (_: Exception) {
        ""
    }
}
