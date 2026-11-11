package id.myplaylist.app.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.myplaylist.app.model.PlayerState
import id.myplaylist.app.model.Track
import id.myplaylist.app.ui.components.CoverArt
import id.myplaylist.app.ui.theme.AppColors

@Composable
fun QueueScreen(
    playerState: PlayerState,
    onBack: () -> Unit,
    onRemoveAt: (Int) -> Unit,
    onClearQueue: () -> Unit,
    onPlayQueueItem: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTrack = playerState.currentTrack
    val currentIdx = playerState.currentIndex
    val upcomingWithIndices = if (currentIdx >= 0 && currentIdx + 1 < playerState.queue.size) {
        playerState.queue.mapIndexed { idx, track -> idx to track }
            .drop(currentIdx + 1)
    } else {
        emptyList()
    }

    val upNextCount = playerState.upNextQueue.size
    val userQueuedItems = upcomingWithIndices.take(upNextCount)
    val contextUpcomingItems = upcomingWithIndices.drop(upNextCount)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = AppColors.BgWhite
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Tutup antrean")
                    }
                    Text(
                        text = "Antrean",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (upcomingWithIndices.isNotEmpty()) {
                    TextButton(onClick = onClearQueue) {
                        Text("Bersihkan antrean", color = AppColors.DangerRed)
                    }
                }
            }

            if (currentTrack == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.QueueMusic,
                            contentDescription = null,
                            tint = AppColors.TextMuted,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Antrean kosong",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "Sedang Diputar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        QueueTrackCard(
                            track = currentTrack,
                            isCurrent = true,
                            onRemove = null,
                            onClick = {}
                        )
                    }

                    if (userQueuedItems.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Antrean Berikutnya (${userQueuedItems.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        itemsIndexed(userQueuedItems, key = { i, pair -> "user_${pair.first}_${pair.second.id}_$i" }) { _, (queueIdx, track) ->
                            QueueTrackCard(
                                track = track,
                                isCurrent = false,
                                onRemove = { onRemoveAt(queueIdx) },
                                onClick = { onPlayQueueItem(track) }
                            )
                        }
                    }

                    if (contextUpcomingItems.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Berikutnya Dari: ${playerState.sourceLabel}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        itemsIndexed(contextUpcomingItems, key = { i, pair -> "ctx_${pair.first}_${pair.second.id}_$i" }) { _, (queueIdx, track) ->
                            QueueTrackCard(
                                track = track,
                                isCurrent = false,
                                onRemove = { onRemoveAt(queueIdx) },
                                onClick = { onPlayQueueItem(track) }
                            )
                        }
                    }

                    if (upcomingWithIndices.isEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Tidak ada lagu berikutnya di antrean. Gunakan menu titik tiga pada lagu untuk menambahkan ke antrean.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.TextMuted
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }
}

@Composable
private fun QueueTrackCard(
    track: Track,
    isCurrent: Boolean,
    onRemove: (() -> Unit)?,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) AppColors.SurfaceAlt else AppColors.BgWhite
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isCurrent) AppColors.AccentViolet.copy(alpha = 0.4f) else AppColors.BorderLight
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CoverArt(
                coverUrl = track.coverUrl,
                seedNumber = track.number,
                size = 44.dp,
                cornerRadius = 8.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isCurrent) AppColors.AccentViolet else AppColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${track.artist} • ${track.genre}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (onRemove != null) {
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Hapus dari antrean",
                        tint = AppColors.TextMuted
                    )
                }
            }
        }
    }
}
