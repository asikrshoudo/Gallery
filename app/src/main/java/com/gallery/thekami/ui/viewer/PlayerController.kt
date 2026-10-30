@file:OptIn(UnstableApi::class)

package com.gallery.thekami.ui.viewer

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.gallery.thekami.core.domain.media.MediaItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.media3.common.MediaItem as ExoMediaItem

enum class PlaybackError { Unsupported, Missing, Generic }

/** Maps Media3 error codes to something we can explain to the user. */
fun classifyPlaybackError(errorCode: Int): PlaybackError = when (errorCode) {
    PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND,
    PlaybackException.ERROR_CODE_IO_NO_PERMISSION -> PlaybackError.Missing
    PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
    PlaybackException.ERROR_CODE_DECODING_FAILED,
    PlaybackException.ERROR_CODE_DECODING_FORMAT_EXCEEDS_CAPABILITIES,
    PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED,
    PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED,
    PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED -> PlaybackError.Unsupported
    else -> PlaybackError.Generic
}

data class PlayerUiState(
    val loadedKey: String? = null,
    val isPlaying: Boolean = false,
    val isEnded: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val error: PlaybackError? = null,
)

/**
 * One ExoPlayer shared by the whole viewer. It lives in the ViewModel so playback position
 * survives rotation, is paused when the app stops, and is released in onCleared.
 * Playback uses whatever decoders the device has: 4K plays only where the device decoder,
 * codec and container support it; otherwise [PlaybackError.Unsupported] is reported.
 */
class PlayerController(context: Context, private val scope: CoroutineScope) {
    val player: ExoPlayer = ExoPlayer.Builder(context.applicationContext)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build(),
            true,
        )
        .setHandleAudioBecomingNoisy(true)
        .build()

    private val _state = MutableStateFlow(PlayerUiState())
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()
    private var pollJob: Job? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) startPolling() else stopPolling()
                syncPosition()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _state.update { it.copy(isEnded = playbackState == Player.STATE_ENDED) }
                syncPosition()
            }

            override fun onPlayerError(error: PlaybackException) {
                _state.update { it.copy(error = classifyPlaybackError(error.errorCode), isPlaying = false) }
            }
        })
    }

    fun load(item: MediaItem, autoplay: Boolean) {
        val current = _state.value
        if (current.loadedKey == item.key && current.error == null) return
        _state.value = PlayerUiState(loadedKey = item.key)
        player.setMediaItem(ExoMediaItem.fromUri(item.uri))
        player.prepare()
        player.playWhenReady = autoplay
    }

    fun togglePlay() {
        val s = _state.value
        when {
            s.error != null -> return
            s.isEnded -> {
                player.seekTo(0)
                player.play()
            }
            player.isPlaying -> player.pause()
            else -> player.play()
        }
    }

    fun pause() = player.pause()

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        _state.update { it.copy(positionMs = positionMs, isEnded = false) }
    }

    fun clear() {
        stopPolling()
        player.stop()
        player.clearMediaItems()
        _state.value = PlayerUiState()
    }

    fun release() {
        stopPolling()
        player.release()
    }

    private fun syncPosition() {
        val duration = player.duration
        _state.update {
            it.copy(
                positionMs = player.currentPosition.coerceAtLeast(0),
                durationMs = if (duration == C.TIME_UNSET) it.durationMs else duration.coerceAtLeast(0),
            )
        }
    }

    private fun startPolling() {
        if (pollJob?.isActive == true) return
        pollJob = scope.launch {
            while (isActive) {
                syncPosition()
                delay(250)
            }
        }
    }

    private fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }
}
