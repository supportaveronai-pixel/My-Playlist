package id.myplaylist.app

import id.myplaylist.app.data.TrackCatalogue
import id.myplaylist.app.domain.TrackFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackFilterTest {

    @Test
    fun `blank query returns all tracks`() {
        val all = TrackCatalogue.tracks
        assertEquals(16, TrackFilter.filterTracks(all, "").size)
        assertEquals(16, TrackFilter.filterTracks(all, "   ").size)
    }

    @Test
    fun `query filters by title case-insensitively with whitespace trimmed`() {
        val result = TrackFilter.filterTracks(TrackCatalogue.tracks, "  song 12 ")
        assertEquals(1, result.size)
        assertEquals("SoundHelix Song 12", result.first().title)
    }

    @Test
    fun `query matching Song 1 returns Song 1 and 10 to 16`() {
        val result = TrackFilter.filterTracks(TrackCatalogue.tracks, "Song 1")
        assertEquals(8, result.size)
        assertTrue(result.any { it.number == 1 })
        assertTrue(result.any { it.number == 16 })
    }

    @Test
    fun `query filters by artist name`() {
        val result = TrackFilter.filterTracks(TrackCatalogue.tracks, "schürger")
        assertEquals(16, result.size)
    }

    @Test
    fun `non matching query returns empty list`() {
        val result = TrackFilter.filterTracks(TrackCatalogue.tracks, "lagu-tidak-ada")
        assertTrue(result.isEmpty())
    }
}
