package id.myplaylist.app.domain

import id.myplaylist.app.model.Album
import id.myplaylist.app.model.Artist
import id.myplaylist.app.model.Playlist
import id.myplaylist.app.model.Track

data class SearchResult(
    val tracks: List<Track> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val genres: List<String> = emptyList()
) {
    val isEmpty: Boolean
        get() = tracks.isEmpty() && albums.isEmpty() && artists.isEmpty() && playlists.isEmpty() && genres.isEmpty()
}

object SearchEngine {

    fun search(
        query: String,
        allTracks: List<Track>,
        allAlbums: List<Album>,
        allArtists: List<Artist>,
        allPlaylists: List<Playlist>,
        allGenres: List<String>
    ): SearchResult {
        val q = query.trim()
        if (q.isEmpty()) {
            return SearchResult(
                tracks = allTracks,
                albums = allAlbums,
                artists = allArtists,
                playlists = allPlaylists,
                genres = allGenres
            )
        }

        val matchedTracks = allTracks.filter {
            it.title.contains(q, ignoreCase = true) ||
                it.artist.contains(q, ignoreCase = true) ||
                it.genre.contains(q, ignoreCase = true)
        }

        val matchedAlbums = allAlbums.filter {
            it.title.contains(q, ignoreCase = true) ||
                it.artist.contains(q, ignoreCase = true)
        }

        val matchedArtists = allArtists.filter {
            it.name.contains(q, ignoreCase = true) ||
                it.bio.contains(q, ignoreCase = true)
        }

        val matchedPlaylists = allPlaylists.filter {
            it.name.contains(q, ignoreCase = true) ||
                it.description.contains(q, ignoreCase = true)
        }

        val matchedGenres = allGenres.filter {
            it.contains(q, ignoreCase = true)
        }

        return SearchResult(
            tracks = matchedTracks,
            albums = matchedAlbums,
            artists = matchedArtists,
            playlists = matchedPlaylists,
            genres = matchedGenres
        )
    }
}
