package com.nawawikids.app.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

data class VoiceRecorderState(
    val isRecording: Boolean = false,
    val elapsedSeconds: Int = 0,
    val hasRecording: Boolean = false,
    val isPlayingBack: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Self-recitation studio recorder allowing children to record their own voice
 * and play it back alongside the Qari for self-evaluation.
 */
class VoiceRecorderController(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private var recorder: MediaRecorder? = null
    private var playbackPlayer: MediaPlayer? = null
    private var timerJob: Job? = null
    private var currentFile: File? = null

    private val _state = MutableStateFlow(VoiceRecorderState())
    val state: StateFlow<VoiceRecorderState> = _state.asStateFlow()

    fun startRecording(hadithId: Int) {
        stopPlayback()
        stopRecordingInternal(keepFile = false)

        val outDir = File(context.cacheDir, "recitations").apply { mkdirs() }
        val file = File(outDir, "rec_h$hadithId.m4a")
        currentFile = file

        try {
            val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            mr.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(96000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            recorder = mr
            _state.update {
                it.copy(
                    isRecording = true,
                    elapsedSeconds = 0,
                    hasRecording = false,
                    errorMessage = null
                )
            }
            timerJob?.cancel()
            timerJob = scope.launch {
                while (isActive) {
                    delay(1000L)
                    val next = _state.value.elapsedSeconds + 1
                    _state.update { it.copy(elapsedSeconds = next) }
                    if (next >= 180) {
                        stopRecording()
                        break
                    }
                }
            }
        } catch (_: Exception) {
            stopRecordingInternal(keepFile = false)
            _state.update {
                it.copy(
                    isRecording = false,
                    errorMessage = "تعذر بدء التسجيل الصوتي. تأكد من صلاحية الميكروفون."
                )
            }
        }
    }

    fun stopRecording() {
        stopRecordingInternal(keepFile = true)
    }

    private fun stopRecordingInternal(keepFile: Boolean) {
        timerJob?.cancel()
        try {
            recorder?.stop()
        } catch (_: Exception) {
        }
        try {
            recorder?.release()
        } catch (_: Exception) {
        }
        recorder = null
        val exists = keepFile && (currentFile?.exists() == true) && (currentFile?.length() ?: 0L) > 0L
        _state.update {
            it.copy(isRecording = false, hasRecording = exists)
        }
    }

    fun togglePlayback() {
        if (_state.value.isPlayingBack) {
            stopPlayback()
            return
        }
        val file = currentFile ?: return
        if (!file.exists()) return
        try {
            val mp = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                setOnCompletionListener {
                    stopPlayback()
                }
                start()
            }
            playbackPlayer = mp
            _state.update { it.copy(isPlayingBack = true) }
        } catch (_: Exception) {
            stopPlayback()
        }
    }

    fun stopPlayback() {
        try {
            playbackPlayer?.stop()
        } catch (_: Exception) {
        }
        try {
            playbackPlayer?.release()
        } catch (_: Exception) {
        }
        playbackPlayer = null
        _state.update { it.copy(isPlayingBack = false) }
    }

    fun resetForNewHadith() {
        stopRecordingInternal(keepFile = false)
        stopPlayback()
        currentFile = null
        _state.value = VoiceRecorderState()
    }

    fun release() {
        resetForNewHadith()
    }
}
