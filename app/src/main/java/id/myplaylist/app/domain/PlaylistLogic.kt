package id.myplaylist.app.domain

import id.myplaylist.app.model.Playlist
import id.myplaylist.app.model.Track

object PlaylistLogic {
    fun buildDefaultPlaylists(tracks: List<Track>): List<Playlist> {
        val byNumber = tracks.associateBy { it.number }
        fun idsInRange(range: IntRange): List<String> =
            range.mapNotNull { byNumber[it]?.id }

        return listOf(
            Playlist(
                id = "builtin_fokus",
                name = "Fokus Santai",
                description = "Alunan ritme stabil untuk menemani fokus harian",
                isBuiltIn = true,
                trackIds = idsInRange(1..6)
            ),
            Playlist(
                id = "builtin_pagi",
                name = "Semangat Pagi",
                description = "Lagu berenergi untuk mengawali aktivitas pagi",
                isBuiltIn = true,
                trackIds = idsInRange(7..11)
            ),
            Playlist(
                id = "builtin_malam",
                name = "Malam Tenang",
                description = "Komposisi santai untuk beristirahat di malam hari",
                isBuiltIn = true,
                trackIds = idsInRange(12..16)
            )
        )
    }

    fun resolveTracks(playlist: Playlist, allTracks: List<Track>): List<Track> {
        val byId = allTracks.associateBy { it.id }
        return playlist.trackIds.mapNotNull { byId[it] }
    }

    fun favoriteTracks(allTracks: List<Track>, favoriteIds: Set<String>): List<Track> {
        return allTracks.filter { it.id in favoriteIds }
    }

    fun createPlaylist(
        existing: List<Playlist>,
        rawName: String,
        newId: String,
        colorIndex: Int = 0,
        createdAt: Long = System.currentTimeMillis()
    ): Pair<List<Playlist>, Playlist?> {
        val name = rawName.trim()
        if (name.isEmpty()) return existing to null
        val created = Playlist(
            id = newId,
            name = name,
            description = "Playlist buatan sendiri",
            isBuiltIn = false,
            trackIds = emptyList(),
            isPinned = false,
            colorIndex = colorIndex.coerceIn(0, 5),
            createdAt = createdAt
        )
        return (existing + created) to created
    }

    fun renamePlaylist(
        playlists: List<Playlist>,
        playlistId: String,
        rawNewName: String
    ): List<Playlist> {
        val trimmed = rawNewName.trim()
        if (trimmed.isEmpty()) return playlists
        return playlists.map {
            if (it.id == playlistId && !it.isBuiltIn) it.copy(name = trimmed) else it
        }
    }

    fun deletePlaylist(
        playlists: List<Playlist>,
        playlistId: String
    ): List<Playlist> {
        return playlists.filterNot { it.id == playlistId && !it.isBuiltIn }
    }

    fun togglePinPlaylist(
        playlists: List<Playlist>,
        playlistId: String
    ): List<Playlist> {
        return playlists.map {
            if (it.id == playlistId && !it.isBuiltIn) it.copy(isPinned = !it.isPinned) else it
        }
    }

    fun setPlaylistColor(
        playlists: List<Playlist>,
        playlistId: String,
        colorIndex: Int
    ): List<Playlist> {
        return playlists.map {
            if (it.id == playlistId && !it.isBuiltIn) it.copy(colorIndex = colorIndex.coerceIn(0, 5)) else it
        }
    }

    fun moveTrackUp(
        playlists: List<Playlist>,
        playlistId: String,
        trackId: String
    ): List<Playlist> {
        return playlists.map { playlist ->
            if (playlist.id == playlistId && !playlist.isBuiltIn) {
                val idx = playlist.trackIds.indexOf(trackId)
                if (idx > 0) LibrarySortLogic.reorderTrackInPlaylist(playlist, idx, idx - 1) else playlist
            } else {
                playlist
            }
        }
    }

    fun moveTrackDown(
        playlists: List<Playlist>,
        playlistId: String,
        trackId: String
    ): List<Playlist> {
        return playlists.map { playlist ->
            if (playlist.id == playlistId && !playlist.isBuiltIn) {
                val idx = playlist.trackIds.indexOf(trackId)
                if (idx in 0 until playlist.trackIds.lastIndex) {
                    LibrarySortLogic.reorderTrackInPlaylist(playlist, idx, idx + 1)
                } else {
                    playlist
                }
            } else {
                playlist
            }
        }
    }

    fun addTrackToPlaylist(
        playlists: List<Playlist>,
        playlistId: String,
        trackId: String
    ): List<Playlist> {
        return playlists.map { playlist ->
            if (playlist.id == playlistId && !playlist.isBuiltIn && trackId !in playlist.trackIds) {
                playlist.copy(trackIds = playlist.trackIds + trackId)
            } else {
                playlist
            }
        }
    }

    fun removeTrackFromPlaylist(
        playlists: List<Playlist>,
        playlistId: String,
        trackId: String
    ): List<Playlist> {
        return playlists.map { playlist ->
            if (playlist.id == playlistId && !playlist.isBuiltIn) {
                playlist.copy(trackIds = playlist.trackIds.filterNot { it == trackId })
            } else {
                playlist
            }
        }
    }
}
