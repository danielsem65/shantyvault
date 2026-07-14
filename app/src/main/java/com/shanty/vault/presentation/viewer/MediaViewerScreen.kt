package com.shanty.vault.presentation.viewer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.shanty.vault.domain.model.VaultFile
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaViewerScreen(
    fileId: String,
    onNavigateBack: () -> Unit,
    viewModel: MediaViewerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(fileId) {
        viewModel.loadFile(fileId)
    }

    val file = uiState.file

    if (uiState.isFullscreen && file != null && (file.isImage || file.isVideo)) {
        FullscreenContent(
            file = file,
            isPlaying = uiState.isPlaying,
            onTogglePlayback = { viewModel.togglePlayback() },
            onToggleFullscreen = { viewModel.toggleFullscreen() }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = file?.name ?: "Loading...",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.downloadFile(context) },
                        enabled = !uiState.isDownloading
                    ) {
                        if (uiState.isDownloading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.Download, contentDescription = "Download")
                        }
                    }
                    IconButton(
                        onClick = { viewModel.shareFile(context) },
                        enabled = !uiState.isDownloading
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share")
                    }
                    if (file != null && (file.isImage || file.isVideo)) {
                        IconButton(onClick = { viewModel.toggleFullscreen() }) {
                            Icon(Icons.Filled.Fullscreen, contentDescription = "Fullscreen")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> LoadingState()
                file == null -> ErrorState()
                file.isImage -> ImageContent(file)
                file.isVideo -> VideoContent(
                    file = file,
                    isPlaying = uiState.isPlaying,
                    onTogglePlayback = { viewModel.togglePlayback() },
                    onToggleFullscreen = { viewModel.toggleFullscreen() }
                )
                file.isDocument -> DocumentContent(file)
                file.isAudio -> AudioContent(
                    file = file,
                    isPlaying = uiState.isPlaying,
                    onTogglePlayback = { viewModel.togglePlayback() }
                )
                file.isArchive -> ArchiveContent(file)
                else -> UnsupportedContent(file)
            }
        }
    }

    uiState.errorMessage?.let { msg ->
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("Dismiss")
                }
            }
        ) {
            Text(msg)
        }
    }

    uiState.successMessage?.let { msg ->
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = { viewModel.clearSuccess() }) {
                    Text("OK")
                }
            }
        ) {
            Text(msg)
        }
    }
}

@Composable
private fun FullscreenContent(
    file: VaultFile,
    isPlaying: Boolean,
    onTogglePlayback: () -> Unit,
    onToggleFullscreen: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        when {
            file.isImage -> {
                var scale by remember { mutableFloatStateOf(1f) }
                var offsetX by remember { mutableFloatStateOf(0f) }
                var offsetY by remember { mutableFloatStateOf(0f) }

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(file.localPath ?: file.remotePath)
                        .crossfade(true)
                        .build(),
                    contentDescription = file.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(0.5f, 5f)
                                offsetX += pan.x
                                offsetY += pan.y
                            }
                        },
                    contentScale = ContentScale.Fit
                )
            }
            file.isVideo -> {
                val context = LocalContext.current
                val player = remember {
                    ExoPlayer.Builder(context).build().apply {
                        val mediaItem = MediaItem.fromUri(file.localPath ?: file.remotePath)
                        setMediaItem(mediaItem)
                        prepare()
                        playWhenReady = isPlaying
                    }
                }

                DisposableEffect(Unit) {
                    onDispose { player.release() }
                }

                AndroidView(
                    factory = {
                        PlayerView(context).apply {
                            player = player
                            useController = true
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        IconButton(
            onClick = onToggleFullscreen,
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
        ) {
            Icon(
                Icons.Filled.FullscreenExit,
                contentDescription = "Exit fullscreen",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun ImageContent(file: VaultFile) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(file.localPath ?: file.remotePath)
                .crossfade(true)
                .build(),
            contentDescription = file.name,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 5f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                },
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun VideoContent(
    file: VaultFile,
    isPlaying: Boolean,
    onTogglePlayback: () -> Unit,
    onToggleFullscreen: () -> Unit
) {
    val context = LocalContext.current
    var isPlayerReady by remember { mutableStateOf(false) }

    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(file.localPath ?: file.remotePath)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = isPlaying
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        isPlayerReady = true
                    }
                }
            })
        }
    }

    DisposableEffect(Unit) {
        onDispose { player.release() }
    }

    LaunchedEffect(isPlaying) {
        player.playWhenReady = isPlaying
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = player
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = { player.seekTo((player.currentPosition - 10000).coerceAtLeast(0L)) }) {
                    Icon(Icons.Filled.Replay10, contentDescription = "Rewind 10s", tint = Color.White)
                }
                IconButton(
                    onClick = onTogglePlayback,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        if (isPlaying) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = Modifier.fillMaxSize(),
                        tint = Color.White
                    )
                }
                IconButton(onClick = { player.seekTo(player.currentPosition + 30000) }) {
                    Icon(Icons.Filled.Forward30, contentDescription = "Forward 30s", tint = Color.White)
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDuration(player.currentPosition),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
                Slider(
                    value = if (player.duration > 0) player.currentPosition.toFloat() / player.duration.toFloat() else 0f,
                    onValueChange = { player.seekTo((it * player.duration).roundToInt()) },
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White
                    )
                )
                Text(
                    text = formatDuration(player.duration),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        IconButton(
            onClick = onToggleFullscreen,
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
        ) {
            Icon(Icons.Filled.Fullscreen, contentDescription = "Fullscreen", tint = Color.White)
        }
    }
}

@Composable
private fun DocumentContent(file: VaultFile) {
    if (file.extension == "txt" || file.mimeType.startsWith("text/")) {
        TextContent(file)
    } else if (file.extension == "pdf") {
        PdfContent()
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(
                    Icons.Filled.Description,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    file.name,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    file.formattedSize,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TextContent(file: VaultFile) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Text file: ${file.name}",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Monospace
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PdfContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.PictureAsPdf,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "PDF Viewer",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "PDF viewing will be available in a future update",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AudioContent(
    file: VaultFile,
    isPlaying: Boolean,
    onTogglePlayback: () -> Unit
) {
    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(file.localPath ?: file.remotePath)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = isPlaying
        }
    }

    DisposableEffect(Unit) {
        onDispose { player.release() }
    }

    LaunchedEffect(isPlaying) {
        player.playWhenReady = isPlaying
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.size(200.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Audiotrack,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            file.name,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(8.dp))

        Text(
            file.formattedSize,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatDuration(player.currentPosition),
                style = MaterialTheme.typography.bodySmall
            )
            Slider(
                value = if (player.duration > 0) player.currentPosition.toFloat() / player.duration.toFloat() else 0f,
                onValueChange = { player.seekTo((it * player.duration).roundToInt()) },
                modifier = Modifier.weight(1f)
            )
            Text(
                text = formatDuration(player.duration),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(16.dp))

        IconButton(
            onClick = onTogglePlayback,
            modifier = Modifier.size(80.dp)
        ) {
            Icon(
                if (isPlaying) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun ArchiveContent(file: VaultFile) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Filled.FolderZip,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Text(
                file.name,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                file.formattedSize,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Archive extraction will be available in a future update",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun UnsupportedContent(file: VaultFile) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Filled.InsertDriveFile,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Text(
                file.name,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Unsupported file type",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("File not found", style = MaterialTheme.typography.bodyLarge)
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
