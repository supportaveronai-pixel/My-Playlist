package id.myplaylist.app.domain

import id.myplaylist.app.model.LibrarySort
import id.myplaylist.app.model.Playlist

object LibrarySortLogic {

    fun sortPlaylists(
        playlists: List<Playlist>,
        sort: LibrarySort,
        playCounts: Map<String, Int>
    ): List<Playlist> {
        val pinned = playlists.filter { it.isPinned }
        val unpinned = playlists.filterNot { it.isPinned }

        val sortedUnpinned = when (sort) {
            LibrarySort.RECENT -> unpinned.sortedByDescending { it.createdAt }
            LibrarySort.ALPHABETICAL -> unpinned.sortedBy { it.name.lowercase() }
            LibrarySort.MOST_PLAYED -> unpinned.sortedByDescending { playlist ->
                playlist.trackIds.sumOf { playCounts[it] ?: 0 }
            }
        }

        return pinned + sortedUnpinned
    }

    fun reorderTrackInPlaylist(
        playlist: Playlist,
        fromIndex: Int,
        toIndex: Int
    ): Playlist {
        val list = playlist.trackIds.toMutableList()
        if (fromIndex !in list.indices || toIndex !in list.indices) return playlist
        val item = list.removeAt(fromIndex)
        list.add(toIndex, item)
        return playlist.copy(trackIds = list)
    }
}
