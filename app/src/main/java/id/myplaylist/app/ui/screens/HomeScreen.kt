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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.myplaylist.app.data.TrackCatalogue
import id.myplaylist.app.domain.MadeForYouGenerator
import id.myplaylist.app.domain.PlaylistLogic
import id.myplaylist.app.model.Album
import id.myplaylist.app.model.PlayerState
import id.myplaylist.app.model.Playlist
import id.myplaylist.app.model.Track
import id.myplaylist.app.ui.components.AlbumFeaturedCard
import id.myplaylist.app.ui.components.CoverArt
import id.myplaylist.app.ui.components.GenreTileCard
import id.myplaylist.app.ui.components.PlaylistFeaturedCard
import id.myplaylist.app.ui.components.TrackRow
import id.myplaylist.app.ui.components.shareTextContent
import id.myplaylist.app.ui.theme.AppColors

@Composable
fun HomeScreen(
    profileName: String,
    playerState: PlayerState,
    favoriteIds: Set<String>,
    recentlyPlayedIds: List<String>,
    playCounts: Map<String, Int>,
    onPlayTrack: (Track, List<Track>, String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onSelectPlaylist: (Playlist) -> Unit,
    onSelectAlbum: (Album) -> Unit,
    onSelectGenre: (String) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenArtist: () -> Unit,
    onRequestAddToPlaylist: (Track) -> Unit,
    onPlayNext: (Track) -> Unit,
    onAddToQueue: (Track) -> Unit,
    onStartRadio: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allTracks = TrackCatalogue.tracks
    val builtInPlaylists = TrackCatalogue.builtInPlaylists
    val albums = TrackCatalogue.albums
    val genres = TrackCatalogue.genres

    val madeForYou = remember(playCounts) {
        MadeForYouGenerator.generateMadeForYou(allTracks, playCounts)
    }

    val recentTracks = remember(recentlyPlayedIds) {
        val byId = allTracks.associateBy { it.id }
        recentlyPlayedIds.mapNotNull { byId[it] }.take(8)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Halo, $profileName",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Siap mendengarkan musik pilihanmu hari ini?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextMuted
                    )
                }
                IconButton(onClick = onOpenHistory) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = "Riwayat Putar",
                        tint = AppColors.TextPrimary
                    )
                }
            }
        }

        // Recently played ("Lompat Kembali")
        if (recentTracks.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lompat Kembali",
                        style = MaterialTheme.typography.titleLarge
                    )
                    TextButton(onClick = onOpenHistory) {
                        Text("Lihat semua", color = AppColors.AccentViolet)
                    }
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(recentTracks, key = { "recent_${it.id}" }) { track ->
                        Card(
                            modifier = Modifier
                                .width(132.dp)
                                .clickable { onPlayTrack(track, recentTracks, "Lompat Kembali") },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceLight),
                            border = BorderStroke(1.dp, AppColors.BorderLight)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                CoverArt(
                                    coverUrl = track.coverUrl,
                                    seedNumber = track.number,
                                    size = 112.dp,
                                    cornerRadius = 10.dp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = track.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = track.genre,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AppColors.TextMuted,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Made For You ("Dibuat Untuk Kamu")
        item {
            Text(
                text = "Dibuat Untuk Kamu",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(madeForYou, key = { it.id }) { mix ->
                    val leadTrack = PlaylistLogic.resolveTracks(mix, allTracks).firstOrNull()
                    PlaylistFeaturedCard(
                        playlist = mix,
                        leadTrack = leadTrack,
                        onClick = { onSelectPlaylist(mix) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Genres Row ("Jelajahi Genre")
        item {
            Text(
                text = "Jelajahi Genre",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(genres, key = { it }) { genre ->
                    GenreTileCard(
                        genre = genre,
                        onClick = { onSelectGenre(genre) },
                        modifier = Modifier.width(136.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Built-in Playlists ("Playlist Pilihan")
        item {
            Text(
                text = "Playlist Pilihan",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(builtInPlaylists, key = { it.id }) { playlist ->
                    val leadTrack = PlaylistLogic.resolveTracks(playlist, allTracks).firstOrNull()
                    PlaylistFeaturedCard(
                        playlist = playlist,
                        leadTrack = leadTrack,
                        onClick = { onSelectPlaylist(playlist) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Albums ("Album Pilihan")
        item {
            Text(
                text = "Album Pilihan",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(albums, key = { it.id }) { album ->
                    AlbumFeaturedCard(
                        album = album,
                        onClick = { onSelectAlbum(album) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Full Track List ("Daftar Lagu")
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar Lagu",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "${allTracks.size} lagu",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.TextMuted
                )
            }
        }

        items(allTracks, key = { it.id }) { track ->
            val isCurrent = playerState.currentTrack?.id == track.id
            TrackRow(
                track = track,
                isCurrentTrack = isCurrent,
                isPlaying = isCurrent && playerState.isPlaying,
                isFavorite = track.id in favoriteIds,
                onPlay = { onPlayTrack(track, allTracks, "Daftar Lagu") },
                onToggleFavorite = { onToggleFavorite(track.id) },
                onAddToPlaylist = { onRequestAddToPlaylist(track) },
                onPlayNext = { onPlayNext(track) },
                onAddToQueue = { onAddToQueue(track) },
                onStartRadio = { onStartRadio(track) },
                onOpenAlbum = {
                    albums.firstOrNull { it.id == track.albumId }?.let(onSelectAlbum)
                },
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
