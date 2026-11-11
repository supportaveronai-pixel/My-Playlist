package id.myplaylist.app.domain

import id.myplaylist.app.model.Track

object TrackFilter {
    fun filterTracks(tracks: List<Track>, query: String): List<Track> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return tracks
        return tracks.filter { track ->
            track.title.contains(trimmed, ignoreCase = true) ||
                track.artist.contains(trimmed, ignoreCase = true)
        }
    }
}
