package id.myplaylist.app

import id.myplaylist.app.data.TrackCatalogue
import id.myplaylist.app.domain.PlaylistLogic
import id.myplaylist.app.model.Playlist
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaylistLogicTest {

    @Test
    fun `catalogue contains 16 SoundHelix tracks with valid URLs`() {
        val tracks = TrackCatalogue.tracks
        assertEquals(16, tracks.size)
        assertEquals("SoundHelix Song 1", tracks.first().title)
        assertEquals("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3", tracks.first().streamUrl)
        assertEquals("https://picsum.photos/seed/myplaylist1/400/400", tracks.first().coverUrl)
        assertEquals("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-16.mp3", tracks.last().streamUrl)
    }

    @Test
    fun `built-in playlists group tracks into 3 themed collections`() {
        val playlists = PlaylistLogic.buildDefaultPlaylists(TrackCatalogue.tracks)
        assertEquals(3, playlists.size)

        assertEquals("Fokus Santai", playlists[0].name)
        assertEquals(listOf("track_1", "track_2", "track_3", "track_4", "track_5", "track_6"), playlists[0].trackIds)

        assertEquals("Semangat Pagi", playlists[1].name)
        assertEquals(listOf("track_7", "track_8", "track_9", "track_10", "track_11"), playlists[1].trackIds)

        assertEquals("Malam Tenang", playlists[2].name)
        assertEquals(listOf("track_12", "track_13", "track_14", "track_15", "track_16"), playlists[2].trackIds)
    }

    @Test
    fun `resolveTracks maps playlist IDs back to Track objects in order`() {
        val fokus = TrackCatalogue.builtInPlaylists.first()
        val resolved = PlaylistLogic.resolveTracks(fokus, TrackCatalogue.tracks)
        assertEquals(6, resolved.size)
        assertEquals(1, resolved.first().number)
        assertEquals(6, resolved.last().number)
    }

    @Test
    fun `createPlaylist trims valid name and ignores blank names`() {
        val (unchanged, failed) = PlaylistLogic.createPlaylist(emptyList(), "   ", "id_1")
        assertNull(failed)
        assertTrue(unchanged.isEmpty())

        val (updated, created) = PlaylistLogic.createPlaylist(emptyList(), "  Musik Santai  ", "user_1")
        assertNotNull(created)
        assertEquals("Musik Santai", created?.name)
        assertEquals(1, updated.size)
    }

    @Test
    fun `add and remove track work on custom playlists without duplicates and protect built-ins`() {
        val custom = Playlist(id = "user_1", name = "Favorit Kerja", isBuiltIn = false)
        val builtIn = TrackCatalogue.builtInPlaylists.first()
        val list = listOf(builtIn, custom)

        // Add track to custom playlist
        val afterAdd = PlaylistLogic.addTrackToPlaylist(list, "user_1", "track_3")
        assertEquals(listOf("track_3"), afterAdd[1].trackIds)

        // Adding duplicate does not duplicate
        val afterDup = PlaylistLogic.addTrackToPlaylist(afterAdd, "user_1", "track_3")
        assertEquals(listOf("track_3"), afterDup[1].trackIds)

        // Attempting to modify built-in playlist leaves it untouched
        val afterBuiltInAttempt = PlaylistLogic.addTrackToPlaylist(afterDup, builtIn.id, "track_16")
        assertEquals(builtIn.trackIds, afterBuiltInAttempt[0].trackIds)

        // Remove track from custom playlist
        val afterRemove = PlaylistLogic.removeTrackFromPlaylist(afterBuiltInAttempt, "user_1", "track_3")
        assertTrue(afterRemove[1].trackIds.isEmpty())
    }

    @Test
    fun `favoriteTracks returns matching tracks in catalogue order`() {
        val favs = PlaylistLogic.favoriteTracks(TrackCatalogue.tracks, setOf("track_10", "track_2"))
        assertEquals(listOf("track_2", "track_10"), favs.map { it.id })
    }
}
