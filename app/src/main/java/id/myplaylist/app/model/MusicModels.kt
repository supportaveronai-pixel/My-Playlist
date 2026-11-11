package id.myplaylist.app.model

data class Track(
    val id: String,
    val number: Int,
    val title: String,
    val artist: String,
    val streamUrl: String,
    val coverUrl: String,
    val durationLabel: String = "demo",
    val genre: String = "Elektronik",
    val albumId: String = "album_1",
    val lyrics: List<String> = emptyList()
)

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val year: String = "2024 (demo)",
    val coverUrl: String,
    val trackIds: List<String> = emptyList(),
    val description: String = ""
)

data class Artist(
    val id: String,
    val name: String,
    val bio: String,
    val coverUrl: String,
    val albumIds: List<String> = emptyList()
)

data class Playlist(
    val id: String,
    val name: String,
    val description: String = "",
    val isBuiltIn: Boolean = false,
    val trackIds: List<String> = emptyList(),
    val isPinned: Boolean = false,
    val colorIndex: Int = 0,
    val createdAt: Long = 0L
)

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

enum class LibraryFilter(val label: String) {
    ALL("Semua"),
    PLAYLIST("Playlist"),
    ALBUM("Album"),
    ARTIST("Artis"),
    FAVORITE("Favorit")
}

enum class LibrarySort(val label: String) {
    RECENT("Terbaru"),
    ALPHABETICAL("A-Z"),
    MOST_PLAYED("Paling Sering Diputar")
}

enum class SearchTab(val label: String) {
    ALL("Semua"),
    TRACKS("Lagu"),
    ALBUMS("Album"),
    ARTISTS("Artis"),
    PLAYLISTS("Playlist"),
    GENRES("Genre")
}

data class PlayerState(
    val currentTrack: Track? = null,
    val queue: List<Track> = emptyList(),
    val upNextQueue: List<Track> = emptyList(),
    val sourceLabel: String = "My Playlist",
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val playbackSpeed: Float = 1.0f,
    val sleepTimerMinutes: Int? = null,
    val sleepTimerRemainingSec: Int = 0,
    val isSleepAtTrackEnd: Boolean = false,
    val errorMessage: String? = null
)
