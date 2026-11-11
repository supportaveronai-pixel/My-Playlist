package id.myplaylist.app.data

import android.content.Context
import android.content.SharedPreferences
import id.myplaylist.app.domain.PlaylistLogic
import id.myplaylist.app.model.Playlist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class SettingsStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _profileName = MutableStateFlow(loadProfileName())
    val profileName: StateFlow<String> = _profileName.asStateFlow()

    private val _favoriteIds = MutableStateFlow(loadStringSet(KEY_FAVORITES))
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    private val _favoriteAlbumIds = MutableStateFlow(loadStringSet(KEY_FAVORITE_ALBUMS))
    val favoriteAlbumIds: StateFlow<Set<String>> = _favoriteAlbumIds.asStateFlow()

    private val _followedArtistIds = MutableStateFlow(loadStringSet(KEY_FOLLOWED_ARTISTS))
    val followedArtistIds: StateFlow<Set<String>> = _followedArtistIds.asStateFlow()

    private val _customPlaylists = MutableStateFlow(loadCustomPlaylists())
    val customPlaylists: StateFlow<List<Playlist>> = _customPlaylists.asStateFlow()

    private val _recentSearches = MutableStateFlow(loadStringList(KEY_RECENT_SEARCHES))
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    private val _recentlyPlayedIds = MutableStateFlow(loadStringList(KEY_RECENTLY_PLAYED))
    val recentlyPlayedIds: StateFlow<List<String>> = _recentlyPlayedIds.asStateFlow()

    private val _playCounts = MutableStateFlow(loadPlayCounts())
    val playCounts: StateFlow<Map<String, Int>> = _playCounts.asStateFlow()

    private val _totalPlays = MutableStateFlow(prefs.getInt(KEY_TOTAL_PLAYS, 0))
    val totalPlays: StateFlow<Int> = _totalPlays.asStateFlow()

    private val _totalListenedMs = MutableStateFlow(prefs.getLong(KEY_TOTAL_LISTENED_MS, 0L))
    val totalListenedMs: StateFlow<Long> = _totalListenedMs.asStateFlow()

    private val _crossfadeSeconds = MutableStateFlow(prefs.getInt(KEY_CROSSFADE_SECONDS, 0).coerceIn(0, 12))
    val crossfadeSeconds: StateFlow<Int> = _crossfadeSeconds.asStateFlow()

    fun setProfileName(name: String) {
        val cleaned = name.trim().ifEmpty { DEFAULT_PROFILE_NAME }
        prefs.edit().putString(KEY_PROFILE_NAME, cleaned).apply()
        _profileName.value = cleaned
    }

    fun toggleFavorite(trackId: String) {
        val updated = _favoriteIds.value.toMutableSet()
        if (!updated.add(trackId)) {
            updated.remove(trackId)
        }
        prefs.edit().putStringSet(KEY_FAVORITES, updated).apply()
        _favoriteIds.value = updated.toSet()
    }

    fun toggleFavoriteAlbum(albumId: String) {
        val updated = _favoriteAlbumIds.value.toMutableSet()
        if (!updated.add(albumId)) {
            updated.remove(albumId)
        }
        prefs.edit().putStringSet(KEY_FAVORITE_ALBUMS, updated).apply()
        _favoriteAlbumIds.value = updated.toSet()
    }

    fun toggleFollowArtist(artistId: String) {
        val updated = _followedArtistIds.value.toMutableSet()
        if (!updated.add(artistId)) {
            updated.remove(artistId)
        }
        prefs.edit().putStringSet(KEY_FOLLOWED_ARTISTS, updated).apply()
        _followedArtistIds.value = updated.toSet()
    }

    fun addRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        val updated = (listOf(trimmed) + _recentSearches.value.filterNot {
            it.equals(trimmed, ignoreCase = true)
        }).take(15)
        saveStringList(KEY_RECENT_SEARCHES, updated)
        _recentSearches.value = updated
    }

    fun clearRecentSearches() {
        prefs.edit().remove(KEY_RECENT_SEARCHES).apply()
        _recentSearches.value = emptyList()
    }

    fun recordTrackPlay(trackId: String) {
        val updatedRecent = (listOf(trackId) + _recentlyPlayedIds.value.filterNot { it == trackId })
            .take(50)
        saveStringList(KEY_RECENTLY_PLAYED, updatedRecent)
        _recentlyPlayedIds.value = updatedRecent

        val updatedCounts = _playCounts.value.toMutableMap()
        updatedCounts[trackId] = (updatedCounts[trackId] ?: 0) + 1
        savePlayCounts(updatedCounts)
        _playCounts.value = updatedCounts.toMap()

        val nextTotal = _totalPlays.value + 1
        prefs.edit().putInt(KEY_TOTAL_PLAYS, nextTotal).apply()
        _totalPlays.value = nextTotal
    }

    fun clearRecentlyPlayed() {
        prefs.edit().remove(KEY_RECENTLY_PLAYED).apply()
        _recentlyPlayedIds.value = emptyList()
    }

    fun addListenedTime(deltaMs: Long) {
        if (deltaMs <= 0L) return
        val next = _totalListenedMs.value + deltaMs
        prefs.edit().putLong(KEY_TOTAL_LISTENED_MS, next).apply()
        _totalListenedMs.value = next
    }

    fun setCrossfadeSeconds(seconds: Int) {
        val clamped = seconds.coerceIn(0, 12)
        prefs.edit().putInt(KEY_CROSSFADE_SECONDS, clamped).apply()
        _crossfadeSeconds.value = clamped
    }

    fun createCustomPlaylist(name: String, colorIndex: Int = 0): Playlist? {
        val (updated, created) = PlaylistLogic.createPlaylist(
            existing = _customPlaylists.value,
            rawName = name,
            newId = "user_${UUID.randomUUID()}",
            colorIndex = colorIndex,
            createdAt = System.currentTimeMillis()
        )
        if (created != null) {
            saveCustomPlaylists(updated)
            _customPlaylists.value = updated
        }
        return created
    }

    fun renameCustomPlaylist(playlistId: String, newName: String) {
        val updated = PlaylistLogic.renamePlaylist(_customPlaylists.value, playlistId, newName)
        if (updated != _customPlaylists.value) {
            saveCustomPlaylists(updated)
            _customPlaylists.value = updated
        }
    }

    fun deleteCustomPlaylist(playlistId: String) {
        val updated = PlaylistLogic.deletePlaylist(_customPlaylists.value, playlistId)
        if (updated != _customPlaylists.value) {
            saveCustomPlaylists(updated)
            _customPlaylists.value = updated
        }
    }

    fun togglePinPlaylist(playlistId: String) {
        val updated = PlaylistLogic.togglePinPlaylist(_customPlaylists.value, playlistId)
        if (updated != _customPlaylists.value) {
            saveCustomPlaylists(updated)
            _customPlaylists.value = updated
        }
    }

    fun setPlaylistColor(playlistId: String, colorIndex: Int) {
        val updated = PlaylistLogic.setPlaylistColor(_customPlaylists.value, playlistId, colorIndex)
        if (updated != _customPlaylists.value) {
            saveCustomPlaylists(updated)
            _customPlaylists.value = updated
        }
    }

    fun addTrackToPlaylist(playlistId: String, trackId: String) {
        val updated = PlaylistLogic.addTrackToPlaylist(_customPlaylists.value, playlistId, trackId)
        if (updated != _customPlaylists.value) {
            saveCustomPlaylists(updated)
            _customPlaylists.value = updated
        }
    }

    fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        val updated = PlaylistLogic.removeTrackFromPlaylist(_customPlaylists.value, playlistId, trackId)
        if (updated != _customPlaylists.value) {
            saveCustomPlaylists(updated)
            _customPlaylists.value = updated
        }
    }

    fun moveTrackUpInPlaylist(playlistId: String, trackId: String) {
        val updated = PlaylistLogic.moveTrackUp(_customPlaylists.value, playlistId, trackId)
        if (updated != _customPlaylists.value) {
            saveCustomPlaylists(updated)
            _customPlaylists.value = updated
        }
    }

    fun moveTrackDownInPlaylist(playlistId: String, trackId: String) {
        val updated = PlaylistLogic.moveTrackDown(_customPlaylists.value, playlistId, trackId)
        if (updated != _customPlaylists.value) {
            saveCustomPlaylists(updated)
            _customPlaylists.value = updated
        }
    }

    private fun loadProfileName(): String {
        return prefs.getString(KEY_PROFILE_NAME, DEFAULT_PROFILE_NAME)
            ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_PROFILE_NAME
    }

    private fun loadStringSet(key: String): Set<String> {
        return prefs.getStringSet(key, emptySet())?.toSet() ?: emptySet()
    }

    private fun loadStringList(key: String): List<String> {
        val raw = prefs.getString(key, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    add(array.getString(i))
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveStringList(key: String, list: List<String>) {
        val array = JSONArray(list)
        prefs.edit().putString(key, array.toString()).apply()
    }

    private fun loadPlayCounts(): Map<String, Int> {
        val raw = prefs.getString(KEY_PLAY_COUNTS, null) ?: return emptyMap()
        return try {
            val obj = JSONObject(raw)
            buildMap {
                obj.keys().forEach { key ->
                    put(key, obj.optInt(key, 0))
                }
            }
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun savePlayCounts(map: Map<String, Int>) {
        val obj = JSONObject()
        map.forEach { (k, v) -> obj.put(k, v) }
        prefs.edit().putString(KEY_PLAY_COUNTS, obj.toString()).apply()
    }

    private fun loadCustomPlaylists(): List<Playlist> {
        val raw = prefs.getString(KEY_CUSTOM_PLAYLISTS, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val trackIdsArray = obj.optJSONArray("trackIds") ?: JSONArray()
                    val trackIds = buildList {
                        for (j in 0 until trackIdsArray.length()) {
                            add(trackIdsArray.getString(j))
                        }
                    }
                    add(
                        Playlist(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            description = obj.optString("description", "Playlist buatan sendiri"),
                            isBuiltIn = false,
                            trackIds = trackIds,
                            isPinned = obj.optBoolean("isPinned", false),
                            colorIndex = obj.optInt("colorIndex", 0),
                            createdAt = obj.optLong("createdAt", 0L)
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveCustomPlaylists(playlists: List<Playlist>) {
        val array = JSONArray()
        playlists.forEach { playlist ->
            val obj = JSONObject().apply {
                put("id", playlist.id)
                put("name", playlist.name)
                put("description", playlist.description)
                put("trackIds", JSONArray(playlist.trackIds))
                put("isPinned", playlist.isPinned)
                put("colorIndex", playlist.colorIndex)
                put("createdAt", playlist.createdAt)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_CUSTOM_PLAYLISTS, array.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "my_playlist_prefs"
        private const val KEY_PROFILE_NAME = "profile_name"
        private const val KEY_FAVORITES = "favorite_track_ids"
        private const val KEY_FAVORITE_ALBUMS = "favorite_album_ids"
        private const val KEY_FOLLOWED_ARTISTS = "followed_artist_ids"
        private const val KEY_CUSTOM_PLAYLISTS = "custom_playlists_json"
        private const val KEY_RECENT_SEARCHES = "recent_searches_json"
        private const val KEY_RECENTLY_PLAYED = "recently_played_json"
        private const val KEY_PLAY_COUNTS = "play_counts_json"
        private const val KEY_TOTAL_PLAYS = "total_plays"
        private const val KEY_TOTAL_LISTENED_MS = "total_listened_ms"
        private const val KEY_CROSSFADE_SECONDS = "crossfade_seconds"
        const val DEFAULT_PROFILE_NAME = "Pendengar"
    }
}
