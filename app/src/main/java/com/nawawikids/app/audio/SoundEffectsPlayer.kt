package com.nawawikids.app.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.sin

enum class SfxKind {
    POP,
    CORRECT,
    WRONG,
    UNLOCK
}

/**
 * Cheerful zero-asset PCM tone synthesizer reproducing the playful storybook chimes
 * (pop, correct, wrong, unlock fanfare) without external sound files.
 */
class SoundEffectsPlayer(private val scope: CoroutineScope) {
    @Volatile
    var muted: Boolean = false

    fun play(kind: SfxKind) {
        if (muted) return
        scope.launch(Dispatchers.IO) {
            try {
                when (kind) {
                    SfxKind.POP -> playNotes(listOf(Note(580.0, 0, 85, 0.25, triangle = true)))
                    SfxKind.CORRECT -> playNotes(
                        listOf(
                            Note(523.25, 0, 120, 0.30, triangle = true),
                            Note(659.25, 85, 160, 0.30, triangle = true)
                        )
                    )
                    SfxKind.WRONG -> playNotes(
                        listOf(
                            Note(260.0, 0, 140, 0.24, triangle = false),
                            Note(215.0, 95, 160, 0.22, triangle = false)
                        )
                    )
                    SfxKind.UNLOCK -> playNotes(
                        listOf(
                            Note(523.25, 0, 130, 0.32, triangle = true),
                            Note(659.25, 90, 130, 0.32, triangle = true),
                            Note(783.99, 180, 150, 0.32, triangle = true),
                            Note(1046.50, 270, 320, 0.36, triangle = true)
                        )
                    )
                }
            } catch (_: Exception) {
            }
        }
    }

    private data class Note(
        val freqHz: Double,
        val startMs: Int,
        val durationMs: Int,
        val volume: Double,
        val triangle: Boolean
    )

    private fun playNotes(notes: List<Note>) {
        val sampleRate = 22050
        val totalMs = notes.maxOfOrNull { it.startMs + it.durationMs } ?: return
        val totalSamples = (sampleRate * totalMs) / 1000
        if (totalSamples <= 0) return

        val mix = DoubleArray(totalSamples)
        for (n in notes) {
            val startSample = (sampleRate * n.startMs) / 1000
            val durSamples = (sampleRate * n.durationMs) / 1000
            for (i in 0 until durSamples) {
                val idx = startSample + i
                if (idx >= totalSamples) break
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / durSamples.coerceAtLeast(1)
                val attack = (i / (sampleRate * 0.012)).coerceAtMost(1.0)
                val release = (1.0 - progress).coerceAtLeast(0.0)
                val env = attack * release
                val phase = 2.0 * PI * n.freqHz * t
                val wave = if (n.triangle) (2.0 / PI) * asin(sin(phase)) else sin(phase)
                mix[idx] += wave * n.volume * env
            }
        }

        val pcm = ShortArray(totalSamples) { i ->
            (mix[i].coerceIn(-0.95, 0.95) * Short.MAX_VALUE).toInt().toShort()
        }

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(pcm.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(pcm, 0, pcm.size)
        track.play()
        Thread.sleep(totalMs.toLong() + 40L)
        track.stop()
        track.release()
    }
}
