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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.myplaylist.app.data.TrackCatalogue
import id.myplaylist.app.model.PlayerState
import id.myplaylist.app.model.Track
import id.myplaylist.app.ui.components.TrackRow
import id.myplaylist.app.ui.components.shareTextContent
import id.myplaylist.app.ui.theme.AppColors

@Composable
fun HistoryScreen(
    recentlyPlayedIds: List<String>,
    playCounts: Map<String, Int>,
    playerState: PlayerState,
    favoriteIds: Set<String>,
    onBack: () -> Unit,
    onClearHistory: () -> Unit,
    onPlayTrack: (Track, List<Track>, String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onAddToPlaylist: (Track) -> Unit,
    onPlayNext: (Track) -> Unit,
    onAddToQueue: (Track) -> Unit,
    onStartRadio: (Track) -> Unit,
    onOpenAlbum: (String) -> Unit,
    onOpenArtist: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val historyTracks = remember(recentlyPlayedIds) {
        val byId = TrackCatalogue.tracks.associateBy { it.id }
        recentlyPlayedIds.mapNotNull { byId[it] }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali")
                }
                Text(
                    text = "Riwayat Putar",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            if (historyTracks.isNotEmpty()) {
                TextButton(onClick = onClearHistory) {
                    Text("Hapus Riwayat", color = AppColors.DangerRed)
                }
            }
        }

        if (historyTracks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = null,
                        tint = AppColors.TextMuted,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Belum ada riwayat putar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Lagu yang baru kamu dengarkan akan otomatis tersimpan di sini hingga 50 lagu terakhir.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(historyTracks, key = { it.id }) { track ->
                    val isCurrent = playerState.currentTrack?.id == track.id
                    val plays = playCounts[track.id] ?: 0
                    Column {
                        TrackRow(
                            track = track,
                            isCurrentTrack = isCurrent,
                            isPlaying = isCurrent && playerState.isPlaying,
                            isFavorite = track.id in favoriteIds,
                            onPlay = { onPlayTrack(track, historyTracks, "Riwayat Putar") },
                            onToggleFavorite = { onToggleFavorite(track.id) },
                            onAddToPlaylist = { onAddToPlaylist(track) },
                            onPlayNext = { onPlayNext(track) },
                            onAddToQueue = { onAddToQueue(track) },
                            onStartRadio = { onStartRadio(track) },
                            onOpenAlbum = { onOpenAlbum(track.albumId) },
                            onOpenArtist = onOpenArtist,
                            onShare = {
                                shareTextContent(
                                    context,
                                    track.title,
                                    "Dengarkan ${track.title} oleh ${track.artist} di My Playlist: ${track.streamUrl}"
                                )
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                        if (plays > 0) {
                            Text(
                                text = "Total diputar: $plays kali",
                                style = MaterialTheme.typography.labelMedium,
                                color = AppColors.TextMuted,
                                modifier = Modifier.padding(start = 24.dp, bottom = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
