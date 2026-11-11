package id.myplaylist.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.myplaylist.app.model.PlayerState
import id.myplaylist.app.model.RepeatMode
import id.myplaylist.app.ui.components.CoverArt
import id.myplaylist.app.ui.components.formatRemainingSeconds
import id.myplaylist.app.ui.components.formatTimeMs
import id.myplaylist.app.ui.components.openSystemEqualizer
import id.myplaylist.app.ui.components.shareTextContent
import id.myplaylist.app.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    playerState: PlayerState,
    isFavorite: Boolean,
    audioSessionId: Int,
    onDismiss: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenLyrics: () -> Unit,
    onSetPlaybackSpeed: (Float) -> Unit,
    onSetSleepTimer: (Int?) -> Unit,
    onAddToPlaylist: () -> Unit,
    onStartRadio: () -> Unit,
    onOpenAlbum: (String) -> Unit,
    onOpenArtist: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val track = playerState.currentTrack ?: return
    val context = LocalContext.current

    var isDragging by remember { mutableStateOf(false) }
    var dragPositionMs by remember { mutableFloatStateOf(0f) }
    var showTopMenu by remember { mutableStateOf(false) }
    var showSleepDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }

    val displayPosition = if (isDragging) dragPositionMs.toLong() else playerState.positionMs
    val duration = playerState.durationMs.coerceAtLeast(1L)
    val sliderValue = (displayPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)

    if (showSleepDialog) {
        SleepTimerDialog(
            activeMinutes = playerState.sleepTimerMinutes,
            remainingSeconds = playerState.sleepTimerRemainingSec,
            isEndOfTrack = playerState.isSleepAtTrackEnd,
            onSelectMinutes = onSetSleepTimer,
            onDismiss = { showSleepDialog = false }
        )
    }

    if (showSpeedDialog) {
        PlaybackSpeedDialog(
            currentSpeed = playerState.playbackSpeed,
            onSelectSpeed = onSetPlaybackSpeed,
            onDismiss = { showSpeedDialog = false }
        )
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = AppColors.BgWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Tutup pemutar penuh",
                        tint = AppColors.TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "MEMUTAR DARI",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = playerState.sourceLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box {
                    IconButton(onClick = { showTopMenu = true }) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "Menu pemutar",
                            tint = AppColors.TextPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = showTopMenu,
                        onDismissRequest = { showTopMenu = false },
                        containerColor = AppColors.BgWhite
                    ) {
                        DropdownMenuItem(
                            text = { Text("Timer Tidur") },
                            leadingIcon = { Icon(Icons.Rounded.Timer, null, tint = AppColors.AccentViolet) },
                            onClick = { showTopMenu = false; showSleepDialog = true }
                        )
                        DropdownMenuItem(
                            text = { Text("Equalizer") },
                            leadingIcon = { Icon(Icons.Rounded.Equalizer, null, tint = AppColors.AccentViolet) },
                            onClick = {
                                showTopMenu = false
                                openSystemEqualizer(context, audioSessionId)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Kecepatan Putar (${playerState.playbackSpeed}x)") },
                            leadingIcon = { Icon(Icons.Rounded.Speed, null, tint = AppColors.AccentViolet) },
                            onClick = { showTopMenu = false; showSpeedDialog = true }
                        )
                        DropdownMenuItem(
                            text = { Text("Buka Antrean") },
                            leadingIcon = { Icon(Icons.Rounded.QueueMusic, null, tint = AppColors.AccentViolet) },
                            onClick = { showTopMenu = false; onOpenQueue() }
                        )
                        DropdownMenuItem(
                            text = { Text("Radio Lagu") },
                            leadingIcon = { Icon(Icons.Rounded.Radio, null, tint = AppColors.AccentViolet) },
                            onClick = { showTopMenu = false; onStartRadio() }
                        )
                        DropdownMenuItem(
                            text = { Text("Tambah ke Playlist") },
                            leadingIcon = { Icon(Icons.Rounded.PlaylistAdd, null, tint = AppColors.AccentViolet) },
                            onClick = { showTopMenu = false; onAddToPlaylist() }
                        )
                        DropdownMenuItem(
                            text = { Text("Lihat Album") },
                            leadingIcon = { Icon(Icons.Rounded.Album, null, tint = AppColors.TextMuted) },
                            onClick = { showTopMenu = false; onOpenAlbum(track.albumId) }
                        )
                        DropdownMenuItem(
                            text = { Text("Lihat Artis") },
                            leadingIcon = { Icon(Icons.Rounded.Person, null, tint = AppColors.TextMuted) },
                            onClick = { showTopMenu = false; onOpenArtist() }
                        )
                        DropdownMenuItem(
                            text = { Text("Bagikan Lagu") },
                            leadingIcon = { Icon(Icons.Rounded.Share, null, tint = AppColors.TextMuted) },
                            onClick = {
                                showTopMenu = false
                                shareTextContent(
                                    context,
                                    track.title,
                                    "Dengarkan ${track.title} oleh ${track.artist} di My Playlist: ${track.streamUrl}"
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.3f))

            // Large Cover Art
            CoverArt(
                coverUrl = track.coverUrl,
                seedNumber = track.number,
                size = 270.dp,
                cornerRadius = 24.dp,
                modifier = Modifier.shadow(10.dp, RoundedCornerShape(24.dp))
            )

            Spacer(modifier = Modifier.weight(0.3f))

            // Track metadata & favorite heart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${track.artist} • ${track.genre}",
                        style = MaterialTheme.typography.titleMedium,
                        color = AppColors.TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable(onClick = onOpenArtist)
                    )
                }

                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = if (isFavorite) "Hapus dari favorit" else "Tambah ke favorit",
                        tint = if (isFavorite) AppColors.AccentViolet else AppColors.TextMuted,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Slider
            Slider(
                value = sliderValue,
                onValueChange = {
                    isDragging = true
                    dragPositionMs = it * duration
                },
                onValueChangeFinished = {
                    isDragging = false
                    onSeekTo(dragPositionMs.toLong())
                },
                colors = SliderDefaults.colors(
                    thumbColor = AppColors.AccentViolet,
                    activeTrackColor = AppColors.AccentViolet,
                    inactiveTrackColor = AppColors.BorderLight
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTimeMs(displayPosition),
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.TextMuted
                )
                Text(
                    text = if (playerState.durationMs > 0L) formatTimeMs(playerState.durationMs) else "demo",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.TextMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Playback controls row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleShuffle) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "Acak urutan lagu",
                        tint = if (playerState.shuffleEnabled) AppColors.AccentViolet else AppColors.TextMuted,
                        modifier = Modifier.size(26.dp)
                    )
                }

                IconButton(onClick = onPreviousTrack) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Lagu sebelumnya",
                        tint = AppColors.TextPrimary,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(AppColors.AccentViolet),
                    contentAlignment = Alignment.Center
                ) {
                    if (playerState.isBuffering) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        IconButton(
                            onClick = onTogglePlayPause,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = if (playerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (playerState.isPlaying) "Jeda" else "Putar",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                IconButton(onClick = onNextTrack) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Lagu berikutnya",
                        tint = AppColors.TextPrimary,
                        modifier = Modifier.size(38.dp)
                    )
                }

                IconButton(onClick = onCycleRepeat) {
                    val (icon, tint) = when (playerState.repeatMode) {
                        RepeatMode.OFF -> Icons.Rounded.Repeat to AppColors.TextMuted
                        RepeatMode.ALL -> Icons.Rounded.Repeat to AppColors.AccentViolet
                        RepeatMode.ONE -> Icons.Rounded.RepeatOne to AppColors.AccentViolet
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = "Ulangi lagu",
                        tint = tint,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Extra Actions Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lyrics button
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AppColors.SurfaceLight,
                    modifier = Modifier.clickable(onClick = onOpenLyrics)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Lyrics, null, tint = AppColors.AccentViolet, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Lirik", style = MaterialTheme.typography.labelMedium, color = AppColors.AccentViolet)
                    }
                }

                // Speed pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AppColors.SurfaceLight,
                    modifier = Modifier.clickable { showSpeedDialog = true }
                ) {
                    Text(
                        text = "${playerState.playbackSpeed}x",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.TextPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                // Sleep timer pill / icon
                val timerActive = playerState.sleepTimerMinutes != null || playerState.isSleepAtTrackEnd
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (timerActive) AppColors.SurfaceAlt else AppColors.SurfaceLight,
                    modifier = Modifier.clickable { showSleepDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Timer,
                            null,
                            tint = if (timerActive) AppColors.AccentViolet else AppColors.TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        if (timerActive) {
                            Spacer(modifier = Modifier.width(4.dp))
                            val timerText = if (playerState.isSleepAtTrackEnd) {
                                "Akhir lagu"
                            } else {
                                formatRemainingSeconds(playerState.sleepTimerRemainingSec)
                            }
                            Text(timerText, style = MaterialTheme.typography.labelMedium, color = AppColors.AccentViolet)
                        }
                    }
                }

                // Queue button
                IconButton(onClick = onOpenQueue) {
                    Icon(
                        imageVector = Icons.Rounded.QueueMusic,
                        contentDescription = "Buka antrean",
                        tint = AppColors.TextPrimary
                    )
                }

                // Equalizer button
                IconButton(onClick = { openSystemEqualizer(context, audioSessionId) }) {
                    Icon(
                        imageVector = Icons.Rounded.Equalizer,
                        contentDescription = "Equalizer sistem",
                        tint = AppColors.TextPrimary
                    )
                }
            }

            // Error display if any
            if (playerState.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.SurfaceAlt)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = playerState.errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.DangerRed,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onClearError) {
                        Text("Tutup", color = AppColors.AccentViolet)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.2f))
        }
    }
}
