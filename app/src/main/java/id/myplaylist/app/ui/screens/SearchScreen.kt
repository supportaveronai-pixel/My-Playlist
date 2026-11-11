package id.myplaylist.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.myplaylist.app.data.TrackCatalogue
import id.myplaylist.app.domain.MadeForYouGenerator
import id.myplaylist.app.domain.SearchEngine
import id.myplaylist.app.model.Album
import id.myplaylist.app.model.Artist
import id.myplaylist.app.model.PlayerState
import id.myplaylist.app.model.Playlist
import id.myplaylist.app.model.SearchTab
import id.myplaylist.app.model.Track
import id.myplaylist.app.ui.components.CoverArt
import id.myplaylist.app.ui.components.GenreTileCard
import id.myplaylist.app.ui.components.PlaylistGradientCover
import id.myplaylist.app.ui.components.TrackRow
import id.myplaylist.app.ui.components.shareTextContent
import id.myplaylist.app.ui.theme.AppColors

@Composable
fun SearchScreen(
    playerState: PlayerState,
    favoriteIds: Set<String>,
    customPlaylists: List<Playlist>,
    recentSearches: List<String>,
    playCounts: Map<String, Int>,
    onAddRecentSearch: (String) -> Unit,
    onClearRecentSearches: () -> Unit,
    onPlayTrack: (Track, List<Track>, String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenAlbum: (Album) -> Unit,
    onOpenArtist: (Artist) -> Unit,
    onOpenPlaylist: (Playlist) -> Unit,
    onOpenGenre: (String) -> Unit,
    onRequestAddToPlaylist: (Track) -> Unit,
    onPlayNext: (Track) -> Unit,
    onAddToQueue: (Track) -> Unit,
    onStartRadio: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(SearchTab.ALL) }

    val allTracks = TrackCatalogue.tracks
    val allAlbums = TrackCatalogue.albums
    val allArtists = TrackCatalogue.artists
    val allGenres = TrackCatalogue.genres
    val allPlaylists = remember(customPlaylists, playCounts) {
        customPlaylists + TrackCatalogue.builtInPlaylists + MadeForYouGenerator.generateMadeForYou(allTracks, playCounts)
    }

    val result = remember(query, allPlaylists) {
        SearchEngine.search(
            query = query,
            allTracks = allTracks,
            allAlbums = allAlbums,
            allArtists = allArtists,
            allPlaylists = allPlaylists,
            allGenres = allGenres
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        Text(
            text = "Cari",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Cari lagu, album, artis, atau genre...") },
            leadingIcon = {
                Icon(Icons.Rounded.Search, contentDescription = null, tint = AppColors.TextMuted)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Rounded.Clear, contentDescription = "Hapus pencarian", tint = AppColors.TextMuted)
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                if (query.isNotBlank()) onAddRecentSearch(query)
            }),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.AccentViolet,
                unfocusedBorderColor = AppColors.BorderLight,
                focusedContainerColor = AppColors.BgWhite,
                unfocusedContainerColor = AppColors.SurfaceLight
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (query.isBlank()) {
            // Empty query -> Recent searches + Browse-All Genre Grid
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                if (recentSearches.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pencarian Terakhir",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = onClearRecentSearches) {
                                Text("Hapus riwayat pencarian", color = AppColors.AccentViolet)
                            }
                        }
                    }

                    items(recentSearches, key = { "rs_$it" }) { itemQuery ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { query = itemQuery }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.History,
                                contentDescription = null,
                                tint = AppColors.TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = itemQuery,
                                style = MaterialTheme.typography.bodyLarge,
                                color = AppColors.TextPrimary
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Jelajahi Semua Genre",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                val rows = allGenres.chunked(2)
                items(rows) { pair ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { genre ->
                            GenreTileCard(
                                genre = genre,
                                onClick = { onOpenGenre(genre) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        } else {
            // Filter Tabs
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SearchTab.entries) { tab ->
                    val selected = selectedTab == tab
                    FilterChip(
                        selected = selected,
                        onClick = { selectedTab = tab },
                        label = { Text(tab.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.AccentViolet,
                            selectedLabelColor = AppColors.OnAccent,
                            containerColor = AppColors.SurfaceLight,
                            labelColor = AppColors.TextPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val hasResultsForTab = when (selectedTab) {
                SearchTab.ALL -> !result.isEmpty
                SearchTab.TRACKS -> result.tracks.isNotEmpty()
                SearchTab.ALBUMS -> result.albums.isNotEmpty()
                SearchTab.ARTISTS -> result.artists.isNotEmpty()
                SearchTab.PLAYLISTS -> result.playlists.isNotEmpty()
                SearchTab.GENRES -> result.genres.isNotEmpty()
            }

            if (!hasResultsForTab) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.SearchOff,
                            contentDescription = null,
                            tint = AppColors.TextMuted,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tidak ada hasil untuk \"$query\"",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Coba kata kunci lain atau ubah tab kategori di atas.",
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
                    if ((selectedTab == SearchTab.ALL || selectedTab == SearchTab.TRACKS) && result.tracks.isNotEmpty()) {
                        item {
                            Text(
                                text = "Lagu",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                        items(result.tracks, key = { "st_${it.id}" }) { track ->
                            val isCurrent = playerState.currentTrack?.id == track.id
                            TrackRow(
                                track = track,
                                isCurrentTrack = isCurrent,
                                isPlaying = isCurrent && playerState.isPlaying,
                                isFavorite = track.id in favoriteIds,
                                onPlay = {
                                    onAddRecentSearch(query)
                                    onPlayTrack(track, result.tracks, "Hasil Pencarian")
                                },
                                onToggleFavorite = { onToggleFavorite(track.id) },
                                onAddToPlaylist = { onRequestAddToPlaylist(track) },
                                onPlayNext = { onPlayNext(track) },
                                onAddToQueue = { onAddToQueue(track) },
                                onStartRadio = { onStartRadio(track) },
                                onOpenAlbum = {
                                    allAlbums.firstOrNull { it.id == track.albumId }?.let(onOpenAlbum)
                                },
                                onOpenArtist = { allArtists.firstOrNull()?.let(onOpenArtist) },
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

                    if ((selectedTab == SearchTab.ALL || selectedTab == SearchTab.ALBUMS) && result.albums.isNotEmpty()) {
                        item {
                            Text(
                                text = "Album",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(result.albums, key = { "sa_${it.id}" }) { album ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .clickable {
                                        onAddRecentSearch(query)
                                        onOpenAlbum(album)
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = AppColors.BgWhite),
                                border = BorderStroke(1.dp, AppColors.BorderLight)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CoverArt(album.coverUrl, album.id.hashCode(), 48.dp, 10.dp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(album.title, style = MaterialTheme.typography.titleMedium)
                                        Text("${album.artist} • ${album.year}", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextMuted)
                                    }
                                }
                            }
                        }
                    }

                    if ((selectedTab == SearchTab.ALL || selectedTab == SearchTab.ARTISTS) && result.artists.isNotEmpty()) {
                        item {
                            Text(
                                text = "Artis",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(result.artists, key = { "sar_${it.id}" }) { artist ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .clickable {
                                        onAddRecentSearch(query)
                                        onOpenArtist(artist)
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = AppColors.BgWhite),
                                border = BorderStroke(1.dp, AppColors.BorderLight)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CoverArt(artist.coverUrl, 99, 48.dp, 24.dp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(artist.name, style = MaterialTheme.typography.titleMedium)
                                        Text("Artis", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextMuted)
                                    }
                                }
                            }
                        }
                    }

                    if ((selectedTab == SearchTab.ALL || selectedTab == SearchTab.PLAYLISTS) && result.playlists.isNotEmpty()) {
                        item {
                            Text(
                                text = "Playlist",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(result.playlists, key = { "sp_${it.id}" }) { playlist ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .clickable {
                                        onAddRecentSearch(query)
                                        onOpenPlaylist(playlist)
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = AppColors.BgWhite),
                                border = BorderStroke(1.dp, AppColors.BorderLight)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PlaylistGradientCover(playlist.colorIndex, 48.dp, 10.dp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(playlist.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("${playlist.trackIds.size} lagu", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextMuted)
                                    }
                                }
                            }
                        }
                    }

                    if ((selectedTab == SearchTab.ALL || selectedTab == SearchTab.GENRES) && result.genres.isNotEmpty()) {
                        item {
                            Text(
                                text = "Genre",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(result.genres, key = { "sg_$it" }) { genre ->
                            GenreTileCard(
                                genre = genre,
                                onClick = {
                                    onAddRecentSearch(query)
                                    onOpenGenre(genre)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
