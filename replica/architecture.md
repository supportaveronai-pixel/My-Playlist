# Architecture: My Playlist v1 (clean-room Android music player)

## Stack

| layer | choice | why |
| --- | --- | --- |
| platform | Native Android (minSdk 26, targetSdk 34, compileSdk 34) | Fixed spec, covers modern Android devices with standard Jetpack libraries |
| language | Kotlin 2.0.20, JDK 17 | Fixed spec, modern null-safety and Compose compiler plugin support |
| UI | Jetpack Compose (Material 3, BOM 2024.09.03) | Declarative single-activity UI matching design tokens cleanly |
| playback | `androidx.media3:media3-exoplayer:1.4.1` | Reliable HTTPS MP3 streaming, playback state listeners, repeat/shuffle/speed/queue handling |
| images | `io.coil-kt:coil-compose:2.7.0` | Async cover art loading with Compose fallback gradient placeholder |
| persistence | Android `SharedPreferences` (`SettingsStore`) + `org.json` | Zero-dependency local persistence for profile, favorites, playlists, history, play counts, searches |
| build | Gradle Wrapper 8.7 + AGP 8.5.2 | Standard reproducible Android build and GitHub Actions APK pipeline |

## Data & Persistence Model

No SQL/Room database or remote backend is used in v1. All user state persists locally in `SharedPreferences` (`my_playlist_prefs`) via `SettingsStore`:

- `profile_name` (`String`, default `"Pendengar"`): user display name shown on Home greeting and Profile screen.
- `favorite_track_ids` (`Set<String>`, default empty): IDs of tracks marked as favorite.
- `favorite_album_ids` (`Set<String>`, default empty): IDs of favorited albums.
- `followed_artist_ids` (`Set<String>`, default empty): IDs of followed artists.
- `custom_playlists_json` (`String`, JSON array): serialized list of user-created playlists with `id`, `name`, `description`, `trackIds`, `isPinned`, `colorIndex` (0..5), and `createdAt`.
- `recent_searches_json` (`String`, JSON array): up to 15 most recent search terms.
- `recently_played_json` (`String`, JSON array): up to 50 most recently played track IDs.
- `play_counts_json` (`String`, JSON object): per-track play count map.
- `total_plays` (`Int`) & `total_listened_ms` (`Long`): cumulative listening stats.
- `crossfade_seconds` (`Int`, 0..12): crossfade transition duration preference.

Built-in catalogue (`TrackCatalogue`) is immutable in-memory data:
- 16 demo tracks (`track_1` .. `track_16`) streaming from `https://www.soundhelix.com/examples/mp3/SoundHelix-Song-N.mp3` with cover `https://picsum.photos/seed/myplaylistN/400/400`.
- Distributed across 6 genres: `Elektronik`, `Akustik`, `Sinematik`, `Santai`, `Fokus`, `Malam`.
- Grouped into 3 albums: `Helix Sessions Vol. 1` (1–5), `Helix Sessions Vol. 2` (6–10), `Helix Sessions Vol. 3` (11–16), with original demo lyrics per album.
- Artist metadata for `T. Schürger` referencing all 3 albums.
- 3 built-in playlists: `Fokus Santai` (1–6), `Semangat Pagi` (7–11), `Malam Tenang` (12–16).

## Application & Navigation Architecture

Single-activity Compose architecture (`MainActivity`) backed by an application-scoped singleton container (`MyPlaylistApp` holding `SettingsStore` and `PlayerRepository`) so playback continues uninterrupted when switching tabs.

- **Root Shell**: `Scaffold` with 4-item bottom `NavigationBar` (`Beranda`, `Cari`, `Koleksi`, `Profil`) and a persistent `MiniPlayer` pinned directly above the bottom bar whenever `currentTrack != null`.
- **Detail Routes**: `DetailRoute` sealed interface supporting `PlaylistDetail`, `AlbumDetail`, `ArtistDetail`, `GenreDetail`, and `History`, integrated with Android `BackHandler`.
- **Full-screen Overlays**: `NowPlayingScreen`, `QueueScreen`, `LyricsModalSheet`, and `SplashOverlay`.
- **Pure Domain Logic**: `TrackFilter`, `PlaylistLogic`, `MadeForYouGenerator`, `SearchEngine`, and `LibrarySortLogic` live in pure Kotlin files with zero Android framework dependencies so they execute cleanly in JVM unit tests.

## Stage 2 Addendum: Made-For-You, Queue, Timer & Crossfade

### 1. Deterministic Made-For-You Generation (`MadeForYouGenerator`)
`MadeForYouGenerator.generateMadeForYou(tracks, playCounts)` produces 4 personalized local mixes deterministically without external servers:
- **Daily Mix 1**: Filters tracks in `Elektronik` and `Sinematik` genres sorted descending by local `playCounts`, prioritizing the listener's most-played tracks in those energetic genres (top 5).
- **Daily Mix 2**: Filters tracks in `Akustik` and `Santai` genres sorted descending by `playCounts` (top 5).
- **Discover Mix**: Sorts all 16 tracks ascending by `playCounts` (0-play / least-played tracks first) and takes 6 tracks to encourage catalogue discovery.
- **Release Mix**: Collects tracks from `album_3` (`Helix Sessions Vol. 3`) and `Fokus` genre (up to 6 tracks).

### 2. Queue & Song Radio (`PlayerRepository`)
- `PlayerRepository` maintains both `queue` (full ExoPlayer playlist), `upNextQueue` (explicit user-added tracks via `Putar Berikutnya` / `Tambah ke Antrean`), and `sourceLabel` (`"Berikutnya Dari: ..."`).
- `playNext(track)` inserts a `MediaItem` at `currentMediaItemIndex + 1` and prepends to `upNextQueue`.
- `addToQueue(track)` inserts a `MediaItem` after existing `upNextQueue` items and appends to `upNextQueue`.
- `playSongRadio(seedTrack, allTracks)` constructs a deterministic radio queue starting with `seedTrack`, followed by all same-genre tracks, then remaining catalogue tracks.

### 3. Sleep Timer, Playback Speed & Equalizer Intent
- **Sleep Timer**: Supports `5, 10, 15, 30, 45, 60` minutes countdown coroutine (pauses ExoPlayer when timer reaches 0) or `Akhir lagu` (`isSleepAtTrackEnd` triggers pause on `MEDIA_ITEM_TRANSITION_REASON_AUTO`). Cancels on playback completion (`STATE_ENDED`) or manual cancellation.
- **Playback Speed**: Applies `0.8f`, `1.0f`, `1.25f`, `1.5f` directly to `exoPlayer.setPlaybackSpeed(speed)`.
- **Equalizer**: Launches `AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL` passing `exoPlayer.audioSessionId`, catching `ActivityNotFoundException` with a graceful Indonesian toast.

### 4. Crossfade Setting & Volume Ramp
- `SettingsStore` stores `crossfade_seconds` (`0..12`).
- When `crossfadeSeconds > 0`, `PlayerRepository` enables `exoPlayer.skipSilenceEnabled = true` and runs a coroutine volume ramp-in from `0.15f` to `1.0f` over `min(crossfadeSeconds * 1000L, 4000L)` ms upon track transition. True dual-decoder overlapping audio crossfade is intentionally avoided in single-ExoPlayer v1 and documented as a clean single-player volume ramp approximation.

## Build order

1. **Shell & Data**: Gradle setup, Theme/tokens, `TrackCatalogue`, `SettingsStore`, `PlayerRepository`, 4-tab shell.
2. **Core Vertical Slice**: `HomeScreen` -> tap track -> ExoPlayer streams -> `MiniPlayer` -> `NowPlayingScreen`.
3. **Stage 1 Screens**: `SearchScreen`, `LibraryScreen`, `ProfileScreen`.
4. **Stage 2 Expansion**: Albums/Artists/Genres, Made-For-You, Queue, History/Stats, Sleep Timer/Speed/Equalizer/Share/Radio, Lyrics sheet, Library Sort/Pin/Colors, Adaptive Icon & Splash.
5. **Tests, Sweep & CI**: JVM unit tests (`TrackFilterTest`, `PlaylistLogicTest`, `Stage2DomainLogicTest`), `sweep.py` brand verification, `.github/workflows/build-apk.yml`.
