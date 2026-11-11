TASK: Build My Playlist v1 — a Spotify-inspired Android music player, WHITE theme, own brand "My Playlist". Clean-room only: rebuild the flows/features, NEVER copy Spotify code, logo, name, artwork or catalogue. Work in small chunks. Do NOT use any subagent/task tool. Do NOT touch any .env or any other project on this server. When done, write BUILD_REPORT.md in the repo root and make sure the project compiles as far as possible locally (at minimum: validate Gradle wrapper/config files exist and are consistent; the real APK build runs in GitHub Actions).

USE THE REPLICA SKILLS (already installed project-local for opencode under .agents/skills/ — read them):
- replica-recon: write replica/recon.md (screens/flows of a generic music player: Home, Search, Library/Playlists, Profile, Now Playing) + replica/features.csv
- replica-architect: write replica/architecture.md with the stack below
- replica-design: write replica/design/tokens.json — WHITE background (#FFFFFF), near-black text (#111111), ONE accent that is NOT Spotify green (use violet #6D28D9), 8px spacing, rounded cards
- replica-build: build the app below, ticking features.csv
- replica-brand sweep: no string "Spotify", no Spotify logo/green anywhere in code or UI text except replica/recon.md may mention it as the reference. App name everywhere = "My Playlist".

STACK (fixed, do not change):
- Native Android, Kotlin, Jetpack Compose (Material 3), single app module, package id.myplaylist.app
- minSdk 26, targetSdk 34, compileSdk 34, JDK 17, Gradle wrapper 8.7, AGP 8.5.2, Kotlin 2.0.20
- Playback: androidx.media3 ExoPlayer (media3-exoplayer 1.4.1) streaming MP3 over HTTPS; simple in-app player state (no MediaSession service needed for v1, keep player in a singleton/repository held by the Application-level scope so playback continues across tabs)
- Images: Coil (io.coil-kt:coil-compose 2.7.0) with gradient placeholder fallback
- Local persistence: SharedPreferences via a small SettingsStore (favorites set, profile name, custom playlists as JSON) — no Room, no backend, no login server
- No API keys, no billing, no ads

DATA (free demo tracks only):
- 16 tracks from SoundHelix: https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3 .. Song-16.mp3, titles "SoundHelix Song 1..16", artist "T. Schürger (demo)". Cover art: https://picsum.photos/seed/myplaylistN/400/400 per track.
- Plus 3 built-in playlists grouping those tracks (e.g. "Fokus Santai" songs 1-6, "Semangat Pagi" 7-11, "Malam Tenang" 12-16).

SCREENS (bottom navigation: Home, Search, Library, Profile; MiniPlayer above the nav bar whenever a track is loaded; tap MiniPlayer opens full Now Playing):
1. Home: greeting with profile name, section "Daftar Lagu" — full track list (cover, title, artist, duration placeholder from player metadata or "demo", favorite heart toggle, tap = play), a horizontal row of the 3 built-in playlist cards.
2. Search: search field filtering tracks by title/artist live, empty-state text in Indonesian.
3. Library: built-in playlists + user playlists; "Buat Playlist" dialog (name), tap a playlist to open its track list, add/remove track to a user playlist from the track row menu (simple dialog), favorites section ("Lagu Favorit").
4. Profile: editable display name (saved), avatar circle with initial, stats (favorites count, playlists count, tracks count), "Tentang" line: My Playlist v1.
5. Now Playing (full screen): big cover, title/artist, seek bar with current/total time, play/pause, next/prev, shuffle, repeat (off/all/one), favorite toggle.
All UI text in Indonesian. White theme everywhere, violet accent, black text — no dark theme in v1.

GITHUB ACTIONS: .github/workflows/build-apk.yml — on push to main and workflow_dispatch: setup JDK 17 (temurin), run ./gradlew assembleDebug, upload artifact named "MyPlaylist-debug-apk" containing app/build/outputs/apk/debug/app-debug.apk. Add gradle wrapper by generating it if a gradle distribution is available, otherwise write wrapper files using the standard 8.7 distribution URL and include gradlew/gradlew.bat scripts from the official template (do not fake them).

README.md: what the app is, features, how to download the APK from Actions artifacts, tech stack, note that songs are free SoundHelix demo tracks.

TESTS/VERIFY: add a JVM unit test for the search filter and the playlist grouping logic (pure Kotlin functions). Write BUILD_REPORT.md: files created, replica docs produced, brand sweep result (grep for "spotify" outside replica/ must be empty), what is NOT verified (APK build itself runs in GitHub Actions after push).
