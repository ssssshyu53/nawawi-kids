package com.nawawikids.app.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.nawawikids.app.model.WordTiming
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class RecitationStatus {
    IDLE,
    LISTENING,
    MISTAKE_PAUSED,
    COMPLETED,
    UNAVAILABLE
}

data class LiveRecitationState(
    val hadithId: Int = -1,
    val status: RecitationStatus = RecitationStatus.IDLE,
    val currentTargetIndex: Int = 0,
    val revealedIndices: Set<Int> = emptySet(),
    val hintedIndices: Set<Int> = emptySet(),
    val mistakeWordSpoken: String? = null,
    val mistakeTargetIndex: Int? = null,
    val mistakeCount: Int = 0,
    val totalWords: Int = 0,
    val lastHeardSnippet: String = "",
    val rmsLevel: Float = 0f,
    val errorMessage: String? = null,
    val speechAvailable: Boolean = true,
    val pauseOnMistake: Boolean = true
) {
    val progressFraction: Float
        get() = if (totalWords <= 0) 0f else (revealedIndices.size.toFloat() / totalWords.toFloat()).coerceIn(0f, 1f)

    val accuracyPercent: Int
        get() {
            val attempts = revealedIndices.size + mistakeCount
            if (attempts <= 0) return 100
            return ((revealedIndices.size.toFloat() / attempts.toFloat()) * 100f).toInt().coerceIn(0, 100)
        }
}

/**
 * Enhanced continuous live Arabic Hadith recitation engine:
 * - Uses full-utterance anchoring + multi-hypothesis (top 3 ASR candidates) for far higher accuracy.
 * - Supports starting from any paragraph/sentence (essential for longer hadiths!).
 * - Supports toggling Pause-on-Mistake vs Gentle-Inline-Warning mode.
 * - Seamlessly restarts listening sessions across natural breathing pauses.
 */
class LiveRecitationController(
    private val context: Context,
    private val onWordRevealed: () -> Unit,
    private val onMistakeDetected: () -> Unit,
    private val onRecitationCompleted: (Int) -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null
    private var activeWords: List<WordTiming> = emptyList()
    private var activeHadithId: Int = -1
    private var shouldKeepListening: Boolean = false
    private var utteranceStartTargetIndex: Int = 0
    private var consecutiveErrors: Int = 0

    private val _state = MutableStateFlow(
        LiveRecitationState(speechAvailable = SpeechRecognizer.isRecognitionAvailable(context))
    )
    val state: StateFlow<LiveRecitationState> = _state.asStateFlow()

    fun prepareForHadith(hadithId: Int, words: List<WordTiming>) {
        stopListening()
        activeHadithId = hadithId
        activeWords = words
        val startIdx = ArabicSpeechMatcher.advancePastPunctuation(words, 0)
        val initialRevealed = (0 until startIdx).toSet()
        utteranceStartTargetIndex = startIdx
        _state.value = LiveRecitationState(
            hadithId = hadithId,
            status = RecitationStatus.IDLE,
            currentTargetIndex = startIdx,
            revealedIndices = initialRevealed,
            totalWords = words.size,
            speechAvailable = SpeechRecognizer.isRecognitionAvailable(context),
            pauseOnMistake = _state.value.pauseOnMistake
        )
    }

    /**
     * Allows the student to start live recitation from any paragraph/word index
     * (revealing earlier words automatically so they can practice a specific section!).
     */
    fun jumpToWordIndex(wordIndex: Int) {
        if (wordIndex !in activeWords.indices) return
        val wasListening = _state.value.status == RecitationStatus.LISTENING
        stopListening()
        val safeIdx = ArabicSpeechMatcher.advancePastPunctuation(activeWords, wordIndex)
        utteranceStartTargetIndex = safeIdx
        _state.update {
            it.copy(
                currentTargetIndex = safeIdx,
                revealedIndices = (0 until safeIdx).toSet(),
                mistakeWordSpoken = null,
                mistakeTargetIndex = null,
                errorMessage = null,
                status = RecitationStatus.IDLE
            )
        }
        if (wasListening) {
            startListening()
        }
    }

    fun setPauseOnMistake(enabled: Boolean) {
        _state.update { it.copy(pauseOnMistake = enabled) }
    }

    fun startListening() {
        mainHandler.post {
            if (activeWords.isEmpty()) return@post
            if (_state.value.status == RecitationStatus.COMPLETED) {
                prepareForHadith(activeHadithId, activeWords)
            }
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                _state.update {
                    it.copy(
                        status = RecitationStatus.UNAVAILABLE,
                        speechAvailable = false,
                        errorMessage = "خدمة التعرف على الصوت غير متوفرة على هذا الجهاز حالياً. يمكنك استخدام الإخفاء التدريجي والتسجيل بالأسفل."
                    )
                }
                return@post
            }

            shouldKeepListening = true
            consecutiveErrors = 0
            utteranceStartTargetIndex = _state.value.currentTargetIndex
            _state.update {
                it.copy(
                    status = RecitationStatus.LISTENING,
                    mistakeWordSpoken = null,
                    mistakeTargetIndex = null,
                    errorMessage = null
                )
            }
            launchRecognizerSession()
        }
    }

    fun stopListening() {
        mainHandler.post {
            shouldKeepListening = false
            mainHandler.removeCallbacksAndMessages(null)
            destroyRecognizer()
            _state.update { curr ->
                val nextStatus = if (curr.status == RecitationStatus.COMPLETED) {
                    RecitationStatus.COMPLETED
                } else {
                    RecitationStatus.IDLE
                }
                curr.copy(status = nextStatus, rmsLevel = 0f)
            }
        }
    }

    fun retryAfterMistake() {
        _state.update {
            it.copy(
                mistakeWordSpoken = null,
                mistakeTargetIndex = null,
                errorMessage = null
            )
        }
        startListening()
    }

    fun hintCurrentWord() {
        val idx = _state.value.currentTargetIndex
        if (idx in activeWords.indices) {
            _state.update { it.copy(hintedIndices = it.hintedIndices + idx) }
        }
    }

    fun revealCurrentWordAndContinue() {
        val idx = _state.value.currentTargetIndex
        if (idx !in activeWords.indices) return
        val wasMistake = _state.value.status == RecitationStatus.MISTAKE_PAUSED
        val nextIdx = ArabicSpeechMatcher.advancePastPunctuation(activeWords, idx + 1)
        val newRevealed = _state.value.revealedIndices + (idx until nextIdx).toSet()
        val isDone = nextIdx >= activeWords.size
        utteranceStartTargetIndex = nextIdx
        _state.update {
            it.copy(
                currentTargetIndex = nextIdx,
                revealedIndices = newRevealed,
                mistakeWordSpoken = null,
                mistakeTargetIndex = null,
                status = if (isDone) RecitationStatus.COMPLETED else it.status
            )
        }
        onWordRevealed()
        if (isDone) {
            shouldKeepListening = false
            destroyRecognizer()
            onRecitationCompleted(activeHadithId)
        } else if (wasMistake) {
            startListening()
        }
    }

    fun resetRecitation() {
        prepareForHadith(activeHadithId, activeWords)
    }

    private fun launchRecognizerSession() {
        if (!shouldKeepListening) return
        destroyRecognizer()
        utteranceStartTargetIndex = _state.value.currentTargetIndex

        try {
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer = recognizer
            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    consecutiveErrors = 0
                }

                override fun onBeginningOfSpeech() {}

                override fun onRmsChanged(rmsdB: Float) {
                    val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                    _state.update { it.copy(rmsLevel = normalized) }
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _state.update { it.copy(rmsLevel = 0f) }
                }

                override fun onError(error: Int) {
                    _state.update { it.copy(rmsLevel = 0f) }
                    if (!shouldKeepListening) return

                    when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH,
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                            scheduleRestart(180L)
                        }
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                            shouldKeepListening = false
                            _state.update {
                                it.copy(
                                    status = RecitationStatus.IDLE,
                                    errorMessage = "يرجى السماح بصلاحية الميكروفون للبدء في التسميع المباشر."
                                )
                            }
                        }
                        SpeechRecognizer.ERROR_NETWORK,
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                            shouldKeepListening = false
                            _state.update {
                                it.copy(
                                    status = RecitationStatus.IDLE,
                                    errorMessage = "تأكد من الاتصال بالإنترنت أو حزمة الصوت العربية لإتمام التعرف الصوتي."
                                )
                            }
                        }
                        else -> {
                            consecutiveErrors++
                            if (consecutiveErrors <= 4) {
                                scheduleRestart(320L)
                            } else {
                                shouldKeepListening = false
                                _state.update {
                                    it.copy(
                                        status = RecitationStatus.IDLE,
                                        errorMessage = "توقف الميكروفون مؤقتاً، اضغط «ابدأ التسميع» للمتابعة من حيث توقفت."
                                    )
                                }
                            }
                        }
                    }
                }

                override fun onResults(results: Bundle?) {
                    val candidates = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.filter { it.isNotBlank() }
                        .orEmpty()
                    if (candidates.isNotEmpty()) {
                        processCandidates(candidates, isFinal = true)
                    }
                    if (shouldKeepListening && _state.value.status == RecitationStatus.LISTENING) {
                        scheduleRestart(140L)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val candidates = partialResults
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.filter { it.isNotBlank() }
                        .orEmpty()
                    if (candidates.isNotEmpty()) {
                        processCandidates(candidates, isFinal = false)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ar-SA")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2600L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
            }
            recognizer.startListening(intent)
        } catch (_: Exception) {
            shouldKeepListening = false
            _state.update {
                it.copy(
                    status = RecitationStatus.UNAVAILABLE,
                    errorMessage = "تعذر تشغيل الميكروفون على هذا الجهاز."
                )
            }
        }
    }

    private fun processCandidates(candidates: List<String>, isFinal: Boolean) {
        val currentState = _state.value
        if (currentState.status != RecitationStatus.LISTENING) return

        val topRaw = candidates.first()
        _state.update { it.copy(lastHeardSnippet = topRaw) }

        val eval = ArabicSpeechMatcher.evaluateCandidates(
            words = activeWords,
            utteranceStartIndex = utteranceStartTargetIndex,
            currentTargetIndex = currentState.currentTargetIndex,
            candidates = candidates,
            isFinalResult = isFinal
        )

        if (eval.newlyMatchedIndices.isNotEmpty() || eval.newTargetIndex > currentState.currentTargetIndex) {
            val allNewlyRevealed = (currentState.currentTargetIndex until eval.newTargetIndex).toSet() + eval.newlyMatchedIndices
            val updatedRevealed = currentState.revealedIndices + allNewlyRevealed
            val isCompleted = eval.newTargetIndex >= activeWords.size

            _state.update {
                it.copy(
                    currentTargetIndex = eval.newTargetIndex,
                    revealedIndices = updatedRevealed,
                    mistakeWordSpoken = null,
                    mistakeTargetIndex = null,
                    status = if (isCompleted) RecitationStatus.COMPLETED else RecitationStatus.LISTENING
                )
            }
            onWordRevealed()

            if (isCompleted) {
                shouldKeepListening = false
                destroyRecognizer()
                onRecitationCompleted(activeHadithId)
                return
            }
        }

        if (eval.mistakeSpokenWord != null) {
            if (currentState.pauseOnMistake) {
                shouldKeepListening = false
                destroyRecognizer()
                _state.update {
                    it.copy(
                        status = RecitationStatus.MISTAKE_PAUSED,
                        mistakeWordSpoken = eval.mistakeSpokenWord,
                        mistakeTargetIndex = eval.newTargetIndex,
                        mistakeCount = it.mistakeCount + 1,
                        rmsLevel = 0f
                    )
                }
                onMistakeDetected()
            } else {
                // Continuous gentle mode: warn without stopping the mic, and anchor after the word
                _state.update {
                    it.copy(
                        mistakeWordSpoken = eval.mistakeSpokenWord,
                        mistakeTargetIndex = eval.newTargetIndex,
                        mistakeCount = it.mistakeCount + 1
                    )
                }
                onMistakeDetected()
            }
        }
    }

    private fun scheduleRestart(delayMs: Long) {
        mainHandler.removeCallbacksAndMessages(null)
        mainHandler.postDelayed({
            if (shouldKeepListening && _state.value.status == RecitationStatus.LISTENING) {
                launchRecognizerSession()
            }
        }, delayMs)
    }

    private fun destroyRecognizer() {
        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Exception) {
        } finally {
            speechRecognizer = null
        }
    }

    fun release() {
        stopListening()
    }
}
