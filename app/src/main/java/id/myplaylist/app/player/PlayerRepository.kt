package id.myplaylist.app.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import id.myplaylist.app.data.SettingsStore
import id.myplaylist.app.model.PlayerState
import id.myplaylist.app.model.RepeatMode
import id.myplaylist.app.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@UnstableApi
class PlayerRepository(
    context: Context,
    private val settingsStore: SettingsStore
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context.applicationContext).build()

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var tickerJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var fadeJob: Job? = null
    private var lastRecordedTrackId: String? = null

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startTicker()
                } else {
                    stopTicker()
                    syncProgress(addListenedDelta = false)
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                val buffering = playbackState == Player.STATE_BUFFERING
                val duration = exoPlayer.duration.takeIf { it > 0 } ?: _state.value.durationMs
                _state.update {
                    it.copy(
                        isBuffering = buffering,
                        durationMs = duration
                    )
                }
                if (playbackState == Player.STATE_READY) {
                    syncProgress(addListenedDelta = false)
                } else if (playbackState == Player.STATE_ENDED) {
                    cancelSleepTimer()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (_state.value.isSleepAtTrackEnd &&
                    reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO
                ) {
                    exoPlayer.pause()
                    cancelSleepTimer()
                }

                val idx = exoPlayer.currentMediaItemIndex
                val queue = _state.value.queue
                val nextTrack = queue.getOrNull(idx)
                val duration = exoPlayer.duration.takeIf { it > 0 } ?: 0L

                val updatedUpNext = _state.value.upNextQueue.toMutableList()
                if (nextTrack != null && updatedUpNext.firstOrNull()?.id == nextTrack.id) {
                    updatedUpNext.removeAt(0)
                }

                _state.update {
                    it.copy(
                        currentTrack = nextTrack ?: it.currentTrack,
                        currentIndex = idx,
                        upNextQueue = updatedUpNext,
                        positionMs = 0L,
                        durationMs = duration,
                        errorMessage = null
                    )
                }

                if (nextTrack != null && nextTrack.id != lastRecordedTrackId) {
                    lastRecordedTrackId = nextTrack.id
                    settingsStore.recordTrackPlay(nextTrack.id)
                    applyCrossfadeRampIfEnabled()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _state.update {
                    it.copy(
                        isPlaying = false,
                        isBuffering = false,
                        errorMessage = "Gagal memutar audio. Periksa koneksi internet Anda."
                    )
                }
            }
        })
    }

    fun playTrack(
        track: Track,
        queue: List<Track>,
        sourceLabel: String = "Daftar Lagu",
        shuffle: Boolean = false
    ) {
        val effectiveQueue = if (queue.any { it.id == track.id }) queue else listOf(track)
        val startIndex = effectiveQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        val mediaItems = effectiveQueue.map { item ->
            MediaItem.Builder()
                .setMediaId(item.id)
                .setUri(item.streamUrl)
                .build()
        }

        lastRecordedTrackId = track.id
        settingsStore.recordTrackPlay(track.id)

        if (shuffle != _state.value.shuffleEnabled) {
            exoPlayer.shuffleModeEnabled = shuffle
        }

        _state.update {
            it.copy(
                currentTrack = track,
                queue = effectiveQueue,
                upNextQueue = emptyList(),
                sourceLabel = sourceLabel,
                currentIndex = startIndex,
                positionMs = 0L,
                durationMs = 0L,
                isBuffering = true,
                shuffleEnabled = shuffle,
                errorMessage = null
            )
        }

        exoPlayer.setMediaItems(mediaItems, startIndex, 0L)
        exoPlayer.prepare()
        applyCrossfadeRampIfEnabled()
        exoPlayer.play()
    }

    fun playNext(track: Track) {
        if (_state.value.currentTrack == null) {
            playTrack(track, listOf(track), "Antrean")
            return
        }
        val currentIdx = exoPlayer.currentMediaItemIndex.coerceAtLeast(0)
        val insertIdx = (currentIdx + 1).coerceAtMost(exoPlayer.mediaItemCount)
        val mediaItem = MediaItem.Builder()
            .setMediaId(track.id)
            .setUri(track.streamUrl)
            .build()
        exoPlayer.addMediaItem(insertIdx, mediaItem)

        _state.update { state ->
            val mutableQueue = state.queue.toMutableList()
            val qInsertIdx = (state.currentIndex + 1).coerceIn(0, mutableQueue.size)
            mutableQueue.add(qInsertIdx, track)
            state.copy(
                queue = mutableQueue,
                upNextQueue = listOf(track) + state.upNextQueue
            )
        }
    }

    fun addToQueue(track: Track) {
        if (_state.value.currentTrack == null) {
            playTrack(track, listOf(track), "Antrean")
            return
        }
        val currentIdx = exoPlayer.currentMediaItemIndex.coerceAtLeast(0)
        val upNextCount = _state.value.upNextQueue.size
        val insertIdx = (currentIdx + 1 + upNextCount).coerceAtMost(exoPlayer.mediaItemCount)
        val mediaItem = MediaItem.Builder()
            .setMediaId(track.id)
            .setUri(track.streamUrl)
            .build()
        exoPlayer.addMediaItem(insertIdx, mediaItem)

        _state.update { state ->
            val mutableQueue = state.queue.toMutableList()
            val qInsertIdx = (state.currentIndex + 1 + upNextCount).coerceIn(0, mutableQueue.size)
            mutableQueue.add(qInsertIdx, track)
            state.copy(
                queue = mutableQueue,
                upNextQueue = state.upNextQueue + track
            )
        }
    }

    fun removeFromQueueAt(queueIndex: Int) {
        val currentIdx = _state.value.currentIndex
        if (queueIndex <= currentIdx || queueIndex !in _state.value.queue.indices) return
        if (queueIndex < exoPlayer.mediaItemCount) {
            exoPlayer.removeMediaItem(queueIndex)
        }
        _state.update { state ->
            val mutableQueue = state.queue.toMutableList()
            val removed = mutableQueue.removeAt(queueIndex)
            val mutableUpNext = state.upNextQueue.toMutableList()
            val upNextOffset = queueIndex - (currentIdx + 1)
            if (upNextOffset in mutableUpNext.indices && mutableUpNext[upNextOffset].id == removed.id) {
                mutableUpNext.removeAt(upNextOffset)
            }
            state.copy(queue = mutableQueue, upNextQueue = mutableUpNext)
        }
    }

    fun clearQueue() {
        val currentIdx = exoPlayer.currentMediaItemIndex
        val currentTrack = _state.value.currentTrack
        if (currentIdx >= 0 && exoPlayer.mediaItemCount > currentIdx + 1) {
            exoPlayer.removeMediaItems(currentIdx + 1, exoPlayer.mediaItemCount)
        }
        _state.update { state ->
            val kept = if (currentTrack != null && currentIdx in state.queue.indices) {
                state.queue.subList(0, currentIdx + 1)
            } else {
                listOfNotNull(currentTrack)
            }
            state.copy(
                queue = kept,
                upNextQueue = emptyList()
            )
        }
    }

    fun playSongRadio(seedTrack: Track, allTracks: List<Track>) {
        val sameGenre = allTracks.filter { it.id != seedTrack.id && it.genre == seedTrack.genre }
        val otherTracks = allTracks.filter { it.id != seedTrack.id && it.genre != seedTrack.genre }
        val radioQueue = listOf(seedTrack) + sameGenre + otherTracks
        playTrack(
            track = seedTrack,
            queue = radioQueue,
            sourceLabel = "Radio Lagu: ${seedTrack.genre}"
        )
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            if (exoPlayer.playbackState == Player.STATE_IDLE && _state.value.currentTrack != null) {
                exoPlayer.prepare()
            } else if (exoPlayer.playbackState == Player.STATE_ENDED) {
                exoPlayer.seekTo(0L)
            }
            exoPlayer.play()
        }
    }

    fun seekTo(positionMs: Long) {
        val clamped = positionMs.coerceIn(0L, _state.value.durationMs.coerceAtLeast(0L))
        exoPlayer.seekTo(clamped)
        _state.update { it.copy(positionMs = clamped) }
    }

    fun nextTrack() {
        val queue = _state.value.queue
        if (queue.isEmpty()) return
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
            exoPlayer.play()
        } else {
            exoPlayer.seekTo(0, 0L)
            exoPlayer.play()
        }
    }

    fun previousTrack() {
        val queue = _state.value.queue
        if (queue.isEmpty()) return
        if (exoPlayer.currentPosition > 3_000L) {
            exoPlayer.seekTo(0L)
            _state.update { it.copy(positionMs = 0L) }
        } else if (exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekToPreviousMediaItem()
            exoPlayer.play()
        } else {
            exoPlayer.seekTo(0L)
            _state.update { it.copy(positionMs = 0L) }
        }
    }

    fun toggleShuffle() {
        val next = !_state.value.shuffleEnabled
        exoPlayer.shuffleModeEnabled = next
        _state.update { it.copy(shuffleEnabled = next) }
    }

    fun cycleRepeatMode() {
        val next = when (_state.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        exoPlayer.repeatMode = when (next) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
        _state.update { it.copy(repeatMode = next) }
    }

    fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.5f, 2.0f)
        exoPlayer.setPlaybackSpeed(clamped)
        _state.update { it.copy(playbackSpeed = clamped) }
    }

    fun setSleepTimerMinutes(minutes: Int?) {
        sleepTimerJob?.cancel()
        sleepTimerJob = null

        if (minutes == null) {
            _state.update {
                it.copy(
                    sleepTimerMinutes = null,
                    sleepTimerRemainingSec = 0,
                    isSleepAtTrackEnd = false
                )
            }
            return
        }

        if (minutes <= 0) {
            // End of current track
            _state.update {
                it.copy(
                    sleepTimerMinutes = null,
                    sleepTimerRemainingSec = 0,
                    isSleepAtTrackEnd = true
                )
            }
            return
        }

        val totalSeconds = minutes * 60
        _state.update {
            it.copy(
                sleepTimerMinutes = minutes,
                sleepTimerRemainingSec = totalSeconds,
                isSleepAtTrackEnd = false
            )
        }

        sleepTimerJob = scope.launch {
            var remaining = totalSeconds
            while (remaining > 0 && isActive) {
                delay(1000L)
                remaining -= 1
                _state.update { it.copy(sleepTimerRemainingSec = remaining) }
            }
            if (isActive && remaining <= 0) {
                exoPlayer.pause()
                _state.update {
                    it.copy(
                        sleepTimerMinutes = null,
                        sleepTimerRemainingSec = 0,
                        isSleepAtTrackEnd = false
                    )
                }
            }
        }
    }

    fun cancelSleepTimer() {
        setSleepTimerMinutes(null)
    }

    fun getAudioSessionId(): Int = exoPlayer.audioSessionId

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    private fun applyCrossfadeRampIfEnabled() {
        fadeJob?.cancel()
        val crossfadeSec = settingsStore.crossfadeSeconds.value
        exoPlayer.skipSilenceEnabled = crossfadeSec > 0
        if (crossfadeSec <= 0) {
            exoPlayer.volume = 1.0f
            return
        }
        val rampMs = (crossfadeSec * 1000L).coerceAtMost(4000L)
        val steps = 10
        val stepDelay = rampMs / steps
        fadeJob = scope.launch {
            exoPlayer.volume = 0.15f
            for (i in 1..steps) {
                delay(stepDelay)
                exoPlayer.volume = 0.15f + (0.85f * i / steps)
            }
            exoPlayer.volume = 1.0f
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                syncProgress(addListenedDelta = true)
                delay(500L)
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private fun syncProgress(addListenedDelta: Boolean) {
        val duration = exoPlayer.duration.takeIf { it > 0 } ?: _state.value.durationMs
        val pos = exoPlayer.currentPosition.coerceAtLeast(0L)
        if (addListenedDelta && exoPlayer.isPlaying) {
            settingsStore.addListenedTime(500L)
        }
        _state.update {
            it.copy(
                positionMs = pos,
                durationMs = duration
            )
        }
    }

    fun release() {
        stopTicker()
        sleepTimerJob?.cancel()
        fadeJob?.cancel()
        exoPlayer.release()
    }
}
