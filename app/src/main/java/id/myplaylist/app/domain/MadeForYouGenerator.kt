package id.myplaylist.app.domain

import id.myplaylist.app.model.Playlist
import id.myplaylist.app.model.Track

object MadeForYouGenerator {

    fun generateMadeForYou(tracks: List<Track>, playCounts: Map<String, Int>): List<Playlist> {
        val tracksByPlayCount = tracks.sortedByDescending { playCounts[it.id] ?: 0 }

        // Daily Mix 1: Elektronik & Sinematik with higher played tracks first
        val mix1Tracks = tracksByPlayCount.filter { it.genre in setOf("Elektronik", "Sinematik") }
            .take(5)
            .map { it.id }

        // Daily Mix 2: Akustik & Santai
        val mix2Tracks = tracksByPlayCount.filter { it.genre in setOf("Akustik", "Santai") }
            .take(5)
            .map { it.id }

        // Discover Mix: Tracks with lowest or zero play counts
        val discoverTracks = tracks.sortedBy { playCounts[it.id] ?: 0 }
            .take(6)
            .map { it.id }

        // Release Mix: Tracks from Album 3 + Fokus genre
        val releaseTracks = tracks.filter { it.albumId == "album_3" || it.genre == "Fokus" }
            .take(6)
            .map { it.id }

        return listOf(
            Playlist(
                id = "made_for_you_daily_1",
                name = "Daily Mix 1",
                description = "Campuran melodi Elektronik dan Sinematik pilihan untukmu",
                isBuiltIn = true,
                trackIds = if (mix1Tracks.isNotEmpty()) mix1Tracks else tracks.take(5).map { it.id },
                colorIndex = 0
            ),
            Playlist(
                id = "made_for_you_daily_2",
                name = "Daily Mix 2",
                description = "Harmoni Akustik dan nada Santai yang paling pas dengan seleramu",
                isBuiltIn = true,
                trackIds = if (mix2Tracks.isNotEmpty()) mix2Tracks else tracks.takeLast(5).map { it.id },
                colorIndex = 1
            ),
            Playlist(
                id = "made_for_you_discover",
                name = "Discover Mix",
                description = "Jelajahi lagu-lagu demo yang belum sering kamu dengarkan",
                isBuiltIn = true,
                trackIds = discoverTracks,
                colorIndex = 2
            ),
            Playlist(
                id = "made_for_you_release",
                name = "Release Mix",
                description = "Koleksi rilisan terbaru dari sesi Helix Sessions Vol. 3",
                isBuiltIn = true,
                trackIds = releaseTracks,
                colorIndex = 3
            )
        )
    }
}
