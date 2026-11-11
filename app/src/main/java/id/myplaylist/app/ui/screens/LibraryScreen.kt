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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.myplaylist.app.data.TrackCatalogue
import id.myplaylist.app.domain.LibrarySortLogic
import id.myplaylist.app.domain.PlaylistLogic
import id.myplaylist.app.model.Album
import id.myplaylist.app.model.Artist
import id.myplaylist.app.model.LibraryFilter
import id.myplaylist.app.model.LibrarySort
import id.myplaylist.app.model.PlayerState
import id.myplaylist.app.model.Playlist
import id.myplaylist.app.model.Track
import id.myplaylist.app.ui.components.ColorPickerDialog
import id.myplaylist.app.ui.components.CoverArt
import id.myplaylist.app.ui.components.CreatePlaylistDialog
import id.myplaylist.app.ui.components.PlaylistGradientCover
import id.myplaylist.app.ui.components.RenamePlaylistDialog
import id.myplaylist.app.ui.components.TrackRow
import id.myplaylist.app.ui.components.shareTextContent
import id.myplaylist.app.ui.theme.AppColors

@Composable
fun LibraryScreen(
    customPlaylists: List<Playlist>,
    favoriteIds: Set<String>,
    favoriteAlbumIds: Set<String>,
    followedArtistIds: Set<String>,
    playCounts: Map<String, Int>,
    playerState: PlayerState,
    selectedPlaylist: Playlist?,
    onSelectPlaylist: (Playlist?) -> Unit,
    onCreatePlaylist: (name: String, colorIndex: Int) -> Unit,
    onRenamePlaylist: (playlistId: String, newName: String) -> Unit,
    onDeletePlaylist: (playlistId: String) -> Unit,
    onTogglePinPlaylist: (playlistId: String) -> Unit,
    onSetPlaylistColor: (playlistId: String, colorIndex: Int) -> Unit,
    onMoveTrackUp: (playlistId: String, trackId: String) -> Unit,
    onMoveTrackDown: (playlistId: String, trackId: String) -> Unit,
    onRemoveFromPlaylist: (playlistId: String, trackId: String) -> Unit,
    onPlayTrack: (Track, List<Track>, String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRequestAddToPlaylist: (Track) -> Unit,
    onPlayNext: (Track) -> Unit,
    onAddToQueue: (Track) -> Unit,
    onStartRadio: (Track) -> Unit,
    onOpenAlbum: (Album) -> Unit,
    onOpenArtist: (Artist) -> Unit,
    modifier: Modifier = Modifier
) {
    val allTracks = TrackCatalogue.tracks
    val builtInPlaylists = TrackCatalogue.builtInPlaylists

    var activeFilter by remember { mutableStateOf(LibraryFilter.ALL) }
    var activeSort by remember { mutableStateOf(LibrarySort.RECENT) }
    var sortMenuOpen by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onCreate = { name, colorIndex ->
                onCreatePlaylist(name, colorIndex)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    if (selectedPlaylist != null) {
        PlaylistDetailView(
            playlist = selectedPlaylist,
            allTracks = allTracks,
            playerState = playerState,
            favoriteIds = favoriteIds,
            onBack = { onSelectPlaylist(null) },
            onPlayTrack = onPlayTrack,
            onToggleFavorite = onToggleFavorite,
            onRenamePlaylist = { newName -> onRenamePlaylist(selectedPlaylist.id, newName) },
            onDeletePlaylist = {
                onDeletePlaylist(selectedPlaylist.id)
                onSelectPlaylist(null)
            },
            onTogglePin = { onTogglePinPlaylist(selectedPlaylist.id) },
            onSetColor = { colorIdx -> onSetPlaylistColor(selectedPlaylist.id, colorIdx) },
            onMoveTrackUp = { trackId -> onMoveTrackUp(selectedPlaylist.id, trackId) },
            onMoveTrackDown = { trackId -> onMoveTrackDown(selectedPlaylist.id, trackId) },
            onRemoveTrack = { trackId -> onRemoveFromPlaylist(selectedPlaylist.id, trackId) },
            onRequestAddToPlaylist = onRequestAddToPlaylist,
            onPlayNext = onPlayNext,
            onAddToQueue = onAddToQueue,
            onStartRadio = onStartRadio,
            onOpenAlbum = { albumId ->
                TrackCatalogue.albums.firstOrNull { it.id == albumId }?.let(onOpenAlbum)
            },
            onOpenArtist = {
                TrackCatalogue.artists.firstOrNull()?.let(onOpenArtist)
            },
            modifier = modifier
        )
        return
    }

    val favoritePlaylist = remember(favoriteIds) {
        Playlist(
            id = "virtual_favorites",
            name = "Lagu Favorit",
            description = "Kumpulan lagu yang telah kamu sukai",
            isBuiltIn = true,
            trackIds = allTracks.filter { it.id in favoriteIds }.map { it.id }
        )
    }

    val sortedCustomPlaylists = remember(customPlaylists, activeSort, playCounts) {
        LibrarySortLogic.sortPlaylists(customPlaylists, activeSort, playCounts)
    }

    val sortedBuiltInPlaylists = remember(builtInPlaylists, activeSort, playCounts) {
        LibrarySortLogic.sortPlaylists(builtInPlaylists, activeSort, playCounts)
    }

    val favoriteAlbums = remember(favoriteAlbumIds) {
        TrackCatalogue.albums.filter { it.id in favoriteAlbumIds }
    }

    val followedArtists = remember(followedArtistIds) {
        TrackCatalogue.artists.filter { it.id in followedArtistIds }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 14.dp, bottom = 96.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Koleksi Saya",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        IconButton(onClick = { sortMenuOpen = true }) {
                            Icon(Icons.Rounded.Sort, contentDescription = "Urutkan", tint = AppColors.TextPrimary)
                        }
                        DropdownMenu(
                            expanded = sortMenuOpen,
                            onDismissRequest = { sortMenuOpen = false },
                            containerColor = AppColors.BgWhite
                        ) {
                            LibrarySort.entries.forEach { sort ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = sort.label,
                                            color = if (activeSort == sort) AppColors.AccentViolet else AppColors.TextPrimary,
                                            fontWeight = if (activeSort == sort) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        activeSort = sort
                                        sortMenuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { showCreateDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, AppColors.AccentViolet),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, tint = AppColors.AccentViolet, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Buat Playlist", style = MaterialTheme.typography.labelMedium, color = AppColors.AccentViolet)
                    }
                }
            }
        }

        // Filter Chips
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(LibraryFilter.entries) { filter ->
                    val selected = activeFilter == filter
                    FilterChip(
                        selected = selected,
                        onClick = { activeFilter = filter },
                        label = { Text(filter.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.AccentViolet,
                            selectedLabelColor = AppColors.OnAccent,
                            containerColor = AppColors.SurfaceLight,
                            labelColor = AppColors.TextPrimary
                        )
                    )
                }
            }
        }

        // Favorites card
        if (activeFilter == LibraryFilter.ALL || activeFilter == LibraryFilter.FAVORITE) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { onSelectPlaylist(favoritePlaylist) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceAlt),
                    border = BorderStroke(1.dp, AppColors.AccentViolet.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Favorite, null, tint = AppColors.AccentViolet, modifier = Modifier.size(34.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Lagu Favorit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("${favoritePlaylist.trackIds.size} lagu tersimpan", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextMuted)
                        }
                    }
                }
            }
        }

        // Playlists Section
        if (activeFilter == LibraryFilter.ALL || activeFilter == LibraryFilter.PLAYLIST) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Playlist Kamu (${sortedCustomPlaylists.size})",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            if (sortedCustomPlaylists.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceLight),
                        border = BorderStroke(1.dp, AppColors.BorderLight)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Belum ada playlist buatanmu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ketuk 'Buat Playlist' untuk membuat koleksi lagumu sendiri dengan sampul warna pilihan.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(sortedCustomPlaylists, key = { it.id }) { playlist ->
                    CustomPlaylistRow(
                        playlist = playlist,
                        onClick = { onSelectPlaylist(playlist) },
                        onTogglePin = { onTogglePinPlaylist(playlist.id) },
                        onRename = { newName -> onRenamePlaylist(playlist.id, newName) },
                        onChangeColor = { colorIdx -> onSetPlaylistColor(playlist.id, colorIdx) },
                        onDelete = { onDeletePlaylist(playlist.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Playlist Bawaan",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            items(sortedBuiltInPlaylists, key = { it.id }) { playlist ->
                BuiltInPlaylistRow(
                    playlist = playlist,
                    onClick = { onSelectPlaylist(playlist) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        // Favorite Albums Section
        if (activeFilter == LibraryFilter.ALL || activeFilter == LibraryFilter.ALBUM || activeFilter == LibraryFilter.FAVORITE) {
            val albumsToShow = if (activeFilter == LibraryFilter.ALBUM) TrackCatalogue.albums else favoriteAlbums
            if (albumsToShow.isNotEmpty() || activeFilter == LibraryFilter.ALBUM) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (activeFilter == LibraryFilter.ALBUM) "Semua Album" else "Album Favorit",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
                items(albumsToShow, key = { "lib_alb_${it.id}" }) { album ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { onOpenAlbum(album) },
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(album.title, style = MaterialTheme.typography.titleMedium)
                                Text("${album.artist} • ${album.year}", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextMuted)
                            }
                        }
                    }
                }
            }
        }

        // Followed Artists Section
        if (activeFilter == LibraryFilter.ALL || activeFilter == LibraryFilter.ARTIST) {
            val artistsToShow = if (activeFilter == LibraryFilter.ARTIST) TrackCatalogue.artists else followedArtists
            if (artistsToShow.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (activeFilter == LibraryFilter.ARTIST) "Artis" else "Artis yang Diikuti",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
                items(artistsToShow, key = { "lib_art_${it.id}" }) { artist ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { onOpenArtist(artist) },
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(artist.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = if (artist.id in followedArtistIds) "Mengikuti" else "Artis Demo",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (artist.id in followedArtistIds) AppColors.AccentViolet else AppColors.TextMuted
                                )
                            }
                        }
                    }
                }
            } else if (activeFilter == LibraryFilter.ARTIST) {
                item {
                    Text(
                        text = "Belum ada artis yang diikuti.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextMuted,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomPlaylistRow(
    playlist: Playlist,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onRename: (String) -> Unit,
    onChangeColor: (Int) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuOpen by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }

    if (showRenameDialog) {
        RenamePlaylistDialog(
            currentName = playlist.name,
            onRename = {
                onRename(it)
                showRenameDialog = false
            },
            onDismiss = { showRenameDialog = false }
        )
    }

    if (showColorDialog) {
        ColorPickerDialog(
            currentColorIndex = playlist.colorIndex,
            onSelectColor = onChangeColor,
            onDismiss = { showColorDialog = false }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.BgWhite),
        border = BorderStroke(
            1.dp,
            if (playlist.isPinned) AppColors.AccentViolet.copy(alpha = 0.45f) else AppColors.BorderLight
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlaylistGradientCover(colorIndex = playlist.colorIndex, size = 48.dp, cornerRadius = 10.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (playlist.isPinned) {
                        Icon(
                            imageVector = Icons.Rounded.PushPin,
                            contentDescription = "Disematkan",
                            tint = AppColors.AccentViolet,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${playlist.trackIds.size} lagu • Playlist Kamu",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextMuted
                )
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "Opsi playlist", tint = AppColors.TextMuted)
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                    containerColor = AppColors.BgWhite
                ) {
                    DropdownMenuItem(
                        text = { Text(if (playlist.isPinned) "Lepas Sematan" else "Sematkan di Atas") },
                        leadingIcon = { Icon(Icons.Rounded.PushPin, null, tint = AppColors.AccentViolet) },
                        onClick = { menuOpen = false; onTogglePin() }
                    )
                    DropdownMenuItem(
                        text = { Text("Ubah Nama") },
                        leadingIcon = { Icon(Icons.Rounded.Edit, null, tint = AppColors.TextPrimary) },
                        onClick = { menuOpen = false; showRenameDialog = true }
                    )
                    DropdownMenuItem(
                        text = { Text("Ubah Warna Sampul") },
                        leadingIcon = { Icon(Icons.Rounded.Palette, null, tint = AppColors.AccentViolet) },
                        onClick = { menuOpen = false; showColorDialog = true }
                    )
                    DropdownMenuItem(
                        text = { Text("Hapus Playlist", color = AppColors.DangerRed) },
                        leadingIcon = { Icon(Icons.Rounded.DeleteOutline, null, tint = AppColors.DangerRed) },
                        onClick = { menuOpen = false; onDelete() }
                    )
                }
            }
        }
    }
}

@Composable
private fun BuiltInPlaylistRow(
    playlist: Playlist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.BgWhite),
        border = BorderStroke(1.dp, AppColors.BorderLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlaylistGradientCover(colorIndex = playlist.colorIndex, size = 48.dp, cornerRadius = 10.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${playlist.trackIds.size} lagu • ${playlist.description}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PlaylistDetailView(
    playlist: Playlist,
    allTracks: List<Track>,
    playerState: PlayerState,
    favoriteIds: Set<String>,
    onBack: () -> Unit,
    onPlayTrack: (Track, List<Track>, String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRenamePlaylist: (String) -> Unit,
    onDeletePlaylist: () -> Unit,
    onTogglePin: () -> Unit,
    onSetColor: (Int) -> Unit,
    onMoveTrackUp: (String) -> Unit,
    onMoveTrackDown: (String) -> Unit,
    onRemoveTrack: (String) -> Unit,
    onRequestAddToPlaylist: (Track) -> Unit,
    onPlayNext: (Track) -> Unit,
    onAddToQueue: (Track) -> Unit,
    onStartRadio: (Track) -> Unit,
    onOpenAlbum: (String) -> Unit,
    onOpenArtist: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val playlistTracks = remember(playlist.trackIds, allTracks) {
        PlaylistLogic.resolveTracks(playlist, allTracks)
    }
    val isEditableUserPlaylist = !playlist.isBuiltIn && playlist.id != "virtual_favorites"

    var showRenameDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }

    if (showRenameDialog) {
        RenamePlaylistDialog(
            currentName = playlist.name,
            onRename = { onRenamePlaylist(it); showRenameDialog = false },
            onDismiss = { showRenameDialog = false }
        )
    }

    if (showColorDialog) {
        ColorPickerDialog(
            currentColorIndex = playlist.colorIndex,
            onSelectColor = onSetColor,
            onDismiss = { showColorDialog = false }
        )
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
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali")
                    }
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        shareTextContent(
                            context,
                            playlist.name,
                            "Dengarkan playlist ${playlist.name} (${playlistTracks.size} lagu) di My Playlist!"
                        )
                    }) {
                        Icon(Icons.Rounded.Share, contentDescription = "Bagikan playlist")
                    }
                    if (isEditableUserPlaylist) {
                        Box {
                            IconButton(onClick = { menuOpen = true }) {
                                Icon(Icons.Rounded.MoreVert, contentDescription = "Kelola playlist")
                            }
                            DropdownMenu(
                                expanded = menuOpen,
                                onDismissRequest = { menuOpen = false },
                                containerColor = AppColors.BgWhite
                            ) {
                                DropdownMenuItem(
                                    text = { Text(if (playlist.isPinned) "Lepas Sematan" else "Sematkan di Atas") },
                                    onClick = { menuOpen = false; onTogglePin() }
                                )
                                DropdownMenuItem(
                                    text = { Text("Ubah Nama") },
                                    onClick = { menuOpen = false; showRenameDialog = true }
                                )
                                DropdownMenuItem(
                                    text = { Text("Ubah Warna Sampul") },
                                    onClick = { menuOpen = false; showColorDialog = true }
                                )
                                DropdownMenuItem(
                                    text = { Text("Hapus Playlist", color = AppColors.DangerRed) },
                                    onClick = { menuOpen = false; onDeletePlaylist() }
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = playlist.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextMuted
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${playlistTracks.size} lagu",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.TextMuted
                    )
                    if (playlistTracks.isNotEmpty()) {
                        Button(
                            onClick = { onPlayTrack(playlistTracks.first(), playlistTracks, playlist.name) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.AccentViolet,
                                contentColor = AppColors.OnAccent
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.PlayArrow, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Putar Semua")
                        }
                    }
                }
            }
        }

        if (playlistTracks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceLight),
                    border = BorderStroke(1.dp, AppColors.BorderLight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (playlist.id == "virtual_favorites") "Belum ada lagu favorit" else "Playlist ini masih kosong",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (playlist.id == "virtual_favorites")
                                "Ketuk ikon hati pada lagu manapun untuk menyimpannya di sini."
                            else
                                "Tambahkan lagu melalui menu titik tiga pada daftar lagu.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(playlistTracks, key = { it.id }) { track ->
                val isCurrent = playerState.currentTrack?.id == track.id
                val idx = playlistTracks.indexOf(track)
                TrackRow(
                    track = track,
                    isCurrentTrack = isCurrent,
                    isPlaying = isCurrent && playerState.isPlaying,
                    isFavorite = track.id in favoriteIds,
                    onPlay = { onPlayTrack(track, playlistTracks, playlist.name) },
                    onToggleFavorite = { onToggleFavorite(track.id) },
                    onAddToPlaylist = { onRequestAddToPlaylist(track) },
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
                    onMoveUp = if (isEditableUserPlaylist && idx > 0) {
                        { onMoveTrackUp(track.id) }
                    } else null,
                    onMoveDown = if (isEditableUserPlaylist && idx < playlistTracks.lastIndex) {
                        { onMoveTrackDown(track.id) }
                    } else null,
                    onRemoveFromPlaylist = if (isEditableUserPlaylist) {
                        { onRemoveTrack(track.id) }
                    } else null,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}
