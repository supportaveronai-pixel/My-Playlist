package id.myplaylist.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.myplaylist.app.data.TrackCatalogue
import id.myplaylist.app.model.Album
import id.myplaylist.app.model.Artist
import id.myplaylist.app.model.Playlist
import id.myplaylist.app.model.Track
import id.myplaylist.app.ui.components.AddToPlaylistDialog
import id.myplaylist.app.ui.components.CreatePlaylistDialog
import id.myplaylist.app.ui.components.MiniPlayer
import id.myplaylist.app.ui.components.SplashOverlay
import id.myplaylist.app.ui.screens.AlbumDetailScreen
import id.myplaylist.app.ui.screens.ArtistDetailScreen
import id.myplaylist.app.ui.screens.GenreDetailScreen
import id.myplaylist.app.ui.screens.HistoryScreen
import id.myplaylist.app.ui.screens.HomeScreen
import id.myplaylist.app.ui.screens.LibraryScreen
import id.myplaylist.app.ui.screens.LyricsModalSheet
import id.myplaylist.app.ui.screens.NowPlayingScreen
import id.myplaylist.app.ui.screens.ProfileScreen
import id.myplaylist.app.ui.screens.QueueScreen
import id.myplaylist.app.ui.screens.SearchScreen
import id.myplaylist.app.ui.theme.AppColors
import id.myplaylist.app.ui.theme.MyPlaylistTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class MainTab(val label: String, val icon: ImageVector) {
    HOME("Beranda", Icons.Rounded.Home),
    SEARCH("Cari", Icons.Rounded.Search),
    LIBRARY("Koleksi", Icons.Rounded.LibraryMusic),
    PROFILE("Profil", Icons.Rounded.Person)
}

sealed interface DetailRoute {
    data class PlaylistDetail(val playlist: Playlist) : DetailRoute
    data class AlbumDetail(val album: Album) : DetailRoute
    data class ArtistDetail(val artist: Artist) : DetailRoute
    data class GenreDetail(val genre: String) : DetailRoute
    data object History : DetailRoute
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.White.toArgb(),
                Color.White.toArgb()
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.White.toArgb(),
                Color.White.toArgb()
            )
        )

        val app = application as MyPlaylistApp

        setContent {
            MyPlaylistTheme {
                MyPlaylistRootScreen(app = app)
            }
        }
    }
}

@Composable
fun MyPlaylistRootScreen(app: MyPlaylistApp) {
    val settingsStore = app.settingsStore
    val playerRepository = app.playerRepository

    val profileName by settingsStore.profileName.collectAsStateWithLifecycle()
    val favoriteIds by settingsStore.favoriteIds.collectAsStateWithLifecycle()
    val favoriteAlbumIds by settingsStore.favoriteAlbumIds.collectAsStateWithLifecycle()
    val followedArtistIds by settingsStore.followedArtistIds.collectAsStateWithLifecycle()
    val customPlaylists by settingsStore.customPlaylists.collectAsStateWithLifecycle()
    val recentSearches by settingsStore.recentSearches.collectAsStateWithLifecycle()
    val recentlyPlayedIds by settingsStore.recentlyPlayedIds.collectAsStateWithLifecycle()
    val playCounts by settingsStore.playCounts.collectAsStateWithLifecycle()
    val totalPlays by settingsStore.totalPlays.collectAsStateWithLifecycle()
    val totalListenedMs by settingsStore.totalListenedMs.collectAsStateWithLifecycle()
    val crossfadeSeconds by settingsStore.crossfadeSeconds.collectAsStateWithLifecycle()
    val playerState by playerRepository.state.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    var detailRoute by remember { mutableStateOf<DetailRoute?>(null) }
    var showNowPlaying by remember { mutableStateOf(false) }
    var showQueue by remember { mutableStateOf(false) }
    var showLyricsSheet by remember { mutableStateOf(false) }
    var showSplash by remember { mutableStateOf(true) }

    var pendingTrackForPlaylist by remember { mutableStateOf<Track?>(null) }
    var showCreatePlaylistFromPicker by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(650L)
        showSplash = false
    }

    // Hardware/gesture back handling
    if (showLyricsSheet) {
        BackHandler { showLyricsSheet = false }
    } else if (showQueue) {
        BackHandler { showQueue = false }
    } else if (showNowPlaying) {
        BackHandler { showNowPlaying = false }
    } else if (detailRoute != null) {
        BackHandler { detailRoute = null }
    }

    if (pendingTrackForPlaylist != null && !showCreatePlaylistFromPicker) {
        val track = pendingTrackForPlaylist!!
        AddToPlaylistDialog(
            track = track,
            userPlaylists = customPlaylists,
            onSelectPlaylist = { playlist ->
                settingsStore.addTrackToPlaylist(playlist.id, track.id)
                pendingTrackForPlaylist = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Ditambahkan ke \"${playlist.name}\"")
                }
            },
            onCreateNewPlaylist = {
                showCreatePlaylistFromPicker = true
            },
            onDismiss = { pendingTrackForPlaylist = null }
        )
    }

    if (showCreatePlaylistFromPicker) {
        CreatePlaylistDialog(
            onCreate = { name, colorIndex ->
                val created = settingsStore.createCustomPlaylist(name, colorIndex)
                val targetTrack = pendingTrackForPlaylist
                if (created != null && targetTrack != null) {
                    settingsStore.addTrackToPlaylist(created.id, targetTrack.id)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Playlist \"${created.name}\" dibuat & lagu ditambahkan")
                    }
                }
                showCreatePlaylistFromPicker = false
                pendingTrackForPlaylist = null
            },
            onDismiss = { showCreatePlaylistFromPicker = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = AppColors.BgWhite,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (playerState.currentTrack != null) {
                        val currentId = playerState.currentTrack!!.id
                        MiniPlayer(
                            playerState = playerState,
                            isFavorite = currentId in favoriteIds,
                            onTogglePlayPause = { playerRepository.togglePlayPause() },
                            onToggleFavorite = { settingsStore.toggleFavorite(currentId) },
                            onOpenNowPlaying = { showNowPlaying = true }
                        )
                    }
                    HorizontalDivider(color = AppColors.BorderLight, thickness = 1.dp)
                    NavigationBar(
                        containerColor = AppColors.BgWhite,
                        tonalElevation = 0.dp
                    ) {
                        MainTab.entries.forEach { tab ->
                            val selected = currentTab == tab && detailRoute == null
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    detailRoute = null
                                    currentTab = tab
                                },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = AppColors.AccentViolet,
                                    selectedTextColor = AppColors.AccentViolet,
                                    indicatorColor = AppColors.SurfaceAlt,
                                    unselectedIconColor = AppColors.TextMuted,
                                    unselectedTextColor = AppColors.TextMuted
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val currentDetail = detailRoute
                if (currentDetail != null) {
                    when (currentDetail) {
                        is DetailRoute.PlaylistDetail -> {
                            val livePlaylist = customPlaylists.firstOrNull { it.id == currentDetail.playlist.id }
                                ?: currentDetail.playlist
                            LibraryScreen(
                                customPlaylists = customPlaylists,
                                favoriteIds = favoriteIds,
                                favoriteAlbumIds = favoriteAlbumIds,
                                followedArtistIds = followedArtistIds,
                                playCounts = playCounts,
                                playerState = playerState,
                                selectedPlaylist = livePlaylist,
                                onSelectPlaylist = { detailRoute = null },
                                onCreatePlaylist = { name, color -> settingsStore.createCustomPlaylist(name, color) },
                                onRenamePlaylist = { id, name -> settingsStore.renameCustomPlaylist(id, name) },
                                onDeletePlaylist = { id ->
                                    settingsStore.deleteCustomPlaylist(id)
                                    detailRoute = null
                                },
                                onTogglePinPlaylist = { id -> settingsStore.togglePinPlaylist(id) },
                                onSetPlaylistColor = { id, col -> settingsStore.setPlaylistColor(id, col) },
                                onMoveTrackUp = { id, trk -> settingsStore.moveTrackUpInPlaylist(id, trk) },
                                onMoveTrackDown = { id, trk -> settingsStore.moveTrackDownInPlaylist(id, trk) },
                                onRemoveFromPlaylist = { id, trk -> settingsStore.removeTrackFromPlaylist(id, trk) },
                                onPlayTrack = { track, queue, label ->
                                    playerRepository.playTrack(track, queue, label)
                                },
                                onToggleFavorite = { settingsStore.toggleFavorite(it) },
                                onRequestAddToPlaylist = { pendingTrackForPlaylist = it },
                                onPlayNext = {
                                    playerRepository.playNext(it)
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Akan diputar berikutnya") }
                                },
                                onAddToQueue = {
                                    playerRepository.addToQueue(it)
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Ditambahkan ke antrean") }
                                },
                                onStartRadio = {
                                    playerRepository.playSongRadio(it, TrackCatalogue.tracks)
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Radio lagu dimulai") }
                                },
                                onOpenAlbum = { detailRoute = DetailRoute.AlbumDetail(it) },
                                onOpenArtist = { detailRoute = DetailRoute.ArtistDetail(it) }
                            )
                        }

                        is DetailRoute.AlbumDetail -> {
                            AlbumDetailScreen(
                                album = currentDetail.album,
                                playerState = playerState,
                                favoriteTrackIds = favoriteIds,
                                isAlbumFavorite = currentDetail.album.id in favoriteAlbumIds,
                                onBack = { detailRoute = null },
                                onToggleAlbumFavorite = { settingsStore.toggleFavoriteAlbum(currentDetail.album.id) },
                                onPlayTrack = { track, queue, label, shuffle ->
                                    playerRepository.playTrack(track, queue, label, shuffle)
                                },
                                onToggleTrackFavorite = { settingsStore.toggleFavorite(it) },
                                onAddToPlaylist = { pendingTrackForPlaylist = it },
                                onPlayNext = {
                                    playerRepository.playNext(it)
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Akan diputar berikutnya") }
                                },
                                onAddToQueue = {
                                    playerRepository.addToQueue(it)
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Ditambahkan ke antrean") }
                                },
                                onStartRadio = {
                                    playerRepository.playSongRadio(it, TrackCatalogue.tracks)
                                },
                                onOpenArtist = {
                                    TrackCatalogue.artists.firstOrNull()?.let {
                                        detailRoute = DetailRoute.ArtistDetail(it)
                                    }
                                }
                            )
                        }

                        is DetailRoute.ArtistDetail -> {
                            ArtistDetailScreen(
                                artist = currentDetail.artist,
                                isFollowing = currentDetail.artist.id in followedArtistIds,
                                playCounts = playCounts,
                                playerState = playerState,
                                favoriteTrackIds = favoriteIds,
                                onBack = { detailRoute = null },
                                onToggleFollow = { settingsStore.toggleFollowArtist(currentDetail.artist.id) },
                                onOpenAlbum = { detailRoute = DetailRoute.AlbumDetail(it) },
                                onPlayTrack = { track, queue, label ->
                                    playerRepository.playTrack(track, queue, label)
                                },
                                onToggleTrackFavorite = { settingsStore.toggleFavorite(it) },
                                onAddToPlaylist = { pendingTrackForPlaylist = it },
                                onPlayNext = {
                                    playerRepository.playNext(it)
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Akan diputar berikutnya") }
                                },
                                onAddToQueue = {
                                    playerRepository.addToQueue(it)
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Ditambahkan ke antrean") }
                                },
                                onStartRadio = {
                                    playerRepository.playSongRadio(it, TrackCatalogue.tracks)
                                }
                            )
                        }

                        is DetailRoute.GenreDetail -> {
                            GenreDetailScreen(
                                genre = currentDetail.genre,
                                playerState = playerState,
                                favoriteTrackIds = favoriteIds,
                                onBack = { detailRoute = null },
                                onPlayTrack = { track, queue, label ->
                                    playerRepository.playTrack(track, queue, label)
                                },
                                onToggleFavorite = { settingsStore.toggleFavorite(it) },
                                onAddToPlaylist = { pendingTrackForPlaylist = it },
                                onPlayNext = {
                                    playerRepository.playNext(it)
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Akan diputar berikutnya") }
                                },
                                onAddToQueue = {
                                    playerRepository.addToQueue(it)
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Ditambahkan ke antrean") }
                                },
                                onStartRadio = {
                                    playerRepository.playSongRadio(it, TrackCatalogue.tracks)
                                },
                                onOpenAlbum = { albumId ->
                                    TrackCatalogue.albums.firstOrNull { it.id == albumId }?.let {
                                        detailRoute = DetailRoute.AlbumDetail(it)
                                    }
                                },
                                onOpenArtist = {
                                    TrackCatalogue.artists.firstOrNull()?.let {
                                        detailRoute = DetailRoute.ArtistDetail(it)
                                    }
                                }
                            )
                        }

                        is DetailRoute.History -> {
                            HistoryScreen(
                                recentlyPlayedIds = recentlyPlayedIds,
                                playCounts = playCounts,
                                playerState = playerState,
                                favoriteIds = favoriteIds,
                                onBack = { detailRoute = null },
                                onClearHistory = { settingsStore.clearRecentlyPlayed() },
                                onPlayTrack = { track, queue, label ->
                                    playerRepository.playTrack(track, queue, label)
                                },
                                onToggleFavorite = { settingsStore.toggleFavorite(it) },
                                onAddToPlaylist = { pendingTrackForPlaylist = it },
                                onPlayNext = {
                                    playerRepository.playNext(it)
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Akan diputar berikutnya") }
                                },
                                onAddToQueue = {
                                    playerRepository.addToQueue(it)
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Ditambahkan ke antrean") }
                                },
                                onStartRadio = {
                                    playerRepository.playSongRadio(it, TrackCatalogue.tracks)
                                },
                                onOpenAlbum = { albumId ->
                                    TrackCatalogue.albums.firstOrNull { it.id == albumId }?.let {
                                        detailRoute = DetailRoute.AlbumDetail(it)
                                    }
                                },
                                onOpenArtist = {
                                    TrackCatalogue.artists.firstOrNull()?.let {
                                        detailRoute = DetailRoute.ArtistDetail(it)
                                    }
                                }
                            )
                        }
                    }
                } else {
                    when (currentTab) {
                        MainTab.HOME -> HomeScreen(
                            profileName = profileName,
                            playerState = playerState,
                            favoriteIds = favoriteIds,
                            recentlyPlayedIds = recentlyPlayedIds,
                            playCounts = playCounts,
                            onPlayTrack = { track, queue, label ->
                                playerRepository.playTrack(track, queue, label)
                            },
                            onToggleFavorite = { settingsStore.toggleFavorite(it) },
                            onSelectPlaylist = { detailRoute = DetailRoute.PlaylistDetail(it) },
                            onSelectAlbum = { detailRoute = DetailRoute.AlbumDetail(it) },
                            onSelectGenre = { detailRoute = DetailRoute.GenreDetail(it) },
                            onOpenHistory = { detailRoute = DetailRoute.History },
                            onOpenArtist = {
                                TrackCatalogue.artists.firstOrNull()?.let {
                                    detailRoute = DetailRoute.ArtistDetail(it)
                                }
                            },
                            onRequestAddToPlaylist = { pendingTrackForPlaylist = it },
                            onPlayNext = {
                                playerRepository.playNext(it)
                                coroutineScope.launch { snackbarHostState.showSnackbar("Akan diputar berikutnya") }
                            },
                            onAddToQueue = {
                                playerRepository.addToQueue(it)
                                coroutineScope.launch { snackbarHostState.showSnackbar("Ditambahkan ke antrean") }
                            },
                            onStartRadio = {
                                playerRepository.playSongRadio(it, TrackCatalogue.tracks)
                                coroutineScope.launch { snackbarHostState.showSnackbar("Radio lagu dimulai") }
                            }
                        )

                        MainTab.SEARCH -> SearchScreen(
                            playerState = playerState,
                            favoriteIds = favoriteIds,
                            customPlaylists = customPlaylists,
                            recentSearches = recentSearches,
                            playCounts = playCounts,
                            onAddRecentSearch = { settingsStore.addRecentSearch(it) },
                            onClearRecentSearches = { settingsStore.clearRecentSearches() },
                            onPlayTrack = { track, queue, label ->
                                playerRepository.playTrack(track, queue, label)
                            },
                            onToggleFavorite = { settingsStore.toggleFavorite(it) },
                            onOpenAlbum = { detailRoute = DetailRoute.AlbumDetail(it) },
                            onOpenArtist = { detailRoute = DetailRoute.ArtistDetail(it) },
                            onOpenPlaylist = { detailRoute = DetailRoute.PlaylistDetail(it) },
                            onOpenGenre = { detailRoute = DetailRoute.GenreDetail(it) },
                            onRequestAddToPlaylist = { pendingTrackForPlaylist = it },
                            onPlayNext = {
                                playerRepository.playNext(it)
                                coroutineScope.launch { snackbarHostState.showSnackbar("Akan diputar berikutnya") }
                            },
                            onAddToQueue = {
                                playerRepository.addToQueue(it)
                                coroutineScope.launch { snackbarHostState.showSnackbar("Ditambahkan ke antrean") }
                            },
                            onStartRadio = {
                                playerRepository.playSongRadio(it, TrackCatalogue.tracks)
                                coroutineScope.launch { snackbarHostState.showSnackbar("Radio lagu dimulai") }
                            }
                        )

                        MainTab.LIBRARY -> LibraryScreen(
                            customPlaylists = customPlaylists,
                            favoriteIds = favoriteIds,
                            favoriteAlbumIds = favoriteAlbumIds,
                            followedArtistIds = followedArtistIds,
                            playCounts = playCounts,
                            playerState = playerState,
                            selectedPlaylist = null,
                            onSelectPlaylist = { if (it != null) detailRoute = DetailRoute.PlaylistDetail(it) },
                            onCreatePlaylist = { name, color ->
                                val created = settingsStore.createCustomPlaylist(name, color)
                                if (created != null) {
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Playlist \"${created.name}\" dibuat") }
                                }
                            },
                            onRenamePlaylist = { id, name -> settingsStore.renameCustomPlaylist(id, name) },
                            onDeletePlaylist = { id -> settingsStore.deleteCustomPlaylist(id) },
                            onTogglePinPlaylist = { id -> settingsStore.togglePinPlaylist(id) },
                            onSetPlaylistColor = { id, col -> settingsStore.setPlaylistColor(id, col) },
                            onMoveTrackUp = { id, trk -> settingsStore.moveTrackUpInPlaylist(id, trk) },
                            onMoveTrackDown = { id, trk -> settingsStore.moveTrackDownInPlaylist(id, trk) },
                            onRemoveFromPlaylist = { id, trk -> settingsStore.removeTrackFromPlaylist(id, trk) },
                            onPlayTrack = { track, queue, label ->
                                playerRepository.playTrack(track, queue, label)
                            },
                            onToggleFavorite = { settingsStore.toggleFavorite(it) },
                            onRequestAddToPlaylist = { pendingTrackForPlaylist = it },
                            onPlayNext = {
                                playerRepository.playNext(it)
                                coroutineScope.launch { snackbarHostState.showSnackbar("Akan diputar berikutnya") }
                            },
                            onAddToQueue = {
                                playerRepository.addToQueue(it)
                                coroutineScope.launch { snackbarHostState.showSnackbar("Ditambahkan ke antrean") }
                            },
                            onStartRadio = {
                                playerRepository.playSongRadio(it, TrackCatalogue.tracks)
                            },
                            onOpenAlbum = { detailRoute = DetailRoute.AlbumDetail(it) },
                            onOpenArtist = { detailRoute = DetailRoute.ArtistDetail(it) }
                        )

                        MainTab.PROFILE -> ProfileScreen(
                            profileName = profileName,
                            favoriteIds = favoriteIds,
                            customPlaylists = customPlaylists,
                            recentlyPlayedIds = recentlyPlayedIds,
                            playCounts = playCounts,
                            totalPlays = totalPlays,
                            totalListenedMs = totalListenedMs,
                            crossfadeSeconds = crossfadeSeconds,
                            onSaveProfileName = { settingsStore.setProfileName(it) },
                            onSetCrossfadeSeconds = { settingsStore.setCrossfadeSeconds(it) },
                            onOpenHistory = { detailRoute = DetailRoute.History },
                            onPlayTrack = { track, queue, label ->
                                playerRepository.playTrack(track, queue, label)
                            }
                        )
                    }
                }
            }
        }

        // Full Screen Now Playing Overlay
        AnimatedVisibility(
            visible = showNowPlaying && playerState.currentTrack != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            val currentTrack = playerState.currentTrack
            val currentId = currentTrack?.id
            NowPlayingScreen(
                playerState = playerState,
                isFavorite = currentId != null && currentId in favoriteIds,
                audioSessionId = playerRepository.getAudioSessionId(),
                onDismiss = { showNowPlaying = false },
                onTogglePlayPause = { playerRepository.togglePlayPause() },
                onNextTrack = { playerRepository.nextTrack() },
                onPreviousTrack = { playerRepository.previousTrack() },
                onSeekTo = { playerRepository.seekTo(it) },
                onToggleShuffle = { playerRepository.toggleShuffle() },
                onCycleRepeat = { playerRepository.cycleRepeatMode() },
                onToggleFavorite = { if (currentId != null) settingsStore.toggleFavorite(currentId) },
                onOpenQueue = { showQueue = true },
                onOpenLyrics = { showLyricsSheet = true },
                onSetPlaybackSpeed = { playerRepository.setPlaybackSpeed(it) },
                onSetSleepTimer = { playerRepository.setSleepTimerMinutes(it) },
                onAddToPlaylist = { if (currentTrack != null) pendingTrackForPlaylist = currentTrack },
                onStartRadio = {
                    if (currentTrack != null) {
                        playerRepository.playSongRadio(currentTrack, TrackCatalogue.tracks)
                        coroutineScope.launch { snackbarHostState.showSnackbar("Radio lagu dimulai") }
                    }
                },
                onOpenAlbum = { albumId ->
                    showNowPlaying = false
                    TrackCatalogue.albums.firstOrNull { it.id == albumId }?.let {
                        detailRoute = DetailRoute.AlbumDetail(it)
                    }
                },
                onOpenArtist = {
                    showNowPlaying = false
                    TrackCatalogue.artists.firstOrNull()?.let {
                        detailRoute = DetailRoute.ArtistDetail(it)
                    }
                },
                onClearError = { playerRepository.clearError() }
            )
        }

        // Full Screen Queue Overlay
        AnimatedVisibility(
            visible = showQueue,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            QueueScreen(
                playerState = playerState,
                onBack = { showQueue = false },
                onRemoveAt = { playerRepository.removeFromQueueAt(it) },
                onClearQueue = {
                    playerRepository.clearQueue()
                    coroutineScope.launch { snackbarHostState.showSnackbar("Antrean dibersihkan") }
                },
                onPlayQueueItem = { track ->
                    playerRepository.playTrack(track, playerState.queue, playerState.sourceLabel)
                }
            )
        }

        // Lyrics Bottom Sheet
        if (showLyricsSheet && playerState.currentTrack != null) {
            LyricsModalSheet(
                track = playerState.currentTrack!!,
                positionMs = playerState.positionMs,
                durationMs = playerState.durationMs,
                onDismiss = { showLyricsSheet = false }
            )
        }

        // Initial Splash Screen
        AnimatedVisibility(
            visible = showSplash,
            exit = fadeOut()
        ) {
            SplashOverlay()
        }
    }
}
