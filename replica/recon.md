# Recon map: Streaming Music Player (Android)

Scope: Core streaming music player loop — browse tracks and curated playlists, search catalogue, manage custom playlists and favorites, edit local user profile, and full-screen audio playback with persistent mini-player.
For: My Playlist v1 (clean-room white-theme Indonesian Android app).
Date: 2026-11-11

## Sources

| # | source | URL | notes |
| --- | --- | --- | --- |
| 1 | Spotify Android Play Store listing (reference UX) | https://play.google.com/store/apps/details?id=com.spotify.music | Screen layout patterns for Home, Search, Library, Mini-player, Now Playing |
| 2 | Spotify Support Docs | https://support.spotify.com | Playlist management, repeat/shuffle states, favorites |
| 3 | Material Design 3 Guidelines | https://m3.material.io | Bottom navigation bar, cards, modals, contrast rules |

## Core loop

Discover or search tracks, tap to stream audio continuously across navigation tabs via a persistent mini-player, and organize favorite tracks into personal playlists.

## Screens

| ID | screen | route / how to reach | purpose | key components | states seen |
| --- | --- | --- | --- | --- | --- |
| S01 | Home (Beranda) | Bottom nav tab 1 (`home`) | Greeting, featured built-in playlists row, full track catalogue | GreetingHeader, PlaylistCarousel, TrackRow, FavoriteToggle | loading, filled |
| S02 | Search (Cari) | Bottom nav tab 2 (`search`) | Live filter tracks by title or artist | SearchInput, TrackRow, EmptyState | empty-query (all tracks or prompt), filtered-filled, empty-results |
| S03 | Library (Koleksi) | Bottom nav tab 3 (`library`) | Manage favorites, built-in playlists, and custom user playlists; view playlist detail | CreatePlaylistButton, PlaylistCard, FavoritesSection, PlaylistDetailSheet/View, CreatePlaylistDialog | empty-custom, filled, playlist-detail-empty, playlist-detail-filled |
| S04 | Profile (Profil) | Bottom nav tab 4 (`profile`) | Display/edit local profile name, avatar initial, library statistics, app info | AvatarCircle, EditableNameField, StatCardRow, AboutRow | default, editing-name, saved |
| S05 | Now Playing (Sedang Diputar) | Tap MiniPlayer from any tab | Full-screen playback controls, scrubbing, queue navigation, repeat/shuffle, favorite | TopDismissBar, LargeCoverArt, TrackMeta, SeekSlider, PlaybackControls, FavoriteButton | buffering, playing, paused, error |

## Flows

```
F01 Stream a track from Home
    S01 (tap TrackRow) -> MiniPlayer appears -> audio streams -> (optional) tap MiniPlayer -> S05 Now Playing
    happy path clicks: 1 (2 to open full player)
    edge: network unavailable / stream buffering / tapping already-playing track

F02 Search and play
    S02 (type query in SearchInput) -> live filtered list -> tap TrackRow -> MiniPlayer updates & plays
    happy path clicks: 2
    edge: empty query shows full list or prompt, no matches shows Indonesian empty state, leading/trailing spaces

F03 Create and populate a custom playlist
    S03 (tap "Buat Playlist") -> CreatePlaylistDialog (enter name, save) -> S01/S02/S03 (tap track menu -> "Tambah ke Playlist") -> select playlist
    happy path clicks: 4
    edge: blank playlist name blocked, duplicate track prevention, remove track from playlist detail

F04 Favorite a track and view in Library
    S01/S02/S05 (tap Heart icon) -> persisted immediately -> S03 ("Lagu Favorit") shows updated list -> S04 stats update
    happy path clicks: 1
    edge: unfavorite while viewing Lagu Favorit

F05 Edit profile name
    S04 (edit display name -> tap Simpan) -> persisted -> S01 greeting & S04 avatar initial update immediately
    happy path clicks: 2
    edge: blank display name falls back or validates
```

## Components

| component | variants | states | used on |
| --- | --- | --- | --- |
| BottomNavBar | 4-tab fixed | default, selected (violet accent) | S01, S02, S03, S04 |
| MiniPlayer | persistent bar above nav | hidden (no track), buffering, playing, paused | S01, S02, S03, S04 |
| TrackRow | standard, with playlist menu | default, active-playing, favorited | S01, S02, S03 |
| PlaylistCard | horizontal featured, list row | default, pressed | S01, S03 |
| SearchField | single-line clearable | empty, focused, filled | S02 |
| PlaybackControls | play/pause, prev/next, shuffle, repeat | off, all, one, disabled | S05 |
| SeekBar | slider with elapsed/total labels | idle, dragging, buffering | S05 |
| Dialog | create playlist, add-to-playlist picker | open, validation-error | S01, S02, S03 |

## Inferred data model

```
Track           id (string), number (int), title (string), artist (string), streamUrl (string), coverUrl (string), durationLabel (string)
                evidence: S01, S02, S05
                confidence: high

Playlist        id (string), name (string), isBuiltIn (boolean), trackIds (list<string>)
                evidence: S01 carousel, S03 library & detail
                confidence: high

UserPreferences profileName (string), favoriteTrackIds (set<string>), customPlaylists (list<Playlist>)
                evidence: S03, S04, S05
                confidence: high

PlayerState     currentTrack (Track?), queue (list<Track>), isPlaying (boolean), isBuffering (boolean), positionMs (long), durationMs (long), shuffleEnabled (boolean), repeatMode (OFF | ALL | ONE), errorMessage (string?)
                evidence: MiniPlayer, S05
                confidence: high
```

Relationships: Playlist n-n Track (via trackIds); UserPreferences 1-n custom Playlists; UserPreferences 1-n favorite Track IDs.

## Feature matrix

See `replica/features.csv`. Must: 14, should: 5, could: 1, skip: 3.

## Out of scope (cannot or should not be cloned)

- Commercial music catalogue / DRM streaming (replaced with 16 royalty-free SoundHelix demo tracks)
- Account registration, OAuth, cloud sync, backend servers (replaced with local SharedPreferences)
- Spotify brand identity, green color, logo, typography, and copy (clean-room white/violet rebrand "My Playlist")
- Social feeds, podcasts, offline encrypted downloads, billing/subscriptions

## Size

Screens: 5, flows: 5, entities: 4. Hard parts: keeping ExoPlayer state reactive and continuous across Compose screens, buffering/seek synchronization, local JSON persistence without Room. Size: S.
