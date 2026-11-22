package com.nawawikids.app.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class SfxKind {
    POP,
    CORRECT,
    WRONG,
    UNLOCK
}

/**
 * Gentle, low-volume warm marimba/droplet sound synthesizer designed for children.
 * Uses pure sine waves with smooth exponential decay and soft amplitude so taps
 * feel crisp and pleasant without ever sounding harsh or loud.
 */
class SoundEffectsPlayer(private val scope: CoroutineScope) {
    @Volatile
    var muted: Boolean = false

    fun play(kind: SfxKind) {
        if (muted) return
        scope.launch(Dispatchers.IO) {
            try {
                when (kind) {
                    // Ultra-soft wooden water-drop tap (quiet, short, warm)
                    SfxKind.POP -> playNotes(
                        listOf(
                            Tone(startFreq = 390.0, endFreq = 510.0, startMs = 0, durationMs = 42, volume = 0.055, decay = 9.0)
                        )
                    )
                    // Gentle cheerful two-note marimba chime
                    SfxKind.CORRECT -> playNotes(
                        listOf(
                            Tone(startFreq = 523.25, endFreq = 523.25, startMs = 0, durationMs = 130, volume = 0.12, decay = 5.5),
                            Tone(startFreq = 659.25, endFreq = 659.25, startMs = 75, durationMs = 175, volume = 0.13, decay = 4.8)
                        )
                    )
                    // Soft, reassuring low double-tap (never jarring)
                    SfxKind.WRONG -> playNotes(
                        listOf(
                            Tone(startFreq = 250.0, endFreq = 225.0, startMs = 0, durationMs = 110, volume = 0.09, decay = 6.5),
                            Tone(startFreq = 210.0, endFreq = 190.0, startMs = 85, durationMs = 135, volume = 0.085, decay = 6.0)
                        )
                    )
                    // Warm celebratory chime arpeggio
                    SfxKind.UNLOCK -> playNotes(
                        listOf(
                            Tone(startFreq = 523.25, endFreq = 523.25, startMs = 0, durationMs = 140, volume = 0.12, decay = 5.2),
                            Tone(startFreq = 659.25, endFreq = 659.25, startMs = 80, durationMs = 140, volume = 0.12, decay = 5.2),
                            Tone(startFreq = 783.99, endFreq = 783.99, startMs = 160, durationMs = 165, volume = 0.13, decay = 4.5),
                            Tone(startFreq = 1046.50, endFreq = 1046.50, startMs = 245, durationMs = 290, volume = 0.14, decay = 3.8)
                        )
                    )
                }
            } catch (_: Exception) {
            }
        }
    }

    private data class Tone(
        val startFreq: Double,
        val endFreq: Double,
        val startMs: Int,
        val durationMs: Int,
        val volume: Double,
        val decay: Double
    )

    private fun playNotes(tones: List<Tone>) {
        val sampleRate = 22050
        val totalMs = tones.maxOfOrNull { it.startMs + it.durationMs } ?: return
        val totalSamples = (sampleRate * totalMs) / 1000
        if (totalSamples <= 0) return

        val mix = DoubleArray(totalSamples)
        for (tone in tones) {
            val startSample = (sampleRate * tone.startMs) / 1000
            val durSamples = (sampleRate * tone.durationMs) / 1000
            var phase = 0.0
            for (i in 0 until durSamples) {
                val idx = startSample + i
                if (idx >= totalSamples) break
                val progress = i.toDouble() / durSamples.coerceAtLeast(1)
                val freq = tone.startFreq + (tone.endFreq - tone.startFreq) * progress
                phase += (2.0 * PI * freq) / sampleRate
                // Smooth 5ms fade-in + natural exponential marimba fade-out + tail taper
                val attack = (i / (sampleRate * 0.005)).coerceAtMost(1.0)
                val expDecay = exp(-tone.decay * progress)
                val tail = ((1.0 - progress) * 8.0).coerceAtMost(1.0)
                val env = attack * expDecay * tail
                // Pure fundamental sine + subtle soft harmonic
                val wave = sin(phase) * 0.90 + sin(phase * 2.0) * 0.10
                mix[idx] += wave * tone.volume * env
            }
        }

        val pcm = ShortArray(totalSamples) { i ->
            (mix[i].coerceIn(-0.90, 0.90) * Short.MAX_VALUE).toInt().toShort()
        }

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
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
        Thread.sleep(totalMs.toLong() + 30L)
        track.stop()
        track.release()
    }
}
