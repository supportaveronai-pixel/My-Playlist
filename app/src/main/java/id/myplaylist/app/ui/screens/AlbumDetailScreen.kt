package id.myplaylist.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.myplaylist.app.data.TrackCatalogue
import id.myplaylist.app.model.Album
import id.myplaylist.app.model.PlayerState
import id.myplaylist.app.model.Track
import id.myplaylist.app.ui.components.CoverArt
import id.myplaylist.app.ui.components.TrackRow
import id.myplaylist.app.ui.components.shareTextContent
import id.myplaylist.app.ui.theme.AppColors

@Composable
fun AlbumDetailScreen(
    album: Album,
    playerState: PlayerState,
    favoriteTrackIds: Set<String>,
    isAlbumFavorite: Boolean,
    onBack: () -> Unit,
    onToggleAlbumFavorite: () -> Unit,
    onPlayTrack: (Track, List<Track>, String, Boolean) -> Unit,
    onToggleTrackFavorite: (String) -> Unit,
    onAddToPlaylist: (Track) -> Unit,
    onPlayNext: (Track) -> Unit,
    onAddToQueue: (Track) -> Unit,
    onStartRadio: (Track) -> Unit,
    onOpenArtist: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val albumTracks = remember(album) {
        val byId = TrackCatalogue.tracks.associateBy { it.id }
        album.trackIds.mapNotNull { byId[it] }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali")
                }
                Text(
                    text = "Detail Album",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = {
                    shareTextContent(
                        context,
                        album.title,
                        "Dengarkan album ${album.title} oleh ${album.artist} (${album.year}) di My Playlist!"
                    )
                }) {
                    Icon(Icons.Rounded.Share, contentDescription = "Bagikan album")
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CoverArt(
                    coverUrl = album.coverUrl,
                    seedNumber = album.id.hashCode(),
                    size = 190.dp,
                    cornerRadius = 18.dp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = album.artist,
                    style = MaterialTheme.typography.titleMedium,
                    color = AppColors.AccentViolet,
                    modifier = Modifier.clickable(onClick = onOpenArtist)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Album • ${album.year} • ${albumTracks.size} lagu",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextMuted
                )
                if (album.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = album.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            albumTracks.firstOrNull()?.let { first ->
                                onPlayTrack(first, albumTracks, album.title, false)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.AccentViolet,
                            contentColor = AppColors.OnAccent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Putar Semua")
                    }

                    OutlinedButton(
                        onClick = {
                            albumTracks.randomOrNull()?.let { pick ->
                                onPlayTrack(pick, albumTracks, album.title, true)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, AppColors.AccentViolet),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.Shuffle, null, tint = AppColors.AccentViolet, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Putar Acak", color = AppColors.AccentViolet)
                    }

                    IconButton(onClick = onToggleAlbumFavorite) {
                        Icon(
                            imageVector = if (isAlbumFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = if (isAlbumFavorite) "Hapus favorit album" else "Favoritkan album",
                            tint = if (isAlbumFavorite) AppColors.AccentViolet else AppColors.TextMuted
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        items(albumTracks, key = { it.id }) { track ->
            val isCurrent = playerState.currentTrack?.id == track.id
            TrackRow(
                track = track,
                isCurrentTrack = isCurrent,
                isPlaying = isCurrent && playerState.isPlaying,
                isFavorite = track.id in favoriteTrackIds,
                onPlay = { onPlayTrack(track, albumTracks, album.title, false) },
                onToggleFavorite = { onToggleTrackFavorite(track.id) },
                onAddToPlaylist = { onAddToPlaylist(track) },
                onPlayNext = { onPlayNext(track) },
                onAddToQueue = { onAddToQueue(track) },
                onStartRadio = { onStartRadio(track) },
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
        }
    }
}
