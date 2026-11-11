package id.myplaylist.app

import id.myplaylist.app.data.TrackCatalogue
import id.myplaylist.app.domain.LibrarySortLogic
import id.myplaylist.app.domain.MadeForYouGenerator
import id.myplaylist.app.domain.PlaylistLogic
import id.myplaylist.app.domain.SearchEngine
import id.myplaylist.app.model.LibrarySort
import id.myplaylist.app.model.Playlist
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Stage2DomainLogicTest {

    @Test
    fun `catalogue assigns all 16 tracks across 6 distinct genres`() {
        val tracks = TrackCatalogue.tracks
        assertEquals(16, tracks.size)

        val foundGenres = tracks.map { it.genre }.toSet()
        assertEquals(TrackCatalogue.genres.toSet(), foundGenres)
        assertEquals(setOf("Elektronik", "Akustik", "Sinematik", "Santai", "Fokus", "Malam"), foundGenres)
    }

    @Test
    fun `catalogue groups 16 tracks into 3 albums with lyrics`() {
        val albums = TrackCatalogue.albums
        assertEquals(3, albums.size)

        assertEquals("Helix Sessions Vol. 1", albums[0].title)
        assertEquals(listOf("track_1", "track_2", "track_3", "track_4", "track_5"), albums[0].trackIds)

        assertEquals("Helix Sessions Vol. 2", albums[1].title)
        assertEquals(listOf("track_6", "track_7", "track_8", "track_9", "track_10"), albums[1].trackIds)

        assertEquals("Helix Sessions Vol. 3", albums[2].title)
        assertEquals(listOf("track_11", "track_12", "track_13", "track_14", "track_15", "track_16"), albums[2].trackIds)

        // Verify each track has original demo lyrics
        TrackCatalogue.tracks.forEach { track ->
            assertTrue("Track ${track.id} has non-empty lyrics", track.lyrics.isNotEmpty())
            assertTrue("Track ${track.id} has at least 4 lines of lyrics", track.lyrics.size >= 4)
        }
    }

    @Test
    fun `artist profile contains bio and references 3 albums`() {
        val artists = TrackCatalogue.artists
        assertEquals(1, artists.size)
        val artist = artists.first()

        assertEquals("T. Schürger", artist.name)
        assertTrue(artist.bio.isNotBlank())
        assertEquals(listOf("album_1", "album_2", "album_3"), artist.albumIds)
    }

    @Test
    fun `made for you generates 4 deterministic mixes based on genres and play counts`() {
        val tracks = TrackCatalogue.tracks
        val playCounts = mapOf(
            "track_1" to 15,
            "track_2" to 20,
            "track_7" to 5
        )

        val mixes = MadeForYouGenerator.generateMadeForYou(tracks, playCounts)
        assertEquals(4, mixes.size)

        val mixNames = mixes.map { it.name }
        assertEquals(listOf("Daily Mix 1", "Daily Mix 2", "Discover Mix", "Release Mix"), mixNames)

        // Daily Mix 1 should prioritize played Elektronik track_2 (20 plays) before track_1 (15 plays)
        val daily1TrackIds = mixes[0].trackIds
        assertTrue(daily1TrackIds.contains("track_2"))
        assertEquals("track_2", daily1TrackIds.first())

        // Discover mix should highlight zero-play tracks
        val discoverTrackIds = mixes[2].trackIds
        assertEquals(6, discoverTrackIds.size)
        discoverTrackIds.forEach { trackId ->
            assertEquals(0, playCounts[trackId] ?: 0)
        }
        assertTrue(discoverTrackIds.contains("track_3"))

        // Generation must be deterministic across repeated calls
        val mixesAgain = MadeForYouGenerator.generateMadeForYou(tracks, playCounts)
        for (i in mixes.indices) {
            assertEquals(mixes[i].trackIds, mixesAgain[i].trackIds)
        }
    }

    @Test
    fun `search engine finds matches across tracks, albums, artists, playlists and genres`() {
        val tracks = TrackCatalogue.tracks
        val albums = TrackCatalogue.albums
        val artists = TrackCatalogue.artists
        val playlists = TrackCatalogue.builtInPlaylists
        val genres = TrackCatalogue.genres

        // Search by genre name
        val genreResult = SearchEngine.search("Sinematik", tracks, albums, artists, playlists, genres)
        assertEquals(1, genreResult.genres.size)
        assertEquals("Sinematik", genreResult.genres.first())
        assertTrue(genreResult.tracks.any { it.genre == "Sinematik" })

        // Search by album name
        val albumResult = SearchEngine.search("Vol. 2", tracks, albums, artists, playlists, genres)
        assertEquals(1, albumResult.albums.size)
        assertEquals("Helix Sessions Vol. 2", albumResult.albums.first().title)

        // Search by artist name
        val artistResult = SearchEngine.search("Schürger", tracks, albums, artists, playlists, genres)
        assertEquals(1, artistResult.artists.size)
        assertEquals("T. Schürger", artistResult.artists.first().name)

        // Blank search returns all items
        val blankResult = SearchEngine.search("   ", tracks, albums, artists, playlists, genres)
        assertEquals(16, blankResult.tracks.size)
        assertEquals(3, blankResult.albums.size)
        assertEquals(1, blankResult.artists.size)
    }

    @Test
    fun `library sort logic puts pinned playlists at top regardless of sort order`() {
        val p1 = Playlist(id = "p1", name = "Zebra", isPinned = false, createdAt = 100L)
        val p2 = Playlist(id = "p2", name = "Alpha", isPinned = false, createdAt = 200L)
        val pPinned = Playlist(id = "p_pinned", name = "Pinned Mix", isPinned = true, createdAt = 50L)

        val list = listOf(p1, p2, pPinned)
        val playCounts = mapOf("track_1" to 10)

        // Alphabetical sort: p_pinned must be index 0, followed by Alpha, then Zebra
        val alphaSorted = LibrarySortLogic.sortPlaylists(list, LibrarySort.ALPHABETICAL, playCounts)
        assertEquals("p_pinned", alphaSorted[0].id)
        assertEquals("Alpha", alphaSorted[1].name)
        assertEquals("Zebra", alphaSorted[2].name)

        // Recent sort: p_pinned must be index 0, followed by p2 (200L), then p1 (100L)
        val recentSorted = LibrarySortLogic.sortPlaylists(list, LibrarySort.RECENT, playCounts)
        assertEquals("p_pinned", recentSorted[0].id)
        assertEquals("p2", recentSorted[1].id)
        assertEquals("p1", recentSorted[2].id)
    }

    @Test
    fun `playlist management operations rename, pin, color, move and delete`() {
        val initial = listOf(
            Playlist(id = "user_1", name = "Lagu Kerja", isBuiltIn = false, trackIds = listOf("t1", "t2", "t3"))
        )

        // Rename
        val renamed = PlaylistLogic.renamePlaylist(initial, "user_1", "Lagu Produktif")
        assertEquals("Lagu Produktif", renamed.first().name)

        // Pin toggle
        val pinned = PlaylistLogic.togglePinPlaylist(renamed, "user_1")
        assertTrue(pinned.first().isPinned)

        // Set color index
        val colored = PlaylistLogic.setPlaylistColor(pinned, "user_1", 4)
        assertEquals(4, colored.first().colorIndex)

        // Move track down: t1 should move from index 0 to 1
        val movedDown = PlaylistLogic.moveTrackDown(colored, "user_1", "t1")
        assertEquals(listOf("t2", "t1", "t3"), movedDown.first().trackIds)

        // Move track up: t1 should move back to index 0
        val movedUp = PlaylistLogic.moveTrackUp(movedDown, "user_1", "t1")
        assertEquals(listOf("t1", "t2", "t3"), movedUp.first().trackIds)

        // Delete playlist
        val deleted = PlaylistLogic.deletePlaylist(movedUp, "user_1")
        assertTrue(deleted.isEmpty())
    }
}
