package id.myplaylist.app.data

import id.myplaylist.app.domain.PlaylistLogic
import id.myplaylist.app.model.Album
import id.myplaylist.app.model.Artist
import id.myplaylist.app.model.Playlist
import id.myplaylist.app.model.Track

object TrackCatalogue {

    val genres = listOf(
        "Elektronik",
        "Akustik",
        "Sinematik",
        "Santai",
        "Fokus",
        "Malam"
    )

    private val album1Lyrics = listOf(
        "Melangkah di bawah cahaya fajar",
        "Suara ritme membawa ketenangan jiwa",
        "Menemani langkah tanpa tergesa",
        "Menemukan harmoni di setiap jeda",
        "Hanyut dalam alunan nada demo",
        "Menyambut hari penuh inspirasi"
    )

    private val album2Lyrics = listOf(
        "Sinar mentari menghangatkan suasana",
        "Detak melodi mengalir bebas",
        "Semangat membara di tengah karya",
        "Mengejar impian tanpa ragu",
        "Langkah tegap menyusuri jalan",
        "Kreativitas mengalir tiada henti"
    )

    private val album3Lyrics = listOf(
        "Langit senja perlahan berganti malam",
        "Hembusan angin meniupkan kedamaian",
        "Melepas lelah setelah seharian berusaha",
        "Melodi lembut menyelimuti sunyi",
        "Bintang berpijar mengiringi istirahat",
        "Hati tenang menyambut mimpi indah"
    )

    val tracks: List<Track> = (1..16).map { index ->
        val genre = when (index) {
            1, 2, 7 -> "Elektronik"
            3, 8, 12 -> "Akustik"
            4, 9, 13 -> "Sinematik"
            5, 10, 14 -> "Santai"
            6, 11, 15 -> "Fokus"
            else -> "Malam"
        }

        val albumId = when (index) {
            in 1..5 -> "album_1"
            in 6..10 -> "album_2"
            else -> "album_3"
        }

        val lyrics = when (albumId) {
            "album_1" -> album1Lyrics
            "album_2" -> album2Lyrics
            else -> album3Lyrics
        }

        Track(
            id = "track_$index",
            number = index,
            title = "SoundHelix Song $index",
            artist = "T. Schürger",
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-$index.mp3",
            coverUrl = "https://picsum.photos/seed/myplaylist$index/400/400",
            durationLabel = "demo",
            genre = genre,
            albumId = albumId,
            lyrics = lyrics
        )
    }

    val albums: List<Album> = listOf(
        Album(
            id = "album_1",
            title = "Helix Sessions Vol. 1",
            artist = "T. Schürger",
            year = "2024 (demo)",
            coverUrl = "https://picsum.photos/seed/myplaylistalbum1/400/400",
            trackIds = (1..5).map { "track_$it" },
            description = "Eksplorasi pembuka dengan paduan melodi elektronik dan nada akustik ritmis."
        ),
        Album(
            id = "album_2",
            title = "Helix Sessions Vol. 2",
            artist = "T. Schürger",
            year = "2024 (demo)",
            coverUrl = "https://picsum.photos/seed/myplaylistalbum2/400/400",
            trackIds = (6..10).map { "track_$it" },
            description = "Aransemen bernuansa sinematik penuh energi untuk menjaga semangat harian."
        ),
        Album(
            id = "album_3",
            title = "Helix Sessions Vol. 3",
            artist = "T. Schürger",
            year = "2024 (demo)",
            coverUrl = "https://picsum.photos/seed/myplaylistalbum3/400/400",
            trackIds = (11..16).map { "track_$it" },
            description = "Komposisi penutup yang menenangkan hati, cocok untuk relaksasi malam hari."
        )
    )

    val artists: List<Artist> = listOf(
        Artist(
            id = "artist_schurger",
            name = "T. Schürger",
            bio = "T. Schürger adalah komposer musik demo SoundHelix. Melalui aransemen dynamic algorithmic synthesis, karya-karyanya menghadirkan harmoni melodi elektronik, akustik, dan ambient yang menenangkan untuk menemani produktivitas serta waktu santai Anda.",
            coverUrl = "https://picsum.photos/seed/myplaylistartist/400/400",
            albumIds = listOf("album_1", "album_2", "album_3")
        )
    )

    val builtInPlaylists: List<Playlist> = PlaylistLogic.buildDefaultPlaylists(tracks)
}
