package com.nawawikids.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import com.nawawikids.app.model.Hadith
import com.nawawikids.app.model.WordTiming
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max

data class AudioPlaybackState(
    val hadithId: Int = -1,
    val isPlaying: Boolean = false,
    val currentSec: Double = 0.0,
    val durationSec: Double = 0.0,
    val activeWordIndex: Int = -1,
    val isHeaderActive: Boolean = false,
    val speed: Float = 1.0f,
    val loopMode: Int = 0, // 0 = off, 3 = 3x, 999 = infinite
    val loopRemaining: Int = 0
)

/**
 * Offline MP3 controller for assets/audio/h01..h42.mp3 with exact word-level karaoke sync,
 * tap-to-seek, speed control, loop modes, and single-word/phrase slice playback for recitation hints.
 */
class HadithAudioController(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private var mediaPlayer: MediaPlayer? = null
    private var syncJob: Job? = null
    private var currentHadith: Hadith? = null
    private var sliceStopSec: Double? = null

    private val speedOptions = listOf(1.0f, 0.75f, 1.15f)
    private var speedIdx = 0

    private val _state = MutableStateFlow(AudioPlaybackState())
    val state: StateFlow<AudioPlaybackState> = _state.asStateFlow()

    fun loadHadith(hadith: Hadith) {
        if (currentHadith?.id == hadith.id && mediaPlayer != null) return
        releasePlayer()
        currentHadith = hadith
        sliceStopSec = null

        val assetPath = hadith.audio.removePrefix("assets/")
        try {
            val afd = context.assets.openFd(assetPath)
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                prepare()
                setOnCompletionListener { handleTrackCompleted() }
            }
            mediaPlayer = player
            applySpeedInternal(player, speedOptions[speedIdx])

            val dur = (player.duration / 1000.0).takeIf { it > 0.0 } ?: hadith.duration
            _state.update {
                it.copy(
                    hadithId = hadith.id,
                    isPlaying = false,
                    currentSec = 0.0,
                    durationSec = dur,
                    activeWordIndex = -1,
                    isHeaderActive = false
                )
            }
        } catch (_: Exception) {
            _state.update {
                it.copy(
                    hadithId = hadith.id,
                    isPlaying = false,
                    currentSec = 0.0,
                    durationSec = hadith.duration,
                    activeWordIndex = -1
                )
            }
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        sliceStopSec = null
        if (player.isPlaying) {
            pause()
        } else {
            val curState = _state.value
            if (curState.loopMode == 3 && curState.loopRemaining <= 0) {
                _state.update { it.copy(loopRemaining = 3) }
            }
            applySpeedInternal(player, speedOptions[speedIdx])
            player.start()
            _state.update { it.copy(isPlaying = true) }
            startSyncLoop()
        }
    }

    fun playFromStart() {
        val player = mediaPlayer ?: return
        sliceStopSec = null
        player.seekTo(0)
        applySpeedInternal(player, speedOptions[speedIdx])
        player.start()
        _state.update { it.copy(isPlaying = true, currentSec = 0.0) }
        startSyncLoop()
    }

    fun pause() {
        sliceStopSec = null
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (_: Exception) {
        }
        syncJob?.cancel()
        _state.update { it.copy(isPlaying = false, isHeaderActive = false) }
    }

    fun stopAndReset() {
        pause()
        try {
            mediaPlayer?.seekTo(0)
        } catch (_: Exception) {
        }
        _state.update { it.copy(currentSec = 0.0, activeWordIndex = -1, isHeaderActive = false) }
    }

    fun seekToSeconds(sec: Double, playImmediately: Boolean = false) {
        val player = mediaPlayer ?: return
        sliceStopSec = null
        val clamped = sec.coerceIn(0.0, _state.value.durationSec.coerceAtLeast(0.1))
        player.seekTo((clamped * 1000.0).toInt())
        updateWordHighlightAt(clamped, isPlayingNow = playImmediately || player.isPlaying)
        if (playImmediately && !player.isPlaying) {
            applySpeedInternal(player, speedOptions[speedIdx])
            player.start()
            _state.update { it.copy(isPlaying = true) }
            startSyncLoop()
        }
    }

    fun rewind5Seconds() {
        val next = max(0.0, _state.value.currentSec - 5.0)
        seekToSeconds(next, playImmediately = _state.value.isPlaying)
    }

    /**
     * Plays a precise slice [startSec, endSec] of the Qari's recitation — ideal for letting
     * the child hear a single missed word or short phrase during live recitation!
     */
    fun playSlice(startSec: Double, endSec: Double) {
        val player = mediaPlayer ?: return
        val safeStart = max(0.0, startSec - 0.08)
        val safeEnd = max(safeStart + 0.45, endSec + 0.14)
        sliceStopSec = safeEnd
        player.seekTo((safeStart * 1000.0).toInt())
        applySpeedInternal(player, speedOptions[speedIdx])
        player.start()
        _state.update { it.copy(isPlaying = true, currentSec = safeStart) }
        startSyncLoop()
    }

    fun cycleSpeed() {
        speedIdx = (speedIdx + 1) % speedOptions.size
        val nextSpeed = speedOptions[speedIdx]
        mediaPlayer?.let { applySpeedInternal(it, nextSpeed) }
        _state.update { it.copy(speed = nextSpeed) }
    }

    fun cycleLoopMode() {
        val nextMode = when (_state.value.loopMode) {
            0 -> 3
            3 -> 999
            else -> 0
        }
        _state.update { it.copy(loopMode = nextMode, loopRemaining = if (nextMode == 3) 3 else 0) }
    }

    private fun applySpeedInternal(player: MediaPlayer, speed: Float) {
        try {
            val wasPlaying = player.isPlaying
            val params = PlaybackParams().setSpeed(speed)
            player.playbackParams = params
            if (!wasPlaying && player.isPlaying) {
                player.pause()
            }
        } catch (_: Exception) {
        }
    }

    private fun handleTrackCompleted() {
        val cur = _state.value
        val player = mediaPlayer ?: return
        if (sliceStopSec != null) {
            sliceStopSec = null
            pause()
            return
        }
        if (cur.loopMode == 999) {
            player.seekTo(0)
            player.start()
            return
        }
        if (cur.loopMode == 3 && cur.loopRemaining > 1) {
            _state.update { it.copy(loopRemaining = it.loopRemaining - 1) }
            player.seekTo(0)
            player.start()
            return
        }
        syncJob?.cancel()
        _state.update {
            it.copy(
                isPlaying = false,
                activeWordIndex = -1,
                isHeaderActive = false,
                loopRemaining = if (cur.loopMode == 3) 3 else 0
            )
        }
    }

    private fun startSyncLoop() {
        syncJob?.cancel()
        syncJob = scope.launch {
            while (isActive) {
                val player = mediaPlayer ?: break
                val playing = try {
                    player.isPlaying
                } catch (_: Exception) {
                    false
                }
                if (!playing) break

                val t = player.currentPosition / 1000.0
                val stopAt = sliceStopSec
                if (stopAt != null && t >= stopAt) {
                    pause()
                    break
                }
                updateWordHighlightAt(t, isPlayingNow = true)
                delay(32L)
            }
        }
    }

    private fun updateWordHighlightAt(t: Double, isPlayingNow: Boolean) {
        val h = currentHadith ?: return
        val hdrStart = h.hdrTime.getOrNull(0) ?: 0.0
        val hdrEnd = h.hdrTime.getOrNull(1) ?: 1.5
        val headerActive = isPlayingNow && t >= max(0.0, hdrStart - 0.08) && t <= hdrEnd + 0.12

        val activeIdx = findWordIndexBinary(h.words, t)
        _state.update {
            it.copy(
                currentSec = t,
                activeWordIndex = activeIdx,
                isHeaderActive = headerActive
            )
        }
    }

    private fun findWordIndexBinary(words: List<WordTiming>, t: Double): Int {
        if (words.isEmpty()) return -1
        var lo = 0
        var hi = words.size - 1
        var found = -1
        while (lo <= hi) {
            val mid = (lo + hi) ushr 1
            val w = words[mid]
            if (t < w.s) {
                hi = mid - 1
            } else if (t > w.e) {
                lo = mid + 1
            } else {
                found = mid
                break
            }
        }
        if (found == -1 && hi >= 0 && t - words[hi].e < 0.35) {
            found = hi
        }
        return found
    }

    private fun releasePlayer() {
        syncJob?.cancel()
        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {
        }
        try {
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
    }

    fun release() {
        releasePlayer()
    }
}
