package id.myplaylist.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.myplaylist.app.data.TrackCatalogue
import id.myplaylist.app.model.Playlist
import id.myplaylist.app.model.Track
import id.myplaylist.app.ui.components.CoverArt
import id.myplaylist.app.ui.theme.AppColors

@Composable
fun ProfileScreen(
    profileName: String,
    favoriteIds: Set<String>,
    customPlaylists: List<Playlist>,
    recentlyPlayedIds: List<String>,
    playCounts: Map<String, Int>,
    totalPlays: Int,
    totalListenedMs: Long,
    crossfadeSeconds: Int,
    onSaveProfileName: (String) -> Unit,
    onSetCrossfadeSeconds: (Int) -> Unit,
    onOpenHistory: () -> Unit,
    onPlayTrack: (Track, List<Track>, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var editedName by remember(profileName) { mutableStateOf(profileName) }
    var savedNotice by remember { mutableStateOf(false) }
    var sliderCrossfade by remember(crossfadeSeconds) { mutableFloatStateOf(crossfadeSeconds.toFloat()) }

    val initial = remember(profileName) {
        profileName.trim().firstOrNull()?.uppercase() ?: "P"
    }

    val totalPlaylists = TrackCatalogue.builtInPlaylists.size + customPlaylists.size
    val approxMinutes = (totalListenedMs / 60000L).coerceAtLeast(0L)

    val topTracks = remember(playCounts) {
        TrackCatalogue.tracks
            .filter { (playCounts[it.id] ?: 0) > 0 }
            .sortedByDescending { playCounts[it.id] ?: 0 }
            .take(3)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(AppColors.AccentViolet),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = profileName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Pengguna My Playlist",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextMuted
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Stats Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(title = "Total Putar", value = "$totalPlays", modifier = Modifier.weight(1f))
                StatCard(title = "Menit Dengar", value = "~$approxMinutes", modifier = Modifier.weight(1f))
                StatCard(title = "Favorit", value = "${favoriteIds.size}", modifier = Modifier.weight(1f))
                StatCard(title = "Playlist", value = "$totalPlaylists", modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // History shortcut card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenHistory),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceLight),
                border = BorderStroke(1.dp, AppColors.BorderLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.History, null, tint = AppColors.AccentViolet)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Riwayat Putar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("${recentlyPlayedIds.size} lagu terakhir diputar", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextMuted)
                        }
                    }
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = AppColors.TextMuted)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Top 3 Tracks
        if (topTracks.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "3 Lagu Teratas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(topTracks, key = { "top_${it.id}" }) { track ->
                val plays = playCounts[track.id] ?: 0
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onPlayTrack(track, topTracks, "3 Lagu Teratas") },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.BgWhite),
                    border = BorderStroke(1.dp, AppColors.BorderLight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CoverArt(track.coverUrl, track.number, 40.dp, 8.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(track.title, style = MaterialTheme.typography.titleMedium)
                            Text("${track.artist} • $plays kali diputar", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextMuted)
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // Crossfade Settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceLight),
                border = BorderStroke(1.dp, AppColors.BorderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Tune, null, tint = AppColors.AccentViolet)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Crossfade Audio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                        Text(
                            text = if (sliderCrossfade.toInt() == 0) "Mati" else "${sliderCrossfade.toInt()} dtk",
                            style = MaterialTheme.typography.titleMedium,
                            color = AppColors.AccentViolet,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Transisi volume mulus antar perpindahan lagu saat memutar musik.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = sliderCrossfade,
                        onValueChange = { sliderCrossfade = it },
                        onValueChangeFinished = { onSetCrossfadeSeconds(sliderCrossfade.toInt()) },
                        valueRange = 0f..12f,
                        steps = 11,
                        colors = SliderDefaults.colors(
                            thumbColor = AppColors.AccentViolet,
                            activeTrackColor = AppColors.AccentViolet,
                            inactiveTrackColor = AppColors.BorderLight
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Edit Profile Name
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceLight),
                border = BorderStroke(1.dp, AppColors.BorderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ubah Nama Tampilan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = {
                            editedName = it
                            savedNotice = false
                        },
                        leadingIcon = {
                            Icon(Icons.Rounded.Person, contentDescription = null, tint = AppColors.TextMuted)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.AccentViolet,
                            unfocusedBorderColor = AppColors.BorderLight,
                            focusedContainerColor = AppColors.BgWhite,
                            unfocusedContainerColor = AppColors.BgWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            onSaveProfileName(editedName)
                            savedNotice = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.AccentViolet,
                            contentColor = AppColors.OnAccent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simpan Nama")
                    }

                    if (savedNotice) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CheckCircle, null, tint = AppColors.AccentViolet, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Nama profil berhasil disimpan",
                                style = MaterialTheme.typography.labelMedium,
                                color = AppColors.AccentViolet
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // About Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.BgWhite),
                border = BorderStroke(1.dp, AppColors.BorderLight)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Rounded.Info, null, tint = AppColors.AccentViolet, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Tentang My Playlist v1", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Aplikasi pemutar musik clean-room bebas iklan bertema putih & violet. Memutar audio demo SoundHelix (T. Schürger). Tidak menyediakan fitur berbayar/Premium seperti download offline atau tier lossless.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextMuted
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceLight),
        border = BorderStroke(1.dp, AppColors.BorderLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.AccentViolet
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.TextMuted,
                maxLines = 1
            )
        }
    }
}
