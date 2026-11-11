package id.myplaylist.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.myplaylist.app.model.Track
import id.myplaylist.app.ui.components.formatRemainingSeconds
import id.myplaylist.app.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsModalSheet(
    track: Track,
    positionMs: Long,
    durationMs: Long,
    onDismiss: () -> Unit
) {
    val lines = track.lyrics.ifEmpty {
        listOf(
            "Alunan musik demo menemani hari",
            "Melodi mengalir tenang dan jernih",
            "Setiap nada memberi ruang untuk fokus",
            "Nikmati musik tanpa gangguan iklan"
        )
    }

    val activeLineIndex = if (durationMs > 0L) {
        val fraction = (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 0.999f)
        (fraction * lines.size).toInt().coerceIn(0, lines.lastIndex)
    } else {
        0
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppColors.BgWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextMuted
                    )
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = AppColors.SurfaceAlt
                ) {
                    Text(
                        text = "Lirik demo",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.AccentViolet,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppColors.SurfaceLight)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                lines.forEachIndexed { idx, line ->
                    val isCurrent = idx == activeLineIndex
                    Text(
                        text = line,
                        fontSize = if (isCurrent) 20.sp else 16.sp,
                        lineHeight = if (isCurrent) 28.sp else 22.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                        color = if (isCurrent) AppColors.AccentViolet else AppColors.TextMuted.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SleepTimerDialog(
    activeMinutes: Int?,
    remainingSeconds: Int,
    isEndOfTrack: Boolean,
    onSelectMinutes: (Int?) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        5 to "5 menit",
        10 to "10 menit",
        15 to "15 menit",
        30 to "30 menit",
        45 to "45 menit",
        60 to "60 menit",
        0 to "Akhir lagu"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.BgWhite,
        title = { Text("Timer Tidur", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (activeMinutes != null || isEndOfTrack) {
                    val statusText = if (isEndOfTrack) {
                        "Aktif: Berhenti di akhir lagu ini"
                    } else {
                        "Aktif: Sisa ${formatRemainingSeconds(remainingSeconds)}"
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.AccentViolet,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                options.forEach { (mins, label) ->
                    val selected = (mins == 0 && isEndOfTrack) || (mins > 0 && activeMinutes == mins)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectMinutes(mins)
                                onDismiss()
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) AppColors.SurfaceAlt else AppColors.BgWhite
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (selected) AppColors.AccentViolet else AppColors.BorderLight
                        )
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (selected) AppColors.AccentViolet else AppColors.TextPrimary,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (activeMinutes != null || isEndOfTrack) {
                TextButton(onClick = {
                    onSelectMinutes(null)
                    onDismiss()
                }) {
                    Text("Matikan Timer", color = AppColors.DangerRed)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = AppColors.TextMuted)
            }
        }
    )
}

@Composable
fun PlaybackSpeedDialog(
    currentSpeed: Float,
    onSelectSpeed: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val speeds = listOf(0.8f, 1.0f, 1.25f, 1.5f)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.BgWhite,
        title = { Text("Kecepatan Putar", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                speeds.forEach { speed ->
                    val selected = kotlin.math.abs(currentSpeed - speed) < 0.01f
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectSpeed(speed)
                                onDismiss()
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) AppColors.SurfaceAlt else AppColors.BgWhite
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (selected) AppColors.AccentViolet else AppColors.BorderLight
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${speed}x",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (selected) AppColors.AccentViolet else AppColors.TextPrimary
                            )
                            Text(
                                text = when (speed) {
                                    0.8f -> "Lebih lambat"
                                    1.0f -> "Normal"
                                    1.25f -> "Agak cepat"
                                    else -> "Cepat"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.TextMuted
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
