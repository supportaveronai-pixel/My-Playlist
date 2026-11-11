package id.myplaylist.app.ui.components

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import id.myplaylist.app.model.Album
import id.myplaylist.app.model.Playlist
import id.myplaylist.app.model.Track
import id.myplaylist.app.ui.theme.AppColors
import id.myplaylist.app.ui.theme.AppSpace

val PlaylistGradientColors: List<List<Color>> = listOf(
    listOf(Color(0xFF6D28D9), Color(0xFFA78BFA)), // 0: Violet
    listOf(Color(0xFF4338CA), Color(0xFF818CF8)), // 1: Indigo
    listOf(Color(0xFFBE185D), Color(0xFFF472B6)), // 2: Rose
    listOf(Color(0xFF0F766E), Color(0xFF2DD4BF)), // 3: Teal
    listOf(Color(0xFFB45309), Color(0xFFFBBF24)), // 4: Amber
    listOf(Color(0xFF334155), Color(0xFF94A3B8))  // 5: Slate
)

fun genreGradient(genre: String): List<Color> {
    return when (genre) {
        "Elektronik" -> PlaylistGradientColors[0]
        "Akustik" -> PlaylistGradientColors[3]
        "Sinematik" -> PlaylistGradientColors[1]
        "Santai" -> PlaylistGradientColors[4]
        "Fokus" -> PlaylistGradientColors[2]
        else -> PlaylistGradientColors[5]
    }
}

@Composable
fun CoverArt(
    coverUrl: String,
    seedNumber: Int,
    size: Dp,
    cornerRadius: Dp = 12.dp,
    modifier: Modifier = Modifier
) {
    val palette = remember(seedNumber) {
        PlaylistGradientColors[seedNumber.mod(PlaylistGradientColors.size)]
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Brush.linearGradient(palette)),
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = coverUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = { GradientNotePlaceholder(palette = palette) },
            error = { GradientNotePlaceholder(palette = palette) }
        )
    }
}

@Composable
private fun GradientNotePlaceholder(palette: List<Color>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(palette)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.MusicNote,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.92f),
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun PlaylistGradientCover(
    colorIndex: Int,
    size: Dp,
    cornerRadius: Dp = 12.dp,
    icon: ImageVector = Icons.Rounded.QueueMusic,
    modifier: Modifier = Modifier
) {
    val palette = PlaylistGradientColors[colorIndex.coerceIn(0, PlaylistGradientColors.lastIndex)]
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Brush.linearGradient(palette)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(size * 0.45f)
        )
    }
}

@Composable
fun TrackRow(
    track: Track,
    isCurrentTrack: Boolean,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: (() -> Unit)? = null,
    onPlayNext: (() -> Unit)? = null,
    onAddToQueue: (() -> Unit)? = null,
    onStartRadio: (() -> Unit)? = null,
    onOpenAlbum: (() -> Unit)? = null,
    onOpenArtist: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
    onRemoveFromPlaylist: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val hasAnyMenuAction = onAddToPlaylist != null ||
        onPlayNext != null ||
        onAddToQueue != null ||
        onStartRadio != null ||
        onOpenAlbum != null ||
        onOpenArtist != null ||
        onShare != null ||
        onMoveUp != null ||
        onMoveDown != null ||
        onRemoveFromPlaylist != null

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentTrack) AppColors.SurfaceAlt else AppColors.BgWhite
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isCurrentTrack) AppColors.AccentViolet.copy(alpha = 0.35f) else AppColors.BorderLight
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CoverArt(
                coverUrl = track.coverUrl,
                seedNumber = track.number,
                size = 52.dp,
                cornerRadius = 10.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isCurrentTrack) AppColors.AccentViolet else AppColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${track.artist} • ${track.genre}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isCurrentTrack) {
                    Text(
                        text = if (isPlaying) "Sedang diputar" else "Dijeda",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.AccentViolet
                    )
                }
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = if (isFavorite) "Hapus dari favorit" else "Tambah ke favorit",
                    tint = if (isFavorite) AppColors.AccentViolet else AppColors.TextMuted
                )
            }

            if (hasAnyMenuAction) {
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "Menu lagu",
                            tint = AppColors.TextMuted
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        containerColor = AppColors.BgWhite
                    ) {
                        onAddToPlaylist?.let { action ->
                            DropdownMenuItem(
                                text = { Text("Tambah ke Playlist", color = AppColors.TextPrimary) },
                                leadingIcon = { Icon(Icons.Rounded.PlaylistAdd, null, tint = AppColors.AccentViolet) },
                                onClick = { menuExpanded = false; action() }
                            )
                        }
                        onPlayNext?.let { action ->
                            DropdownMenuItem(
                                text = { Text("Putar Berikutnya", color = AppColors.TextPrimary) },
                                leadingIcon = { Icon(Icons.Rounded.PlaylistPlay, null, tint = AppColors.AccentViolet) },
                                onClick = { menuExpanded = false; action() }
                            )
                        }
                        onAddToQueue?.let { action ->
                            DropdownMenuItem(
                                text = { Text("Tambah ke Antrean", color = AppColors.TextPrimary) },
                                leadingIcon = { Icon(Icons.Rounded.QueueMusic, null, tint = AppColors.AccentViolet) },
                                onClick = { menuExpanded = false; action() }
                            )
                        }
                        onStartRadio?.let { action ->
                            DropdownMenuItem(
                                text = { Text("Radio Lagu", color = AppColors.TextPrimary) },
                                leadingIcon = { Icon(Icons.Rounded.Radio, null, tint = AppColors.AccentViolet) },
                                onClick = { menuExpanded = false; action() }
                            )
                        }
                        onOpenAlbum?.let { action ->
                            DropdownMenuItem(
                                text = { Text("Lihat Album", color = AppColors.TextPrimary) },
                                leadingIcon = { Icon(Icons.Rounded.Album, null, tint = AppColors.TextMuted) },
                                onClick = { menuExpanded = false; action() }
                            )
                        }
                        onOpenArtist?.let { action ->
                            DropdownMenuItem(
                                text = { Text("Lihat Artis", color = AppColors.TextPrimary) },
                                leadingIcon = { Icon(Icons.Rounded.Person, null, tint = AppColors.TextMuted) },
                                onClick = { menuExpanded = false; action() }
                            )
                        }
                        onShare?.let { action ->
                            DropdownMenuItem(
                                text = { Text("Bagikan", color = AppColors.TextPrimary) },
                                leadingIcon = { Icon(Icons.Rounded.Share, null, tint = AppColors.TextMuted) },
                                onClick = { menuExpanded = false; action() }
                            )
                        }
                        if (onMoveUp != null || onMoveDown != null || onRemoveFromPlaylist != null) {
                            HorizontalDivider(color = AppColors.BorderLight)
                        }
                        onMoveUp?.let { action ->
                            DropdownMenuItem(
                                text = { Text("Geser ke Atas", color = AppColors.TextPrimary) },
                                leadingIcon = { Icon(Icons.Rounded.ArrowUpward, null, tint = AppColors.TextMuted) },
                                onClick = { menuExpanded = false; action() }
                            )
                        }
                        onMoveDown?.let { action ->
                            DropdownMenuItem(
                                text = { Text("Geser ke Bawah", color = AppColors.TextPrimary) },
                                leadingIcon = { Icon(Icons.Rounded.ArrowDownward, null, tint = AppColors.TextMuted) },
                                onClick = { menuExpanded = false; action() }
                            )
                        }
                        onRemoveFromPlaylist?.let { action ->
                            DropdownMenuItem(
                                text = { Text("Hapus dari Playlist", color = AppColors.DangerRed) },
                                leadingIcon = { Icon(Icons.Rounded.DeleteOutline, null, tint = AppColors.DangerRed) },
                                onClick = { menuExpanded = false; action() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistFeaturedCard(
    playlist: Playlist,
    leadTrack: Track?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceLight),
        border = BorderStroke(1.dp, AppColors.BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (leadTrack != null) {
                CoverArt(
                    coverUrl = leadTrack.coverUrl,
                    seedNumber = leadTrack.number,
                    size = 136.dp,
                    cornerRadius = 12.dp
                )
            } else {
                PlaylistGradientCover(
                    colorIndex = playlist.colorIndex,
                    size = 136.dp,
                    cornerRadius = 12.dp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${playlist.trackIds.size} lagu",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextMuted
            )
        }
    }
}

@Composable
fun AlbumFeaturedCard(
    album: Album,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceLight),
        border = BorderStroke(1.dp, AppColors.BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            CoverArt(
                coverUrl = album.coverUrl,
                seedNumber = album.id.hashCode(),
                size = 136.dp,
                cornerRadius = 12.dp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = album.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${album.artist} • ${album.year}",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun GenreTileCard(
    genre: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = genreGradient(genre)
    Box(
        modifier = modifier
            .height(76.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(colors))
            .clickable(onClick = onClick)
            .padding(14.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Text(
            text = genre,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CreatePlaylistDialog(
    onCreate: (name: String, colorIndex: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableIntStateOf(0) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.BgWhite,
        title = {
            Text(
                text = "Buat Playlist Baru",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        showError = false
                    },
                    label = { Text("Nama playlist") },
                    placeholder = { Text("Misalnya: Lagu Kerja") },
                    singleLine = true,
                    isError = showError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (showError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Nama playlist tidak boleh kosong",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.DangerRed
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Pilih Warna Sampul",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PlaylistGradientColors.forEachIndexed { idx, colors ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(colors))
                                .border(
                                    width = if (selectedColor == idx) 3.dp else 0.dp,
                                    color = if (selectedColor == idx) AppColors.TextPrimary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = idx },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == idx) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = name.trim()
                    if (trimmed.isEmpty()) {
                        showError = true
                    } else {
                        onCreate(trimmed, selectedColor)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.AccentViolet,
                    contentColor = AppColors.OnAccent
                )
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = AppColors.TextMuted)
            }
        }
    )
}

@Composable
fun RenamePlaylistDialog(
    currentName: String,
    onRename: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember(currentName) { mutableStateOf(currentName) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.BgWhite,
        title = { Text("Ubah Nama Playlist", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        showError = false
                    },
                    label = { Text("Nama baru") },
                    singleLine = true,
                    isError = showError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (showError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Nama tidak boleh kosong",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.DangerRed
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = name.trim()
                    if (trimmed.isEmpty()) showError = true else onRename(trimmed)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.AccentViolet,
                    contentColor = AppColors.OnAccent
                )
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = AppColors.TextMuted)
            }
        }
    )
}

@Composable
fun ColorPickerDialog(
    currentColorIndex: Int,
    onSelectColor: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.BgWhite,
        title = { Text("Pilih Warna Sampul", style = MaterialTheme.typography.titleLarge) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PlaylistGradientColors.forEachIndexed { idx, colors ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(colors))
                            .border(
                                width = if (currentColorIndex == idx) 3.dp else 0.dp,
                                color = if (currentColorIndex == idx) AppColors.TextPrimary else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable {
                                onSelectColor(idx)
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentColorIndex == idx) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = AppColors.TextMuted)
            }
        }
    )
}

@Composable
fun AddToPlaylistDialog(
    track: Track,
    userPlaylists: List<Playlist>,
    onSelectPlaylist: (Playlist) -> Unit,
    onCreateNewPlaylist: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.BgWhite,
        title = {
            Text(
                text = "Tambah ke Playlist",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.AccentViolet,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (userPlaylists.isEmpty()) {
                    Text(
                        text = "Kamu belum punya playlist sendiri. Buat playlist baru terlebih dahulu.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextMuted
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(200.dp),
                        verticalArrangement = Arrangement.spacedBy(AppSpace.s1)
                    ) {
                        items(userPlaylists, key = { it.id }) { playlist ->
                            val alreadyAdded = track.id in playlist.trackIds
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !alreadyAdded) {
                                        onSelectPlaylist(playlist)
                                    },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (alreadyAdded) AppColors.SurfaceLight else AppColors.BgWhite
                                ),
                                border = BorderStroke(1.dp, AppColors.BorderLight)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PlaylistGradientCover(
                                        colorIndex = playlist.colorIndex,
                                        size = 36.dp,
                                        cornerRadius = 8.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = playlist.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${playlist.trackIds.size} lagu",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                    if (alreadyAdded) {
                                        Text(
                                            text = "Sudah ada",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = AppColors.AccentViolet
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onCreateNewPlaylist,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, AppColors.AccentViolet)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = AppColors.AccentViolet
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Buat Playlist Baru", color = AppColors.AccentViolet)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = AppColors.TextMuted)
            }
        }
    )
}

@Composable
fun SplashOverlay(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = AppColors.BgWhite
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(AppColors.SurfaceAlt),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.QueueMusic,
                        contentDescription = null,
                        tint = AppColors.AccentViolet,
                        modifier = Modifier.size(34.dp)
                    )
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = AppColors.AccentViolet,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "My Playlist",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Musik Demo Bebas Royalti",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextMuted
            )
        }
    }
}

fun shareTextContent(context: Context, subject: String, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Bagikan melalui"))
}

fun openSystemEqualizer(context: Context, audioSessionId: Int) {
    val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
        putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
        putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
        putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
    }
    try {
        if (context is Activity) {
            context.startActivityForResult(intent, 0)
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(
            context,
            "Equalizer sistem tidak tersedia di perangkat ini",
            Toast.LENGTH_SHORT
        ).show()
    } catch (_: Exception) {
        Toast.makeText(
            context,
            "Tidak dapat membuka equalizer sistem",
            Toast.LENGTH_SHORT
        ).show()
    }
}

fun formatTimeMs(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val totalSeconds = ms / 1000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(minutes, seconds)
}

fun formatRemainingSeconds(sec: Int): String {
    if (sec <= 0) return "0:00"
    val m = sec / 60
    val s = sec % 60
    return "%d:%02d".format(m, s)
}
