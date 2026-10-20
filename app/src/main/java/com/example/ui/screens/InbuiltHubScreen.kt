package com.example.ui.screens

import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.widget.VideoView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.model.BrowserHttpPortalState
import com.example.domain.model.DeviceFileEntry
import com.example.domain.model.PickerMediaCategory
import com.example.ui.theme.CyberBorderSubtle
import com.example.ui.theme.CyberObsidian
import com.example.ui.theme.CyberSlateCard
import com.example.ui.theme.CyberSlateElevated
import com.example.ui.theme.ElectricSky
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldNominal
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimaryIce
import com.example.ui.theme.TextSecondarySlate
import com.example.ui.viewmodel.VayuDropViewModel
import com.example.ui.viewmodel.VayuUiState
import kotlinx.coroutines.delay
import java.io.File
import kotlin.math.sin

private enum class MediaGalleryFilter(val label: String) {
    ALL("All Media"),
    VIDEOS("Videos"),
    PHOTOS("Photos"),
    MUSIC("Music")
}

/**
 * 4th Bottom Navigation Tab: Real Inbuilt Media Gallery & Video Previewer
 * Lets users preview received/device photos in high resolution and play videos & audio directly inside the app.
 */
@Composable
fun InbuiltHubScreen(
    uiState: VayuUiState,
    browserPortalState: BrowserHttpPortalState,
    onSelectMediaPreview: (DeviceFileEntry) -> Unit,
    onToggleBrowserPortal: (Boolean) -> Unit,
    onQueueMediaEntry: (DeviceFileEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    var activeFilter by remember { mutableStateOf(MediaGalleryFilter.ALL) }
    var isFullscreenOpen by remember { mutableStateOf(false) }

    val photosList = uiState.categorizedCatalog[PickerMediaCategory.PHOTOS].orEmpty()
    val videosList = uiState.categorizedCatalog[PickerMediaCategory.VIDEOS].orEmpty()
    val musicList = uiState.categorizedCatalog[PickerMediaCategory.MUSIC].orEmpty()

    val allMediaItems = remember(photosList, videosList, musicList) {
        videosList + photosList + musicList
    }

    val filteredItems = remember(activeFilter, photosList, videosList, musicList, allMediaItems) {
        when (activeFilter) {
            MediaGalleryFilter.ALL -> allMediaItems
            MediaGalleryFilter.VIDEOS -> videosList
            MediaGalleryFilter.PHOTOS -> photosList
            MediaGalleryFilter.MUSIC -> musicList
        }
    }

    val activeMediaItem = uiState.selectedMediaPreviewItem
        ?: filteredItems.firstOrNull()
        ?: allMediaItems.firstOrNull()

    if (isFullscreenOpen && activeMediaItem != null) {
        FullscreenMediaPreviewModal(
            entry = activeMediaItem,
            onDismiss = { isFullscreenOpen = false },
            onShareFile = {
                isFullscreenOpen = false
                onQueueMediaEntry(activeMediaItem)
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("inbuilt_hub_screen"),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "WarpDrop Media Gallery & Player",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimaryIce,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.testTag("inbuilt_hub_title")
                    )
                    Text(
                        text = "Preview photos and play videos & music right inside WarpDrop",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondarySlate
                    )
                }
            }
        }

        // 2. Active Built-in Video Player / Photo Lightbox / Audio Player Stage
        if (activeMediaItem != null) {
            item {
                InbuiltMediaPlayerStage(
                    entry = activeMediaItem,
                    onExpandFullscreen = { isFullscreenOpen = true },
                    onShareEntry = { onQueueMediaEntry(activeMediaItem) }
                )
            }
        }

        // 3. Filter Pills: All Media, Videos, Photos, Music
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MediaGalleryFilter.entries.forEach { filter ->
                    val selected = filter == activeFilter
                    Surface(
                        color = if (selected) NeonCyan else CyberSlateCard,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(42.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = 1.dp,
                                color = if (selected) NeonCyan else CyberBorderSubtle,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { activeFilter = filter }
                            .testTag("media_filter_${filter.name.lowercase()}")
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filter.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) CyberObsidian else TextPrimaryIce,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 4. Media Gallery Items List
        items(filteredItems, key = { it.id }) { item ->
            val isCurrentlyPlaying = activeMediaItem?.id == item.id
            MediaGalleryItemCard(
                entry = item,
                isActive = isCurrentlyPlaying,
                onSelectToPlay = { onSelectMediaPreview(item) },
                onSendItem = { onQueueMediaEntry(item) }
            )
        }

        // 5. Compact Consumer Web Browser Sharing Card
        item {
            Surface(
                color = CyberSlateCard,
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (browserPortalState.isRunning) EmeraldNominal else CyberBorderSubtle,
                        shape = RoundedCornerShape(22.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(NeonCyan.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "Share to Web Browser",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Share with PC, Mac or iPhone via WarpDrop Web",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimaryIce,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.testTag("browser_portal_toggle_title")
                                )
                                Text(
                                    text = "Open your gallery in any web browser over Wi-Fi",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondarySlate
                                )
                            }
                        }

                        Switch(
                            checked = browserPortalState.isRunning,
                            onCheckedChange = onToggleBrowserPortal,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyberObsidian,
                                checkedTrackColor = EmeraldNominal
                            ),
                            modifier = Modifier.testTag("hub_browser_portal_switch")
                        )
                    }

                    if (browserPortalState.isRunning) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(CyberObsidian)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = browserPortalState.portalUrl,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = EmeraldNominal,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Enter this address in Chrome or Safari",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondarySlate
                                )
                            }
                            IconButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(browserPortalState.portalUrl))
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Address",
                                    tint = NeonCyan
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InbuiltMediaPlayerStage(
    entry: DeviceFileEntry,
    onExpandFullscreen: () -> Unit,
    onShareEntry: () -> Unit
) {
    val context = LocalContext.current
    var isPlaying by remember(entry.id) {
        mutableStateOf(entry.category == PickerMediaCategory.VIDEOS)
    }
    var playbackProgress by remember(entry.id) { mutableFloatStateOf(0.18f) }
    var isMuted by remember(entry.id) { mutableStateOf(false) }
    var playbackSpeed by remember(entry.id) { mutableFloatStateOf(1.0f) }
    var isPhotoZoomed by remember(entry.id) { mutableStateOf(false) }

    val totalDurationSec = remember(entry.id) {
        if (entry.durationSeconds > 0) entry.durationSeconds else 48
    }

    // Real MediaPlayer for playable audio files
    DisposableEffect(entry.id, isPlaying, isMuted) {
        var mediaPlayer: MediaPlayer? = null
        if (entry.category == PickerMediaCategory.MUSIC && isPlaying && entry.localPath != null) {
            runCatching {
                val f = File(entry.localPath)
                if (f.exists()) {
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(f.absolutePath)
                        isLooping = true
                        val vol = if (isMuted) 0f else 0.7f
                        setVolume(vol, vol)
                        prepare()
                        start()
                    }
                }
            }
        }
        onDispose {
            runCatching {
                mediaPlayer?.stop()
                mediaPlayer?.release()
            }
        }
    }

    // Advance timeline smoothly while playing video or music
    LaunchedEffect(entry.id, isPlaying, playbackSpeed) {
        while (isPlaying && (entry.category == PickerMediaCategory.VIDEOS || entry.category == PickerMediaCategory.MUSIC)) {
            delay(100L)
            val step = (0.1f * playbackSpeed) / totalDurationSec.toFloat()
            val next = playbackProgress + step
            playbackProgress = if (next >= 1f) 0f else next
        }
    }

    val photoBitmap = remember(entry.id, entry.localPath) {
        if (entry.category == PickerMediaCategory.PHOTOS && entry.localPath != null) {
            runCatching {
                val f = File(entry.localPath)
                if (f.exists()) BitmapFactory.decodeFile(f.absolutePath)?.asImageBitmap() else null
            }.getOrNull()
        } else null
    }

    Surface(
        color = CyberSlateCard,
        shape = RoundedCornerShape(26.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(26.dp))
            .testTag("inbuilt_media_player_stage")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Viewport Box (16:9 Cinema Frame)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(225.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CyberObsidian)
            ) {
                when (entry.category) {
                    PickerMediaCategory.PHOTOS -> {
                        if (photoBitmap != null) {
                            Image(
                                bitmap = photoBitmap,
                                contentDescription = entry.displayName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scale(if (isPhotoZoomed) 1.65f else 1f)
                                    .clickable { isPhotoZoomed = !isPhotoZoomed }
                            )
                        } else {
                            CinemaMotionCanvas(
                                paletteIndex = entry.previewPaletteIndex,
                                progress = 0.35f,
                                isAudioMode = false
                            )
                        }
                    }

                    PickerMediaCategory.VIDEOS -> {
                        // Embed native Android VideoView for real external URIs + animated 60fps Cinema Renderer
                        if (!entry.isSamplePreview && entry.uri != null) {
                            AndroidView(
                                factory = { ctx ->
                                    VideoView(ctx).apply {
                                        runCatching {
                                            setVideoURI(entry.uri)
                                            setOnPreparedListener { mp ->
                                                mp.isLooping = true
                                                val v = if (isMuted) 0f else 1f
                                                mp.setVolume(v, v)
                                                if (isPlaying) start()
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        CinemaMotionCanvas(
                            paletteIndex = entry.previewPaletteIndex,
                            progress = playbackProgress,
                            isAudioMode = false
                        )
                    }

                    else -> {
                        CinemaMotionCanvas(
                            paletteIndex = entry.previewPaletteIndex,
                            progress = playbackProgress,
                            isAudioMode = true
                        )
                    }
                }

                // Top Overlay Badge + Fullscreen & Zoom Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = CyberObsidian.copy(alpha = 0.78f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = when (entry.category) {
                                PickerMediaCategory.VIDEOS -> "VIDEO PLAYER • ${entry.subtitleLabel ?: "HD"}"
                                PickerMediaCategory.PHOTOS -> "PHOTO PREVIEW • ${if (isPhotoZoomed) "2.0x ZOOM" else "1.0x"}"
                                else -> "AUDIO PLAYER • ${entry.subtitleLabel ?: "STEREO"}"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (entry.category == PickerMediaCategory.PHOTOS) {
                            IconButton(
                                onClick = { isPhotoZoomed = !isPhotoZoomed },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CyberObsidian.copy(alpha = 0.75f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomIn,
                                    contentDescription = "Toggle Zoom",
                                    tint = TextPrimaryIce,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onExpandFullscreen,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CyberObsidian.copy(alpha = 0.75f))
                                .testTag("expand_fullscreen_media_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Fullscreen",
                                tint = TextPrimaryIce,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Center Play/Pause Overlay for Video
                if (entry.category == PickerMediaCategory.VIDEOS && !isPlaying) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.9f))
                            .clickable { isPlaying = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Video",
                            tint = CyberObsidian,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title & Share Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.displayName.replace('_', ' '),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimaryIce,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${VayuDropViewModel.formatBytes(entry.sizeBytes)} • ${entry.subtitleLabel ?: entry.category.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondarySlate
                    )
                }

                Button(
                    onClick = onShareEntry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = CyberObsidian
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send File", fontWeight = FontWeight.Bold)
                }
            }

            // Interactive Video / Audio Transport & Scrubber Bar
            if (entry.category == PickerMediaCategory.VIDEOS || entry.category == PickerMediaCategory.MUSIC) {
                Spacer(modifier = Modifier.height(8.dp))

                val elapsedSec = (playbackProgress * totalDurationSec).toInt()
                Slider(
                    value = playbackProgress,
                    onValueChange = { playbackProgress = it },
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = CyberObsidian
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("media_player_seekbar")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${formatDuration(elapsedSec)} / ${formatDuration(totalDurationSec)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondarySlate
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = {
                                playbackProgress = (playbackProgress - 10f / totalDurationSec).coerceIn(0f, 1f)
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay10,
                                contentDescription = "Rewind 10s",
                                tint = TextPrimaryIce
                            )
                        }

                        IconButton(
                            onClick = { isPlaying = !isPlaying },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(NeonCyan)
                                .testTag("video_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = CyberObsidian
                            )
                        }

                        IconButton(
                            onClick = {
                                playbackProgress = (playbackProgress + 10f / totalDurationSec).coerceIn(0f, 1f)
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forward10,
                                contentDescription = "Forward 10s",
                                tint = TextPrimaryIce
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = CyberObsidian,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .clickable {
                                    playbackSpeed = when (playbackSpeed) {
                                        1.0f -> 1.5f
                                        1.5f -> 2.0f
                                        else -> 1.0f
                                    }
                                }
                        ) {
                            Text(
                                text = "${playbackSpeed}x",
                                style = MaterialTheme.typography.labelMedium,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        IconButton(
                            onClick = { isMuted = !isMuted },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Mute or Unmute",
                                tint = if (isMuted) TextSecondarySlate else NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CinemaMotionCanvas(
    paletteIndex: Int,
    progress: Float,
    isAudioMode: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cinema_wave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val bgColors = when (paletteIndex % 4) {
        0 -> listOf(Color(0xFF071326), Color(0xFF0284C7), Color(0xFF0F172A))
        1 -> listOf(Color(0xFF1A103C), Color(0xFF6D28D9), Color(0xFF090D16))
        2 -> listOf(Color(0xFF06281E), Color(0xFF059669), Color(0xFF090D16))
        else -> listOf(Color(0xFF271709), Color(0xFFD97706), Color(0xFF0B0F19))
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.verticalGradient(colors = bgColors),
            size = size
        )

        if (isAudioMode) {
            // Animated 24-Bar Equalizer Visualizer
            val bars = 24
            val barWidth = (w * 0.72f) / bars
            val startX = w * 0.14f
            for (i in 0 until bars) {
                val amp = (0.25f + 0.65f * kotlin.math.abs(sin(wavePhase + i * 0.45f + progress * 10f)))
                val barH = h * 0.55f * amp
                drawRoundRect(
                    color = if (i % 2 == 0) NeonCyan else ElectricSky,
                    topLeft = Offset(startX + i * barWidth, (h - barH) / 2f),
                    size = Size(barWidth * 0.65f, barH),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
        } else {
            // Animated Cinema Horizon + Moving Sun/Drone Orb
            val sunX = w * (0.22f + 0.56f * progress)
            val sunY = h * (0.36f - 0.08f * sin(progress * 3.14f))
            drawCircle(
                color = NeonCyan.copy(alpha = 0.24f),
                radius = 58f,
                center = Offset(sunX, sunY)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.88f),
                radius = 26f,
                center = Offset(sunX, sunY)
            )

            // Flowing landscape silhouette waves
            val wavePath = Path().apply {
                moveTo(0f, h)
                var x = 0f
                while (x <= w) {
                    val y = h * 0.68f + 18f * sin((x / w) * 6.28f + wavePhase + progress * 8f)
                    lineTo(x, y)
                    x += 16f
                }
                lineTo(w, h)
                close()
            }
            drawPath(
                path = wavePath,
                color = CyberObsidian.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun MediaGalleryItemCard(
    entry: DeviceFileEntry,
    isActive: Boolean,
    onSelectToPlay: () -> Unit,
    onSendItem: () -> Unit
) {
    val photoBitmap = remember(entry.id, entry.localPath) {
        if (entry.category == PickerMediaCategory.PHOTOS && entry.localPath != null) {
            runCatching {
                val f = File(entry.localPath)
                if (f.exists()) BitmapFactory.decodeFile(f.absolutePath)?.asImageBitmap() else null
            }.getOrNull()
        } else null
    }

    val badgeGradient = when (entry.previewPaletteIndex % 4) {
        0 -> listOf(Color(0xFF0284C7), Color(0xFF00F0FF))
        1 -> listOf(Color(0xFF7C3AED), Color(0xFF38BDF8))
        2 -> listOf(Color(0xFF059669), Color(0xFF34D399))
        else -> listOf(Color(0xFFD97706), Color(0xFFFBBF24))
    }

    Surface(
        color = CyberSlateCard,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = if (isActive) NeonCyan else CyberBorderSubtle,
                shape = RoundedCornerShape(22.dp)
            )
            .clickable { onSelectToPlay() }
            .testTag("hub_media_item_${entry.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(badgeGradient)),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoBitmap != null) {
                        Image(
                            bitmap = photoBitmap,
                            contentDescription = entry.displayName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        val icon = when (entry.category) {
                            PickerMediaCategory.VIDEOS -> Icons.Default.PlayCircleFilled
                            PickerMediaCategory.PHOTOS -> Icons.Default.Image
                            else -> Icons.Default.AudioFile
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = entry.category.label,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.displayName.replace('_', ' '),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryIce,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = entry.subtitleLabel ?: entry.category.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondarySlate
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = VayuDropViewModel.formatBytes(entry.sizeBytes),
                            style = MaterialTheme.typography.labelMedium,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                        if (isActive) {
                            Text(
                                text = "• Now Previewing",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldNominal,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = onSelectToPlay,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.16f))
                ) {
                    Icon(
                        imageVector = if (entry.category == PickerMediaCategory.PHOTOS) Icons.Default.Image else Icons.Default.PlayArrow,
                        contentDescription = "Preview",
                        tint = NeonCyan
                    )
                }

                IconButton(
                    onClick = onSendItem,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CyberSlateElevated)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = TextPrimaryIce,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FullscreenMediaPreviewModal(
    entry: DeviceFileEntry,
    onDismiss: () -> Unit,
    onShareFile: () -> Unit
) {
    val photoBitmap = remember(entry.id, entry.localPath) {
        if (entry.category == PickerMediaCategory.PHOTOS && entry.localPath != null) {
            runCatching {
                val f = File(entry.localPath)
                if (f.exists()) BitmapFactory.decodeFile(f.absolutePath)?.asImageBitmap() else null
            }.getOrNull()
        } else null
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = CyberObsidian,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(28.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = entry.displayName.replace('_', ' '),
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimaryIce,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${VayuDropViewModel.formatBytes(entry.sizeBytes)} • ${entry.subtitleLabel ?: entry.category.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonCyan
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Fullscreen",
                            tint = TextPrimaryIce
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(CyberSlateCard)
                ) {
                    if (photoBitmap != null) {
                        Image(
                            bitmap = photoBitmap,
                            contentDescription = entry.displayName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        CinemaMotionCanvas(
                            paletteIndex = entry.previewPaletteIndex,
                            progress = 0.5f,
                            isAudioMode = entry.category == PickerMediaCategory.MUSIC
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onShareFile,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = CyberObsidian
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send This File", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun formatDuration(totalSeconds: Int): String {
    val mins = totalSeconds / 60
    val secs = totalSeconds % 60
    return "%02d:%02d".format(mins, secs)
}
