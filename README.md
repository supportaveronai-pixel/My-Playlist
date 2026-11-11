# My Playlist v1 (Stage 2)

Aplikasi pemutar musik Android clean-room dengan tema putih bersih (`#FFFFFF`), teks hitam pekat (`#111111`), aksen ungu violet (`#6D28D9`), dan antarmuka lengkap berbahasa Indonesia.

## Fitur Utama

### Beranda (`Beranda`)
- Sapaan personal sesuai nama profil dan pintasan menuju **Riwayat Putar**.
- **Lompat Kembali**: Deretan lagu yang baru saja diputar (tersimpan hingga 50 lagu terakhir).
- **Dibuat Untuk Kamu**: 4 mix otomatis lokal (`Daily Mix 1`, `Daily Mix 2`, `Discover Mix`, `Release Mix`) yang disusun secara deterministik dari tag genre dan jumlah putar lagu.
- **Jelajahi Genre**: Kartu genre (`Elektronik`, `Akustik`, `Sinematik`, `Santai`, `Fokus`, `Malam`) menuju halaman lagu per genre.
- **Playlist Pilihan** & **Album Pilihan** (`Helix Sessions Vol. 1`, `Vol. 2`, `Vol. 3`).
- **Daftar Lagu**: Seluruh 16 trek demo dengan menu lengkap (*Tambah ke Playlist*, *Putar Berikutnya*, *Tambah ke Antrean*, *Radio Lagu*, *Lihat Album*, *Lihat Artis*, *Bagikan*).

### Pencarian (`Cari`)
- Tab filter hasil pencarian: `Semua`, `Lagu`, `Album`, `Artis`, `Playlist`, `Genre`.
- Riwayat pencarian tersimpan lokal dengan tombol **Hapus riwayat pencarian**.
- Grid kategori genre saat kolom pencarian kosong.

### Album, Artis, & Genre
- **Halaman Album**: Sampul album, tahun rilis `2024 (demo)`, tombol *Putar Semua*, *Putar Acak*, favorit album, dan bagikan.
- **Halaman Artis**: Tombol *Ikuti Artis / Berhenti Mengikuti* yang tersimpan, *Lagu Populer* berdasarkan jumlah putar, *Diskografi* (3 album), dan bio *Tentang Artis*.
- **Halaman Genre**: Banner gradien dan daftar lagu berdasarkan genre.

### Koleksi (`Koleksi`)
- Chip filter: `Semua`, `Playlist`, `Album`, `Artis`, `Favorit`.
- Menu urutkan: `Terbaru`, `A-Z`, `Paling Sering Diputar`.
- **Sematkan Playlist** (*Pin*) agar selalu tampil di bagian atas.
- Kelola playlist kustom: buat playlist dengan **pilihan 1 dari 6 warna gradien sampul**, ubah nama, ubah warna sampul, hapus playlist, serta atur urutan lagu (*geser atas/bawah*, *hapus lagu*).
- Menampilkan *Lagu Favorit*, *Album Favorit*, dan *Artis yang Diikuti*.

### Pemutar Audio, Antrean, Lirik, & Profil
- **MiniPlayer** persisten dan layar penuh **Sedang Diputar** dengan seek bar, play/pause, next/prev, shuffle, repeat (Off/All/One).
- **Antrean (`Antrean`)**: Menampilkan *Sedang Diputar*, *Antrean Berikutnya* (yang ditambahkan pengguna), dan *Berikutnya Dari* (sumber playlist/album), lengkap dengan hapus item & *Bersihkan antrean*.
- **Lirik Demo (`Lirik demo`)**: Lembar lirik orisinal per album dengan sorotan baris aktif mengikuti progres pemutaran lagu.
- **Ekstra Pemutar**: *Timer Tidur* (5/10/15/30/45/60 menit & akhir lagu dengan hitung mundur aktif), pemilih *Kecepatan Putar* (`0.8x`, `1.0x`, `1.25x`, `1.5x`), tombol *Equalizer* (intent sistem Android dengan fallback aman), *Radio Lagu* (antrean otomatis se-genre), dan tombol *Bagikan*.
- **Profil & Riwayat**: Statistik lengkap (*Total Putar*, *Perkiraan Menit Dengar*, *Favorit*, *Playlist*), *3 Lagu Teratas*, halaman *Riwayat Putar* dengan hapus riwayat, pengaturan *Crossfade Audio* (`0–12 detik`), serta edit nama profil.

## Fitur Premium yang Dikecualikan (EXCLUDED)

Sesuai spesifikasi pengembangan bersih (*free-tier only*), fitur-fitur berbayar/Premium berikut **TIDAK** dibangun dan dikecualikan dari aplikasi:
- **Mode Download / Offline**: Tidak ada pengunduhan file audio atau cache terenkripsi untuk pemutaran luring.
- **Kualitas Audio Sangat Tinggi / Tier Lossless**: Tidak ada pengatur bitrate berbayar atau streaming lossless.
- **Kontrol Antrean Premium Berbayar** di luar fitur dasar antrean.
- **Tanpa Iklan & Tanpa Paywall**: Aplikasi ini secara alami tidak memiliki iklan, tidak membatasi *skip*, dan tidak menampilkan layar upsell/berlangganan palsu.
- *(Catatan di luar cakupan server/lisensi: Podcast, Audiobooks, Jam/Group Session & Connect-to-device, serta AI DJ).*

## Sumber Audio Demo

Seluruh 16 trek audio di aplikasi ini adalah trek demo bebas royalti dari **SoundHelix** (`SoundHelix-Song-1.mp3` hingga `SoundHelix-Song-16.mp3`, oleh T. Schürger) yang diputar langsung melalui HTTPS:
- Audio: `https://www.soundhelix.com/examples/mp3/SoundHelix-Song-N.mp3`
- Gambar sampul: `https://picsum.photos/seed/myplaylistN/400/400` dengan placeholder gradien otomatis.

## Stack Teknologi

- **Bahasa & UI**: Kotlin `2.0.20`, Jetpack Compose (Material 3), Single-Activity (`id.myplaylist.app`)
- **SDK**: `minSdk 26`, `targetSdk 34`, `compileSdk 34`, JDK 17
- **Build**: Gradle Wrapper `8.7`, Android Gradle Plugin `8.5.2`
- **Pemutar Audio**: `androidx.media3:media3-exoplayer:1.4.1`
- **Gambar**: `io.coil-kt:coil-compose:2.7.0`
- **Penyimpanan Lokal**: `SharedPreferences` (`SettingsStore`) — tanpa backend, tanpa login, tanpa API key, tanpa iklan.

## Cara Mengunduh APK dari GitHub Actions

1. Buka tab **Actions** pada repositori GitHub ini.
2. Pilih workflow **Build Debug APK** pada run terbaru yang berstatus hijau (*success*).
3. Unduh artifact **`MyPlaylist-debug-apk`** yang berisi `app-debug.apk`.
4. Ekstrak file ZIP dan pasang `app-debug.apk` di perangkat Android (Android 8.0 / API 26+).

## Pengujian Unit Lokal

```bash
./gradlew testDebugUnitTest
```
