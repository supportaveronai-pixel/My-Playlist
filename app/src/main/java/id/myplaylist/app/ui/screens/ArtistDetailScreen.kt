package id.myplaylist.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import id.myplaylist.app.model.Artist
import id.myplaylist.app.model.PlayerState
import id.myplaylist.app.model.Track
import id.myplaylist.app.ui.components.AlbumFeaturedCard
import id.myplaylist.app.ui.components.CoverArt
import id.myplaylist.app.ui.components.TrackRow
import id.myplaylist.app.ui.components.shareTextContent
import id.myplaylist.app.ui.theme.AppColors

@Composable
fun ArtistDetailScreen(
    artist: Artist,
    isFollowing: Boolean,
    playCounts: Map<String, Int>,
    playerState: PlayerState,
    favoriteTrackIds: Set<String>,
    onBack: () -> Unit,
    onToggleFollow: () -> Unit,
    onOpenAlbum: (Album) -> Unit,
    onPlayTrack: (Track, List<Track>, String) -> Unit,
    onToggleTrackFavorite: (String) -> Unit,
    onAddToPlaylist: (Track) -> Unit,
    onPlayNext: (Track) -> Unit,
    onAddToQueue: (Track) -> Unit,
    onStartRadio: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val popularTracks = remember(playCounts) {
        TrackCatalogue.tracks
            .sortedWith(compareByDescending<Track> { playCounts[it.id] ?: 0 }.thenBy { it.number })
            .take(6)
    }
    val artistAlbums = remember(artist) {
        TrackCatalogue.albums.filter { it.id in artist.albumIds }
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Profil Artis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
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
                    coverUrl = artist.coverUrl,
                    seedNumber = 99,
                    size = 156.dp,
                    cornerRadius = 78.dp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Artis Instrumental Demo • ${artistAlbums.size} Album",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextMuted
                )
                Spacer(modifier = Modifier.height(14.dp))
                if (isFollowing) {
                    OutlinedButton(
                        onClick = onToggleFollow,
                        border = BorderStroke(1.dp, AppColors.AccentViolet),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Mengikuti • Berhenti Mengikuti", color = AppColors.AccentViolet)
                    }
                } else {
                    Button(
                        onClick = onToggleFollow,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.AccentViolet,
                            contentColor = AppColors.OnAccent
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Ikuti Artis")
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        item {
            Text(
                text = "Lagu Populer",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        items(popularTracks, key = { it.id }) { track ->
            val isCurrent = playerState.currentTrack?.id == track.id
            val count = playCounts[track.id] ?: 0
            Column {
                TrackRow(
                    track = track,
                    isCurrentTrack = isCurrent,
                    isPlaying = isCurrent && playerState.isPlaying,
                    isFavorite = track.id in favoriteTrackIds,
                    onPlay = { onPlayTrack(track, popularTracks, "Populer: ${artist.name}") },
                    onToggleFavorite = { onToggleTrackFavorite(track.id) },
                    onAddToPlaylist = { onAddToPlaylist(track) },
                    onPlayNext = { onPlayNext(track) },
                    onAddToQueue = { onAddToQueue(track) },
                    onStartRadio = { onStartRadio(track) },
                    onOpenAlbum = {
                        TrackCatalogue.albums.firstOrNull { it.id == track.albumId }?.let(onOpenAlbum)
                    },
                    onShare = {
                        shareTextContent(
                            context,
                            track.title,
                            "Dengarkan ${track.title} oleh ${track.artist} di My Playlist: ${track.streamUrl}"
                        )
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                if (count > 0) {
                    Text(
                        text = "Diputar $count kali",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.TextMuted,
                        modifier = Modifier.padding(start = 24.dp, bottom = 4.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Diskografi",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(artistAlbums, key = { it.id }) { album ->
                    AlbumFeaturedCard(
                        album = album,
                        onClick = { onOpenAlbum(album) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceLight),
                border = BorderStroke(1.dp, AppColors.BorderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tentang Artis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = artist.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextMuted
                    )
                }
            }
        }
    }
}
